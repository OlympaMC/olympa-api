package fr.olympa.api.spigot.ranking;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.sql.statement.OlympaStatement;

public abstract class AbstractSQLRank extends AbstractRank {
	
	protected AbstractSQLRank(String id, Location location, int maxSlots, boolean keepTopScore) throws SQLException {
		super(id, location, maxSlots, keepTopScore);
	}
	
	@Override
	protected void fillUpScores(ScoreEntry[] scores) throws SQLException {
		String db = AccountProviderAPI.getter().getPluginPlayerTable().getName();
		String column = getColumn();
		OlympaStatement topStatement =
				new OlympaStatement("SELECT pseudo, " + db + "." + column
						+ " FROM " + db
						+ " INNER JOIN common.players ON " + db + ".player_id = common.players.id"
						+ " ORDER BY " + db + "." + column + " DESC LIMIT " + getMaxSlots());
		try (PreparedStatement statement = topStatement.createStatement()) {
			ResultSet resultSet = topStatement.executeQuery(statement);
			int i = 0;
			while (resultSet.next()) {
				double value = resultSet.getDouble(column);
				if (value != getDefaultValue()) scores[i].fill(resultSet.getString("pseudo"), value);
				i++;
			}
			resultSet.close();
		}catch (SQLException ex) {
			if (ex.getCause().getMessage().contains("doesn't exist")) return; // table does not exist
			ex.printStackTrace();
		}
	}
	
	protected abstract String getColumn();
	
	protected double getDefaultValue() {
		return 0;
	}
	
}
