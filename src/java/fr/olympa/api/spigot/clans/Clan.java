package fr.olympa.api.spigot.clans;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public abstract class Clan<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> {

	protected final ClansManager<T, D> manager;
	protected final int id;

	protected Map<OlympaPlayerInformations, D> members = new HashMap<>(8);
	private OlympaPlayerInformations chief;
	private String name;
	private String tag;
	private int maxSize;
	private OlympaMoney money;
	private long created;

	protected Clan(ClansManager<T, D> manager, int id, String name, String tag, OlympaPlayerInformations chief, int maxSize) {
		this(manager, id, name, tag, chief, maxSize, 0, Utils.getCurrentTimeInSeconds());
	}

	protected Clan(ClansManager<T, D> manager, int id, String name, String tag, OlympaPlayerInformations chief, int maxSize, double money, long created) {
		this.manager = manager;
		this.id = id;
		this.name = name;
		this.tag = tag;
		this.chief = chief;
		this.maxSize = maxSize;
		this.created = created;
		this.money = new OlympaMoney(money);
		this.money.observe("clan_update_db", this::updateMoney);
	}

	public boolean addPlayer(ClanPlayerInterface<T, D> p, boolean msg) {
		if (members.size() >= getMaxSize())
			return false;
		p.setClan((T) this);
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		/*Nametag nametag = new Nametag(null, " §a" + this.getName());
		nameTagApi.updateFakeNameTag(p.getPlayer(), nametag, getPlayers());
		for (Player player : getPlayers())
			nameTagApi.updateFakeNameTag(player, nametag, Arrays.asList(p.getPlayer()));*/
		nameTagApi.callNametagUpdate(p);
		members.put(p.getInformation(), manager.createClanData(p.getInformation()));
		memberJoin(p);
		if (msg) broadcast(String.format(manager.stringPlayerJoin, p.getName()));
		manager.insertPlayerInClan(p, this);
		return true;
	}

	protected String format(String message, Object... args) {
		return "§6§l" + name + " §7➤ §e" + String.format(message, args) + " Terminé.";
	}

	public void broadcast(String message, Object... args) {
		String finalMessage = format(message, args);
		executeAllPlayers(p -> p.sendMessage(finalMessage));
	}

	public boolean contains(OlympaPlayerInformations informations) {
		return members.containsKey(informations);
	}

	public void disband() {
		broadcast(manager.stringClanDisband);
		for (OlympaPlayerInformations member : new ArrayList<>(members.keySet()))
			removePlayer(member, false);
		manager.removeClan((T) this);
	}

	public void executeAllPlayers(Consumer<Player> consumer) {
		for (ClanPlayerData<T, D> member : members.values())
			if (member.isConnected())
				consumer.accept((Player) member.getConnectedPlayer().getPlayer());
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

	public Collection<D> getMembers() {
		return members.values();
	}

	public D getMember(OlympaPlayerInformations player) {
		return members.get(player);
	}

	public int getMembersAmount() {
		return members.size();
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}
	
	public String getNameAndTag() {
		return name + " [" + tag + "]";
	}
	
	public OlympaMoney getMoney() {
		return money;
	}

	public Set<Player> getPlayers() {
		return members.values().stream().filter(entry -> entry.isConnected()).map(entry -> (Player) entry.getConnectedPlayer().getPlayer()).collect(Collectors.toSet());
	}

	public Set<OlympaPlayer> getOlympaPlayers() {
		return members.values().stream().filter(entry -> entry.isConnected()).map(D::getConnectedPlayer).collect(Collectors.toSet());
	}
	
	public long getCreationTime() {
		return created;
	}

	public boolean isSameClan(T clan) {
		return this.id == clan.getID();
	}

	public <M extends ClansManager<T, D>> M getClansManager() {
		return (M) manager;
	}

	public void memberJoin(ClanPlayerInterface<T, D> member) {
		members.get(member.getInformation()).playerJoin(member);
	}

	public void memberLeave(ClanPlayerInterface<T, D> p) {
		members.get(p.getInformation()).playerLeaves();
	}

	protected void removedOnlinePlayer(ClanPlayerInterface<T, D> oplayer) {
		oplayer.setClan(null);
		OlympaCore.getInstance().getNameTagApi().callNametagUpdate(oplayer);
	}

	public void removePlayer(OlympaPlayerInformations pinfo, boolean message) {
		D member = members.get(pinfo);
		manager.removePlayerFromClan(member, () -> {
			members.remove(pinfo);
			if (message)
				broadcast(String.format(manager.stringPlayerLeave, pinfo.getName()));
			if (member.isConnected())
				removedOnlinePlayer(member.getConnectedPlayer());
		}, ex -> broadcast("Une erreur est survenue lors de la suppression d'un joueur."));
	}

	public void setChief(OlympaPlayerInformations p) {
		manager.chiefColumn.updateAsync((T) this, p.getId(), () -> {
			this.chief = p;
			broadcast(String.format(manager.stringPlayerChief, p.getName()));
		}, null);
	}

	public void setMaxSize(int maxSize) {
		manager.sizeColumn.updateAsync((T) this, maxSize, () -> {
			int old = this.maxSize;
			this.maxSize = maxSize;
			broadcast("Le nombre maximal de membres est passé de %d à %d !", old, maxSize);
		}, null);
	}

	public void setName(String name) {
		manager.nameColumn.updateAsync((T) this, name, () -> {
			this.name = name;
			broadcast(manager.stringNameChange, name);
		}, null);
	}

	public void setTag(String tag) {
		manager.tagColumn.updateAsync((T) this, tag, () -> {
			this.tag = tag;
			broadcast(manager.stringTagChange, tag);
			getMembers().stream().filter(D::isConnected).map(D::getConnectedPlayer).forEach(OlympaCore.getInstance().getNameTagApi()::callNametagUpdate);
		}, null);
	}
	
	private void updateMoney() {
		manager.moneyColumn.updateAsync((T) this, money.get(), null, SQLException::printStackTrace);
	}

}
