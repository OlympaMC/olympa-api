package fr.olympa.api.command.complex;

import java.lang.reflect.Method;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.permission.OlympaSpigotPermission;

public abstract class InternalCommand {

	public Cmd cmd;
	public OlympaPermission perm;
	public Method method;
	public Object commands;
	public String name;

	public InternalCommand(Cmd cmd, Method method, Object commandsClass) {
		this.cmd = cmd;
		this.method = method;
		commands = commandsClass;
		name = method.getName();
		String permName = cmd.permissionName();
		if (!permName.isBlank()) {
			perm = OlympaPermission.permissions.get(cmd.permissionName());
			if (perm == null) {
				new IllegalAccessError(ColorUtils.format("&4ComplexCommand %s > &cpermission &4%s&c introuvable, la permission est mise à &4OlympaGroup.FONDA&c.", name, cmd.permissionName()))
						.printStackTrace();
				perm = new OlympaSpigotPermission(OlympaGroup.FONDA);
			}
		}
	}

	String getSyntax() {
		return cmd.syntax();
	}

	public abstract boolean canRun();

}