package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.server.OlympaServer;

public class OlympaAPIPermissions {

	public static final OlympaSpigotPermission CONNECT_SERVERSTATUS_DEV = new OlympaSpigotPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaSpigotPermission CONNECT_SERVERSTATUS_MAINTENANCE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaSpigotPermission CONNECT_SERVERSTATUS_BETA = new OlympaSpigotPermission(OlympaGroup.YOUTUBER);
	public static final OlympaSpigotPermission CONNECT_SERVERSTATUS_SOON = new OlympaSpigotPermission(OlympaGroup.FRIEND);

	public static final OlympaSpigotPermission CONNECT_SERVER_BUILDER = new OlympaSpigotPermission(/*new OlympaGroup[] { OlympaGroup.BUILDER }, OlympaGroup.RESP_TECH*/ OlympaGroup.BUILDER);
	public static final OlympaSpigotPermission CONNECT_SERVER_DEV = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission COMMAND_HOLOGRAMS_MANAGE = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission COMMAND_IMAGEMAP = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission COMMAND_BYPASS_REGIONS = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission NAMESPACED_COMMANDS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission AFK_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);

	// Don't fogot to change it in ZTA
	public static final OlympaSpigotPermission GAMEMODE_COMMAND = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission TP_COMMAND = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission GAMEMODE_COMMAND_CREATIVE = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission FLY_COMMAND = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission INVSEE_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission INVSEE_COMMAND_INTERACT = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission ECSEE_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission ECSEE_COMMAND_INTERACT = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission ERRORS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission PING_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission VANISH_SEE = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission VANISH_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_STAFF, new OlympaGroup[] { OlympaGroup.MODP });

	{
		OlympaServer.DEV.setJoinPermission(CONNECT_SERVER_DEV);
		OlympaServer.BUILDEUR.setJoinPermission(CONNECT_SERVER_BUILDER);
	}

}
