package fr.olympa.api.spigot.region.tracking.flags;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerBlockInteractFlag extends AbstractProtectionFlag {

	public static List<Material> inventoryBlocks = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.CRAFTING_TABLE, Material.FURNACE, Material.BREWING_STAND, Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.LOOM, Material.SMITHING_TABLE, Material.DISPENSER, Material.DROPPER, Material.ENCHANTING_TABLE, Material.ENDER_CHEST, Material.ANVIL, Material.BEACON, Material.HOPPER, Material.SHULKER_BOX, Material.BARREL, Material.BLAST_FURNACE, Material.SMOKER, Material.STONECUTTER);

	private boolean handleBlocksInventory;
	private boolean blockInventoryProtected;

	public PlayerBlockInteractFlag(boolean protectedByDefault) {
		this(protectedByDefault, false, false);
	}

	public PlayerBlockInteractFlag(boolean protectedByDefault, boolean handleBlocksInventory, boolean blockInventoryProtected) {
		super(protectedByDefault);
		this.handleBlocksInventory = handleBlocksInventory;
		this.blockInventoryProtected = blockInventoryProtected;
	}

	public void interactEvent(PlayerInteractEvent event) {
		if (handleBlocksInventory && inventoryBlocks.contains(event.getClickedBlock().getType())) {
			handleInventoryBlock(event);
		}else handleOtherBlock(event);
	}
	
	protected void handleInventoryBlock(PlayerInteractEvent event) {
		handleCancellable(event, event.getPlayer(), blockInventoryProtected);
	}
	
	protected void handleOtherBlock(PlayerInteractEvent event) {
		handleCancellable(event, event.getPlayer());
	}
	
	@Override
	public void appendDescription(StringJoiner joiner) {
		super.appendDescription(joiner);
		if (handleBlocksInventory) joiner.add("Inventory blocks protected: " + blockInventoryProtected);
	}

}
