package fr.olympa.api.spigot.clans;

import java.sql.SQLException;
import java.util.Collections;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.clans.gui.NoClanGUI;
import fr.olympa.api.spigot.command.ComplexCommand;

public class ClansCommand<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends ComplexCommand {

	protected final ClansManager<T, D> manager;

	public ClansCommand(ClansManager<T, D> manager, String description, OlympaSpigotPermission permission, OlympaSpigotPermission managePermission, String... aliases) {
		super(manager.plugin, manager.getClansCommand(), description, permission, aliases);
		this.manager = manager;
		
		super.addArgumentParser("CLANPLAYER", (player, arg) -> {
			ClanPlayerInterface<T, D> p = getOlympaPlayer();
			T clan = p.getClan();
			if (clan == null) return Collections.emptyList();
			return clan.getMembers().stream().map(x -> x.getPlayerInformations().getName()).collect(Collectors.toList());
		}, arg -> {
			ClanPlayerInterface<T, D> p = getOlympaPlayer();
			T clan = p.getClan();
			if (clan == null) return null;
			return clan.getMembers().stream().filter(x -> x.getPlayerInformations().getName().equalsIgnoreCase(arg)).findFirst().orElse(null);
		}, x -> String.format(manager.stringPlayerNotInClan, x));
		
		super.addArgumentParser("CLAN", (player, arg) -> manager.getClans().stream().map(e -> e.getValue().getName()).collect(Collectors.toSet()),
                manager::getByName,
				x -> String.format(manager.stringClanNotExist, x));
		
		super.getCommand("forcechief").perm = managePermission;
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player p) {
			ClanPlayerInterface<T, D> olp = AccountProviderAPI.getter().get(p.getUniqueId());
			T clan = olp.getClan();
			if (clan == null) {
				new NoClanGUI<>(p, manager).create(p);
			}else manager.provideManagementGUI(olp).create(p);
			return true;
		}
		return false;
	}

	@Cmd (player = true, min = 1, syntax = "<nom>", description = "Permet de cr??er un clan")
	public void create(CommandContext cmd) {
		ClanPlayerInterface<T, D> player = getOlympaPlayer();
		if (player.getClan() != null) {
			sendError(manager.stringYouAlreadyInClan);
			return;
		}
		String name = cmd.getFrom(0);
		if (!manager.checkName(getPlayer(), name)) return;
		try {
			manager.createClan(player, name, manager.generateTag(name));
		}catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenue.");
		}
	}

	@Cmd (player = true, min = 1, args = "PLAYERS", syntax = "<nom du joueur>", description = "Permet d'inviter un joueur dans son clan")
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
	
	@Cmd (player = true, min = 1, syntax = "<tag du clan>", description = "Permet d'accepter une invitation de clan", aliases = "join")
	public void accept(CommandContext cmd) {
		String tag = cmd.getFrom(0);
		T clan = manager.getPlayerInvitations(getPlayer()).stream().filter(x -> x.getTag().equals(tag)).findFirst().orElse(null);
		if (clan == null) {
			sendError(manager.stringNoInvitation, tag);
			return;
		}

		if (clan.addPlayer(getOlympaPlayer(), true)) {
			sendSuccess(manager.stringClanJoined, clan.getName());
			manager.clearPlayerInvitations(getPlayer());
		}else {
			sendError(manager.stringClanFull);
			manager.getPlayerInvitations(getPlayer()).remove(clan);
		}
	}

	@Cmd (player = true, description = "Permet de quitter son clan")
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

	@Cmd (player = true, min = 1, args = { "PLAYERS" }, syntax = "<nom du joueur>", description = "Permet de transmettre la direction de son clan", aliases = "leader")
	public void chief(CommandContext cmd) {
		T clan = getPlayerClan(false);
		if (clan == null) return;

		if (cmd.getArgument(0) == getPlayer()) {
			sendError(manager.stringCantChiefSelf);
			return;
		}

		OlympaPlayer target = AccountProviderAPI.getter().get(cmd.<Player>getArgument(0).getUniqueId());
		if (!clan.contains(target.getInformation())) {
			sendError(manager.stringPlayerNotInClan, target.getName());
			return;
		}

		clan.setChief(target.getInformation());
	}
	
	@Cmd (player = true, min = 1, args = { "PLAYERS" })
	public void forcechief(CommandContext cmd) {
		ClanPlayerInterface<T, D> target = AccountProviderAPI.getter().get(cmd.<Player>getArgument(0).getUniqueId());
		T clan = target.getClan();
		
		if (clan == null) {
			sendError(manager.stringMustBeInClan);
			return;
		}
		
		if (clan.getChief().getId() == target.getId()) {
			sendError("Le joueur %s est d??j?? chef.", target.getName());
			return;
		}
		
		clan.setChief(target.getInformation());
		sendSuccess("Le joueur %s est d??sormais chef de %s.", target.getName(), clan.getName());
	}

	@Cmd (player = true, aliases = { "settag" }, min = 1, syntax = "<nouveau tag>")
	public void tag(CommandContext cmd) {
		T clan = getPlayerClan(true);
		if (clan == null) return;
		
		String tag = cmd.<String>getArgument(0).toUpperCase();
		if (clan.getTag().equals(tag)) {
			sendError("Le tag propos?? est identique ?? celui d??j?? choisi.");
			return;
		}
		if (!manager.checkTag(getPlayer(), tag)) return;
		
		clan.setTag(tag);
	}
	
	protected T getPlayerClan(boolean chief) {
		ClanPlayerInterface<T, D> p = getOlympaPlayer();
		T clan = p.getClan();
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
