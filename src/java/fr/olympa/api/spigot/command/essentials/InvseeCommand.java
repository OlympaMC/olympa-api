package fr.olympa.api.spigot.command.essentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;

public class InvseeCommand extends OlympaCommand {

	public InvseeCommand(Plugin plugin) {
		super(plugin, "invsee", "Pour voir l'inventaire d'un joueur.", OlympaAPIPermissionsSpigot.INVSEE_COMMAND);
		addArgs(true, "JOUEUR");
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String targetName;
		Player target = Bukkit.getPlayer(args[0]);
		Inventory inventory = null;
		if (target == null) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			inventory = getOfflinePlayerInventory(offlinePlayer);
			if (inventory == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
			targetName = offlinePlayer.getName();
		} else {
			targetName = target.getName();
			inventory = target.getInventory();
		}
		player.openInventory(inventory);
		Prefix.DEFAULT_GOOD.sendMessage(player, "&aOuverture de l'inventaire de &2%s&a.", targetName);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Nullable
	public Inventory getOfflinePlayerInventory(OfflinePlayer target) {
		Inventory inv = null;
		Server server = Bukkit.getServer();
		File inventoryFile = new File(server.getWorldContainer() + server.getWorlds().get(0).getName() + File.separator + "playerdata", target.getUniqueId().toString() + ".dat");
		if (!inventoryFile.exists())
			return null;
		try {
			NBTTagCompound nbt = NBTCompressedStreamTools.a(new FileInputStream(inventoryFile));
			NBTTagList inventory = (NBTTagList) nbt.get("Inventory");
			inv = new CraftInventoryCustom(null, inventory.size());
			for (int i = 0; i < inventory.size() - 1; i++) {
				NBTTagCompound compound = (NBTTagCompound) inventory.get(i);
				if (!compound.isEmpty()) {
					ItemStack stack = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R3.ItemStack.a(compound));
					inv.setItem(i, stack);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inv;
	}
}
