package exemple;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class ExemplePermissions {

	public static final OlympaPermission EXEMPLE_NYAN = new OlympaPermission(OlympaGroup.FRIEND);
	public static final OlympaPermission EXEMPLE_COMMAND = new OlympaPermission(OlympaGroup.DEV);

}
