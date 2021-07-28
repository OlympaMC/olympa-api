package fr.olympa.api.common.machine;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedSpigot;
import fr.olympa.api.spigot.utils.SpigotInfoFork;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.api.spigot.version.VersionHandler;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class TpsMessageProvider {
	
	private OlympaPlayer olympaPlayer;
	private ServerInfoAdvanced serverInfo;
	private LinkSpigotBungee<?> main;

	/**
	 * Constructs a new {@link TpsMessageProvider} for this server.
	 * @param olympaPlayer
	 */
	public TpsMessageProvider(OlympaPlayer olympaPlayer) {
		this.olympaPlayer = olympaPlayer;
		main = LinkSpigotBungee.getInstance();
		serverInfo = main.isSpigot() ? new ServerInfoAdvancedSpigot((OlympaCore) main) : new ServerInfoAdvanced(main);
	}
	
	/**
	 * Constructs a new {@link TpsMessageProvider} for another server.
	 * @param serverInfo
	 */
	public TpsMessageProvider(ServerInfoAdvanced serverInfo) {
		this.serverInfo = serverInfo;
	}
	
	public TxtComponentBuilder getInfoMessage() {
		TxtComponentBuilder textBuilder = new TxtComponentBuilder("&e&m-------------------");
		if (main != null) {
			textBuilder.extra("\n");
			textBuilder.extra(new TxtComponentBuilder("&3Serveur &b%s", main.getServerName()));
			textBuilder.extra(" ");
			textBuilder.extra(new TxtComponentBuilder("&3Statut: %s", main.getStatus().getNameColored()));
			textBuilder.extra("\n");
			textBuilder.extra(new TxtComponentBuilder("&3En ligne depuis &b%s&3.", main.getUptime()).onHoverText("&e%s.", Utils.timestampToDateAndHour(main.getUptimeLong())));
		}
		if (serverInfo.getTpsArray() != null)
			textBuilder.extra(getSpigotTPSInfo());
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3RAM: &b%s&3 (%s)", serverInfo.getMemUsage(), serverInfo.getMemUse()).onHoverText("&eRAM utilisée/RAM maximum du serveur Minecraft."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3Threads: &b%d&3.", serverInfo.getThreads()).onHoverText("&eNombre de 'sous-processus'."));
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3CPU: &b%s&3", serverInfo.getCPUUsage()).onHoverText("&eUtilisation du processeur par le serveur."));
		textBuilder.extra(" ");
		textBuilder.extra(new TxtComponentBuilder("&3CPU Système: &b%s&3 (%d cores).", serverInfo.getCPUSysUsage(), serverInfo.getCPUSysCore()).onHoverText("&eUtilisation globale du processeur."));
		boolean showValueOfVersions = olympaPlayer == null || OlympaAPIPermissionsGlobal.PLUGINS_SEE_VALUE_VERSION.hasPermission(olympaPlayer);
		textBuilder.extra("\n");
		textBuilder.extra(new TxtComponentBuilder("&3Plugins maison: &b"));
		textBuilder.extra(ServerInfoAdvanced.getPluginsToString(ServerInfoAdvanced.getAllHomeMadePlugins(), olympaPlayer == null, showValueOfVersions));
		textBuilder.extra("\n");
		TxtComponentBuilder textBuilder2 = new TxtComponentBuilder("&3Versions autorisées: &b%s&3 ", serverInfo.getRangeVersionMinecraft());
		if (main != null) {
			VersionHandler<?> versionHandler = main.getVersionHandler();
			if (versionHandler != null) {
				String unSupVer = versionHandler.getVersionsDisabled();
				if (!unSupVer.isBlank() && !unSupVer.equals("unknown"))
					textBuilder2.onHoverText("&e[&6!&e] &cVersions désactivées &4%s&c.", unSupVer);
			}
		}
		textBuilder.extra(textBuilder2);
		if (main != null && main.isSpigot())
			textBuilder.extra(getCurrentSpigotInfo());
		return textBuilder;
	}
	
	private TxtComponentBuilder getSpigotTPSInfo() {
		TxtComponentBuilder textBuilder = new TxtComponentBuilder("\n");
		double[] tps = TPS.getDoubleTPS();
		float average = TPSUtils.getAverage(tps);
		textBuilder.extra(new TxtComponentBuilder("&3TPS: &b1m %s&b 5m %s&b 15m %s ", TPSUtils.getTpsColor(tps[0]), TPSUtils.getTpsColor(tps[1]), TPSUtils.getTpsColor(tps[2])));
		textBuilder.extra(new TxtComponentBuilder("&3Moyenne: &b%s&3.", TPSUtils.getTpsColor(average)).onHoverText("&eLes TPS (0 à environ 20) sont les ticks par seconde."));
		return textBuilder;
	}
	
	private TxtComponentBuilder getCurrentSpigotInfo() {
		OlympaCore core = (OlympaCore) main;
		TxtComponentBuilder textBuilder = new TxtComponentBuilder();
		textBuilder.extra(new TxtComponentBuilder("&3Bukkit API: &b%s&3.", Bukkit.getBukkitVersion().replace("-SNAPSHOT", "")).onHoverText("&eServeur sous &6%s&e.", SpigotInfoFork.getVersionBukkit()));
		textBuilder.extra(" ");
		for (World world : core.getServer().getWorlds()) {
			textBuilder.extra("\n");
			Chunk[] chunks = world.getLoadedChunks();
			List<Entity> entities = world.getEntities();
			List<LivingEntity> livingEntities = world.getLivingEntities();
			
			textBuilder.extra("&3Monde &b%s&3: ", world.getName());
			textBuilder.extra(new TxtComponentBuilder("&b%d&3 chunks", chunks.length).onHoverText("&eChunks (régions de 16x16) chargés dans le monde"));
			textBuilder.extra(" ");
			Collection<Chunk> forceChunks = world.getForceLoadedChunks();
			if (!forceChunks.isEmpty()) {
				textBuilder.extra(new TxtComponentBuilder("(%d forcés)", forceChunks.size()).onHoverText("&eLes chunks forcés sont les chunks du spawn du monde &6ou &edes chunks victime de Chunk Loader."));
				textBuilder.extra(" ");
			}
			textBuilder.extra(new TxtComponentBuilder("&b%d&3 entités et &b%d&3 non vivantes", livingEntities.size(), entities.size() - livingEntities.size()).onHoverText("&eEntités chargées dans le monde."));
		}
		textBuilder.extra(new TxtComponentBuilder("&3."));
		return textBuilder;
	}
	
}