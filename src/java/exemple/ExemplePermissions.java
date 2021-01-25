package exemple;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.permission.OlympaSpigotPermission;

public class ExemplePermissions {

	public static final OlympaSpigotPermission EXEMPLE_NYAN = new OlympaSpigotPermission(OlympaGroup.FRIEND);
	public static final OlympaSpigotPermission EXEMPLE_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static OlympaSpigotPermission EXEMPLE;

	public ExemplePermissions() {

		// Donne la permission EXEMPLE au Admin & + (Admin, Fonda) et aux Dev et Resp_Tech = Admin, Fonda, Resp_Tech, Dev (on a donc sauté le MOD+ qui n'aura pas la perm).
		OlympaGroup minGroup = OlympaGroup.ADMIN;
		OlympaGroup[] allowedGroups = new OlympaGroup[] { OlympaGroup.DEV, OlympaGroup.RESP_TECH };
		boolean lockPermission = true;
		EXEMPLE = new OlympaSpigotPermission(minGroup, allowedGroups, lockPermission);

		// La permission est accessible
		OlympaSpigotPermission permission = OlympaAPIPermissions.CONNECT_SERVER_DEV;

		// ou La permission est dans le core ou autre plugin
		permission = (OlympaSpigotPermission) OlympaPermission.permissions.get("CONNECT_SERVER_DEV");

		// enlever les groupes qui avait la perms indépendamment + change la permission minimale
		permission.clearAllowedGroups();
		permission.setMinGroup(OlympaGroup.RESP_TECH);

		// désactive/réactive la permission -> désactiver = uniquement le haut staff + AllowedBypass peuvent l'utiliser
		permission.disable();
		permission.enable();

		// allow uniquement ce groupe
		permission.allowGroup(OlympaGroup.YOUTUBER);

		// allow uniquement ce joueur (même quand la permission est désactiver)
		Player player = null;
		permission.allowPlayer(player);

		// Impossible de modifier la permission après sauf pour la désactiver/réactiver
		permission.lockPermission();
		// Si la permission est vérouillée
		permission.isLocked();
	}
}
