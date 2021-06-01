package fr.olympa.api.machine;

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
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.match.MatcherPattern;
import fr.olympa.api.utils.SpigotInfo;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.api.utils.spigot.TPSUtils;
import fr.olympa.core.spigot.OlympaCore;

public class MachineMessage extends MachineInfo {

	public boolean isConsole;
	public LinkSpigotBungee main;

	public MachineMessage(boolean isConsole) {
		this.isConsole = isConsole;
		main = LinkSpigotBungee.Provider.link;
	}

	public TxtComponentBuilder getInfoMessage() {
		TxtComponentBuilder textBuilder = new TxtComponentBuilder("&e&m-------------------");
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3Serveur &b%s", main.getServerName()));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3Statut: %s", main.getStatus().getNameColored()));
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3En ligne depuis &b%s&3.", main.getUptime()).onHoverText("&e%s.", Utils.timestampToDateAndHour(main.getUptimeLong())));
		if (main.isSpigot())
			textBuilder.extra(getSpigotTPSInfo());
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3RAM: &b%s&3 (%s)", getMemUsage(), getMemUse()).onHoverText("&eRAM utilisée/RAM maximum du serveur Minecraft."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3Threads: &b%d&3.", getThreads()).onHoverText("&eNombre de 'sous-processus'."));
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3CPU: &b%s&3", getCPUUsage()).onHoverText("&eUtilisation du processeur par le serveur."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3CPU Système: &b%s&3 (%d cores).", getCPUSysUsage(), getCPUSysCore()).onHoverText("&eUtilisation globale du processeur."));
		if (main.isSpigot())
			textBuilder.extra(getOtherSpigotInfo());
		return textBuilder;
	}

	private TxtComponentBuilder getSpigotTPSInfo() {
		if (!main.isSpigot())
			throw new UnsupportedOperationException("Unable to get Spigot Info on not Spigot Environment");
		TxtComponentBuilder textBuilder = new TxtComponentBuilder("\n");
		double[] tps = TPS.getDoubleTPS();
		float average = TPS.getAverage(tps);
		textBuilder.extra(new TxtComponentBuilder("&3TPS: &b1m %s&b 5m %s&b 15m %s ", TPSUtils.getTpsColor(tps[0]), TPSUtils.getTpsColor(tps[1]), TPSUtils.getTpsColor(tps[2])));
		textBuilder.extra(new TxtComponentBuilder("&3Moyenne: &b%s&3.", TPSUtils.getTpsColor(average)).onHoverText("&eLes TPS (0 à environ 20) sont les ticks par secondes."));
		return textBuilder;
	}

	private TxtComponentBuilder getOtherSpigotInfo() {
		if (!main.isSpigot())
			throw new UnsupportedOperationException("Unable to get Spigot Info on not Spigot Environment");
		TxtComponentBuilder textBuilder = new TxtComponentBuilder("\n");
		try {
			textBuilder.extra(new TxtComponentBuilder("&3Plugins Olympa: &b"));
			for (TxtComponentBuilder txt : Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(f -> f.getName().startsWith("Olympa"))
					.map(ff -> {
						@NotNull
						PluginDescriptionFile desc = ff.getDescription();
						String fileInfo = Utils.tsToShortDur(new File(ff.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).lastModified() / 1000L);
						String website = desc.getWebsite();
						String gitCommitCompareLastest = null;
						MatcherPattern<?> regexHexGitCommit = MatcherPattern.of("\\b[a-fA-F0-9]{8,40}\\b");
						if (website != null && website.contains("git") && regexHexGitCommit.contains(desc.getVersion()))
							gitCommitCompareLastest = (desc.getWebsite().endsWith("/") ? desc.getWebsite() : desc.getWebsite() + "/") + "-/compare/" + regexHexGitCommit.extract(desc.getVersion()) + "...master";
						return new TxtComponentBuilder("&6%s ", desc.getName().substring(6)).onHoverText("&eDernière MAJ %s (%s)", fileInfo, desc.getVersion())
								.onClickUrl(gitCommitCompareLastest).console(isConsole);
					})
					.collect(Collectors.toList()))
				textBuilder.extra(txt);
			textBuilder.extra("\n");
		} catch (Exception e) {
			e.printStackTrace();
			textBuilder.extra("&cErreur\n");
		}
		textBuilder.extra("&3Versions supportées: &b%s&3 ", ((OlympaCore) main).getRangeVersion());
		IProtocolSupport protocolSupport = ((OlympaCore) main).getProtocolSupport();
		if (protocolSupport != null) {
			String unSupVer = protocolSupport.getVersionUnSupportedInRange();
			if (!unSupVer.isBlank())
				textBuilder.extra(new TxtComponentBuilder("&4[&c!&4]&3.").onHoverText("&4Versions non supportées: &c%s&4.", unSupVer));
		}
		textBuilder.extra(new TxtComponentBuilder("&3Bukkit API: &b%s&3.", Bukkit.getBukkitVersion().replace("-SNAPSHOT", ""))
				.onHoverText("&eServeur sous &6%s&e.", SpigotInfo.getVersionBukkit()));
		textBuilder.extra(" ");
		for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
			textBuilder.extra("\n");
			Chunk[] chunks = world.getLoadedChunks();
			List<Entity> entities = world.getEntities();
			List<LivingEntity> livingEntities = world.getLivingEntities();

			textBuilder.extra("&3Monde &b%s&3: ", world.getName());
			textBuilder.extra(new TxtComponentBuilder("&b%d&3 chunks", chunks.length).onHoverText("&eChunks (region de 16x16) chargés dans le monde"));
			textBuilder.extra(" ");
			Collection<Chunk> forceChunks = world.getForceLoadedChunks();
			if (!forceChunks.isEmpty()) {
				textBuilder.extra(new TxtComponentBuilder("(%d forcés)", forceChunks.size()).onHoverText("&eLes chunks forcés sont les chunks du spawn du monde &6ou &edes chunks victime de Chunk Loader."));
				textBuilder.extra(" ");
			}
			textBuilder.extra(new TxtComponentBuilder("&b%d&3 entités et &b%d&3 non vivantes", livingEntities.size(), entities.size() - livingEntities.size()).onHoverText("&eEntités chargés dans le monde."));
		}
		textBuilder.extra(new TxtComponentBuilder("&3."));
		return textBuilder;
	}
}
