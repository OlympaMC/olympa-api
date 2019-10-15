package fr.olympa.api.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager {

	private Plugin plugin;
	private HashMap<String, Integer> taskList = new HashMap<>();

	public TaskManager(Plugin plugin) {
		this.plugin = plugin;

	}

	public void addTask(final String name, final int id) {
		this.taskList.put(name, id);
	}

	public void cancelAllTask() {
		BukkitScheduler scheduler = Bukkit.getScheduler();
		for (final int taskId : this.taskList.values()) {
			scheduler.cancelTask(taskId);
		}
		scheduler.cancelAllTasks();
	}

	public void cancelTaskById(final int id) {
		Bukkit.getScheduler().cancelTask(id);
	}

	public boolean cancelTaskByName(final String taskName) {
		if (this.taskExist(taskName)) {
			final int taskId = this.getTaskId(taskName);
			this.taskList.remove(taskName);
			Bukkit.getScheduler().cancelTask(taskId);
			return true;
		}
		return false;
	}

	public void checkIfExist(final String taskName) {
		if (this.taskExist(taskName)) {
			this.cancelTaskByName(taskName);
		}
	}

	public BukkitTask getTask(final int id) {
		final BukkitTask task = null;
		if (id > 0) {
			for (final BukkitTask pendingTask : Bukkit.getScheduler().getPendingTasks()) {
				if (pendingTask.getTaskId() == id) {
					return task;
				}
			}
		}
		return null;
	}

	public BukkitTask getTask(final String taskName) {
		return this.getTask(this.getTaskId(taskName));
	}

	public int getTaskId(final String taskName) {
		if (this.taskExist(taskName)) {
			return this.taskList.get(taskName);
		}
		return 0;
	}

	public String getTaskName(final String string) {
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); this.taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}

	public String getTaskNameById(final int id) {
		for (final Map.Entry<String, Integer> entry : this.taskList.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void removeTaskByName(final String taskName) {
		this.taskList.remove(taskName);
	}

	public BukkitTask runTask(final Runnable runnable) {
		return Bukkit.getScheduler().runTask(this.plugin, runnable);
	}

	public BukkitTask runTaskAsynchronously(final Runnable runnable) {
		return Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
	}

	public BukkitTask runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = this.runTaskAsynchronously(runnable);
		this.addTask(taskName, bukkitTask.getTaskId());
		return bukkitTask;
	}

	public BukkitTask runTaskLater(final Runnable runnable, final int tick) {
		return Bukkit.getScheduler().runTaskLater(this.plugin, runnable, tick);
	}

	public BukkitTask runTaskLater(final String taskName, final Runnable task, final int duration) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this.plugin, task, duration);
		final int id = bukkitTask.getTaskId();
		this.addTask(taskName, id);
		this.runTaskLater(() -> {
			if (this.taskList.get(taskName) != null && this.taskList.get(taskName) == id) {
				this.taskList.remove(taskName);
			}
		}, duration);
		return bukkitTask;
	}

	public BukkitTask scheduleSyncRepeatingTask(final String taskName, final Runnable runnable, final long delay, final long refresh) {
		this.cancelTaskByName(taskName);
		final BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delay, refresh);
		this.taskList.put(taskName, task.getTaskId());
		return task;
	}

	public boolean taskExist(final String taskName) {
		return this.taskList.containsKey(taskName);
	}
}
