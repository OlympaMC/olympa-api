package fr.olympa.api.common.task;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTaskManager implements OlympaTask {

	protected Plugin plugin;

	public BukkitTaskManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void cancelAllTask() {
		BukkitScheduler scheduler = getScheduler();
		for (int taskId : taskList.values())
			scheduler.cancelTask(taskId);
		scheduler.cancelTasks(plugin);
	}

	@Override
	public void cancelTaskById(int id) {
		getScheduler().cancelTask(id);
	}

	@Override
	public boolean cancelTaskByName(String taskName) {
		if (taskExist(taskName)) {
			taskList.remove(taskName);
			BukkitTask task = (BukkitTask) getTask(taskName);
			if (task != null) {
				int taskId = task.getTaskId();
				cancelTaskById(taskId);
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkIfExist(String taskName) {
		if (taskExist(taskName))
			cancelTaskByName(taskName);
	}

	@Override
	public BukkitTask getTask(int id) {
		BukkitTask task = null;
		if (id > 0)
			for (BukkitTask pendingTask : getScheduler().getPendingTasks())
				if (pendingTask.getTaskId() == id)
					return task;
		return null;
	}

	@Override
	public int runTask(Runnable runnable) {
		return getScheduler().runTask(plugin, runnable).getTaskId();
	}

	@Override
	public int runTaskAsynchronously(Runnable runnable) {
		return getScheduler().runTaskAsynchronously(plugin, runnable).getTaskId();
	}

	@Override
	public int runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTask = taskList.get(taskName);
		if (oldTask != null)
			getTask(oldTask).cancel();
		int bukkitTask = this.runTaskAsynchronously(runnable);
		addTask(taskName, bukkitTask);
		return bukkitTask;
	}

	@Override
	public int runTaskLater(Runnable runnable, long delay) {
		return getScheduler().runTaskLater(plugin, runnable, delay).getTaskId();
	}

	@Override
	public int runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
		return runTaskLater(runnable, timeUnit.toMillis(delay) / 50l);
	}

	@Override
	public int runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit) {
		Integer oldTask = taskList.get(taskName);
		if (oldTask != null)
			getTask(oldTask).cancel();
		int taskId = runTaskLater(runnable, delay, timeUnit);
		addTask(taskName, taskId);
		this.runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName) == taskId)
				taskList.remove(taskName);
		}, delay, timeUnit);
		return taskId;
	}

	@Override
	public int runTaskLater(String taskName, Runnable runnable, long delay) {
		Integer oldTask = taskList.get(taskName);
		if (oldTask != null)
			getTask(oldTask).cancel();
		int taskId = runTaskLater(runnable, delay);
		addTask(taskName, taskId);
		this.runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName) == taskId)
				taskList.remove(taskName);
		}, delay);
		return taskId;
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		return scheduleSyncRepeatingTask(runnable, timeUnit.toMillis(delay) / 50l, timeUnit.toMillis(refresh) / 50l);
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh) {
		return getScheduler().runTaskTimer(plugin, runnable, delay, refresh).getTaskId();
	}

	@Override
	public int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		int taskId = scheduleSyncRepeatingTask(runnable, delay, refresh, timeUnit);
		addTask(taskName, taskId);
		return taskId;
	}

	private BukkitScheduler getScheduler() {
		return plugin.getServer().getScheduler();
	}

}
