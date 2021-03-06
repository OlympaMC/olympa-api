package fr.olympa.api.common.command.complex;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.bukkit.entity.Player;

import fr.olympa.api.common.permission.OlympaPermission;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Cmd {

	/**
	 * If true, the command will not be executed if the executor is not a Player
	 * @return true if the command <i>need</i> to be executed by a player
	 */
	public boolean player() default false;

	/**
	 * If arguments amount is lower than this value, the command will not be executed
	 * @return minimal amount of arguments for the command to be executed
	 */
	public int min() default 0;

	/**
	 * <b>Disponible :</b>
	 * <ul>
	 * <li> PLAYERS : liste des joueurs en ligne, <i>sera remplacé par une instance {@link Player}</i>
	 * <li> DOUBLE : liste vide, <i>sera remplacé par une instance {@link Double}</i>
	 * <li> INTEGER : liste 1|2|3|..., <i>sera remplacé par une instance {@link Integer}</i>
	 * <li> BOOLEAN : liste true|false, <i>sera remplacé par une instance {@link Boolean}</i>
	 * <li> xxx|yyy|zzz : valeurs possibles, séparées par une pipe {@code |}, <id>sera laissé en {@link String}</i>
	 * </ul>
	 * @return String array of possibles arguments
	 */
	public String[] args() default {};

	/**
	 * Adds an alias to the argument
	 * @return String array of possibles aliases
	 */
	public String[] aliases() default {};

	/**
	 * @return syntaxe attendue
	 */
	public String syntax() default "";

	/**
	 * Required permission to execute this command (if empty, no permission will be required)<br>
	 * Final permission will be fetched in {@link OlympaPermission#permissions}
	 * @return name of the permission
	 */
	public String permissionName() default "";

	public String description() default "";

	/**
	 * Hide from tab and from /help
	 */
	public boolean hide() default false;

	/**
	 * If the command is not taken any of the other arguments,
	 * the original argument 0 (the name of the method for the other arguments
	 * of the command remains as argument 0 in the CommandContext.
	 */
	public boolean otherArg() default false;

	/**
	 * Add aliases to tab & /help
	 */
	public boolean registerAliasesInTab() default false;

}
