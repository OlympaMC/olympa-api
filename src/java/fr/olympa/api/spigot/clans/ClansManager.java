package fr.olympa.api.spigot.clans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.observable.ObservableList;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.spigot.clans.gui.ClanManagementGUI;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.utils.Prefix;
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
	
	public String stringAlreadyInClan = "Ce joueur est déjà dans un clan.";
	public String stringAlreadyInvited = "Tu as déjà invité ce joueur.";
	public String stringPlayerInvited = "Tu as invité le joueur à rejoindre ton clan !";
	public String stringInvitationReceive = "§l%s§r§a t'a invité à rejoindre son clan : \"§l%s§r§a\" ! §oClique ici ou accepte l'invitation depuis le menu.";
	public String stringClickToJoin = "§e§lClique pour rejoindre le clan !";
	public String stringYouAlreadyInClan = "Tu fais déjà partie d'un clan.";
	public String stringClanAlreadyExists = "Un clan avec ce nom existe déjà.";
	public String stringClanCreated = "Le clan a été créé !";
	public String stringNoInvitation = "Tu n'as pas reçu d'invitation de la part du clan \"%s\".";
	public String stringClanJoined = "Tu viens de rejoindre le clan §l\"%s\"§r§a !";
	public String stringClanFull = "Ce clan n'a plus la place pour accueillir un autre joueur...";
	public String stringCantLeaveChief = "Tu ne peux pas quitter le clan en en étant le chef. Transfère la direction de celui-ci à un autre joueur.";
	public String stringCantChiefSelf = "Tu ne peux pas te transférer la direction de ton propre clan.";
	public String stringPlayerNotInClan = "Le joueur %s ne fait pas partie du clan.";
	public String stringClanNotExist = "§cLe clan §4%s §cn'existe pas.";
	public String stringMustBeInClan = "Tu dois appartenir à un clan pour faire cette commande.";
	public String stringMustBeChief = "Tu dois être le chef du clan pour effectuer cette commande.";
	public String stringClanDisband = "§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.";
	public String stringPlayerChief = "Le joueur %s est désormais le chef du clan.";
	public String stringPlayerLeave = "Le joueur %s a quitté le clan.";
	public String stringPlayerJoin = "Le joueur %s rejoint le clan.";
	public String stringNameChange = "Le clan a changé de nom pour s'appeler %s.";
	public String stringTagChange = "Le clan a changé de tag et devient [%s] !";
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
	public String stringClanNameTooShort = "Le nom d'un clan ne peut pas faire moins de %d caractères !";
	public String stringClanNameInvalid = "Le nom du clan est invalide... Il doit seulement être constitué de lettres, de chiffres et de ces caractères : §l-'_";
	public String stringItemDisband = "§cDémenteler le clan";
	
	protected final SQLTable<T> clansTable;
	protected SQLColumn<T> nameColumn;
	protected SQLColumn<T> tagColumn;
	protected SQLColumn<T> chiefColumn;
	protected SQLColumn<T> sizeColumn;
	protected SQLColumn<T> moneyColumn;
	
	protected final SQLTable<D> playersTable;
	protected SQLColumn<D> playerIDColumn;
	protected SQLColumn<D> clanIDColumn;
	
	private Pattern clanNamePattern = Pattern.compile("^[0-9A-Za-zÀ-ÖØ-öø-ÿ-'_]+$");
	private Pattern clanTagPattern = Pattern.compile("^[0-9A-Za-zÀ-ÖØ-öø-ÿ]+$");
	private String randomTagCharacters = "AZERTYUIOPQSDFGHJKLMWXCVBN0123456789";
	
	public ClansManager(OlympaAPIPlugin plugin, String clansName) throws SQLException, ReflectiveOperationException {
		this.plugin = plugin;
		
		this.playersTable = new SQLTable<D>(clansName + "_players", addDBPlayersCollums(new ArrayList<>())).createOrAlter();
		this.clansTable = new SQLTable<T>(clansName, addDBClansCollums(new ArrayList<>()), resultSet -> {
			T clan = provideClan(
					resultSet.getInt("id"),
					resultSet.getString("name"),
					resultSet.getString("tag"),
					AccountProviderAPI.getter().getPlayerInformations(resultSet.getLong("chief")),
					resultSet.getInt("max_size"),
					resultSet.getDouble("money"),
					resultSet.getDate("created").getTime() / 1000L,
					resultSet);
			
			ResultSet playersSet = playersTable.getFromColumn(clanIDColumn, clan.getID());
			while (playersSet.next()) {
				D playerData = provideClanData(AccountProviderAPI.getter().getPlayerInformations(playersSet.getLong("player_id")), playersSet);
				clan.members.put(playerData.getPlayerInformations(), playerData);
			}
			playersSet.close();
			return clan;
		}).createOrAlter();
		
		clansTable.selectAll((ex, resultSet) -> {
			plugin.getLogger().severe("Impossible de charger le groupe " + resultSet.getInt("id"));
			return true;
		}).forEach(clan -> clans.put(clan.getID(), clan));
		
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		nameTagApi.addNametagHandler(EventPriority.LOWEST, (nametag, player, to) -> {
			ClanPlayerInterface<T, D> clanPlayer = (ClanPlayerInterface<T, D>) player;
			ClanPlayerInterface<T, D> clanTo = (ClanPlayerInterface<T, D>) to;
			if (clanPlayer.getClan() == null) return;
			ChatColor color;
			if (clanTo.getClan() != null) {
				color = clanTo.getClan() == clanPlayer.getClan() ? ChatColor.GREEN : ChatColor.RED;
			}else color = ChatColor.RED;
			nametag.appendPrefix(color + "[" + clanPlayer.getClan().getTag() + "]");
		});
	}
	
	public boolean nameExists(String name) {
		return clans.values().stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
	}
	
	public boolean tagExists(String tag) {
		return clans.values().stream().anyMatch(x -> x.getTag().equalsIgnoreCase(tag));
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
	
	public int getMinClanNameLength() {
		return 6;
	}
	
	/* Operations */
	
	public T get(final String nameOrTagOrPlayer) throws SQLException {
		T clan = getByName(nameOrTagOrPlayer);
		if (clan == null) {
			clan = getByTag(nameOrTagOrPlayer);
		}
		if (clan == null) {
			clan = AccountProviderAPI.getter().<ClanPlayerInterface<T, D>>get(nameOrTagOrPlayer).getClan();
		}
		return clan;
	}
	
	public T getByName(final String name) {
		return getClans().stream().filter(c -> name.equalsIgnoreCase(c.getValue().getName())).map(Entry<Integer, T>::getValue).findFirst().orElse(null);
	}
	
	public T getByTag(final String tag) {
		return getClans().stream().filter(c -> tag.equalsIgnoreCase(c.getValue().getTag())).map(Entry<Integer, T>::getValue).findFirst().orElse(null);
	}
	
	/* Abstraction */
	
	protected abstract String getClansCommand();
	
	protected abstract T createClan(int id, String name, String tag, OlympaPlayerInformations chief, int maxSize);
	
	protected abstract T provideClan(int id, String name, String tag, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException;
	
	protected abstract D createClanData(OlympaPlayerInformations informations);
	
	protected abstract D provideClanData(OlympaPlayerInformations informations, ResultSet resultSet) throws SQLException;
	
	public abstract int getMaxSize(ClanPlayerInterface<T, D> p);
	
	public List<SQLColumn<T>> addDBClansCollums(List<SQLColumn<T>> columns) {
		columns.add(new SQLColumn<T>("id", "int(11) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(T::getID));
		columns.add(nameColumn = new SQLColumn<T>("name", "varchar(45) NOT NULL", Types.VARCHAR).setUpdatable());
		columns.add(tagColumn = new SQLColumn<T>("tag", "char(3) NOT NULL UNIQUE", Types.CHAR).setUpdatable());
		columns.add(chiefColumn = new SQLColumn<T>("chief", "bigint(20) NOT NULL", Types.BIGINT).setUpdatable());
		columns.add(sizeColumn = new SQLColumn<T>("max_size", "tinyint(1) NOT NULL", Types.TINYINT).setUpdatable());
		columns.add(moneyColumn = new SQLColumn<T>("money", "DOUBLE NOT NULL DEFAULT 0", Types.DOUBLE).setUpdatable());
		columns.add(new SQLColumn<T>("created", "DATE NOT NULL DEFAULT curdate()", Types.DATE));
		return columns;
	}
	
	public List<SQLColumn<D>> addDBPlayersCollums(List<SQLColumn<D>> columns) {
		columns.add(playerIDColumn = new SQLColumn<D>("player_id", "BIGINT NOT NULL", Types.BIGINT).setPrimaryKey(player -> player.getPlayerInformations().getId()));
		columns.add(clanIDColumn = new SQLColumn<D>("clan", "INT NOT NULL", Types.INTEGER));
		return columns;
	}
	
	public Pattern getClanNamePattern() {
		return clanNamePattern;
	}
	
	public Pattern getClanTagPattern() {
		return clanTagPattern;
	}
	
	public final boolean checkName(Player p, String name) {
		if (name.length() > getMaxClanNameLength()) {
			Prefix.DEFAULT_BAD.sendMessage(p, stringClanNameTooLong, getMaxClanNameLength());
			return false;
		}
		if (name.length() < getMinClanNameLength()) {
			Prefix.DEFAULT_BAD.sendMessage(p, stringClanNameTooShort, getMinClanNameLength());
			return false;
		}
		if (!getClanNamePattern().matcher(name).matches()) {
			Prefix.DEFAULT_BAD.sendMessage(p, stringClanNameInvalid);
			return false;
		}
		if (nameExists(name)) {
			Prefix.DEFAULT_BAD.sendMessage(p, stringClanAlreadyExists);
			return false;
		}
		return true;
	}
	
	public final boolean checkTag(Player p, String tag) {
		if (tag.length() != 3) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Le tag doit faire 3 caractères de long.");
			return false;
		}
		if (!getClanTagPattern().matcher(tag).matches()) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tag invalide (le tag doit être uniquement composé de lettres et de chiffres).");
			return false;
		}
		if (tagExists(tag)) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Le tag est déjà utilisé.");
			return false;
		}
		return true;
	}
	
	public final String generateTag(String name) {
		name = name.replaceAll("['\\-_]", "").toUpperCase();
		if (name.length() < 3) name += "a".repeat(3 - name.length());
		for (int removedIndex = -1; removedIndex < name.length(); removedIndex++) { // enlève 1 lettre au fur et à mesure pour tenter des combinaisons pas encore faites
			String nameM = removedIndex == -1 ? name : name.substring(0, removedIndex) + name.substring(removedIndex + 1);
			for (int startIndex = 0; startIndex + 3 < nameM.length(); startIndex++) { // prend les 3 lettres en partant à chaque fois d'un peu + loin dans le mot
				String tag = nameM.substring(startIndex, startIndex + 3);
				if (!tagExists(tag)) return tag;
			}
		}
		
		Random random = ThreadLocalRandom.current();
		String tag;
		do {
			tag = "";
			for (int i = 0; i < 3; i++) tag += randomTagCharacters.charAt(random.nextInt(randomTagCharacters.length()));
		}while (tagExists(tag));
		return tag;
	}
	
	/* SQL statements */
	
	public T createClan(ClanPlayerInterface<T, D> p, String name, String tag) throws SQLException {
		int maxSize = getMaxSize(p);
		ResultSet resultSet = clansTable.insert(name, tag, p.getId(), maxSize);
		resultSet.next();
		int id = resultSet.getInt(1);
		resultSet.close();
		T clan = createClan(id, name, tag, p.getInformation(), maxSize);
		clans.put(id, clan);
		clan.addPlayer(p, false);
		clan.broadcast(stringClanCreated);
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
	
	public void insertPlayerInClan(ClanPlayerInterface<T, D> p, Clan<T, D> clan) {
		playersTable.deleteSQLObjectAsync(p.getId(), () -> {
			playersTable.insertAsync(null, null, p.getId(), clan.getID());
		}, null);
	}
	
	public void removePlayerFromClan(D player, Runnable success, Consumer<SQLException> fail) {
		playersTable.deleteAsync(player, success, fail);
	}
	
	public ClanManagementGUI<T, D> provideManagementGUI(ClanPlayerInterface<T, D> player) {
		return new ClanManagementGUI<>(player, player.getClan(), this, 2);
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
		ClanPlayerInterface<T, D> target = AccountProviderAPI.getter().get(targetPlayer.getUniqueId());
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
		
		BaseComponent[] texts = TextComponent.fromLegacyText(Prefix.DEFAULT_GOOD.formatMessage(stringInvitationReceive, inviter.getName(), clan.getNameAndTag()));
		for (BaseComponent comp : texts) {
			comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getClansCommand() + " accept " + clan.getTag()));
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
		ClanPlayerInterface<T, D> oplayer = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		if (oplayer == null)
			return;
		T clan = oplayer.getClan();
		if (clan != null)
			clan.memberLeave(oplayer);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		ClanPlayerInterface<T, D> target = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		if (target.getClan() == null) return;
		e.setFormat("§8[" + target.getClan().getTag() + "] " + target.getGroupPrefix() + "%s " + target.getGroup().getChatSuffix() + " %s");
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
