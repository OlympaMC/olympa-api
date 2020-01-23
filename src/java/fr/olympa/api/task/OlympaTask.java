package fr.olympa.api.task;

import org.bukkit.scheduler.BukkitTask;

public interface OlympaTask {

	void addTask(String name, int id);

	void cancelAllTask();

	void cancelTaskById(int id);

	boolean cancelTaskByName(String taskName);

	void checkIfExist(String taskName);

	BukkitTask getTask(int id);

	BukkitTask getTask(String taskName);

	int getTaskId(String taskName);

	String getTaskName(String string);

	String getTaskNameById(int id);

	void removeTaskByName(String taskName);

	BukkitTask runTask(Runnable runnable);

	BukkitTask runTaskAsynchronously(Runnable runnable);

	BukkitTask runTaskAsynchronously(String taskName, Runnable runnable);

	BukkitTask runTaskLater(Runnable runnable, int tick);

	BukkitTask runTaskLater(String taskName, Runnable task, int duration);

	BukkitTask scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh);

	boolean taskExist(String taskName);

}