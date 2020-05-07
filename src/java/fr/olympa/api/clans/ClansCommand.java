package fr.olympa.api.clans;

import java.sql.SQLException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.clans.gui.NoClanGUI;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;

public abstract class ClansCommand<T extends Clan<T>> extends ComplexCommand {

	private ClansManager<T> manager;

	public ClansCommand(ClansManager<T> manager, String name, String description, OlympaPermission permission, String... aliases) {
		super(manager.plugin, name, description, permission, aliases);
		this.manager = manager;
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ClanPlayerInterface<T> olp = AccountProvider.get(p.getUniqueId());
			T clan = olp.getClan();
			if (clan == null) {
				new NoClanGUI<T>(p, manager).create(p);
			}else new ClanManagementGUI<T>(olp, manager).create(p);
			return true;
		}
		return false;
	}

	@Cmd (player = true, min = 1, syntax = "<nom>")
	public void create(CommandContext cmd) {
		ClanPlayerInterface<T> player = getOlympaPlayer();
		if (player.getClan() != null) {
			sendError(manager.stringYouAlreadyInClan);
			return;
		}
		String name = cmd.getArgument(0);
		if (manager.clanExists(name)) {
			sendError(manager.stringClanAlreadyExists);
			return;
		}
		try {
			manager.createClan(player, name);
			sendSuccess(manager.stringClanCreated);
		}catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenue.");
		}
	}

	@Cmd (player = true, min = 1, args = "PLAYERS", syntax = "<nom du joueur>")
	public void invite(CommandContext cmd) {
		T clan = getPlayerClan(true);
		if (clan == null) return;
		
		if (clan.members.size() >= clan.getMaxSize()) {
			sendError(manager.stringClanFull);
			return;
		}

		Player targetPlayer = (Player) cmd.getArgument(0);
		manager.invite(clan, getPlayer(), targetPlayer);
	}
	
	@Cmd (player = true, min = 1, syntax = "<nom du clan>")
	public void accept(CommandContext cmd) {
		T clan = manager.getPlayerInvitations(getPlayer()).stream().filter(x -> x.getName().equals(cmd.getArgument(0))).findFirst().orElse(null);
		if (clan == null) {
			sendError(String.format(manager.stringNoInvitation, cmd.args[0]));
			return;
		}

		if (clan.addPlayer(getOlympaPlayer())) {
			sendSuccess(String.format(manager.stringClanJoined, clan.getName()));
			manager.clearPlayerInvitations(getPlayer());
		}else {
			sendError(manager.stringClanFull);
			manager.getPlayerInvitations(getPlayer()).remove(clan);
		}
	}

	@Cmd (player = true)
	public void quit(CommandContext cmd) {
		T clan = getPlayerClan(false);
		if (clan == null) return;
		
		OlympaPlayer p = getOlympaPlayer();
		if (clan.getChief() == p.getInformation()) {
			if (clan.getMembersAmount() == 1) {
				clan.disband();
			}else sendError(manager.stringCantLeaveChief);
			return;
		}
		
		clan.removePlayer(p.getInformation(), true);
	}

	@Cmd (player = true, min = 1, args = "PLAYERS", syntax = "<nom du joueur>")
	public void chief(CommandContext cmd) {
		T clan = getPlayerClan(false);
		if (clan == null) return;

		if (cmd.getArgument(0) == getPlayer()) {
			sendError(manager.stringCantChiefSelf);
			return;
		}

		OlympaPlayer target = AccountProvider.get(((Player) cmd.args[0]).getUniqueId());
		if (!clan.contains(target)) {
			sendError(String.format(manager.stringPlayerNotInClan, target.getName()));
			return;
		}

		clan.setChief(target.getInformation());
	}

	protected T getPlayerClan(boolean chief) {
		ClanPlayerInterface<T> p = getOlympaPlayer();
		T clan = (T) p.getClan();
		if (clan == null){
			sendError(manager.stringMustBeInClan);
			return null;
		}
		if (chief && clan.getChief() != p.getInformation()) {
			sendError(manager.stringMustBeChief);
			return null;
		}
		return clan;
	}

}
