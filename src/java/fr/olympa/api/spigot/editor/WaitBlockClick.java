package fr.olympa.api.spigot.editor;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WaitBlockClick extends InventoryClear{
	
	private Consumer<Block> run;
	private Predicate<Block> blockPredicate;
	private ItemStack item;
	
	public WaitBlockClick(Player p, Consumer<Block> end, ItemStack is) {
		this(p, end, is, null);
	}

	public WaitBlockClick(Player p, Consumer<Block> end, ItemStack is, Predicate<Block> block) {
		super(p);
		this.run = end;
		this.blockPredicate = block;
		this.item = is;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getClickedBlock() == null) return;
		if (e.getItem() == null) return;
		if (!e.getItem().equals(item)) return;
		if (blockPredicate != null && !blockPredicate.test(e.getClickedBlock())) return;
		e.setCancelled(true);
		leave(e.getPlayer());
		run.accept(e.getClickedBlock());
	}

	@Override
	protected void begin() {
		super.begin();
		p.getInventory().setItem(4, item);
	}
	
}
