package fr.olympa.api.command.essentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.Prefix;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;

public class InvseeCommand extends OlympaCommand {

	public InvseeCommand(Plugin plugin) {
		super(plugin, "invsee", OlympaAPIPermissions.INVSEE_COMMAND);
		addArgs(true, "JOUEUR");
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		Inventory inventory = null;
		if (target == null) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			if (offlinePlayer == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
			inventory = getOfflinePlayerInventory(target);
		} else
			inventory = target.getInventory();
		player.openInventory(inventory);
		Prefix.DEFAULT_GOOD.sendMessage(player, "&aOuverture de l'inventaire de &2%s&a.", target.getName());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	public Inventory getOfflinePlayerInventory(OfflinePlayer target) {
		Inventory inv = null;
		Server server = Bukkit.getServer();
		File inventoryFile = new File(server.getWorldContainer() + server.getWorlds().get(0).getName() + File.separator + "playerdata", target.getUniqueId().toString() + ".dat");
		try {
			NBTTagCompound nbt = NBTCompressedStreamTools.a(new FileInputStream(inventoryFile));
			NBTTagList inventory = (NBTTagList) nbt.get("Inventory");
			inv = new CraftInventoryCustom(null, inventory.size());
			for (int i = 0; i < inventory.size() - 1; i++) {
				NBTTagCompound compound = (NBTTagCompound) inventory.get(i);
				if (!compound.isEmpty()) {
					ItemStack stack = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_15_R1.ItemStack.a(compound));
					inv.setItem(i, stack);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inv;
	}
}
