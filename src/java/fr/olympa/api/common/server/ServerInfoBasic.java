package fr.olympa.api.common.server;

import com.google.gson.annotations.Expose;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;

@Deprecated
public class ServerInfoBasic {

	private static final String ID_REGEX = "\\d*$";

	@Expose
	protected String serverName;
	@Expose
	protected OlympaServer olympaServer;
	@Expose
	protected int serverID;
	@Expose
	protected Integer ping;
	@Expose
	protected Integer onlinePlayers;
	@Expose
	protected Integer maxPlayers;
	@Expose
	protected Long ramUsage;
	@Expose
	protected Integer threads;
	@Expose
	protected Long allThreads;
	@Expose
	protected ServerStatus status = ServerStatus.UNKNOWN;
	@Expose
	protected String error;
	@Expose
	protected Float tps;
	@Expose
	protected String firstVersion = "unknown";
	@Expose
	protected String lastVersion = "unknown";
	@Expose
	protected long lastModifiedCore;

	public ServerInfoBasic() {}

	@Deprecated
	public ServerInfoBasic(String serverName, OlympaServer olympaServer, int serverID, Integer ping, Integer onlinePlayers, Integer maxPlayers, Long ramUsage, Integer threads, Long allThreads, ServerStatus status, String error,
			Float tps, String firstVersion, String lastVersion, long lastModifiedCore) {
		this.serverName = serverName;
		this.olympaServer = olympaServer;
		this.serverID = serverID;
		this.ping = ping;
		this.onlinePlayers = onlinePlayers;
		this.maxPlayers = maxPlayers;
		this.ramUsage = ramUsage;
		this.threads = threads;
		this.allThreads = allThreads;
		this.status = status;
		this.error = error;
		this.tps = tps;
		this.firstVersion = firstVersion;
		this.lastVersion = lastVersion;
		this.lastModifiedCore = lastModifiedCore;
	}

	public boolean canConnect(OlympaPlayer olympaPlayer) {
		return status.canConnect() && olympaServer.canConnect(olympaPlayer) && status.hasPermission(olympaPlayer);
	}

	/**
	 * @return name in lowercase without number like creatif for creatif1
	 */
	public String getClearName() {
		return serverName.replaceFirst(ID_REGEX, "");
	}

	/**
	 * @return officiel bungee name
	 */
	public String getName() {
		return serverName;
	}

	/**
	 * @return officiel bungee name like Cr??atif ???
	 */
	public String getHumanName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOlympaServer().getNameCaps());
		String symbole = getIdSymbole();
		if (!symbole.isBlank())
			sb.append(" " + symbole);
		return sb.toString();
	}

	public String getIdSymbole() {
		int id = getServerID();
		if (id <= 0 || id > 10)
			return "";
		return Utils.intToSymbole(id);
	}

	public String getRangeVersion() {
		if (firstVersion.equals(lastVersion))
			return firstVersion;
		return firstVersion + " ?? " + lastVersion;
	}

	public String getLastModifiedCore() {
		if (lastModifiedCore == 0)
			return null;
		return Utils.tsToShortDur(lastModifiedCore);
	}

	public Long getRamUsage() {
		return ramUsage;
	}

	public Integer getThreads() {
		return threads;
	}

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	public int getServerID() {
		return serverID;
	}

	public Long getAllThreads() {
		return allThreads;
	}

	public String getError() {
		return error;
	}

	public boolean isUsualError() {
		return status == ServerStatus.CLOSE && (error == null || error.isEmpty());
	}

	public boolean isDefaultError() {
		return status == ServerStatus.CLOSE && error != null && error.isEmpty();
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	public Integer getOnlinePlayers() {
		return onlinePlayers;
	}

	public Integer getPing() {
		return ping;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public Float getTps() {
		return tps;
	}

	public boolean isOpen() {
		return status != ServerStatus.CLOSE;
	}

	public String getLastVersion() {
		return lastVersion;
	}

	public String getFirstVersion() {
		return firstVersion;
	}

	public void setStatus(ServerStatus status) {
		this.status = status;
	}

}