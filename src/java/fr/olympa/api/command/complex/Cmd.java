package fr.olympa.api.command.complex;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.bukkit.entity.Player;

import fr.olympa.api.permission.OlympaPermission;

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
	 * <b>Available :</b>
	 * <ul>
	 * <li> PLAYERS : <i>list of players online</i>
	 * <li> xxx|yyy|zzz : <i>available values, separated by a pipe (|)</i>
	 * </ul>
	 * In the case of PLAYERS, it will be directly replaced by an instance of {@link Player} before command executing (no need to parse the String)
	 * @return String array of possibles arguments
	 */
	public String[] args() default {};
	
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
	
}
