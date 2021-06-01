package fr.olympa.api.spigot.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireWorkUtils {

	public static void spawnWelcomeFireworks(Location location) {
		World world = location.getWorld();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		List<Location> locations = new ArrayList<>();
		locations.add(new Location(world, x + 2, y, z + 2));
		locations.add(new Location(world, x + 2, y, z - 2));
		locations.add(new Location(world, x - 2, y, z + 2));
		locations.add(new Location(world, x + 3, y, z));

		Firework fw = (Firework) world.spawnEntity(new Location(world, x - 2, y, z - 2), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		fwm.setPower(1);
		fwm.addEffect(FireworkEffect.builder().trail(true).withFade(Color.RED).withColor(Color.YELLOW).flicker(true).with(Type.STAR).build());

		fw.setFireworkMeta(fwm);
		fw.detonate();

		for (Location loc : locations) {
			Firework fw2 = (Firework) world.spawnEntity(loc, EntityType.FIREWORK);
			fw2.setFireworkMeta(fwm);
		}
	}
}
