package fr.olympa.api.common.permission.list;

import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.groups.OlympaGroup;

public class OlympaAPIPermissionsBungee {

	private OlympaAPIPermissionsBungee() {}

	public static final OlympaBungeePermission BYPASS_PERM_NOT_EXIST = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission STATS_EXEMPT = new OlympaBungeePermission();
	public static final OlympaBungeePermission COMMAND_STATS_INFO = new OlympaBungeePermission(OlympaGroup.YOUTUBER);
	public static final OlympaBungeePermission COMMAND_STATS_SETTINGS = new OlympaBungeePermission(OlympaGroup.MODP, new OlympaGroup[] { OlympaGroup.DEVP, OlympaGroup.DEV });
	public static final OlympaBungeePermission COMMAND_BUNGEE_SEND = new OlympaBungeePermission(OlympaGroup.DEV);
	public static final OlympaBungeePermission COMMAND_BUNGEE_IP = new OlympaBungeePermission(OlympaGroup.MODP);
	public static final OlympaBungeePermission COMMAND_BUNGEE_RELOAD = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEVP, OlympaGroup.DEV });
	public static final OlympaBungeePermission COMMAND_BUNGEE_END = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEVP });
	public static final OlympaBungeePermission COMMAND_BUNGEE_LIST = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission COMMAND_BUNGEE_FIND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission COMMAND_BUNGEE_SERVER = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission COMMAND_BUNGEE_ALERT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission COMMAND_REDISBUNGEE_ALERT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission COMMAND_REDISBUNGEE_SENDTOALL = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEVP, OlympaGroup.DEV });
	public static final OlympaBungeePermission COMMAND_REDISBUNGEE_INFODIV = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEVP, OlympaGroup.DEV });
	public static final OlympaBungeePermission COMMAND_DEFAULT_NO_PERM_NEEDED = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEVP, OlympaGroup.DEV, OlympaGroup.GAMEMASTER });

	public static final OlympaBungeePermission DISCORD_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, true);
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission STAFF_CHAT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission TEAMSPEAK_SEE_MODHELP = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission TEAMSPEAK_SEE_ADMINHELP = new OlympaBungeePermission(OlympaGroup.MODP);

}
