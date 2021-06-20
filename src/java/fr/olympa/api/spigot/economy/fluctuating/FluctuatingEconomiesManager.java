package fr.olympa.api.spigot.economy.fluctuating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;

public class FluctuatingEconomiesManager {
	
	protected static final SQLColumn<FluctuatingEconomy> COLUMN_ID = new SQLColumn<FluctuatingEconomy>("id", "VARCHAR(45) NOT NULL", Types.VARCHAR).setPrimaryKey(FluctuatingEconomy::getId);
	protected static final SQLColumn<FluctuatingEconomy> COLUMN_VALUE = new SQLColumn<FluctuatingEconomy>("value", "DOUBLE NOT NULL", Types.DOUBLE).setUpdatable();
	protected static final SQLColumn<FluctuatingEconomy> COLUMN_NEXT_UPDATE = new SQLColumn<FluctuatingEconomy>("next_update", "BIGINT(20) NOT NULL", Types.BIGINT).setUpdatable();
	
	protected final SQLTable<FluctuatingEconomy> table;
	private final Plugin plugin;
	
	private final List<FluctuatingEconomy> economies = new ArrayList<>();
	
	public FluctuatingEconomiesManager(Plugin plugin, String id, OlympaSpigotPermission commandPermission) throws SQLException {
		this.plugin = plugin;
		
		table = new SQLTable<>(id + "_economies", Arrays.asList(COLUMN_ID, COLUMN_VALUE, COLUMN_NEXT_UPDATE));
		table.createOrAlter();
		
		new EconomiesCommand(this, commandPermission).register();
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public List<FluctuatingEconomy> getEconomies() {
		return economies;
	}
	
	public FluctuatingEconomy getEconomy(String id) {
		return economies.stream().filter(x -> x.getId().equals(id)).findAny().orElse(null);
	}
	
	public void register(FluctuatingEconomy... economies) throws SQLException {
		for (FluctuatingEconomy eco : economies) {
			ResultSet resultSet = table.get(eco.getId());
			if (resultSet.next()) {
				eco.value.set(resultSet.getDouble("value"));
				eco.nextUp.set(resultSet.getLong("next_update"));
			}else {
				table.insertAsync(null, null, eco.getId(), eco.value.get(), eco.nextUp.get());
			}
			eco.value.observe("sql", () -> COLUMN_VALUE.updateAsync(eco, eco.value.get(), null, null));
			eco.nextUp.observe("sql", () -> COLUMN_NEXT_UPDATE.updateAsync(eco, eco.nextUp.get(), null, null));
			eco.start(plugin);
			this.economies.add(eco);
		}
	}
	
}
