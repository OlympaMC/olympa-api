package fr.olympa.api.clans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.observable.ObservableList;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public abstract class ClansManager<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> implements Listener {
	
	public final OlympaAPIPlugin plugin;
	
	protected Map<Integer, T> clans = new HashMap<>();
	private Map<Player, ObservableList<T>> invitations = new HashMap<>();
	public int defaultMaxSize;
	
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
	public String stringCantChiefSelf = "Tu ne peux pas te transférer la direction de ton propre clan.";
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
	
	protected final SQLTable<T> clansTable;
	protected SQLColumn<T> nameColumn;
	protected SQLColumn<T> chiefColumn;
	protected SQLColumn<T> sizeColumn;
	protected SQLColumn<T> moneyColumn;
	
	protected final SQLTable<D> playersTable;
	protected SQLColumn<D> playerIDColumn;
	protected SQLColumn<D> clanIDColumn;
	
	public ClansManager(OlympaAPIPlugin plugin, String clansName, int defaultMaxSize) throws SQLException, ReflectiveOperationException {
		this.plugin = plugin;
		this.defaultMaxSize = defaultMaxSize;
		
		this.clansTable = new SQLTable<T>(clansName, addDBClansCollums(new ArrayList<>())).createOrAlter();
		this.playersTable = new SQLTable<D>(clansName + "_players", addDBPlayersCollums(new ArrayList<>())).createOrAlter();
		
		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + clansTable.getName());
		while (resultSet.next()) {
			try {
				T clan = provideClan(resultSet.getInt("id"), resultSet.getString("name"), AccountProvider.getPlayerInformations(resultSet.getLong("chief")), resultSet.getInt("max_size"), resultSet.getDouble("money"), resultSet.getDate("created").getTime() / 1000L, resultSet);
				
				ResultSet playersSet = playersTable.getFromColumn(clanIDColumn, clan.getID());
				while (playersSet.next()) {
					D playerData = provideClanData(AccountProvider.getPlayerInformations(playersSet.getLong("player_id")), playersSet);
					clan.members.put(playerData.getPlayerInformations(), playerData);
				}
				playersSet.close();
				
				clans.put(clan.getID(), clan);
			} catch (Exception ex) {
				ex.printStackTrace();
				plugin.getLogger().severe("Impossible de charger le groupe " + resultSet.getInt("id"));
			}
		}
		resultSet.close();
		
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		nameTagApi.addNametagHandler(EventPriority.LOWEST, (nametag, player, to) -> {
			ClanPlayerInterface<T, D> clanPlayer = (ClanPlayerInterface<T, D>) player;
			ClanPlayerInterface<T, D> clanTo = (ClanPlayerInterface<T, D>) to;
			if (clanPlayer.getClan() == null) return;
			ChatColor color;
			if (clanTo.getClan() != null) {
				color = clanTo.getClan() == clanPlayer.getClan() ? ChatColor.GREEN : ChatColor.RED;
			}else color = ChatColor.RED;
			nametag.appendPrefix(color + "[" + clanPlayer.getClan().getName() + "]");
		});
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
	
	/* Abstraction */
	
	protected abstract String getClansCommand();
	
	protected abstract T createClan(int id, String name, OlympaPlayerInformations chief, int maxSize);
	
	protected abstract T provideClan(int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException;
	
	protected abstract D createClanData(OlympaPlayerInformations informations);
	
	protected abstract D provideClanData(OlympaPlayerInformations informations, ResultSet resultSet) throws SQLException;
	
	public List<SQLColumn<T>> addDBClansCollums(List<SQLColumn<T>> columns) {
		columns.add(new SQLColumn<T>("id", "int(11) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(T::getID));
		columns.add(nameColumn = new SQLColumn<T>("name", "varchar(45) NOT NULL", Types.VARCHAR).setUpdatable());
		columns.add(chiefColumn = new SQLColumn<T>("chief", "bigint(20) NOT NULL", Types.BIGINT).setUpdatable());
		columns.add(sizeColumn = new SQLColumn<T>("max_size", "tinyint(1) NOT NULL DEFAULT " + defaultMaxSize, Types.TINYINT).setUpdatable());
		columns.add(moneyColumn = new SQLColumn<T>("money", "DOUBLE NOT NULL DEFAULT 0", Types.DOUBLE).setUpdatable());
		columns.add(new SQLColumn<T>("created", "DATE NOT NULL DEFAULT curdate()", Types.DATE));
		return columns;
	}
	
	public List<SQLColumn<D>> addDBPlayersCollums(List<SQLColumn<D>> columns) {
		columns.add(playerIDColumn = new SQLColumn<D>("player_id", "BIGINT NOT NULL", Types.BIGINT).setPrimaryKey(player -> player.getPlayerInformations().getId()));
		columns.add(clanIDColumn = new SQLColumn<D>("clan", "INT NOT NULL", Types.INTEGER));
		return columns;
	}
	
	/* SQL statements */
	
	public T createClan(ClanPlayerInterface<T, D> p, String name) throws SQLException {
		ResultSet resultSet = clansTable.insert(name, p.getId());
		resultSet.next();
		int id = resultSet.getInt(1);
		resultSet.close();
		T clan = createClan(id, name, p.getInformation(), defaultMaxSize);
		clans.put(id, clan);
		clan.addPlayer(p);
		plugin.sendMessage("Clan " + name + " créé.");
		return clan;
	}
	
	public void removeClan(T clan) {
		for (List<T> invits : invitations.values()) invits.remove(clan);
		try {
			clansTable.delete(clan);
			plugin.sendMessage("Clan " + clan.getName() + " supprimé.");
			clans.remove(clan.getID());
		}catch (SQLException ex) {
			ex.printStackTrace();
			plugin.getLogger().severe("Le groupe " + clan.getID() + " n'a pas pu être supprimé de la base de données.");
		}
	}
	
	public void insertPlayerInClan(ClanPlayerInterface<T, D> p, Clan<T, D> clan) throws SQLException {
		playersTable.deleteSQLObject(p.getId());
		playersTable.insert(p.getId(), clan.getID());
	}
	
	public void removePlayerFromClan(D player) throws SQLException {
		playersTable.delete(player);
	}
	
	public ClanManagementGUI<T, D> provideManagementGUI(ClanPlayerInterface<T, D> player) {
		return new ClanManagementGUI<>(player, this, 2);
	}
	
	/* Invitations */
	
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
			comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getClansCommand() + " accept " + clan.getName()));
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ColorUtils.color(stringClickToJoin)))));
		}
		targetPlayer.spigot().sendMessage(texts);
	}
	
	/* Events */
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(OlympaPlayerLoadEvent e) {
		ClanPlayerInterface<T, D> oplayer = e.getOlympaPlayer();
		
		for (T clan : clans.values()) {
			if (clan.contains(oplayer.getInformation())) {
				oplayer.setClan(clan);
				clan.memberJoin(oplayer);
				break;
			}
		}
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
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		ClanPlayerInterface<T, D> target = AccountProvider.get(e.getPlayer().getUniqueId());
		if (target.getClan() == null) return;
		e.setFormat("§8[" + target.getClan().getName() + "] " + target.getGroupPrefix() + "%s " + target.getGroup().getChatSuffix() + " %s");
	}
	
	/*public void setSuffix(Player p) {
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		for (Entry<Integer, T> entry : this.getClans()) {
			T clan = entry.getValue();
			String clanName = entry.getValue().getName();
			Collection<D> members = clan.getMembers();
			ChatColor chatColor = members.stream().anyMatch(e -> e.getPlayerInformations().getUUID().equals(p.getUniqueId())) ? ChatColor.GREEN : ChatColor.RED;
			Nametag nameTag = new Nametag(null, " " + chatColor + clanName);
			List<Player> player = Arrays.asList(p);
			members.stream().filter(D::isConnected).forEach(member -> nameTagApi.updateFakeNameTag(member.getPlayerInformations().getName(), nameTag, player));
			nameTagApi.updateFakeNameTag(p, nameTag, members.stream().filter(D::isConnected).map(member -> member.getConnectedPlayer().getPlayer()).collect(Collectors.toList()));
		}
	}*/
	
}
