package fr.olympa.api.common.permission.list;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaGlobalPermission;
import fr.olympa.api.common.server.OlympaServer;

public class OlympaAPIPermissionsGlobal {
	
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_DEV = new OlympaGlobalPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_MAINTENANCE = new OlympaGlobalPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_BETA = new OlympaGlobalPermission(OlympaGroup.YOUTUBER);
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_SOON = new OlympaGlobalPermission(OlympaGroup.FRIEND);
	
	public static final OlympaGlobalPermission CONNECT_SERVER_BUILDER = new OlympaGlobalPermission(/*new OlympaGroup[] { OlympaGroup.BUILDER }, OlympaGroup.RESP_TECH*/ OlympaGroup.BUILDER);
	public static final OlympaGlobalPermission CONNECT_SERVER_DEV = new OlympaGlobalPermission(OlympaGroup.DEV);
	
	{
		OlympaServer.DEV.setJoinPermission(CONNECT_SERVER_DEV);
		OlympaServer.BUILDEUR.setJoinPermission(CONNECT_SERVER_BUILDER);
	}
	
}
