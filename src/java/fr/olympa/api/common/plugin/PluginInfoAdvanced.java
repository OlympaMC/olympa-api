package fr.olympa.api.common.plugin;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.annotation.Nullable;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class PluginInfoAdvanced {

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

	public PluginInfoAdvanced() {
		super();
	}

	public String getName() {
		return name;
	}

	public String getNameColored() {
		return (enabled ? ChatColor.GREEN : ChatColor.RED) + name;
	}

	public String getVersion() {
		if (superVersion != null && !superVersion.isUsless())
			return superVersion.get();
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

	public TxtComponentBuilder getToTxtComponent() {
		TxtComponentBuilder txt = new TxtComponentBuilder(getNameWithBuildTime());
		String hoverText = getToStringHumain();
		String url = getGitCompareUrl();
		if (url != null) {
			hoverText += "\n\n&7[&2CLIQUE&7] pour git compare cette version avec la dernière push";
			txt.onClickUrl(url);
		} else if (website != null) {
			hoverText += "\n\n&7[&2CLIQUE&7] pour aller sur le site web";
			txt.onClickUrl(website);
		}
		txt.onHoverText(hoverText);
		return txt;
	}

	public String getNameWithBuildTime() {
		if (superVersion != null && superVersion.getBuildTime() != null)
			return getNameColored() + "&7(" + superVersion.getBuildTime() + ")";
		return getNameColored();
	}

	@Nullable
	public String getGitCompareUrl() {
		if (superVersion != null) {
			String commitId = superVersion.getGitCommit();
			String branch = superVersion.getGitBranch();
			if (branch == null || branch.isBlank())
				branch = "master";
			if (commitId != null && website != null && website.contains("git"))
				return website + (website.endsWith("/") ? "" : "/") + "-/compare/" + commitId + "..." + branch;
		}
		return null;

	}

	public String getToStringHumain() {
		StringJoiner sj = new StringJoiner("\n&7", "&7", "");
		sj.add(getNameColored());
		sj.add("Version &2" + getVersion());
		sj.add("MAJ du fichier &e" + getLastModifiedTime());
		List<String> authors = getAuthors();
		if (authors != null && !authors.isEmpty())
			sj.add("Auteur" + Utils.withOrWithoutS(authors.size()) + " &a" + String.join("&7, &a", authors));
		List<String> contributors = getContributors();
		if (contributors != null && !contributors.isEmpty())
			sj.add("Contributeur" + Utils.withOrWithoutS(contributors.size()) + " &a" + String.join("&7, &a", contributors));
		if (getDescription() != null)
			sj.add("Description " + getDescription());
		if (getWebsite() != null)
			sj.add("Site Web " + getWebsite());
		return sj.toString();
	}

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
			StringJoiner sj = new StringJoiner(" &7");
			if (versionMore != null)
				sj.add("&a" + versionMore);
			if (versionNumber != null && !versionNumber.isBlank())
				sj.add("&a" + versionNumber);
			if (gitBranch != null && !gitBranch.isBlank())
				sj.add("branch &a" + gitBranch);
			if (gitCommit != null && !gitCommit.isBlank())
				sj.add("commit &a" + gitCommit);
			if (buildTime != 0)
				sj.add("build &a" + getBuildTime());
			return sj.toString();
		}

		public String getOriginal() {
			return original;
		}

		public String getVersionNumber() {
			return versionNumber;
		}

		public String getGitBranch() {
			return gitBranch;
		}

		public String getGitCommit() {
			return gitCommit;
		}

		public String getBuildTime() {
			if (buildTime == 0)
				return null;
			return Utils.tsToShortDur(buildTime);
		}

		public String getVersionMore() {
			return versionMore;
		}

		public boolean isUsless() {
			return isUsless;
		}
	}

}