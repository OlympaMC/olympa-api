package fr.olympa.api.common.server;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;

public class ServerInfoBasic {

	private static final String ID_REGEX = "\\d*$";
	private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);

	public static Entry<OlympaServer, Integer> getOlympaServer(String serverName) {
		java.util.regex.Matcher matcher = ID_PATTERN.matcher(serverName);
		matcher.find();
		String id = matcher.group();
		int serverID = Utils.isEmpty(id) ? 0 : Integer.parseInt(id);
		OlympaServer olympaServer = OlympaServer.valueOf(matcher.replaceAll("").toUpperCase());
		return new AbstractMap.SimpleEntry<>(olympaServer, serverID);
	}

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
	protected Integer ramUsage;
	@Expose
	protected Integer threads;
	@Expose
	protected Integer allThreads;
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

	public boolean canConnect(OlympaPlayer olympaPlayer) {
		return status.canConnect() && olympaServer.canConnect(olympaPlayer) && status.hasPermission(olympaPlayer);
	}

	public static Pattern getIdPattern() {
		return ID_PATTERN;
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
	 * @return officiel bungee name like Créatif ➊
	 */
	public String getHumanName() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getOlympaServer().getNameCaps());
		String symbole = getIdSymbole();
		if (!symbole.isBlank())
			sb.append(" " + symbole);
		return sb.toString();
	}

	public String getIdSymbole() {
		int id = getId();
		if (id <= 0 || id > 10)
			return "";
		return Utils.intToSymbole(id);
	}

	/**
	 * usless ? same as getServerID()
	 */
	@Deprecated
	public int getId() {
		java.util.regex.Matcher matcher = ID_PATTERN.matcher(serverName);
		matcher.find();
		String id = matcher.group();
		return id.isBlank() ? 0 : Integer.parseInt(id);
	}

	public String getRangeVersion() {
		if (firstVersion.equals(lastVersion))
			return firstVersion;
		return firstVersion + " à " + lastVersion;
	}

	public String getLastModifiedCore() {
		if (lastModifiedCore == 0)
			return null;
		return Utils.tsToShortDur(lastModifiedCore);
	}

	public ServerInfoBasic() {
		super();
	}

	public Integer getRamUsage() {
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

	public Integer getAllThreads() {
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