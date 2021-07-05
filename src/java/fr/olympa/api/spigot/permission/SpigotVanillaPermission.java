package fr.olympa.api.spigot.permission;

import java.util.Arrays;
import java.util.Optional;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;

public enum SpigotVanillaPermission {

	BYPASS_PERM_NOT_EXIST(OlympaAPIPermissionsSpigot.BYPASS_PERM_NOT_EXIST),
	VIAVERSION_ADMIN(OlympaAPIPermissionsSpigot.VERVERSION_ADMIN),
	;

	public static SpigotVanillaPermission get(String minecraftVanillaName) {
		return Arrays.stream(SpigotVanillaPermission.values()).filter(p -> p.getSpigotVanillaName().equals(minecraftVanillaName)).findFirst().or(() -> {
			LinkSpigotBungee.getInstance().sendMessage("&4%s&c is not register in %s. You need to add it to handle this permission with OlympaPermission system.",
					minecraftVanillaName, SpigotVanillaPermission.class.getName());
			return Optional.of(BYPASS_PERM_NOT_EXIST);
		}).get();

	}

	OlympaSpigotPermission permission;

	SpigotVanillaPermission(OlympaSpigotPermission permission) {
		this.permission = permission;
	}

	public String getSpigotVanillaName() {
		return name().replace("_", ".").toLowerCase();
	}

	public OlympaSpigotPermission getPermission() {
		return permission;
	}

}
