package fr.olympa.api.scoreboard.tab;

import java.util.StringJoiner;

public class Nametag {
	private final StringJoiner prefix, suffix;
	private String cachedPrefix, cachedSuffix;

	public Nametag() {
		prefix = new StringJoiner(" ");
		suffix = new StringJoiner(" ");
	}

	/*public Nametag(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}*/

	public String getPrefix() {
		if (cachedPrefix == null) cachedPrefix = prefix.toString();
		return cachedPrefix;
	}

	public String getSuffix() {
		if (cachedSuffix == null) cachedSuffix = suffix.toString();
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
		this.prefix.add(prefix);
		cachedPrefix = null;
	}
	
	public void appendSuffix(String suffix) {
		if (suffix.isBlank()) return;
		this.suffix.add(suffix);
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
}