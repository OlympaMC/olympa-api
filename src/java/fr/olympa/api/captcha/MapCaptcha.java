package fr.olympa.api.captcha;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapCaptcha {

	private ItemStack map;
	private String answer;

	public MapCaptcha() {
		this(128);
	}

	/**
	 * Create a new captcha. Should be called async since some used methods are low-performance
	 */
	public MapCaptcha(int size) {
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		answer = RandomStringUtils.randomAlphabetic(5).toLowerCase();

		Canvas canvas = new Canvas();
		canvas.setSize(size, size);

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.paint(graphics);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, size, size);

		/*
		try {
		    BufferedImage background = ImageIO.read(plugin.getResource("background.png"));
		    AffineTransform affineTransform = new AffineTransform();
		    graphics.drawImage(background, affineTransform, null);
		} catch (Exception exception) {
		    exception.printStackTrace();
		}*/

		//boolean color = plugin.getConfig().getBoolean("captcha-settings.color");

		int lines = ThreadLocalRandom.current().nextInt(10, 25);
		while (lines != 0) {
			lines--;
			graphics.setColor(getRandomColor());
			graphics.drawLine(getRandomCoordinate(), getRandomCoordinate(), getRandomCoordinate(), getRandomCoordinate());
		}

		graphics.setFont(new Font("Arial", Font.PLAIN, 34));

		String[] split = answer.split("");
		for (int i = 0; i != split.length; i++) {
			AffineTransform original = graphics.getTransform();
			int rotation = ThreadLocalRandom.current().nextInt(0, 45);
			graphics.rotate(Math.toRadians(rotation) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1), 10 + 20 * (i + 1), 64);
			graphics.setColor(getRandomColor());
			graphics.drawString(split[i], 20 * (i + 1) - 10, 70);
			graphics.setTransform(original);
		}

		graphics.dispose();

		map = generateItem(image);
	}

	private ItemStack generateItem(BufferedImage image) {
		ItemStack it = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta) it.getItemMeta();
		MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));

		view.getRenderers().clear();
		view.addRenderer(new MapRenderer() {
			boolean rendered = false;

			@Override
			public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
				if (rendered)
					return;
				mapCanvas.drawImage(0, 0, image);
				rendered = true;
			}
		});

		meta.setMapView(view);
		it.setItemMeta(meta);

		return it;
	}

	public String getAnswer() {
		return answer;
	}

	public ItemStack getMap() {
		return map;
	}

	private int getRandomCoordinate() {
		return ThreadLocalRandom.current().nextInt(0, 128);
	}

	private Color getRandomColor() {
		return new Color(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256));
	}
}
