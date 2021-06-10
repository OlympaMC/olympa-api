package fr.olympa.api.bungee.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.common.task.OlympaTask;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeTaskManager implements OlympaTask {

	protected Plugin plugin;
	Map<Integer, ScheduledTask> taskList2 = new HashMap<>();

	public BungeeTaskManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void removeTaskById(int id) {
		String taskName = getTaskNameById(id);
		if (taskName != null)
			taskList.remove(taskName);
		ScheduledTask task = taskList2.get(id);
		if (task != null)
			taskList2.remove(id);
	}

	@Override
	public void cancelTaskById(int id) {
		getScheduler().cancel(id);
		removeTaskById(id);
	}

	@Override
	public void checkIfExist(String taskName) {
		if (taskExist(taskName))
			cancelTaskByName(taskName);
	}

	@Override
	public ScheduledTask getTask(int id) {
		if (id > 0 && id < taskList2.size())
			return taskList2.get(id);
		return null;
	}

	@Override
	public Object getTask(String taskName) {
		return getTask(getTaskIdByName(taskName));
	}

	@Override
	public int runTask(Runnable runnable) {
		return getScheduler().schedule(plugin, runnable, 0, TimeUnit.SECONDS).getId();
	}

	@Override
	public int runTaskAsynchronously(Runnable runnable) {
		return runTaskAsynchronously(getUniqueTaskName("runTaskAsynchronously"), runnable);
	}

	@Override
	public int runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTaskId = taskList.get(taskName);
		if (oldTaskId != null)
			getScheduler().cancel(oldTaskId);
		ScheduledTask task = getScheduler().runAsync(plugin, runnable);
		taskList.put(taskName, task.getId());
		taskList2.put(task.getId(), task);
		return task.getId();
	}

	@Override
	public int runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
		return runTaskLaterAndGet(runnable, delay, timeUnit).getId();
	}

	public ScheduledTask runTaskLaterAndGet(Runnable runnable, long delay, TimeUnit timeUnit) {
		return getScheduler().schedule(plugin, runnable, delay, timeUnit);
	}

	@Override
	public int runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit) {
		Integer oldTaskId = taskList.get(taskName);
		if (oldTaskId != null)
			getScheduler().cancel(oldTaskId);
		ScheduledTask schTask = getScheduler().schedule(plugin, runnable, delay, timeUnit);
		int id = schTask.getId();
		taskList.put(taskName, id);
		taskList2.put(id, schTask);
		this.runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName) == id)
				taskList.remove(taskName);
		}, delay, timeUnit);
		return schTask.getId();
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		return getScheduler().schedule(plugin, runnable, delay, refresh, timeUnit).getId();
	}

	public ScheduledTask scheduleSyncRepeatingTaskAndGet(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		ScheduledTask task = getScheduler().schedule(plugin, runnable, delay, refresh, timeUnit);
		taskList.put(taskName, task.getId());
		taskList2.put(task.getId(), task);
		return task;
	}

	@Override
	public int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		return scheduleSyncRepeatingTaskAndGet(taskName, runnable, delay, refresh, timeUnit).getId();
	}

	private TaskScheduler getScheduler() {
		return plugin.getProxy().getScheduler();
	}

}
