package fr.olympa.api.afk;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class AfkPlayer {

	public static final String AFK_SUFFIX = " §4[§cAFK§4]";

	boolean afk;
	String lastAction;
	String lastSuffix;
	long time;
	Integer taskId;

	public AfkPlayer(Player player) {
		afk = false;
		launchTask(player);
	}

	public boolean disableTask() {
		if (taskId != null) {
			LinkSpigotBungee.Provider.link.getTask().cancelTaskById(taskId);
			taskId = null;
			return true;
		}
		return false;
	}

	void launchTask(Player player) {
		disableTask();
		OlympaTask taskHandler = LinkSpigotBungee.Provider.link.getTask();
		taskId = taskHandler.runTaskLater(() -> {
			setAfk(player);
		}, 15, TimeUnit.MINUTES);
	}

	public AfkPlayer(boolean afk, String lastAction) {
		this.lastAction = lastAction;
		this.afk = afk;
		time = Utils.getCurrentTimeInSeconds();
	}

	public void setAfk(boolean afk) {
		this.afk = afk;
	}

	public void setAfk(Player player) {
		afk = true;
		Prefix.DEFAULT_BAD.sendMessage(player, "Tu es désormais &4AFK&c.");
		INametagApi api = OlympaCore.getInstance().getNameTagApi();
		if (api != null) {
			/*Nametag oldNameTag = api.getNametag(player);
			lastSuffix = oldNameTag.getSuffix();
			if (AFK_SUFFIX.contains(lastSuffix))
				lastSuffix = lastSuffix.replace(AFK_SUFFIX, "");
			OlympaAPIPermissions.AFK_SEE_IN_TAB.getPlayers(players -> api.updateFakeNameTag(player, new Nametag(oldNameTag.getPrefix(), lastSuffix + AFK_SUFFIX), players));*/
			api.callNametagUpdate(AccountProvider.get(player.getUniqueId()));
		}
	}

	public void setNotAfk(Player player) {
		afk = false;
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu n'es plus &2AFK&a.");
		launchTask(player);
		INametagApi api = OlympaCore.getInstance().getNameTagApi();
		if (api != null) {
			//OlympaAPIPermissions.AFK_SEE_IN_TAB.getPlayers(players -> api.updateFakeNameTag(player, new Nametag(api.getNametag(player).getPrefix(), getLastSuffix()), players));
			api.callNametagUpdate(AccountProvider.get(player.getUniqueId()));
		}
	}

	private String getLastSuffix() {
		return lastSuffix != null ? lastSuffix : " ";
	}

	public boolean isAfk() {
		return afk;
	}

	public String getLastAction() {
		return lastAction;
	}

	public long getTime() {
		return time;
	}

	public void toggleAfk(Player player) {
		afk = !afk;
		if (afk)
			setAfk(player);
		else
			setNotAfk(player);
	}

}
