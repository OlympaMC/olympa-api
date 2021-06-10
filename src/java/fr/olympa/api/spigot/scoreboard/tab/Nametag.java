package fr.olympa.api.spigot.scoreboard.tab;

import net.md_5.bungee.api.ChatColor;

public class Nametag {
	//private final StringJoiner prefix, suffix;
	private String cachedPrefix, cachedSuffix;
	private StringBuilder prefix;
	private StringBuilder suffix;
	private boolean prefixSpace = false;
	private boolean suffixSpace = false;

	public Nametag() {
		/*prefix = new StringJoiner(" ");
		suffix = new StringJoiner(" ");*/
		prefix = new StringBuilder();
		suffix = new StringBuilder();
	}

	/*public Nametag(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}*/

	public String getPrefix() {
		if (cachedPrefix == null) {
			if (prefixSpace) prefix.append(' ');
			cachedPrefix = prefix.toString();
		}
		return cachedPrefix;
	}

	public String getSuffix() {
		if (cachedSuffix == null) {
			cachedSuffix = suffix.toString();
			if (!ChatColor.stripColor(cachedSuffix).isBlank()) cachedSuffix = " " + cachedSuffix;
		}
		return cachedSuffix;
	}

	public boolean isEmpty() {
		return prefix.length() == 0 && suffix.length() == 0;
	}

	/*public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}*/

	public void appendPrefix(String prefix) {
		if (prefix.isBlank()) return;
		//this.prefix.add(prefix);
		boolean blank = ChatColor.stripColor(prefix).isBlank();
		if (prefixSpace && !blank) this.prefix.append(' ');
		this.prefix.append(prefix);
		prefixSpace = !blank || prefixSpace;
		cachedPrefix = null;
	}
	
	public void appendSuffix(String suffix) {
		if (suffix.isBlank()) return;
		//this.suffix.add(suffix);
		boolean blank = ChatColor.stripColor(suffix).isBlank();
		if (suffixSpace && !blank) this.suffix.append(' ');
		this.suffix.append(suffix);
		suffixSpace = !blank || suffixSpace;
		cachedSuffix = null;
	}
	
	@Override
	public int hashCode() {
		int hash = 37;
		hash += hash * 11 * getPrefix().hashCode();
		hash += hash * 11 * getSuffix().hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Nametag) {
			Nametag tag = (Nametag) obj;
			return tag.getPrefix().equals(getPrefix()) && tag.getSuffix().equals(getSuffix());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getPrefix() + "|" + getSuffix();
	}
	
}