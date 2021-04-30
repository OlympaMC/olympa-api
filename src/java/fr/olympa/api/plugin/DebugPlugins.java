package fr.olympa.api.plugin;

import java.util.List;

import javax.annotation.Nullable;

import fr.olympa.api.utils.Utils;

public class DebugPlugins {

	protected String name;
	protected String version;
	protected List<String> authors;
	protected boolean enabled;
	@Nullable
	protected String website;
	@Nullable
	protected Boolean dependNotFound;
	@Nullable
	protected Boolean softDependNotFound;
	protected long lastModifiedTime;
	protected boolean hasConfig;

	public DebugPlugins() {
		super();
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean hasWebsite() {
		return website != null;
	}

	public String getWebsite() {
		return website;
	}

	public Boolean getDependNotFound() {
		return dependNotFound;
	}

	public Boolean getSoftDependNotFound() {
		return softDependNotFound;
	}

	public String getLastModifiedTime() {
		return Utils.tsToShortDur(lastModifiedTime);
	}

	public long getLastModifiedTimeLong() {
		return lastModifiedTime;
	}

}