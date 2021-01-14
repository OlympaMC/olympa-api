package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.server.OlympaServer;

public class OlympaAPIPermissions {

	public static final OlympaPermission CONNECT_SERVERSTATUS_DEV = new OlympaPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaPermission CONNECT_SERVERSTATUS_MAINTENANCE = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission CONNECT_SERVERSTATUS_BETA = new OlympaPermission(OlympaGroup.YOUTUBER);
	public static final OlympaPermission CONNECT_SERVERSTATUS_SOON = new OlympaPermission(OlympaGroup.FRIEND);

	public static final OlympaPermission CONNECT_SERVER_BUILDER = new OlympaPermission(/*new OlympaGroup[] { OlympaGroup.BUILDER }, OlympaGroup.RESP_TECH*/ OlympaGroup.BUILDER);
	public static final OlympaPermission CONNECT_SERVER_DEV = new OlympaPermission(OlympaGroup.DEV);

	public static final OlympaPermission COMMAND_HOLOGRAMS_MANAGE = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission COMMAND_IMAGEMAP = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission COMMAND_BYPASS_REGIONS = new OlympaPermission(OlympaGroup.DEV);

	public static final OlympaPermission NAMESPACED_COMMANDS = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission AFK_SEE_IN_TAB = new OlympaPermission(OlympaGroup.GRAPHISTE);

	// Don't fogot to change it in ZTA
	public static final OlympaPermission GAMEMODE_COMMAND = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission TP_COMMAND = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission GAMEMODE_COMMAND_CREATIVE = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission FLY_COMMAND = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission INVSEE_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission INVSEE_COMMAND_INTERACT = new OlympaPermission(OlympaGroup.MODP);
	public static final OlympaPermission ECSEE_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission ECSEE_COMMAND_INTERACT = new OlympaPermission(OlympaGroup.MODP);
	public static final OlympaPermission ERRORS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission PING_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);

	{
		OlympaServer.DEV.setJoinPermission(CONNECT_SERVER_DEV);
		OlympaServer.BUILDEUR.setJoinPermission(CONNECT_SERVER_BUILDER);
	}

}
