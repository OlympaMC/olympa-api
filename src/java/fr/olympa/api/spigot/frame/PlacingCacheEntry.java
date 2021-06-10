package fr.olympa.api.spigot.frame;

public class PlacingCacheEntry {
	private String image;
	private boolean fastsend;
	private double scale;

	public PlacingCacheEntry(String image, boolean fastsend, double scale) {
		this.image = image;
		this.fastsend = fastsend;
		this.scale = scale;
	}

	public String getImage() {
		return this.image;
	}

	public double getScale() {
		return this.scale;
	}

	public boolean isFastSend() {
		return this.fastsend;
	}
}
