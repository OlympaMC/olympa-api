package fr.olympa.api.scoreboard.tab;

public class Nametag {
	private String prefix;
	private String suffix;

	public Nametag() {
		prefix = "";
		suffix = "";
	}

	public Nametag(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isEmpty() {
		return (prefix == null || prefix.isBlank()) && (suffix == null || suffix.isBlank());
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void appendSuffix(String suffix) {
		this.suffix += suffix;
	}

	public void appendPrefix(String prefix) {
		this.prefix += prefix;
	}
}