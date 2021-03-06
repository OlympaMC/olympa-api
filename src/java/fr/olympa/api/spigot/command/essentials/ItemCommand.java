package fr.olympa.api.spigot.command.essentials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.item.ItemUtils;

public class ItemCommand extends ComplexCommand {
	
	public ItemCommand(Plugin plugin) {
		super(plugin, "item", "Modifier un item.", OlympaAPIPermissionsSpigot.ITEM_COMMAND);
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
	
	@Cmd (description = "Modifier les flags", args = "FLAGS", syntax = "<flag>", min = 1)
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
