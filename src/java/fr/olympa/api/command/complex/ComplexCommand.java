	package fr.olympa.api.command.complex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.OlympaCore;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;

public class ComplexCommand extends OlympaCommand{

	public final Map<String, InternalCommand> commands = new HashMap<>();
	private Predicate<CommandSender> noArgs;
	
	/**
	 * @param noArgs Predicate(CommandSender) who'll be ran if the command is executed without any arguments. If is null or return <tt>false</tt>, "incorrect syntax" will be sent to the player
	 */
	public ComplexCommand(Predicate<CommandSender> noArgs, Plugin plugin, String command, String description, OlympaPermission permission, String... alias) {
		super(plugin, command, description, permission, alias);
		this.noArgs = noArgs;
		
		this.registerCommandsClass(this);
	}
	
	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	public void registerCommandsClass(Object commandsClassInstance){
		for(Method method : commandsClassInstance.getClass().getDeclaredMethods()){
			if (method.isAnnotationPresent(Cmd.class)){
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1){
					if (method.getParameterTypes()[0] == CommandContext.class){
						this.commands.put(method.getName().toLowerCase(), new InternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				}
				OlympaCore.getInstance().sendMessage("Error when loading command annotated method " + method.getName() + " in class " + commandsClassInstance.getClass().getName() + ". Required argument: fr.olympa.api.command.complex.CommandContext");
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (args.length == 0){
			if (noArgs == null || !noArgs.test(sender)) {
				sendIncorrectSyntax();
			}
			return true;
		}
		
		InternalCommand internal = commands.get(args[0].toLowerCase());
		if (internal == null){
			sendError("La commande n'existe pas.");
			return true;
		}
		
		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof Player)){
			sendImpossibleWithConsole();
			return true;
		}
		
		if (!cmd.permissionName().isEmpty() && !hasPermission(cmd.permissionName())) {
			sendDoNotHavePermission();
			return true;
		}
		
		if (args.length - 1 < cmd.min()){
			if (cmd.syntax() == "") {
				sendIncorrectSyntax();
			}else sendIncorrectSyntax(internal.method.getName() + " " + cmd.syntax());
			return true;
		}
		
		Object[] argsCmd = new Object[args.length - 1];
		for (int i = 1; i < args.length; i++){
			String arg = args[i];
			String type = i > cmd.args().length ? "" : cmd.args()[i-1];
			if (type.equals("PLAYERS")){
				Player target = Bukkit.getPlayerExact(arg);
				if (target == null){
					sendUnknownPlayer(arg);
					return true;
				}
				argsCmd[i-1] = target;
			}else {
				argsCmd[i-1] = arg;
			}
		}
		
		try {
			internal.method.invoke(internal.commands, new CommandContext(this, sender, argsCmd, label));
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sendError("Une erreur est survenue.");
			e.printStackTrace();
		}
		
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];
		
		if (args.length == 1){
			for (Entry<String, InternalCommand> en : commands.entrySet()){ // PERMISSIONS
				String perm = en.getValue().cmd.permissionName();
				if (perm != null && hasPermission(perm)) find.add(en.getKey());
			}
		}else if (args.length >= 2){
			int index = args.length-2;
			if (!commands.containsKey(sel)) return tmp;
			InternalCommand internal = commands.get(sel);
			String[] needed = internal.cmd.args();
			if (needed.length <= index) return tmp;
			if (!internal.cmd.permissionName().isEmpty() && !hasPermission(internal.cmd.permissionName())) return tmp;
			sel = args[index + 1];
			String key = needed[index];
			if (key.equals("PLAYERS")) {
				return null;
			}else {
				find.addAll(Arrays.asList(key.split("\\|")));
			}
		}else return tmp;
		
		for (String arg : find){
			if (arg.startsWith(sel)) tmp.add(arg);
		}
		return tmp;
	}
	
	class InternalCommand{
		Cmd cmd;
		Method method;
		Object commands;
		
		InternalCommand(Cmd cmd, Method method, Object commandsClass){
			this.cmd = cmd;
			this.method = method;
			this.commands = commandsClass;
		}
	}
	
}