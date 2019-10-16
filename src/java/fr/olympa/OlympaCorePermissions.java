package fr.olympa;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class OlympaCorePermissions {

	// TODO change to OlympaGroup.ADMIN
	public static OlympaPermission GROUP_COMMAND = new OlympaPermission(OlympaGroup.DEV);

	public static OlympaPermission CHAT_COMMAND = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission CHAT_SEEINSULTS = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission CHAT_BYPASS = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission CHAT_MUTEDBYPASS = new OlympaPermission(OlympaGroup.MOD);

	public static OlympaPermission BAN_BAN_COMMAND = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_BANIP_COMMAND = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission BAN_DELBAN_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static OlympaPermission BAN_UNBAN_COMMAND = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission BAN_UNMUTE_COMMAND = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_BANHIST_COMMAND = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_SEEBANMSG = new OlympaPermission(OlympaGroup.BUILDER);
	public static OlympaPermission BAN_BYPASS_BAN = new OlympaPermission(OlympaGroup.BUILDER);
	public static OlympaPermission BAN_BYPASS_MAXTIME = new OlympaPermission(OlympaGroup.DEV);
	public static OlympaPermission BAN_BYPASS_MINTIME = new OlympaPermission(OlympaGroup.DEV);
	public static OlympaPermission BAN_DEF = new OlympaPermission(OlympaGroup.MODP);

	public static OlympaPermission SPAWN_SPAWN_COMMAND_SET = new OlympaPermission(OlympaGroup.DEV);

	public static OlympaPermission CHAT_COLOR = new OlympaPermission(OlympaGroup.MODP);

}
