package fr.olympa.api.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public interface OlympaTask {

	Map<String, Integer> taskList = new HashMap<>();

	default void addTask(String name, int id) {
		taskList.put(name, id);
	}

	default boolean taskExist(String taskName) {
		return taskList.containsKey(taskName);
	}

	default boolean taskExist(int id) {
		return taskList.entrySet().stream().anyMatch(e -> e.getValue() == id);
	}

	/**
	 * Non recommandé, supprime l'entrée de la task, ne l'arrête pas
	 * @param id
	 */
	default void removeTaskByName(String taskName) {
		taskList.remove(taskName);
	}

	/**
	 * Non recommandé, supprime l'entrée de la task, ne l'arrête pas
	 * @param id
	 */
	default void removeTaskById(int id) {
		String task = getTaskNameById(id);
		if (task != null)
			taskList.remove(task);
	}

	default String getTaskNameById(int id) {
		return taskList.entrySet().stream().filter(e -> e.getValue() == id).map(Entry::getKey).findFirst().orElse(null);
	}

	default Integer getTaskIdByName(String taskName) {
		return taskList.get(taskName);
	}

	default boolean cancelTaskByName(String taskName) {
		if (taskExist(taskName)) {
			cancelTaskById(taskList.get(taskName));
			removeTaskByName(taskName);
			return true;
		}
		return false;
	}

	default void checkIfExist(String taskName) {
		if (taskExist(taskName))
			cancelTaskByName(taskName);
	}

	default String getUniqueTaskName(String string) {
		String taskName;
		do
			taskName = string + "_" + new Random().nextInt(99999);
		while (taskExist(taskName));
		return taskName;
	}

	default void cancelAllTask() {
		for (int taskId : taskList.values())
			cancelTaskById(taskId);
		taskList.clear();
	}

	void cancelTaskById(int id);

	int runTask(Runnable runnable);

	int runTaskAsynchronously(Runnable runnable);

	int runTaskAsynchronously(String taskName, Runnable runnable);

	@Deprecated(forRemoval = true)
	default int runTaskLater(Runnable runnable, long tick) {
		return runTaskLater(runnable, tick * 50, TimeUnit.MILLISECONDS);
	}

	@Deprecated(forRemoval = true)
	default int runTaskLater(String taskName, Runnable runnable, long tick) {
		return runTaskLater(taskName, runnable, tick * 50, TimeUnit.MILLISECONDS);
	}

	int runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit);

	int runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit);

	@Deprecated(forRemoval = true)
	default int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh) {
		return scheduleSyncRepeatingTask(runnable, delay * 50, refresh * 50, TimeUnit.MILLISECONDS);
	}

	int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh, TimeUnit timeUnit);

	int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit);

	Object getTask(int id);

	default Object getTask(String taskName) {
		return getTask(getTaskIdByName(taskName));
	}

}