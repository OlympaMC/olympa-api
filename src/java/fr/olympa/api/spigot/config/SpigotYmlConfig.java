package fr.olympa.api.spigot.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * https://www.spigotmc.org/wiki/spigot-configuration/
 */
public class SpigotYmlConfig {

	final ConfigYmlType<Integer> COMMANDS_TAB$COMPLETE = new ConfigYmlType<>(0, 0, 0, -1, 1, 2);
	final ConfigYmlType<Boolean> COMMANDS_SEND$NAMESPACED = new ConfigYmlType<>(true, true, true, false);
	final ConfigYmlType<Boolean> COMMANDS_SILENT$COMMANDBLOCK$CONSOLE = new ConfigYmlType<>(false, false, true, false);
	final ConfigYmlType<Boolean> COMMANDS_LOG = new ConfigYmlType<>(true, true, true, false);
	final ConfigYmlType<List<String>> COMMANDS_SPAM$EXCLUSIONS = new ConfigYmlType<>(Collections.emptyList(), Arrays.asList("/skill"));
	final ConfigYmlType<Boolean> PLAYERS_DISABLE$SAVING = new ConfigYmlType<>(false, false, true, false);
	final ConfigYmlType<Boolean> STATS_DISABLE$SAVING = new ConfigYmlType<>(false, false, true, false);
	final ConfigYmlType<Boolean> ADVANCEMENTS_DISABLE$SAVING = new ConfigYmlType<>(false, false, true, false);
	final ConfigYmlType<List<String>> ADVANCEMENTS_DISABLED = new ConfigYmlType<>(Arrays.asList("minecraft:story/disabled"), Arrays.asList("minecraft:story/disabled"), Arrays.asList("*"));
	//	final ConfigYmlType<Boolean> WORLD$SETTINGS_ANTI$XRAY_ENABLE =  new ConfigYmlType<>(true, true, true, false);

}
