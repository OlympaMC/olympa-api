package fr.olympa.api.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class TaskManager implements OlympaTask {

	protected Plugin plugin;
	private HashMap<String, Integer> taskList = new HashMap<>();

	public TaskManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void addTask(String name, int id) {
		this.taskList.put(name, id);
	}

	@Override
	public void cancelAllTask() {
		BukkitScheduler scheduler = Bukkit.getScheduler();
		for (int taskId : this.taskList.values()) {
			scheduler.cancelTask(taskId);
		}
		scheduler.cancelTasks(this.plugin);
	}

	@Override
	public void cancelTaskById(int id) {
		Bukkit.getScheduler().cancelTask(id);
	}

	@Override
	public boolean cancelTaskByName(String taskName) {
		if (this.taskExist(taskName)) {
			int taskId = this.getBukkitTaskId(taskName);
			this.taskList.remove(taskName);
			Bukkit.getScheduler().cancelTask(taskId);
			return true;
		}
		return false;
	}

	@Override
	public void checkIfExist(String taskName) {
		if (this.taskExist(taskName)) {
			this.cancelTaskByName(taskName);
		}
	}

	public BukkitTask getBukkitTask(int id) {
		BukkitTask task = null;
		if (id > 0) {
			for (BukkitTask pendingTask : Bukkit.getScheduler().getPendingTasks()) {
				if (pendingTask.getTaskId() == id) {
					return task;
				}
			}
		}
		return null;
	}

	public BukkitTask getBukkitTask(String taskName) {
		return this.getBukkitTask(this.getBukkitTaskId(taskName));
	}

	public int getBukkitTaskId(String taskName) {
		if (this.taskExist(taskName)) {
			return this.taskList.get(taskName);
		}
		return 0;
	}

	public String getTaskName(String string) {
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); this.taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}

	public String getTaskNameById(int id) {
		for (Map.Entry<String, Integer> entry : this.taskList.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void removeTaskByName(String taskName) {
		this.taskList.remove(taskName);
	}

	@Override
	public int runTask(Runnable runnable) {
		return Bukkit.getScheduler().runTask(this.plugin, runnable).getTaskId();
	}

	@Override
	public int runTaskAsynchronously(Runnable runnable) {
		return Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable).getTaskId();
	}

	@Override
	public int runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getBukkitTask(oldTask).cancel();
		}
		int bukkitTask = this.runTaskAsynchronously(runnable);
		this.addTask(taskName, bukkitTask);
		return bukkitTask;
	}

	@Override
	public int runTaskLater(Runnable runnable, int tick) {
		return Bukkit.getScheduler().runTaskLater(this.plugin, runnable, tick).getTaskId();
	}

	@Override
	public int runTaskLater(String taskName, Runnable task, int duration) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getBukkitTask(oldTask).cancel();
		}
		BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this.plugin, task, duration);
		int id = bukkitTask.getTaskId();
		this.addTask(taskName, id);
		this.runTaskLater(() -> {
			if (this.taskList.get(taskName) != null && this.taskList.get(taskName) == id) {
				this.taskList.remove(taskName);
			}
		}, duration);
		return bukkitTask.getTaskId();
	}

	@Override
	public int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh) {
		this.cancelTaskByName(taskName);
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delay, refresh);
		this.taskList.put(taskName, task.getTaskId());
		return task.getTaskId();
	}

	@Override
	public boolean taskExist(String taskName) {
		return this.taskList.containsKey(taskName);
	}
}
