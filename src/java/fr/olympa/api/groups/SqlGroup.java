package fr.olympa.api.groups;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fr.olympa.api.player.Gender;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.StatementType;
import fr.olympa.api.utils.Utils;

public class SqlGroup {

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
	private static OlympaStatement insertPlayerStatement = new OlympaStatement(StatementType.INSERT, tableName, "id", "name", "name_fem", "power", "prefix", "chatSuffix", "highStaff", "server");

	public static void insert(OlympaGroup group) throws SQLException {
		PreparedStatement statement = insertPlayerStatement.getStatement();
		int i = 1;
		statement.setLong(i++, group.getId());
		statement.setString(i++, group.getName(Gender.MALE));
		statement.setString(i++, group.getName(Gender.FEMALE));
		statement.setInt(i++, group.getPower());
		statement.setString(i++, group.getPrefix());
		statement.setString(i++, group.getChatSuffix());
		statement.setInt(i++, Utils.booleanToBinary(group.isHighStaff()));
		statement.setString(i++, group.getServer().name());
		insertPlayerStatement.execute(statement);
		statement.close();

	}

	private static OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, tableName, "id", new String[] { "name", "name_fem", "power", "prefix", "chatSuffix", "highStaff", "server" });

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
		statement.setLong(i++, group.getId());
		insertPlayerStatement.execute(statement);
	}

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, tableName, "id", null);

	public static boolean hasGroup(OlympaGroup group) throws SQLException {
		boolean b = false;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setInt(1, group.getId());
		ResultSet resultSet = insertPlayerStatement.executeQuery(statement);
		if (resultSet.next())
			b = true;
		resultSet.close();
		return b;
	}

	public static Boolean hasExactlyGroup(OlympaGroup group) throws SQLException {
		Boolean b = null;
		PreparedStatement statement = selectStatement.getStatement();
		statement.setInt(1, group.getId());
		ResultSet resultSet = insertPlayerStatement.executeQuery(statement);
		if (resultSet.next())
			b = group.getName(Gender.UNSPECIFIED).equals(resultSet.getString("name")) && group.getName(Gender.FEMALE).equals(resultSet.getString("name_fem"))
					&& group.getPower() == resultSet.getInt("power") && group.getPrefix().equals(resultSet.getString("prefix")) && group.getChatSuffix().equals(resultSet.getString("chatSuffix"))
					&& Utils.booleanToBinary(group.isHighStaff()) == resultSet.getInt("highStaff") && group.getServer().name().equals(resultSet.getString("server"));
		resultSet.close();
		return b;
	}
}
