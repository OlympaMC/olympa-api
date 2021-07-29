package fr.olympa.api.spigot.feedback;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;

public class FeedbackManager {
	
	public static final SQLColumn<FeedbackEntry> COLUMN_ID = new SQLColumn<FeedbackEntry>("id", "int(11) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(FeedbackEntry::getID);
	public static final SQLColumn<FeedbackEntry> COLUMN_DATE = new SQLColumn<FeedbackEntry>("date", "BIGINT NOT NULL", Types.BIGINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_OWNER = new SQLColumn<FeedbackEntry>("owner", "BIGINT NOT NULL", Types.BIGINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_TYPE = new SQLColumn<FeedbackEntry>("type", "TINYINT(4) NOT NULL", Types.TINYINT);
	public static final SQLColumn<FeedbackEntry> COLUMN_STATUS = new SQLColumn<FeedbackEntry>("status", "TINYINT(3) NULL", Types.TINYINT).setNotDefault();
	public static final SQLColumn<FeedbackEntry> COLUMN_DESCRIPTION = new SQLColumn<FeedbackEntry>("description", "TEXT NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_SERVER = new SQLColumn<FeedbackEntry>("server", "VARCHAR(255) NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_SERVERINFO = new SQLColumn<FeedbackEntry>("server_info", "TEXT NOT NULL", Types.VARCHAR);
	public static final SQLColumn<FeedbackEntry> COLUMN_POSITION = new SQLColumn<FeedbackEntry>("position", "VARCHAR(255) NOT NULL", Types.VARCHAR);
	
	private SQLTable<FeedbackEntry> table;
	
	public FeedbackManager(Plugin plugin) throws SQLException {
		table = new SQLTable<>("feedback", Arrays.asList(COLUMN_ID, COLUMN_DATE, COLUMN_OWNER, COLUMN_STATUS, COLUMN_TYPE, COLUMN_DESCRIPTION, COLUMN_SERVER, COLUMN_SERVERINFO, COLUMN_POSITION), resultSet -> {
			FeedbackEntry entry = new FeedbackEntry();
			entry.id = resultSet.getInt("id");
			entry.date = resultSet.getLong("date");
			entry.owner = AccountProviderAPI.getter().getPlayerInformations(resultSet.getLong("owner"));
			entry.type = FeedbackType.fromId(resultSet.getInt("type"));
			int statusID = resultSet.getInt("status");
			if (!resultSet.wasNull()) entry.status = FeedbackStatus.fromId(statusID);
			entry.description = resultSet.getString("description");
			entry.server = OlympaServer.valueOf(resultSet.getString("server"));
			entry.serverInfo = LinkSpigotBungee.getInstance().getGson().fromJson(resultSet.getString("server_info"), ServerInfoAdvanced.class);
			entry.setPosition(resultSet.getString("position"));
			return entry;
		}).createOrAlter();
		
		new FeedbackCommand(plugin).register();
	}
	
	public void registerFeedback(FeedbackEntry entry, IntConsumer callback, Consumer<SQLException> failCallback) {
		table.insertAsync(resultSet -> {
			try {
				resultSet.next();
				callback.accept(resultSet.getInt("id"));
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}, failCallback, entry.date, entry.owner.getId(), entry.status == null ? null : entry.status.getId(), entry.type.getId(), entry.description, entry.server.name(), entry.serverInfo.toString(), entry.getPositionString());
	}
	
}
