package fr.olympa.api.common.permission.list;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;

public class OlympaAPIPermissionsSpigot {

	private OlympaAPIPermissionsSpigot() {}
	
	public static final OlympaSpigotPermission COMMAND_HOLOGRAMS_MANAGE = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission COMMAND_IMAGEMAP = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission COMMAND_BYPASS_REGIONS = new OlympaSpigotPermission(OlympaGroup.BUILDER); // TODO + haute perm après ouverture

	public static final OlympaSpigotPermission NAMESPACED_COMMANDS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission AFK_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);

	public static final OlympaSpigotPermission SAY_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
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
	public static final OlympaSpigotPermission VANISH_COMMAND = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission VANISH_COMMAND_HIDE_STAFF = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission ITEM_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP);
	public static final OlympaSpigotPermission TRADE_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);

	public static final OlympaSpigotPermission ARG_COLOR = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

}
