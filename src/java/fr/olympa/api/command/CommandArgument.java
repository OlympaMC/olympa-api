package fr.olympa.api.command;

import java.util.Arrays;
import java.util.Map;

import fr.olympa.api.permission.OlympaPermission;

public class CommandArgument {
	
	String argName;
	String description;
	OlympaPermission permission;
	Map<Integer, String[]> requirePrecedentArg;
	
	public CommandArgument(String argName) {
		this.argName = argName;
	}
	
	public CommandArgument(String argName, Map<Integer, String[]> requirePrecedentArg, OlympaPermission permission) {
		this.argName = argName;
		this.requirePrecedentArg = requirePrecedentArg;
		this.permission = permission;
	}

	public String getDescription() {
		return description;
	}
	
	public OlympaPermission getPermission() {
		return permission;
	}
	
	public Map<Integer, String[]> getRequirePrecedentArg() {
		return requirePrecedentArg;
	}
	
	public String getArgName() {
		return argName;
	}

	public Boolean hasRequireArg(String[] args, int index) {
		if (requirePrecedentArg == null)
			return true;
		if (index >= requirePrecedentArg.size() || index >= args.length)
			return null;
		String argUsed = args[index];
		return Arrays.stream(requirePrecedentArg.get(index)).anyMatch(requireArgs -> requireArgs.equalsIgnoreCase(argUsed));
	}
}
