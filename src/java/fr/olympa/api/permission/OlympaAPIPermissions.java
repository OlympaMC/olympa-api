package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;

public class OlympaAPIPermissions {
	
	public static final OlympaPermission CONNECT_SERVERSTATUS_DEV = new OlympaPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaPermission CONNECT_SERVERSTATUS_MAINTENANCE = new OlympaPermission(new OlympaGroup[] { OlympaGroup.DEV }, OlympaGroup.RESP_TECH);
	public static final OlympaPermission CONNECT_SERVERSTATUS_BETA = new OlympaPermission(OlympaGroup.YOUTUBER);
	public static final OlympaPermission CONNECT_SERVERSTATUS_SOON = new OlympaPermission(OlympaGroup.FRIEND);
	public static final OlympaPermission CONNECT_SERVER_BUILDER = new OlympaPermission(new OlympaGroup[] { OlympaGroup.BUILDER }, OlympaGroup.RESP_TECH);
	
	public static final OlympaPermission COMMAND_HOLOGRAMS_MANAGE = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission COMMAND_IMAGEMAP = new OlympaPermission(OlympaGroup.DEV);
	
}