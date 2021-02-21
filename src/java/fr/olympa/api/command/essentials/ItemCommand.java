package fr.olympa.api.command.essentials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.permission.OlympaAPIPermissions;

public class ItemCommand extends ComplexCommand {
	
	public ItemCommand(Plugin plugin) {
		super(plugin, "item", "Modifier un item", OlympaAPIPermissions.ITEM_COMMAND);
		setAllowConsole(false);
		addArgumentParser("FLAGS", ItemFlag.class);
	}
	
	@Cmd (description = "Renommer un item", syntax = "<nouveau nom>", min = 1)
	public void rename(CommandContext cmd) {
		ItemStack item = getItemInHand();
		if (item == null) return;
		ItemUtils.name(item, cmd.getFrom(0));
		sendSuccess("L'item a été renommé.");
	}
	
	@Cmd (description = "Mettre un item en incassable")
	public void unbreakable(CommandContext cmd) {
		ItemStack item = getItemInHand();
		if (item == null) return;
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(!meta.isUnbreakable());
		item.setItemMeta(meta);
		sendSuccess("L'item est désormais %s§a.", meta.isUnbreakable() ? "§eincassable" : "§ccassable");
	}
	
	@Cmd (description = "Modifier les flags", args = "FLAGS", syntax = "<flag>")
	public void flag(CommandContext cmd) {
		ItemStack item = getItemInHand();
		if (item == null) return;
		ItemMeta meta = item.getItemMeta();
		ItemFlag flag = cmd.getArgument(0);
		if (meta.hasItemFlag(flag)) {
			meta.removeItemFlags(flag);
			sendError("L'item n'a plus le flag %s.", flag.name());
		}else {
			meta.addItemFlags(flag);
			sendSuccess("L'item a maintenant le flag %s.", flag.name());
		}
		item.setItemMeta(meta);
	}
	
	private ItemStack getItemInHand() {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			sendError("Tu dois tenir un item dans ta main !");
			return null;
		}
		return item;
	}
	
}
