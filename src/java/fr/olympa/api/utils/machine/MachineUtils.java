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

		TxtComponentBuilder textBuilder = new TxtComponentBuilder("&e&m-------------------");
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3Serveur &b%s", main.getServerName()));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3Status: %s", main.getStatus().getNameColored()));
		if (main.isSpigot()) {
			textBuilder.extra(" ");
			textBuilder.extra(new TxtComponentBuilder("&3Versions: &b%s&3.", OlympaCore.getInstance().getRangeVersion()));
		}
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3En ligne depuis &b%s&3.", main.getUptime()).onHoverText("&e%s.", Utils.timestampToDateAndHour(main.getUptimeLong())));
		textBuilder.extra("\n");
		if (main.isSpigot()) {
			double[] tps = TPS.getDoubleTPS();
			float average = TPS.getAverage(tps);
			textBuilder.extra(new TxtComponentBuilder("&3TPS: &b1m %s&b 5m %s&b 15m %s ", TPSUtils.getTpsColor(tps[0]), TPSUtils.getTpsColor(tps[1]), TPSUtils.getTpsColor(tps[2])));
			textBuilder.extra(new TxtComponentBuilder("&3Moyenne: &b%s&3.", TPSUtils.getTpsColor(average)).onHoverText("&eLes TPS (0 à 20) sont les ticks par secondes."));
			textBuilder.extra("\n");
		}
		textBuilder.extra(new TxtComponentBuilder("&3RAM: &b%s&3 (%s).", machine.getMemUsage().replace("%", "%%"), machine.getMemUse()).onHoverText("&eRAM utilisée/RAM maximum du serveur Minecraft."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3CPU: &b%s&3 (%d cores).", machine.getCPUUsage().replace("%", "%%"), machine.getCores()).onHoverText("&eUtilisation globale du processeur du serveur dédié."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3Threads: &b%d&3.", machine.getThreads()).onHoverText("&eNombre de 'sous-processus'."));
		textBuilder.extra("\n");

		if (main.isSpigot()) {
			try {
				textBuilder.extra(new TxtComponentBuilder("&3Modules: &b"));
				for (TxtComponentBuilder txt : Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(f -> f.getName().startsWith("Olympa"))
						.map(ff -> {
							String fileInfo = Utils.tsToShortDur(new File(ff.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).lastModified() / 1000L);
							return new TxtComponentBuilder("&6%s ", ff.getName().substring(6)).onHoverText("&eDernière MAJ %s", fileInfo).console(isConsole);
						})
						.collect(Collectors.toList()))
					textBuilder.extra(txt);
				textBuilder.extra("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			textBuilder.extra(new TxtComponentBuilder("&3Versions supportées: &b%s&3 ", ((OlympaCore) main).getRangeVersion()));
			IProtocolSupport protocolSupport = ((OlympaCore) main).getProtocolSupport();
			if (protocolSupport != null) {
				String unSupVer = protocolSupport.getVersionUnSupportedInRange();
				if (!unSupVer.isBlank())
					textBuilder.extra(new TxtComponentBuilder("&4[&c!&4]&3.").onHoverText("&4Versions non supportées: &c%s&4.", unSupVer));
			}
			textBuilder.extra(
					new TxtComponentBuilder("&3Bukkit API: &b%s&3.", Bukkit.getBukkitVersion().replace("-SNAPSHOT", "")).onHoverText("&eServeur sous &6%s&e.", TPS.isSpigot() ? isPaper() ? "PaperSpigot" : "Spigot" : "Bukkit"));
			textBuilder.extra(" ");
			for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
				textBuilder.extra("\n");
				Chunk[] chunks = world.getLoadedChunks();
				List<Entity> entities = world.getEntities();
				List<LivingEntity> livingEntities = world.getLivingEntities();

				textBuilder.extra(new TxtComponentBuilder("&3Monde &b%s&3: ", world.getName()));

				textBuilder.extra(new TxtComponentBuilder("&b%d&3 chunks", chunks.length).onHoverText("&eChunks (region de 16x16) chargés dans le monde"));
				textBuilder.extra(" ");
				Collection<Chunk> forceChunks = world.getForceLoadedChunks();
				if (!forceChunks.isEmpty()) {
					textBuilder.extra(new TxtComponentBuilder("(%d forcés)", forceChunks.size()).onHoverText("&eLes chunks forcés sont les chunks du spawn du monde &6ou &edes chunks victime de Chunk Loader."));
					textBuilder.extra(" ");
				}
				textBuilder.extra(new TxtComponentBuilder("&b%d/%d&3 entités", livingEntities.size(), entities.size()).onHoverText("&eEntités vivantes/Toutes Entités."));
			}
			textBuilder.extra(new TxtComponentBuilder("&3."));
		}
		return textBuilder.build();
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
