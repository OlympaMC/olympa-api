package fr.tristiisch.olympa.api.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager {
	public static BukkitScheduler scheduler = Bukkit.getScheduler();
	
	private Plugin plugin;
	private HashMap<String, Integer> taskList = new HashMap<>();
	
	public TaskManager(Plugin plugin){
		this.plugin = plugin;
	}

	public void addTask(final String name, final int id){
		taskList.put(name, id);
	}

	public void cancelAllTask(){
		for (final int taskId : taskList.values()) {
			scheduler.cancelTask(taskId);
		}
		scheduler.cancelAllTasks();
	}

	public void cancelTaskById(final int id){
		scheduler.cancelTask(id);
	}

	public boolean cancelTaskByName(final String taskName){
		if (taskExist(taskName)) {
			final int taskId = getTaskId(taskName);
			taskList.remove(taskName);
			scheduler.cancelTask(taskId);
			return true;
		}
		return false;
	}

	public void removeTaskByName(final String taskName){
		taskList.remove(taskName);
	}
	
	public boolean taskExist(final String taskName){
		return taskList.containsKey(taskName);
	}
	
	public void checkIfExist(final String taskName){
		if (taskExist(taskName)) {
			cancelTaskByName(taskName);
		}
	}

	public BukkitTask getTask(final int id){
		final BukkitTask task = null;
		if (id > 0) {
			for (final BukkitTask pendingTask : scheduler.getPendingTasks()) {
				if (pendingTask.getTaskId() == id) {
					return task;
				}
			}
		}
		return null;
	}

	public BukkitTask getTask(final String taskName){
		return getTask(getTaskId(taskName));
	}

	public int getTaskId(final String taskName){
		if (taskExist(taskName)) {
			return taskList.get(taskName);
		}
		return 0;
	}

	public String getTaskName(final String string){
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}

	public String getTaskNameById(final int id){
		for (final Map.Entry<String, Integer> entry : taskList.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		return null;
	}

	public BukkitTask runTask(final Runnable runnable){
		return scheduler.runTask(plugin, runnable);
	}

	public BukkitTask runTaskAsynchronously(final Runnable runnable){
		return scheduler.runTaskAsynchronously(plugin, runnable);
	}

	public BukkitTask runTaskAsynchronously(String taskName, Runnable runnable){
		Integer oldTask = taskList.get(taskName);
		if (oldTask != null) {
			getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = runTaskAsynchronously(runnable);
		addTask(taskName, bukkitTask.getTaskId());
		return bukkitTask;
	}

	public BukkitTask runTaskLater(final Runnable runnable, final int tick){
		return scheduler.runTaskLater(plugin, runnable, tick);
	}

	public BukkitTask runTaskLater(final String taskName, final Runnable task, final int duration){
		Integer oldTask = taskList.get(taskName);
		if (oldTask != null) {
			getTask(oldTask).cancel();
		}
		final BukkitTask bukkitTask = scheduler.runTaskLater(plugin, task, duration);
		final int id = bukkitTask.getTaskId();
		addTask(taskName, id);
		runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName) == id) {
				taskList.remove(taskName);
			}
		}, duration);
		return bukkitTask;
	}

	public BukkitTask scheduleSyncRepeatingTask(final String taskName, final Runnable runnable, final long delay, final long refresh){
		cancelTaskByName(taskName);
		final BukkitTask task = scheduler.runTaskTimer(plugin, runnable, delay, refresh);
		taskList.put(taskName, task.getTaskId());
		return task;
	}
}
