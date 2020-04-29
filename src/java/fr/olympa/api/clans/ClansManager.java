package fr.olympa.api.clans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.NMS;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.ScoreboardTeam;

public abstract class ClansManager<T extends Clan<T>> implements Listener {

	public final OlympaAPIPlugin plugin;
	public final String tableName;

	public Team enemiesBukkit;
	public ScoreboardTeam enemies;
	public ScoreboardTeam clan;
	public ScoreboardTeam allies;

	private Map<Integer, T> clans = new HashMap<>();
	private Map<Player, List<T>> invitations = new HashMap<>();
	private int defaultMaxSize;
	private final OlympaStatement createClanStatement;
	private final OlympaStatement removeClanStatement;
	private final OlympaStatement getPlayersInClanStatement;
	private final OlympaStatement removeOfflinePlayerInClanStatement;
	public final OlympaStatement updateClanNameStatement;
	public final OlympaStatement updateClanChiefStatement;
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
	public String[] stringItemLeaveChiefLore = { "§7§oPour pouvoir quitter votre clan,", "§7§ovous devez tout d'abord", "§7§otransmettre la direction de celui-ci", "§7§oà un autre membre." };
	public String stringItemDisband = "§cDémenteler le clan";

	public ClansManager(OlympaAPIPlugin plugin, String tableName, List<String> columns, int defaultMaxSize) throws SQLException, ReflectiveOperationException {
		this.plugin = plugin;
		this.defaultMaxSize = defaultMaxSize;
		this.tableName = "`" + tableName + "`";

		StringJoiner columnsJoiner = new StringJoiner(", ");
		columnsJoiner.add("`id` int(11) unsigned NOT NULL AUTO_INCREMENT");
		columnsJoiner.add("`name` varchar(45) NOT NULL");
		columnsJoiner.add("`chief` bigint(20) NOT NULL");
		columnsJoiner.add("`max_size` tinyint(1) NOT NULL DEFAULT " + defaultMaxSize);
		columns.forEach(x -> columnsJoiner.add(x));
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				columnsJoiner.toString() +
				",  PRIMARY KEY (`id`))");

