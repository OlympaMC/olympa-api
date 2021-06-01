package fr.olympa.api.plugin;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.annotation.Nullable;

import fr.olympa.api.match.MatcherPattern;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.utils.Utils;

public class DebugPlugins {

	public class SuperVersion {

		String original;

		String versionNumber;
		String gitBranch;
		String gitCommit;
		long buildTime;
		String versionMore;
		boolean isUsless;

		public SuperVersion(String s) {
			original = s;
			MatcherPattern<?> regexMatcher;
			Entry<String, ?> entry;
			int i = 0;
			regexMatcher = MatcherPattern.of("\\W*([a-fA-F0-9]{8,40})\\b");
			entry = regexMatcher.extractAndParseGroupOne(s);
			if (entry.getValue() != null) {
				s = entry.getKey();
				gitCommit = (String) entry.getValue();
				i++;
			}
			regexMatcher = RegexMatcher.BUILD_TIME;
			entry = regexMatcher.extractAndParseGroupOne(s);
			if (entry.getValue() != null) {
				s = entry.getKey();
				buildTime = (long) entry.getValue();
				i++;
			}
			regexMatcher = MatcherPattern.of("\\W*(\\d+\\.\\d+(\\.\\d+)?(-SNAPSHOT)?)\\b");
			entry = regexMatcher.extractAndParseGroupOne(s);
			if (entry.getValue() != null) {
				s = entry.getKey();
				versionNumber = (String) entry.getValue();
				i++;
			}
			regexMatcher = MatcherPattern.of("\\W*(test|dev|master)\\b");
			entry = regexMatcher.extractAndParseGroupOne(s);
			if (entry.getValue() != null) {
				s = entry.getKey();
				gitBranch = (String) entry.getValue();
				i++;
			}
			if (!s.isBlank())
				versionMore = s;
			isUsless = i < 2;
		}

		public String get() {
			if (isUsless())
				return original;
			StringJoiner sj = new StringJoiner(" ");
			if (versionMore != null)
				sj.add(versionMore);
			if (versionNumber != null && versionNumber.isBlank())
				sj.add(versionNumber);
			if (gitBranch != null && gitBranch.isBlank())
				sj.add("branch " + gitBranch);
			if (gitCommit != null && gitCommit.isBlank())
				sj.add("commit " + gitCommit);
			if (buildTime != 0)
				sj.add("build " + Utils.tsToShortDur(buildTime));
			return sj.toString();
		}

		public boolean isUsless() {
			return isUsless;
		}
	}

	protected String name;
	protected String version;
	protected @Nullable SuperVersion superVersion;
	protected List<String> authors;
	protected List<String> contributors;
	protected boolean enabled;
	protected @Nullable String website;
	protected @Nullable Boolean dependNotFound;
	protected @Nullable Boolean softDependNotFound;
	protected @Nullable String description;
	protected long lastModifiedTime;
	protected boolean hasConfig;
	protected @Nullable String apiVersion;
	protected List<String> provides;

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

	@Nullable
	public String getWebsite() {
		return website;
	}

	@Nullable
	public Boolean getDependNotFound() {
		return dependNotFound;
	}

	@Nullable
	public Boolean getSoftDependNotFound() {
		return softDependNotFound;
	}

	public String getLastModifiedTime() {
		return Utils.tsToShortDur(lastModifiedTime);
	}

	public long getLastModifiedTimeLong() {
		return lastModifiedTime;
	}

	@Nullable
	public SuperVersion getSuperVersion() {
		return superVersion;
	}

	public List<String> getContributors() {
		return contributors;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public boolean isHasConfig() {
		return hasConfig;
	}

	@Nullable
	public String getApiVersion() {
		return apiVersion;
	}

	public List<String> getProvides() {
		return provides;
	}

}