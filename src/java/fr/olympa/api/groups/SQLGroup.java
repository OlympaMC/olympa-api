package fr.olympa.api.groups;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fr.olympa.api.player.Gender;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.api.utils.Utils;

@SuppressWarnings("deprecation")
public class SQLGroup {

	public static void init() {
		for (OlympaGroup group : OlympaGroup.values())
			try {
				Boolean b = hasExactlyGroup(group);
				if (b == null)
					insert(group);
				else if (!b)
					update(group);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		// Todo remove unused ID in db
	}

	static String tableName = "commun.groups";
	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableName, "id", "name", "name_fem", "power", "prefix", "chat_suffix", "high_staff", "server");

	public static void insert(OlympaGroup group) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setInt(i++, group.getId());
		statement.setString(i++, group.getName(Gender.MALE));
		statement.setString(i++, group.getName(Gender.FEMALE));
		statement.setInt(i++, group.getPower());
		statement.setString(i++, group.getPrefix());
		statement.setString(i++, group.getChatSuffix());
		statement.setInt(i++, Utils.booleanToBinary(group.isHighStaff()));
		statement.setString(i, group.getServer().name());
		insertPlayerStatement.execute();
		statement.close();

	}

	private static OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, tableName, "id", new String[] { "name", "name_fem", "power", "prefix", "chat_suffix", "high_staff", "server" });

	public static void update(OlympaGroup group) throws SQLException {
		PreparedStatement statement = updateStatement.getStatement();
		int i = 1;
		statement.setString(i++, group.getName(Gender.UNSPECIFIED));
		statement.setString(i++, group.getName(Gender.FEMALE));
		statement.setInt(i++, group.getPower());
		statement.setString(i++, group.getPrefix());
		statement.setString(i++, group.getChatSuffix());
		statement.setInt(i++, Utils.booleanToBinary(group.isHighStaff()));
		statement.setString(i++, group.getServer().name());
		statement.setInt(i, group.getId());
		updateStatement.execute();
	}

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, tableName, "id", null);

	public static boolean hasGroup(OlympaGroup group) throws SQLException {
		boolean b = false;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setInt(1, group.getId());
		ResultSet resultSet = selectStatement.executeQuery();
		if (resultSet.next())
			b = true;
		resultSet.close();
		return b;
	}

	public static Boolean hasExactlyGroup(OlympaGroup group) throws SQLException {
		Boolean b = null;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setInt(1, group.getId());
		ResultSet resultSet = selectStatement.executeQuery();
		if (resultSet.next())
			b = group.getName(Gender.UNSPECIFIED).equals(resultSet.getString("name")) && group.getName(Gender.FEMALE).equals(resultSet.getString("name_fem"))
					&& group.getPower() == resultSet.getInt("power") && group.getPrefix().equals(resultSet.getString("prefix")) && group.getChatSuffix().equals(resultSet.getString("chat_suffix"))
					&& Utils.booleanToBinary(group.isHighStaff()) == resultSet.getInt("high_staff") && group.getServer().name().equals(resultSet.getString("server"));
		resultSet.close();
		return b;
	}
}
