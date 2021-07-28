package fr.olympa.api.spigot.feedback;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.spigot.utils.SpigotUtils;

public class FeedbackManager {
	
	public static final SQLColumn<FeedbackEntry> COLUMN_ID = new SQLColumn<FeedbackEntry>("id", "int(11) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(FeedbackEntry::getID);
	public static final SQLColumn<FeedbackEntry> COLUMN_DATE = new SQLColumn<FeedbackEntry>("date", "BIGINT NOT NULL", Types.BIGINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_OWNER = new SQLColumn<FeedbackEntry>("owner", "BIGINT NOT NULL", Types.BIGINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_TYPE = new SQLColumn<FeedbackEntry>("type", "TINYINT(3) NOT NULL", Types.TINYINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_DESCRIPTION = new SQLColumn<FeedbackEntry>("description", "VARCHAR(65532) NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_SERVER = new SQLColumn<FeedbackEntry>("server", "VARCHAR(255) NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_SERVERINFO = new SQLColumn<FeedbackEntry>("server_info", "TEXT NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_POSITION = new SQLColumn<FeedbackEntry>("position", "VARCHAR(255) NOT NULL", Types.VARCHAR);
	
	private SQLTable<FeedbackEntry> table;
	
	public FeedbackManager(Plugin plugin) throws SQLException {
		table = new SQLTable<>("feedback", Arrays.asList(COLUMN_ID, COLUMN_DATE, COLUMN_OWNER, COLUMN_TYPE, COLUMN_DESCRIPTION, COLUMN_SERVER, COLUMN_SERVERINFO, COLUMN_POSITION), resultSet -> {
			FeedbackEntry entry = new FeedbackEntry();
			entry.id = resultSet.getInt("id");
			entry.date = resultSet.getLong("date");
			entry.owner = AccountProviderAPI.getter().getPlayerInformations(resultSet.getLong("owner"));
			entry.type = FeedbackType.fromId(resultSet.getInt("type"));
			entry.description = resultSet.getString("description");
			entry.server = OlympaServer.valueOf(resultSet.getString("server"));
			entry.serverInfo = LinkSpigotBungee.getInstance().getGson().fromJson(resultSet.getString("server_info"), ServerInfoAdvanced.class);
			String position = resultSet.getString("position");
			entry.position = SpigotUtils.convertStringToLocation(position);
			entry.worldName = position.split(" ")[0];
			return entry;
		}).createOrAlter();
		
		new FeedbackCommand(plugin).register();
	}
	
}
