package fr.olympa.api.common.sql;

public abstract class SQLClass {

	protected static String table;
	//	private static String createTable;

	public String getTable() {
		return table;
	}

	public String getTableCleanName() {
		return table.replace("`", "");
	}

	public static void init(String dataBase, String table) {
		//		SQLClass.createTable = createTable;
		SQLClass.table = String.format("`%s`.`%s`", dataBase, table);
	}
}
