package fr.olympa.api.server;

import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.plugin.DebugPlugins;
import fr.olympa.api.utils.machine.MachineInfo;

public class ServerDebug extends MachineInfo {

	public static ServerDebug fromJson(String string) {
		return new Gson().fromJson(string, ServerDebug.class);
	}

	protected static List<DebugPlugins> cachePlugins = null;
	protected String name;
	protected ServerStatus status;
	protected String uptime;
	protected float tps;
	protected List<DebugPlugins> plugins;
	protected String firstVersionMinecraft;
	protected String lastVersionMinecraft;
	protected String bukkitVersion;
	boolean hasConfig;
	protected boolean databaseConnected;
	boolean redisConnected;

	public ServerDebug() {
		super();
	}

	public String getName() {
		return name;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public float getTps() {
		return tps;
	}

	public List<DebugPlugins> getPlugins() {
		return plugins;
	}

	public String getFirstVersionMinecraft() {
		return firstVersionMinecraft;
	}

	public String getLastVersionMinecraft() {
		return lastVersionMinecraft;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}