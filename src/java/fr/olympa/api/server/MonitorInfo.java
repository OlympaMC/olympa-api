package fr.olympa.api.server;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import fr.olympa.api.utils.Utils;

public class MonitorInfo {

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
	protected String lastModifiedCore;

	public static Pattern getIdPattern() {
		return ID_PATTERN;
	}

	public String getClearName() {
		return serverName.replaceFirst(ID_REGEX, "");
	}

	public String getIdSymbole() {
		int id = getId();
		if (id <= 0 || id > 10)
			return "";
		return Utils.intToSymbole(id);
	}

	public int getId() {
		java.util.regex.Matcher matcher = ID_PATTERN.matcher(serverName);
		matcher.find();
		String id = matcher.group();
		return id.isBlank() ? 0 : Integer.parseInt(id);
	}

	public String getLastModifiedCore() {
		return lastModifiedCore;
	}

	public MonitorInfo() {
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

	/**
	 * usless ? same as getId()
	 */
	@Deprecated
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

	public String getName() {
		return serverName;
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

	public void setStatus(ServerStatus status) {
		this.status = status;
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

	public String getRangeVersion() {
		if (firstVersion.equals(lastVersion))
			return firstVersion;
		return firstVersion + " à " + lastVersion;
	}

}