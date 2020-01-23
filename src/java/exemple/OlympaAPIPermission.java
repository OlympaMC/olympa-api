package exemple;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class OlympaAPIPermission {

	public static final OlympaPermission CONNECT_SERVER_DEV = new OlympaPermission(new OlympaGroup[] { OlympaGroup.DEV }, OlympaGroup.RESP_TECH);
	public static final OlympaPermission CONNECT_SERVER_BUILDER = new OlympaPermission(new OlympaGroup[] { OlympaGroup.BUILDER }, OlympaGroup.RESP_TECH);
	public static final OlympaPermission CONNECT_SERVER_MAINTENANCE = new OlympaPermission(OlympaGroup.DEV, OlympaGroup.RESP_TECH);
	public static final OlympaPermission CONNECT_SERVER_BETA = new OlympaPermission(OlympaGroup.YOUTUBER);
	public static final OlympaPermission CONNECT_SERVER_SOON = new OlympaPermission(OlympaGroup.FRIEND);
	public static final OlympaPermission EXEMPLE_NYAN = new OlympaPermission(OlympaGroup.FRIEND);

	public static final OlympaPermission EXEMPLE_COMMAND = new OlympaPermission(OlympaGroup.ADMIN_SYS);

}
