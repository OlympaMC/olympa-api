package fr.tristiisch.olympa.api.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager {
	private static HashMap<String, Integer> taskList = new HashMap<>();
	public static BukkitScheduler scheduler;
	static Plugin plugin;

	public static void addTask(final String name, final int id) {
		TaskManager.taskList.put(name, id);
	}

	public static void cancelAllTask() {
		for (final int taskId : TaskManager.taskList.values()) {
			TaskManager.scheduler.cancelTask(taskId);
		}
		scheduler.cancelAllTasks();
	}

	public static void cancelTaskById(final int id) {
		TaskManager.scheduler.cancelTask(id);
	}

	public static boolean cancelTaskByName(final String taskName) {
		if (taskExist(taskName)) {
			final int taskId = getTaskId(taskName);
			TaskManager.taskList.remove(taskName);
			TaskManager.scheduler.cancelTask(taskId);
			return true;
		}
		return false;
	}

	public static void checkIfExist(final String taskName) {
		if (taskExist(taskName)) {
			cancelTaskByName(taskName);
		}
	}

	public static BukkitTask getTask(final int id) {
		final BukkitTask task = null;
		if (id > 0) {
			for (final BukkitTask pendingTask : TaskManager.scheduler.getPendingTasks()) {
				if (pendingTask.getTaskId() == id) {
					return task;
				}
			}
		}
		return null;
	}

	public static BukkitTask getTask(final String taskName) {
		return getTask(getTaskId(taskName));
	}

	public static int getTaskId(final String taskName) {
		if (taskExist(taskName)) {
			return TaskManager.taskList.get(taskName);
		}
		return 0;
	}

	public static String getTaskName(final String string) {
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}

	public static String getTaskNameById(final int id) {
		for (final Map.Entry<String, Integer> entry : TaskManager.taskList.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static void removeTaskByName(final String taskName) {
		TaskManager.taskList.remove(taskName);
	}

	public static BukkitTask runTask(final Runnable runnable) {
		return TaskManager.scheduler.runTask(TaskManager.plugin, runnable);
	}

	public static BukkitTask runTaskAsynchronously(final Runnable runnable) {
		return TaskManager.scheduler.runTaskAsynchronously(TaskManager.plugin, runnable);
	}

	public static BukkitTask runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTask = TaskManager.taskList.get(taskName);
		if (oldTask != null) {
			getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = runTaskAsynchronously(runnable);
		addTask(taskName, bukkitTask.getTaskId());
		return bukkitTask;
	}

	public static BukkitTask runTaskLater(final Runnable runnable, final int tick) {
		return TaskManager.scheduler.runTaskLater(TaskManager.plugin, runnable, tick);
	}

	public static BukkitTask runTaskLater(final String taskName, final Runnable task, final int duration) {
		Integer oldTask = TaskManager.taskList.get(taskName);
		if (oldTask != null) {
			getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = TaskManager.scheduler.runTaskLater(TaskManager.plugin, task, duration);
		final int id = bukkitTask.getTaskId();
		addTask(taskName, id);
		runTaskLater(() -> {
			if (TaskManager.taskList.get(taskName) != null && TaskManager.taskList.get(taskName) == id) {
				TaskManager.taskList.remove(taskName);
			}
		}, duration);
		return bukkitTask;
	}

	public static BukkitTask scheduleSyncRepeatingTask(final String taskName, final Runnable runnable, final long delay, final long refresh) {
		cancelTaskByName(taskName);
		final BukkitTask task = TaskManager.scheduler.runTaskTimer(TaskManager.plugin, runnable, delay, refresh);
		TaskManager.taskList.put(taskName, task.getTaskId());
		return task;
	}

	public static boolean taskExist(final String taskName) {
		return TaskManager.taskList.containsKey(taskName);
	}

	public TaskManager(Plugin plugin) {
		TaskManager.plugin = plugin;
		TaskManager.scheduler = plugin.getServer().getScheduler();
	}
}
