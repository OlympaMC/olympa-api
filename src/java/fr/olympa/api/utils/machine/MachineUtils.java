package fr.olympa.api.utils.machine;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class MachineUtils {

	private static Boolean isPapermc;

	@Deprecated(forRemoval = true)
	public static TextComponent getInfos() {
		return getInfos(false);
	}

	public static TextComponent getInfos(boolean isConsole) {
		MachineInfo machine = new MachineInfo();
		LinkSpigotBungee main = LinkSpigotBungee.Provider.link;
		TextComponent out = new TextComponent();

		TextComponent out2 = new TxtComponentBuilder("§e§m-------------------").build();
		out.addExtra(out2);
		out.addExtra("\n");
		out.addExtra(new TxtComponentBuilder("§3Serveur §b%s", main.getServerName()).build());
		out.addExtra(" ");
		out.addExtra(new TxtComponentBuilder("§3Status: %s", main.getStatus().getNameColored() + "§3.").build());
		if (main.isSpigot()) {
			out.addExtra(" ");
			out.addExtra(new TxtComponentBuilder("§3Versions: §v%s§3.", OlympaCore.getInstance().getRangeVersion()).build());
		}
		out.addExtra("\n");
		out2 = new TxtComponentBuilder("§3En ligne depuis §b%s§3.", main.getUptime()).onHoverText("§e%s.", Utils.timestampToDateAndHour(main.getUptimeLong())).build();
		out.addExtra(out2);
		out.addExtra("\n");
		if (main.isSpigot()) {
			double[] tps = TPS.getDoubleTPS();
			float average = TPS.getAverage(tps);
			out2 = new TxtComponentBuilder("§3TPS: §b1m %s§b 5m %s§b 15m %s", TPSUtils.getTpsColor(tps[0]), TPSUtils.getTpsColor(tps[1]), TPSUtils.getTpsColor(tps[2]))
					.extra(new TxtComponentBuilder(" §3Moyenne: §b%s§3.", TPSUtils.getTpsColor(average)).onHoverText("§eLes TPS (0 à 20) sont les ticks par secondes.")).build();
			out.addExtra(out2);
			out.addExtra("\n");
		}
		out2 = new TxtComponentBuilder("§3RAM: §b" + machine.getMemUsage() + "§3 (" + machine.getMemUse() + ").").onHoverText("§eRAM utilisée/RAM maximum du serveur Minecraft.").build();
		out.addExtra(out2);
		out.addExtra(" ");
		out2 = new TxtComponentBuilder("§3CPU: §b" + machine.getCPUUsage() + "§3 (" + machine.getCores() + " cores).").onHoverText("§eUtilisation globale du processeur du serveur dédié.").build();
		out.addExtra(out2);
		out.addExtra(" ");
		out2 = new TxtComponentBuilder("§3Threads: §b" + machine.getThreads() + "§3.").onHoverText("§eNombre de 'sous-processus'.").build();
		out.addExtra(out2);
		out.addExtra("\n");

		if (main.isSpigot()) {
			try {
				out.addExtra(new TxtComponentBuilder("§3Modules: §b").build());
				for (TextComponent txt : Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(f -> f.getName().startsWith("Olympa"))
						.map(ff -> {
							String fileInfo = Utils.tsToShortDur(new File(ff.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).lastModified() / 1000L);
							return new TxtComponentBuilder("§6%s ", ff.getName().substring(6)).onHoverText("§eDernière MAJ %s", fileInfo).console(isConsole).build();
						})
						.collect(Collectors.toList()))
					out.addExtra(txt);
				out.addExtra("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			out2 = new TxtComponentBuilder("§3Versions supportées: §b%s§3.", ((OlympaCore) main).getRangeVersion()).build();
			IProtocolSupport protocolSupport = ((OlympaCore) main).getProtocolSupport();
			if (protocolSupport != null) {
				String unSupVer = protocolSupport.getVersionUnSupportedInRange();
				if (!unSupVer.isBlank())
					out2.addExtra(new TxtComponentBuilder("§4[§c!§4]§3.").onHoverText("§4Versions non supportées: §c%s§4.", unSupVer).build());
				out.addExtra(out2);
			}
			out2 = new TxtComponentBuilder("§3Bukkit API: §b%s§3.", Bukkit.getBukkitVersion().replace("-SNAPSHOT", "")).onHoverText("§eServeur sous §6%s§e.", TPS.isSpigot() ? isPaper() ? "PaperSpigot" : "Spigot" : "Bukkit" + "§e.").build();
			out.addExtra(" ");
			out.addExtra(out2);
			for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
				out.addExtra("\n");
				Chunk[] chunks = world.getLoadedChunks();
				List<Entity> entities = world.getEntities();
				List<LivingEntity> livingEntities = world.getLivingEntities();

				out2 = new TxtComponentBuilder("§3Monde §b" + world.getName() + "§3: ").build();

				TextComponent out3 = new TxtComponentBuilder("§b" + chunks.length + "§3 chunks").build();
				out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eChunks (region de 16x16) chargés dans le monde")));
				out2.addExtra(out3);
				out2.addExtra(" ");
				Collection<Chunk> forceChunks = world.getForceLoadedChunks();
				if (!forceChunks.isEmpty()) {
					out3 = new TxtComponentBuilder("(" + forceChunks.size() + " forcés)").build();
					out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eLes chunks forcés sont les chunks du spawn du monde §6ou §edes chunks victime de Chunk Loader.")));
					out2.addExtra(out3);
					out2.addExtra(" ");
				}
				out3 = new TxtComponentBuilder("§b" + livingEntities.size() + "/" + entities.size() + "§3 entités").build();
				out3.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText("§eEntités vivantes/Toutes Entités.")));
				out2.addExtra(out3);
				out.addExtra(out2);
			}
			out.addExtra(new TxtComponentBuilder("§3.").build());
		}
		return out;
	}

	private static boolean isPaper() {
		if (isPapermc == null)
			try {
				isPapermc = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
			} catch (ClassNotFoundException e) {
				isPapermc = false;
			}
		return isPapermc;
	}
}
