package fr.olympa.api.utils.machine;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.ProtocolAPI;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class MachineUtils {

	public static TextComponent getInfos() {
		MachineInfo machine = new MachineInfo();
		LinkSpigotBungee main = LinkSpigotBungee.Provider.link;
		TextComponent out = new TextComponent();

		TextComponent out2 = new TextComponent(TextComponent.fromLegacyText("§e§m--------------------"));
		out.addExtra(out2);
		out.addExtra("\n");
		out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3Serveur §b" + main.getServerName())));
		out.addExtra(" ");
		out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3Status: " + main.getStatus().getNameColored() + "§3.")));
		out.addExtra("\n");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3En ligne depuis §b" + main.getUptime() + "§3."));
		out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§e" + Utils.timestampToDateAndHour(main.getUptimeLong()) + ".")));
		out.addExtra(out2);
		out.addExtra("\n");
		if (main.isSpigot()) {
			double[] tps = TPS.getDoubleTPS();
			float average = TPS.getAverage(tps);
			out2 = new TextComponent(TextComponent.fromLegacyText("§3TPS: §b1m " + TPSUtils.getTpsColor(tps[0]) + "§b 5m " + TPSUtils.getTpsColor(tps[1]) + "§b 15m " + TPSUtils.getTpsColor(tps[2])));
			out2.addExtra(new TextComponent(TextComponent.fromLegacyText("§3Moyenne: §b" + TPSUtils.getTpsColor(average) + "§3.")));
			out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eLes TPS (0 à 20) sont les ticks par secondes.")));
			out.addExtra(out2);
			out.addExtra("\n");
		}
		out2 = new TextComponent(TextComponent.fromLegacyText("§3RAM: §b" + machine.getMemUsage() + "§3 (" + machine.getMemUse() + ")."));
		out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eRAM utilisée/RAM maximum du serveur Minecraft.")));
		out.addExtra(out2);
		out.addExtra(" ");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3CPU: §b" + machine.getCPUUsage() + "§3 (" + machine.getCores() + " cores)."));
		out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eUtilisation globale du processeur du serveur dédié.")));
		out.addExtra(out2);
		out.addExtra(" ");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Threads: §b" + machine.getThreads() + "§3."));
		out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eNombre de processus.")));
		out.addExtra(out2);
		out.addExtra("\n");

		if (main.isSpigot()) {
			IProtocolSupport protocolSupport = ((OlympaCore) main).getProtocolSupport();
			if (protocolSupport != null) {
				out2 = new TextComponent(TextComponent.fromLegacyText("§3Versions supportés: §b" + protocolSupport.getRangeVersion() + "§3."));
				String unSupVer = protocolSupport.getVersionUnSupportedInRange();
				if (!unSupVer.isBlank()) {
					out2.addExtra(new TextComponent(TextComponent.fromLegacyText("§4[§c!§4]§3.")));
					out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§4Versions non supportées: §c" + unSupVer + "§4.")));
				}
				out.addExtra(out2);
			} else {
				String versionsString = "erreur";
				try {
					versionsString = ProtocolAPI.getVersionSupportedToString();
				} catch (Exception e) {
					versionsString = "erreur : " + e.getMessage();
				}
				out2 = new TextComponent(TextComponent.fromLegacyText("§3Versions supportés: §b" + versionsString + "§3."));
			}
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Bukkit API: §b" + Bukkit.getBukkitVersion().replace("-SNAPSHOT", "") + "§3."));
			out2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eServeur sous §6" + (TPS.isSpigot() ? TPS.isPaper() ? "Paper" : "Spigot" : "Bukkit" + "§e."))));
			out.addExtra(out2);
			out.addExtra("\n");
			for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
				Chunk[] chunks = world.getLoadedChunks();
				List<Entity> entities = world.getEntities();
				List<LivingEntity> livingEntities = world.getLivingEntities();

				out2 = new TextComponent(TextComponent.fromLegacyText("§3Monde §b" + world.getName() + "§3:" + "§3."));

				TextComponent out3 = new TextComponent(TextComponent.fromLegacyText("§b" + chunks.length + "§3 chunks "));
				out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eChunks (region de 16x16) chargés dans le monde")));
				out2.addExtra(out3);
				out2.addExtra("");
				Collection<Chunk> forceChunks = world.getForceLoadedChunks();
				if (!forceChunks.isEmpty()) {
					out3 = new TextComponent(TextComponent.fromLegacyText("(" + forceChunks.size() + " forcés) "));
					out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eLes chunks forcés sont les chunks du spawn du monde §6ou §edes chunks victime de Chunk Loader.")));
					out2.addExtra(out3);
					out2.addExtra("");
				}
				out3 = new TextComponent(TextComponent.fromLegacyText("§b" + livingEntities.size() + "/" + entities.size() + "§3 entités"));
				out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eEntités vivantes/Toutes Entités.")));
				out2.addExtra(out3);
			}
			out.addExtra(out2);
			out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3.")));
		}
		return out;
	}
}
