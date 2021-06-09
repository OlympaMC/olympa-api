package fr.olympa.api.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SpigotOrBungee {

	public AllowedFramework allow();

	public enum AllowedFramework {

		SPIGOT,
		BUNGEE,
		SPIGOT_BUNGEE,
		DISCORD_JDA,

	}

}
