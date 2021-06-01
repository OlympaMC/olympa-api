package fr.olympa.api.spigot.frame;

public class ImageMap {
	private String image;
	private int x;
	private int y;
	private boolean fastsend;
	private double scale;

	public ImageMap(String image, int x, int y, boolean fastsend, double scale) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.fastsend = fastsend;
		this.scale = scale;
	}

	public String getImage() {
		return this.image;
	}

	public double getScale() {
		return this.scale;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public boolean isFastSend() {
		return this.fastsend;
	}

	public boolean isSimilar(String file, int x2, int y2, double d) {
		if (!this.getImage().equalsIgnoreCase(file)) {
			return false;
		}
		if (this.getX() != x2) {
			return false;
		}
		if (this.getY() != y2) {
			return false;
		}

		double diff = d - this.getScale();
		return diff > -0.0001 && diff < 0.0001;
	}
}
