package fr.olympa.api.clans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.observable.ObservableList;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class ClansManager<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> implements Listener {
	
	//	private static ScoreboardTeam createOrGetTeam(Scoreboard sc, String name, ChatColor color) throws ReflectiveOperationException {
	//		Team team = sc.getTeam(name);
	//		if (team == null) {
	//			team = sc.registerNewTeam(name);
	//			team.setColor(color);
	//		}
	//		return NMS.getNMSTeam(team);
	//	}
	
	public final OlympaAPIPlugin plugin;
	
	public final String tableName;
	//	public Team enemiesBukkit;
	//	public ScoreboardTeam enemies;
	//	public ScoreboardTeam clan;
	
	//	public ScoreboardTeam allies;
	protected Map<Integer, T> clans = new HashMap<>();
	private Map<Player, ObservableList<T>> invitations = new HashMap<>();
	public int defaultMaxSize;
	private final OlympaStatement createClanStatement;
	private final OlympaStatement removeClanStatement;
	private final OlympaStatement getPlayersInClanStatement;
	private final OlympaStatement removeOfflinePlayerInClanStatement;
	public final OlympaStatement updateClanNameStatement;
	public final OlympaStatement updateClanChiefStatement;
	public final OlympaStatement updateClanMoneyStatement;
	
	public final OlympaStatement updateClanMaxStatement;
	public String stringAlreadyInClan = "Ce joueur est déjà dans un clan.";
	public String stringAlreadyInvited = "Tu as déjà invité ce joueur.";
	public String stringPlayerInvited = "Tu as invité le joueur à rejoindre ton clan !";
	public String stringInvitationReceive = "§l%s§r§a t'a invité à rejoindre son clan : \"§l%s§r§a\" ! §oClique ici ou accepte l'invitation depuis le menu.";
	public String stringClickToJoin = "§e§lClique pour rejoindre le clan !";
	public String stringYouAlreadyInClan = "Tu fais déjà partie d'un clan.";
	public String stringClanAlreadyExists = "Un clan avec ce nom existe déjà.";
	public String stringClanCreated = "Tu viens de créer ton clan !";
	public String stringNoInvitation = "Tu n'as pas reçu d'invitation de la part du clan \"%s\".";
	public String stringClanJoined = "Tu viens de rejoindre le clan §l\"%s\"§r§a !";
	public String stringClanFull = "Ce clan n'a plus la place pour accueillir un autre joueur...";
	public String stringCantLeaveChief = "Tu ne peux pas quitter le clan en en étant le chef. Transfère la direction de celui-ci à un autre joueur.";
	public String stringCantChiefSelf = "Tu ne peux pas te transférer la direction de ton propre.";
	public String stringPlayerNotInClan = "Le joueur %s ne fait pas partie du clan.";
	public String stringMustBeInClan = "Tu dois appartenir à un clan pour faire cette commande.";
	public String stringMustBeChief = "Tu dois être le chef du clan pour effectuer cette commande.";
	public String stringClanDisband = "§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.";
	public String stringPlayerChief = "Le joueur %s est désormais le chef du clan.";
	public String stringPlayerLeave = "Le joueur %s a quitté le clan.";
	public String stringPlayerJoin = "Le joueur %s rejoint le clan.";
	public String stringNameChange = "Le clan a changé de nom pour s'appeler %s.";
	public String stringSureDisband = "§7Veux-tu vraiment supprimer le clan ?";
	public String stringSureChief = "§7Veux-tu vraiment donner la direction au joueur %s ?";
	public String stringSureKick = "§7Veux-tu vraiment éjecter le joueur %s ?";
	public String stringSureLeave = "§7Veux-tu vraiment quitter le clan ?";
	public String stringItemCreate = "§eCréer mon clan";
	public String stringChooseName = "§aChoisis le nom de ton clan :";
	public String stringInventoryManage = "Gérer son clan";
	public String stringInventoryJoin = "Rejoindre un clan";
	public String stringItemLeave = "Quitter le clan";
	public String stringAddedMoney = "Tu viens d'ajouter %s à la cagnotte du clan !";
	public String[] stringItemLeaveChiefLore = { "§7§oPour pouvoir quitter votre clan,", "§7§ovous devez tout d'abord", "§7§otransmettre la direction de celui-ci", "§7§oà un autre membre." };
	public String stringClanNameTooLong = "Le nom d'un clan ne peut pas excéder %d caractères !";
	
	public String stringItemDisband = "§cDémenteler le clan";
	
	public ClansManager(OlympaAPIPlugin plugin, String table, int defaultMaxSize) throws SQLException, ReflectiveOperationException {
		this.plugin = plugin;
		this.defaultMaxSize = defaultMaxSize;
		this.tableName = "`" + table + "`";
		
		StringJoiner columnsJoiner = new StringJoiner(", ");
		columnsJoiner = addDBCollums(columnsJoiner);
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				columnsJoiner.toString() +
				",  PRIMARY KEY (`id`))");
		
		createClanStatement = new OlympaStatement("INSERT INTO " + tableName + " (`name`, `chief`) VALUES (?, ?)", true);
		removeClanStatement = new OlympaStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
		getPlayersInClanStatement = new OlympaStatement("SELECT `player_id` FROM " + AccountProvider.getPlayerProviderTableName() + " WHERE (`clan` = ?)");
		removeOfflinePlayerInClanStatement = new OlympaStatement("UPDATE " + AccountProvider.getPlayerProviderTableName() + " SET `clan` = -1 WHERE (`player_id` = ?)");
		updateClanNameStatement = new OlympaStatement("UPDATE " + tableName + " SET `name` = ? WHERE (`id` = ?)");
		updateClanChiefStatement = new OlympaStatement("UPDATE " + tableName + " SET `chief` = ? WHERE (`id` = ?)");
		updateClanMaxStatement = new OlympaStatement("UPDATE " + tableName + " SET `max_size` = ? WHERE (`id` = ?)");
		updateClanMoneyStatement = new OlympaStatement("UPDATE " + tableName + " SET `money` = ? WHERE (`id` = ?)");
		
		//		Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
		//		enemies = createOrGetTeam(sc, "enemies", ChatColor.RED);
		//		clan = createOrGetTeam(sc, "clan", ChatColor.GREEN);
		//		allies = createOrGetTeam(sc, "allies", ChatColor.AQUA);
		//		enemiesBukkit = sc.getTeam("enemies");
		
		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
		while (resultSet.next())
			try {
				T clan = provideClan(resultSet.getInt("id"), resultSet.getString("name"), AccountProvider.getPlayerInformations(resultSet.getLong("chief")), resultSet.getInt("max_size"), resultSet.getDouble("money"), resultSet.getDate("created").getTime() / 1000L, resultSet);
				for (OlympaPlayerInformations pinfo : getPlayersInClan(clan))
					clan.members.put(pinfo, createClanData(pinfo));
				clans.put(clan.getID(), clan);
			} catch (Exception ex) {
				ex.printStackTrace();
				plugin.getLogger().severe("Impossible de charger le groupe " + resultSet.getInt("id"));
			}
		resultSet.close();
	}
	
	public boolean clanExists(String name) {
		return clans.values().stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
	}
	
	public T getClan(int id) {
		return clans.get(id);
	}
	
	public Set<Entry<Integer, T>> getClans() {
		return clans.entrySet();
	}
	
	public int getMaxClanNameLength() {
		return 16;
	}
	
	public T createClan(ClanPlayerInterface<T, D> p, String name) throws SQLException {
		PreparedStatement statement = createClanStatement.getStatement();
		statement.setString(1, name);
		statement.setLong(2, p.getId());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		int id = resultSet.getInt(1);
		resultSet.close();
		T clan = createClan(id, name, p.getInformation(), defaultMaxSize);
		clans.put(id, clan);
		clan.addPlayer(p);
		plugin.sendMessage("Clan " + name + " créé.");
		return clan;
	}
	
	protected abstract T createClan(int id, String name, OlympaPlayerInformations chief, int maxSize);
	
	protected abstract T provideClan(int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException;
	
	protected abstract D createClanData(OlympaPlayerInformations informations);
	
	public ClanManagementGUI<T, D> provideManagementGUI(ClanPlayerInterface<T, D> player) {
		return new ClanManagementGUI<>(player, this, 2);
	}
	
	public StringJoiner addDBCollums(StringJoiner columnsJoiner) {
		columnsJoiner.add("`id` int(11) unsigned NOT NULL AUTO_INCREMENT");
		columnsJoiner.add("`name` varchar(45) NOT NULL");
		columnsJoiner.add("`chief` bigint(20) NOT NULL");
		columnsJoiner.add("`max_size` tinyint(1) NOT NULL DEFAULT " + defaultMaxSize);
		columnsJoiner.add("`money` DOUBLE NOT NULL DEFAULT 0");
		columnsJoiner.add("`created` DATE NOT NULL DEFAULT curdate()");
		return columnsJoiner;
	}
	
	public ObservableList<T> getPlayerInvitations(Player p) {
		ObservableList<T> localInvites = invitations.get(p);
		return localInvites == null ? ObservableList.EMPTY_LIST : localInvites;
	}
	
	public void clearPlayerInvitations(Player p) {
		invitations.remove(p);
	}
	
	public void invite(T clan, Player inviter, Player targetPlayer) {
		ClanPlayerInterface<T, D> target = AccountProvider.get(targetPlayer.getUniqueId());
		if (target.getClan() != null) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, stringAlreadyInClan);
			return;
		}
		
		if (getPlayerInvitations(targetPlayer).contains(clan)) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, stringAlreadyInvited);
			return;
		}
		
		ObservableList<T> localInvites = invitations.get(targetPlayer);
		if (localInvites == null) {
			localInvites = new ObservableList<>(new ArrayList<>());
			invitations.put(targetPlayer, localInvites);
		}
		localInvites.add(clan);
		Prefix.DEFAULT_GOOD.sendMessage(inviter, stringPlayerInvited);
		
		BaseComponent[] texts = TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage(stringInvitationReceive, inviter.getName(), clan.getName()));
		for (BaseComponent comp : texts) {
			comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clans accept " + clan.getName()));
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color(stringClickToJoin))));
		}
		targetPlayer.spigot().sendMessage(texts);
	}
	
	public List<OlympaPlayerInformations> getPlayersInClan(T clan) throws SQLException {
		List<OlympaPlayerInformations> players = new ArrayList<>();
		PreparedStatement statement = getPlayersInClanStatement.getStatement();
		statement.setInt(1, clan.getID());
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
			players.add(AccountProvider.getPlayerInformations(resultSet.getLong("player_id")));
		return players;
	}
	
	public void removeClan(T clan) {
		for (List<T> invits : invitations.values())
			invits.remove(clan);
		try {
			PreparedStatement statement = removeClanStatement.getStatement();
			statement.setInt(1, clan.getID());
			statement.executeUpdate();
			plugin.sendMessage("Clan " + clan.getName() + " supprimé.");
			clans.remove(clan.getID());
		} catch (SQLException ex) {
			ex.printStackTrace();
			plugin.getLogger().severe("Le groupe " + clan.getID() + " n'a pas pu être supprimé de la base de données.");
		}
	}
	
	public void removeOfflinePlayerFromClan(OlympaPlayerInformations player) throws SQLException {
		PreparedStatement statement = removeOfflinePlayerInClanStatement.getStatement();
		statement.setLong(1, player.getId());
		statement.executeUpdate();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(OlympaPlayerLoadEvent e) {
		ClanPlayerInterface<T, D> oplayer = e.getOlympaPlayer();
		//		String name = e.getPlayer().getName();
		//		if (!enemiesBukkit.hasEntry(name)) {
		//			enemiesBukkit.addEntry(name);
		//		}
		//NMS.sendPacket(NMS.addPlayersToTeam(clan, Arrays.asList(e.getPlayer().getName())), e.getPlayer());
		this.setSuffix(oplayer.getPlayer());
		T clan = oplayer.getClan();
		if (clan != null)
			clan.memberJoin(oplayer);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		ClanPlayerInterface<T, D> oplayer = AccountProvider.get(e.getPlayer().getUniqueId());
		if (oplayer == null)
			return;
		T clan = oplayer.getClan();
		if (clan != null)
			clan.memberLeave(oplayer);
	}
	
	public void setSuffix(Player p) {
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		for (Entry<Integer, T> entry : this.getClans()) {
			T clan = entry.getValue();
			String clanName = entry.getValue().getName();
			Collection<D> members = clan.getMembers();
			ChatColor chatColor = members.stream().anyMatch(e -> e.getPlayerInformations().getUUID().equals(p.getUniqueId())) ? ChatColor.GREEN : ChatColor.RED;
			Nametag nameTag = new Nametag(null, " " + chatColor + clanName);
			for (D m : members) nameTagApi.updateFakeNameTag(m.getPlayerInformations().getName(), nameTag, Arrays.asList(p));
		}
	}
	
}
