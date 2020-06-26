package fr.olympa.api.clans;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public abstract class Clan<T extends Clan<T>> {

	protected final ClansManager<T> manager;
	protected final int id;

	protected Map<OlympaPlayerInformations, ClanPlayerData<T>> members = new HashMap<>(8);
	private OlympaPlayerInformations chief;
	private String name;
	private int maxSize;
	private OlympaMoney money;
	private long created;

	public Clan(ClansManager<T> manager, int id, String name, OlympaPlayerInformations chief, int maxSize) {
		this(manager, id, name, chief, maxSize, 0, Utils.getCurrentTimeInSeconds());
	}

	public Clan(ClansManager<T> manager, int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created) {
		this.manager = manager;
		this.id = id;
		this.name = name;
		this.chief = chief;
		this.maxSize = maxSize;
		this.created = created;
		this.money = new OlympaMoney(money);
		this.money.observe("clan_update_db", this::updateMoney);
	}

	public boolean addPlayer(ClanPlayerInterface<T> p) {
		if (members.size() >= getMaxSize())
			return false;
		p.setClan((T) this);
		// packets pour mettre le nouveau joueur en vert pour les anciens
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		Nametag nametag = new Nametag(null, " §a" + this.getName());
		nameTagApi.updateFakeNameTag(p.getPlayer(), nametag, getPlayers());
		//Player[] players = getPlayersArray();
		//List<String> joiner = Arrays.asList(p.getName());
		//NMS.sendPacket(NMS.removePlayersFromTeam(manager.enemies, joiner), players);
		//NMS.sendPacket(NMS.addPlayersToTeam(manager.clan, joiner), players);
		// packets pour mettre les autres joueurs en vert pour le nouveau
		for (Player player : getPlayers()) nameTagApi.updateFakeNameTag(player, nametag, Arrays.asList(p.getPlayer()));
		members.put(p.getInformation(), manager.createClanData(p.getInformation()));
		memberJoin(p);
		broadcast(String.format(manager.stringPlayerJoin, p.getName()));
		return true;
	}

	public void broadcast(String message) {
		String finalMessage = ColorUtils.color(Prefix.DEFAULT + "§6" + name + " §e: " + message + " Terminé.");
		executeAllPlayers(p -> p.sendMessage(finalMessage));
	}

	public boolean contains(OlympaPlayer p) {
		return members.containsKey(p.getInformation());
	}

	public void disband() {
		broadcast(manager.stringClanDisband);
		for (OlympaPlayerInformations member : members.keySet()) removePlayer(member, false);
		manager.removeClan((T) this);
	}

	public void executeAllPlayers(Consumer<Player> consumer) {
		for (ClanPlayerData<T> member : members.values())
			if (member.isConnected()) consumer.accept(member.getConnectedPlayer().getPlayer());
	}

	public OlympaPlayerInformations getChief() {
		return chief;
	}

	public int getID() {
		return id;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public Collection<ClanPlayerData<T>> getMembers() {
		return members.values();
	}

	public int getMembersAmount() {
		return members.size();
	}

	public String getName() {
		return name;
	}

	public OlympaMoney getMoney() {
		return money;
	}

	public Set<Player> getPlayers() {
		return members.values().stream().filter(entry -> entry.isConnected()).map(entry -> entry.getConnectedPlayer().getPlayer()).collect(Collectors.toSet());
	}

	public long getCreationTime() {
		return created;
	}

	public void memberJoin(ClanPlayerInterface<T> member) {
		members.get(member.getInformation()).playerJoin(member);

		//List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		//NMS.sendPacket(NMS.removePlayersFromTeam(manager.enemies, names), p);
		//NMS.sendPacket(NMS.addPlayersToTeam(manager.clan, names), p);
	}

	public void memberLeave(ClanPlayerInterface<T> p) {
		members.get(p.getInformation()).playerLeaves();
	}

	protected void removedOnlinePlayer(ClanPlayerInterface<T> oplayer) {
		// packets pour mettre le suffix des autres joueurs en rouge pour le partant
		Player player = oplayer.getPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		Nametag nametag = new Nametag(null, " §c" + this.getName());
		for (Player otherP : getPlayers()) nameTagApi.updateFakeNameTag(otherP, nametag, Arrays.asList(player));
		// packets pour mettre les autres joueurs en rouge pour le partant
		//List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		//NMS.sendPacket(NMS.removePlayersFromTeam(manager.clan, names), player);
		//NMS.sendPacket(NMS.addPlayersToTeam(manager.enemies, names), player);
		oplayer.setClan(null);
	}

	public void removePlayer(OlympaPlayerInformations pinfo, boolean message) {
		if (message)
			broadcast(String.format(manager.stringPlayerLeave, pinfo.getName()));
		ClanPlayerData<T> member = members.remove(pinfo);
		// packets pour mettre le joueur partant sans suffix pour tous le monde
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		Nametag nametag = new Nametag(null, "");
		nameTagApi.updateFakeNameTag(pinfo.getName(), nametag, Bukkit.getOnlinePlayers());
		// packets pour mettre le joueur partant en rouge pour les restants
		//Player[] players = getPlayersArray();
		//List<String> leaver = Arrays.asList(pinfo.getName());
		//NMS.sendPacket(NMS.removePlayersFromTeam(manager.clan, leaver), players);
		//NMS.sendPacket(NMS.addPlayersToTeam(manager.enemies, leaver), players);

		if (member.isConnected()) {
			removedOnlinePlayer(member.getConnectedPlayer());
		}else {
			try {
				manager.removeOfflinePlayerFromClan(pinfo);
			} catch (SQLException e) {
				e.printStackTrace();
				broadcast("Une erreur est survenue.");
			}
			return;
		}
	}

	public void setChief(OlympaPlayerInformations p) {
		try {
			PreparedStatement statement = manager.updateClanChiefStatement.getStatement();
			statement.setLong(1, p.getId());
			statement.setInt(2, id);
			statement.executeUpdate();
			this.chief = p;
			broadcast(String.format(manager.stringPlayerChief, p.getName()));
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}

	public void setMaxSize(int maxSize) {
		try {
			PreparedStatement statement = manager.updateClanMaxStatement.getStatement();
			statement.setInt(1, maxSize);
			statement.setInt(2, id);
			statement.executeUpdate();
			this.maxSize = maxSize;
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}

	public void setName(String name) {
		try {
			PreparedStatement statement = manager.updateClanNameStatement.getStatement();
			statement.setString(1, name);
			statement.setInt(2, id);
			statement.executeUpdate();
			this.name = name;
			broadcast(manager.stringNameChange);
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}
	
	private void updateMoney() {
		try {
			PreparedStatement statement = manager.updateClanMoneyStatement.getStatement();
			statement.setDouble(1, money.get());
			statement.setInt(2, id);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			broadcast("Une erreur est survenue.");
		}
	}

}
