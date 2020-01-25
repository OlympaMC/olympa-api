package fr.olympa.api.task;

public interface OlympaTask {

	void addTask(String name, int id);

	void cancelAllTask();

	void cancelTaskById(int id);

	boolean cancelTaskByName(String taskName);

	void checkIfExist(String taskName);

	void removeTaskByName(String taskName);

	int runTask(Runnable runnable);

	int runTaskAsynchronously(Runnable runnable);

	int runTaskAsynchronously(String taskName, Runnable runnable);

	int runTaskLater(Runnable runnable, int tick);

	int runTaskLater(String taskName, Runnable task, int duration);

	int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh);

	boolean taskExist(String taskName);

}