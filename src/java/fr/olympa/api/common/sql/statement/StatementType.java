package fr.olympa.api.common.sql.statement;

public class StatementType {

	public static final StatementTypeInsertDelete INSERT = new StatementTypeInsertDelete("INSERT INTO");
	public static final StatementTypeInsertDelete DELETE = new StatementTypeInsertDelete("DELETE FROM");
	public static final StatementTypeSelectUpdate SELECT = new StatementTypeSelectUpdate("SELECT");
	public static final StatementTypeSelectUpdate UPDATE = new StatementTypeSelectUpdate("UPDATE");
	public static final StatementTypeDefault TRUNCATE = new StatementTypeDefault("TRUNCATE");

}
