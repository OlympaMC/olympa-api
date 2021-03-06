package fr.olympa.api.spigot.editor;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.region.Point2D;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.shapes.Cuboid;
import fr.olympa.api.spigot.region.shapes.Cylinder;
import fr.olympa.api.spigot.region.shapes.ExpandedCuboid;
import fr.olympa.api.spigot.region.shapes.Polygon;
import fr.olympa.api.utils.Prefix;

public class RegionEditor extends InventoryClear {

	private static ItemStack blockSelector = ItemUtils.item(Material.STICK, "§bSélectionner un bloc");
	private static ItemStack blockRemover = ItemUtils.item(Material.SHEARS, "§cRetirer le dernier bloc");
	private static ItemStack regionCuboid = ItemUtils.item(Material.STONE, "§aRégion cubique (cuboid)");
	private static ItemStack regionPolygon = ItemUtils.item(Material.COBBLESTONE_STAIRS, "§aRégion polygonale (polygon)");
	private static ItemStack regionCylinder = ItemUtils.item(Material.SLIME_BALL, "§aRégion cylindrique (cylinder)");
	private static ItemStack validate = ItemUtils.item(Material.DIAMOND, "§bValider la sélection");

	private RegionType regionType;
	private boolean expanded = false;

	private LinkedList<Location> locations = new LinkedList<>();

	private Consumer<Region> end;

	public RegionEditor(Player p, Consumer<Region> end) {
		super(p);
		this.end = end;
		this.regionType = RegionType.CUBOID;
	}
	
	public RegionEditor edit(Region region) {
		if (region instanceof Cuboid cuboid) {
			locations.add(cuboid.getMin());
			locations.add(cuboid.getMax());
			regionType = RegionType.CUBOID;
		}else if (region instanceof Polygon polygon) {
			locations.addAll(polygon.getLocations());
			regionType = RegionType.POLYGON;
		}else if (region instanceof Cylinder cylinder) {
			Location center = cylinder.getCenter();
			locations.add(center);
			Location edge = center.clone();
			edge.setX(edge.getX() + cylinder.getRadius());
			edge.setY(cylinder.getMaxY());
			locations.add(edge);
			regionType = RegionType.CYLINDER;
		}else throw new IllegalArgumentException("Impossible to edit region type " + region.getClass().getName());
		return this;
	}

	@Override
	protected void begin() {
		super.begin();
		Inventory inv = p.getInventory();
		inv.setItem(0, blockSelector);
		inv.setItem(1, blockRemover);
		inv.setItem(3, regionCuboid.clone());
		inv.setItem(4, regionPolygon.clone());
		inv.setItem(5, regionCylinder.clone());
		inv.setItem(6, ItemUtils.itemSwitch("Région étendue", expanded));
		inv.setItem(8, validate);
		setRegionType(regionType);
	}

