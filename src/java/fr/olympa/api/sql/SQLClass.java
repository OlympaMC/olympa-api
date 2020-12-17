package fr.olympa.api.sql;

public abstract class SQLClass {

	protected static String table;
	//	private static String createTable;

	public String getTable() {
		return table;
	}

	public static void init(String dataBase, String table) {
		//		SQLClass.createTable = createTable;
		SQLClass.table = String.format("`%s`.`%s`", dataBase, table);
	}
}
