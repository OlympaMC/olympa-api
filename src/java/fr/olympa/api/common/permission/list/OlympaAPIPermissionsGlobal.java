package fr.olympa.api.common.permission.list;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaGlobalPermission;

public class OlympaAPIPermissionsGlobal {

	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_DEV = new OlympaGlobalPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_MAINTENANCE = new OlympaGlobalPermission();
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_BETA = new OlympaGlobalPermission(OlympaGroup.YOUTUBER);
	public static final OlympaGlobalPermission CONNECT_SERVERSTATUS_SOON = new OlympaGlobalPermission(OlympaGroup.FRIEND);

	public static final OlympaGlobalPermission CONNECT_SERVER_BUILDER = new OlympaGlobalPermission();
	public static final OlympaGlobalPermission CONNECT_SERVER_DEV = new OlympaGlobalPermission(OlympaGroup.DEV);
	public static final OlympaGlobalPermission PLUGINS_SEE_VALUE_VERSION = new OlympaGlobalPermission();

}
