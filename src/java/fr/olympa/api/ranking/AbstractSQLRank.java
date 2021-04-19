package fr.olympa.api.ranking;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.statement.OlympaStatement;

public abstract class AbstractSQLRank extends AbstractRank {
	
	protected AbstractSQLRank(String id, Location location, int maxSlots, boolean keepTopScore) throws SQLException {
		super(id, location, maxSlots, keepTopScore);
	}
	
	@Override
	protected void fillUpScores(ScoreEntry[] scores) throws SQLException {
		String db = AccountProvider.getPluginPlayerTable().getName();
		String column = getColumn();
		OlympaStatement topStatement =
				new OlympaStatement("SELECT pseudo, " + db + "." + column
						+ " FROM " + db
						+ " INNER JOIN commun.players ON " + db + ".player_id = commun.players.id"
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
		}
	}
	
	protected abstract String getColumn();
	
	protected double getDefaultValue() {
		return 0;
	}
	
}
