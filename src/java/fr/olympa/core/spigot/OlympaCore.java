package fr.olympa.core.spigot;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;

import fr.olympa.api.common.mongo.MongoManager;
import fr.olympa.api.common.mongo.MongoServerInfo;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.translation.AbstractTranslationManager;
import fr.olympa.api.common.translation.TranslationManager;
import fr.olympa.api.spigot.config.CustomConfig;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import com.google.gson.Gson;

import fr.olympa.api.common.bpmc.SpigotBPMCEvent;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaSpigot;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.redis.ResourcePackHandler;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.spigot.command.CommandListener;
import fr.olympa.api.spigot.frame.ImageFrameManager;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.holograms.HologramsManager;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.core.spigot.datamanagement.DataManagmentListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Version minimale du Core, faite pour fonctionner sans lien à la BDD sur des
 * serveurs tests
 */
public class OlympaCore extends OlympaSpigot {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	private HologramsManager holograms;
	private RegionManager regions;
	private AbstractTranslationManager translations;

	//Temporary DB access, will be removed when released
	private MongoManager mongo;

	@Override
	public RegionManager getRegionManager() {
		return regions;
	}

	@Override
	public HologramsManager getHologramsManager() {
		return holograms;
	}

	@Override
	public AbstractTranslationManager getTranslationManager() {
		return translations;
	}

	@Override
	public void onDisable() {
		holograms.unload();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		instance = this;

		//Temporary load of config.yml & MongoClient instantiation, will be removed when merged to master
		CustomConfig dbCfg = new CustomConfig(this, "config");
		dbCfg.load();

		mongo = new MongoManager(new MongoServerInfo(
				dbCfg.getString("mongo.host"),
				dbCfg.getInt("mongo.port"),
				dbCfg.getString("mongo.user"),
				dbCfg.getString("mongo.password")
		));

		translations = new TranslationManager(this);
		translations.loadTranslations();

		System.out.println(translations.translate("FRENCH", "olympacore.general.test", Gender.UNSPECIFIED));

		OlympaPermission.registerPermissions(OlympaAPIPermissionsSpigot.class);

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(regions = new RegionManager(), this);

		try {
			pluginManager.registerEvents(holograms = new HologramsManager(this, new File(getDataFolder(), "holograms.yml")), this);
		} catch (Exception e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}
		Messenger messenger = getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		new SpigotBPMCEvent().register(this);

		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") is enabled.");
	}

	@Override
	public ImageFrameManager getImageFrameManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return null;
	}

	@Override
	public MongoManager getMongo() {
		return mongo; //Shall return null in final release
	}

	@Override
	public void retreiveMonitorInfos(BiConsumer<List<ServerInfoAdvanced>, Boolean> callback, boolean freshDoubleCallBack) {}

	@Override
	public void registerPackListener(ResourcePackHandler packHandler) {}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {}

	@Override
	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {}

	@Override
	public RedisConnection getRedisAccess() {
		return null;
	}

	@Override
	public void launchAsync(Runnable run) {}

	@Override
	public boolean isSpigot() {
		return true;
	}

	@Override
	public Gson getGson() {
		return null;
	}

	@Override
	public List<String> getPlayersNames() {
		return null;
	}

	@Override
	public boolean isRedisConnected() {
		return false;
	}

	@Override
	public boolean isDatabaseConnected() {
		return false;
	}

}