		createClanStatement = new OlympaStatement("INSERT INTO " + tableName + " (`name`, `chief`) VALUES (?, ?)", true);
		removeClanStatement = new OlympaStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
		getPlayersInClanStatement = new OlympaStatement("SELECT `player_id` FROM " + AccountProvider.getPlayerProviderTableName() + " WHERE (`clan` = ?)");
		removeOfflinePlayerInClanStatement = new OlympaStatement("UPDATE " + AccountProvider.getPlayerProviderTableName() + " SET `clan` = NULL WHERE (`player_id` = ?)");
		updateClanNameStatement = new OlympaStatement("UPDATE " + tableName + " SET `name` = ? WHERE (`id` = ?)");
		updateClanChiefStatement = new OlympaStatement("UPDATE " + tableName + " SET `chief` = ? WHERE (`id` = ?)");
		updateClanMaxStatement = new OlympaStatement("UPDATE " + tableName + " SET `max_size` = ? WHERE (`id` = ?)");

		Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
		enemies = createOrGetTeam(sc, "enemies", ChatColor.RED);
		clan = createOrGetTeam(sc, "clan", ChatColor.GREEN);
		allies = createOrGetTeam(sc, "allies", ChatColor.AQUA);
		enemiesBukkit = sc.getTeam("enemies");

		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
		while (resultSet.next()) {
			try {
				T clan = provideClan(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getLong("chief"), resultSet.getInt("max_size"), resultSet);
				for (OlympaPlayerInformations pinfo : getPlayersInClan(clan)) {
					clan.members.put(pinfo.getId(), new AbstractMap.SimpleEntry<>(pinfo, null));
				}
				clans.put(clan.getID(), clan);
			}catch (Exception ex) {
				ex.printStackTrace();
				plugin.getLogger().severe("Impossible de charger le groupe " + resultSet.getInt("id"));
			}
		}
		resultSet.close();
	}

	protected abstract T provideClan(int id, String name, long chief, int maxSize, ResultSet resultSet);

	protected abstract T createClan(int id, String name, long chief, int maxSize);

	public boolean clanExists(String name) {
		return clans.values().stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
	}

	public T createClan(ClanPlayerInterface<T> p, String name) throws SQLException {
		PreparedStatement statement = createClanStatement.getStatement();
		statement.setString(1, name);
		statement.setLong(2, p.getId());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		int id = resultSet.getInt(1);
		resultSet.close();
		T clan = createClan(id, name, p.getId(), defaultMaxSize);
		clans.put(id, clan);
		clan.addPlayer(p);
		return clan;
	}

	public void removeClan(T clan) {
		for (List<T> invits : invitations.values()) {
			invits.remove(clan);
		}
		try {
			PreparedStatement statement = removeClanStatement.getStatement();
			statement.setInt(1, clan.getID());
			statement.executeUpdate();
		}catch (SQLException ex) {
			ex.printStackTrace();
			plugin.getLogger().severe("Le groupe " + clan.getID() + " n'a pas pu être supprimé de la base de données.");
		}
	}

	public T getClan(int id) {
		return clans.get(id);
	}

	public List<OlympaPlayerInformations> getPlayersInClan(T clan) throws SQLException {
		List<OlympaPlayerInformations> players = new ArrayList<>();
		PreparedStatement statement = getPlayersInClanStatement.getStatement();
		statement.setInt(1, clan.getID());
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			players.add(AccountProvider.getPlayerInformations(resultSet.getLong("player_id")));
		}
		return players;
	}

	public void removeOfflinePlayerFromClan(OlympaPlayerInformations player) throws SQLException {
		PreparedStatement statement = removeOfflinePlayerInClanStatement.getStatement();
		statement.setLong(1, player.getId());
		statement.executeUpdate();
	}

	public void invite(T clan, Player inviter, Player targetPlayer) {
		ClanPlayerInterface<T> target = AccountProvider.get(targetPlayer.getUniqueId());
		if (target.getClan() != null) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, stringAlreadyInClan);
			return;
		}

		if (getPlayerInvitations(targetPlayer).contains(clan)) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, stringAlreadyInvited);
			return;
		}

		List<T> localInvites = invitations.get(targetPlayer);
		if (localInvites == null) {
			localInvites = new ArrayList<>();
			invitations.put(targetPlayer, localInvites);
		}
		localInvites.add(clan);
		Prefix.DEFAULT_GOOD.sendMessage(inviter, stringPlayerInvited);

		BaseComponent[] texts = TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage(String.format(stringInvitationReceive, inviter.getName(), clan.getName())));
		for (BaseComponent comp : texts) {
			comp.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "clans accept " + clan.getName()));
			comp.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color(stringClickToJoin))));
		}
		targetPlayer.spigot().sendMessage(texts);
	}

	public List<T> getPlayerInvitations(Player p) {
		List<T> localInvites = invitations.get(p);
		return localInvites == null ? Collections.EMPTY_LIST : localInvites;
	}

	public void clearPlayerInvitations(Player p) {
		invitations.remove(p);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onJoin(OlympaPlayerLoadEvent e) {
		ClanPlayerInterface<T> oplayer = e.getOlympaPlayer();
		String name = e.getPlayer().getName();
		if (!enemiesBukkit.hasEntry(name)) enemiesBukkit.addEntry(name);
		NMS.sendPacket(NMS.addPlayersToTeam(clan, Arrays.asList(e.getPlayer().getName())), e.getPlayer());
		T clan = oplayer.getClan();
		if (clan != null) clan.memberJoin(oplayer);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		ClanPlayerInterface<T> oplayer = AccountProvider.get(e.getPlayer().getUniqueId());
		if (oplayer == null) return;
		T clan = oplayer.getClan();
		if (clan != null) clan.memberLeave(oplayer);
	}

	private static ScoreboardTeam createOrGetTeam(Scoreboard sc, String name, ChatColor color) throws ReflectiveOperationException {
		Team team = sc.getTeam(name);
		if (team == null) {
			team = sc.registerNewTeam(name);
			team.setColor(color);
		}
		return NMS.getNMSTeam(team);
	}

}