	public void setRegionType(RegionType newRegion) {
		if (regionType != null) p.getInventory().getItem(regionType.slot).removeEnchantment(Enchantment.ARROW_DAMAGE); // retire l'effet glowing sur l'item de l'ancienne région
		regionType = newRegion;
		p.getInventory().getItem(regionType.slot).addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1); // ajoute l'effet glowing sur l'item de la nouvelle région
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getPlayer() != p) return;
		if (e.getHand() != EquipmentSlot.HAND) return;

		int slot = p.getInventory().getHeldItemSlot();
		RegionType regionTypeClicked = RegionType.fromSlot(slot);
		if (regionTypeClicked != null) {
			if (regionTypeClicked == regionType) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu sélectionnais déjà une zone de type " + regionTypeClicked.name() + ".");
			}else {
				setRegionType(regionTypeClicked);
				locations.clear();
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu sélectionne maintenant une zone de type " + regionTypeClicked.name() + ". Son nombre de blocs minimal est " + regionTypeClicked.minBlocks + ". Ton ancienne sélection a été effacée.");
			}
		}else if (slot == 0) {
			if (e.getClickedBlock() == null) return;
			Location loc = e.getClickedBlock().getLocation();
			Action action = e.getAction();
			switch (regionType) {
			case CUBOID:
				if (action == Action.LEFT_CLICK_BLOCK || locations.isEmpty()) {
					if (!locations.isEmpty()) locations.removeFirst();
					locations.addFirst(loc);
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as choisi le bloc 1/2 de la sélection. " + ensureValid());
				}else {
					if (ensureWorld(loc)) {
						if (locations.size() >= 2) locations.remove(1);
						locations.add(loc);
						Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as choisi le bloc 2/2 de la sélection.");
					}
				}
				break;
			case POLYGON:
				if (ensureWorld(loc)) {
					locations.add(loc);
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as ajouté 1 bloc à la sélection (" + locations.size() + " blocs). " + ensureValid());
				}
				break;
			case CYLINDER:
				if (action == Action.LEFT_CLICK_BLOCK || locations.isEmpty()) {
					if (!locations.isEmpty()) locations.removeFirst();
					locations.addFirst(loc);
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as choisi le centre de la sélection. " + ensureValid());
				}else {
					if (ensureWorld(loc)) {
						if (locations.size() >= 2) locations.remove(1);
						locations.add(loc);
						Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as choisi un point du cercle de la sélection. Rayon du cylindre : " + (int) locations.getFirst().distance(loc) + " blocs");
					}
				}
				break;
			}
		}else if (slot == 1) {
			if (locations.isEmpty()) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as sélectionné aucun bloc.");
			}else {
				locations.removeLast();
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as supprimé le dernier bloc de la sélection (reste " + locations.size() + " blocs). " + ensureValid());
			}
		}else if (slot == 6) {
			expanded = ItemUtils.toggle(e.getItem());
			Prefix.DEFAULT_GOOD.sendMessage(p, expanded ? "La région s'étend désormais verticalement." : "La région est limitée aux hauteurs des blocs choisis.");
		}else if (slot == 8) {
			leave(p);
			Region region = null;
			if (regionType.minBlocks <= locations.size()) {
				switch (regionType) {
				case CUBOID:
					Location first = locations.getFirst();
					Location last = locations.getLast();
					region = expanded ? new ExpandedCuboid(first.getWorld(), first.getBlockX(), first.getBlockZ(), last.getBlockX(), last.getBlockZ()) : new Cuboid(first, last);
					break;
				case POLYGON:
					MinMax y = getMinMax();
					region = new Polygon(locations.getFirst().getWorld(), locations.stream().map(Point2D::new).collect(Collectors.toList()), y.min, y.max);
					break;
				case CYLINDER:
					y = getMinMax();
					region = new Cylinder(locations.getFirst(), (int) locations.getFirst().distance(locations.getLast()), y.min, y.max);
					break;
				}
			}
			end.accept(region);
		}else return;
		e.setCancelled(true);
	}

	private MinMax getMinMax() {
		int minY = 256;
		int maxY = 0;
		if (!expanded) {
			for (Location loc : locations) {
				int y = loc.getBlockY();
				if (y < minY) minY = y;
				if (y > maxY) maxY = y;
			}
		}
		return new MinMax(minY, maxY);
	}

	private String ensureValid() {
		return "§eSélection valide : §l" + (locations.size() < regionType.minBlocks ? "§cnon" : "§aoui");
	}

	private boolean ensureWorld(Location location) {
		if (locations.isEmpty() || locations.getFirst().getWorld().equals(location.getWorld())) return true;
		Prefix.DEFAULT_BAD.sendMessage(p, "Le bloc que tu as sélectionné ne se trouve pas dans le même monde que les autres.");
		return false;
	}

	enum RegionType {
		CUBOID(3, 2), POLYGON(4, 3), CYLINDER(5, 2);

		public final int slot;
		public final int minBlocks;

		RegionType(int slot, int minBlocks) {
			this.slot = slot;
			this.minBlocks = minBlocks;
		}

		public static RegionType fromSlot(int slot) {
			for (RegionType region : values()) {
				if (region.slot == slot) return region;
			}
			return null;
		}
	}

	private record MinMax(int min, int max) {}

}
