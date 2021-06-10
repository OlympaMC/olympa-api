package fr.olympa.api.common.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NativeTask implements OlympaTask {

	private static final NativeTask INSTANCE = new NativeTask();

	public static NativeTask getInstance() {
		return INSTANCE;
	}

	private int taskId = 1;

	private Map<Integer, UniversalTask> tasks = new HashMap<>();
	private Map<String, Integer> tasksNames = new HashMap<>();

	@Override
	public boolean cancelTaskByName(String taskName) {
		Integer id = tasksNames.remove(taskName);
		UniversalTask task;
		if (id != null) {
			task = tasks.get(id);
			if (task != null)
				return task.cancel(false);
		}
		return false;
	}

	@Override
	public void cancelTaskById(int id) {
		UniversalTask task = tasks.remove(id);
		if (task != null)
			task.cancel(false);
		Entry<String, Integer> taskNameEntry = tasksNames.entrySet().stream().filter(entry -> entry.getValue() == id).findFirst().orElse(null);
		if (taskNameEntry != null)
			tasksNames.remove(taskNameEntry.getKey(), taskNameEntry.getValue());
	}

	/**
	 * As {@link #cancelTaskByName(String)} but with killing the current task if running
	 */
	public boolean terminateTaskByName(String taskName) {
		Integer id = tasksNames.remove(taskName);
		UniversalTask task;
		if (id != null) {
			task = tasks.get(id);
			if (task != null)
				return task.cancel(true);
		}
		return false;
	}

	/**
	 * As {@link #cancelTaskById(int)} but with killing the current task if running
	 */
	public void terminateTaskById(int id) {
		UniversalTask task = tasks.remove(id);
		if (task != null)
			task.cancel(true);
		Entry<String, Integer> taskNameEntry = tasksNames.entrySet().stream().filter(entry -> entry.getValue() == id).findFirst().orElse(null);
		if (taskNameEntry != null)
			tasksNames.remove(taskNameEntry.getKey(), taskNameEntry.getValue());
	}

	public void runTaskNewThread(Runnable runnable) {
		Executors.newSingleThreadScheduledExecutor().schedule(() -> runnable.run(), 0, TimeUnit.SECONDS);
	}

	@Override
	public int runTask(Runnable runnable) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					tasks.remove(taskId);
				}
			}
		};
		tasks.put(taskId, new UniversalTask(task));
		timer.schedule(task, 0);
		return taskId++;
	}

	@Override
	public int runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					tasks.remove(taskId);
				}
			}
		};
		tasks.put(taskId, new UniversalTask(task));
		timer.schedule(task, timeUnit.toMillis(delay));
		return taskId++;
	}

	@Override
	public int runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					tasksNames.remove(taskName);
					tasks.remove(taskId);
				}
			}
		};
		tasksNames.put(taskName, taskId);
		tasks.put(taskId, new UniversalTask(task));
		timer.schedule(task, timeUnit.toMillis(delay));
		return taskId++;
	}

	@Override
	public int runTaskAsynchronously(Runnable runnable) {
		tasks.put(taskId, new UniversalTask(CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} finally {
				tasks.remove(taskId);
			}
		})));
		return taskId++;
	}

	public long runTaskAsynchronouslyNewThread(Runnable runnable) {
		tasks.put(taskId, new UniversalTask(CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} finally {
				tasks.remove(taskId);
			}
		}, Executors.newSingleThreadScheduledExecutor())));
		return taskId++;
	}

	@Override
	public int runTaskAsynchronously(String taskName, Runnable runnable) {
		cancelTaskByName(taskName);
		tasksNames.put(taskName, taskId);
		tasks.put(taskId, new UniversalTask(CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} finally {
				tasks.remove(taskId);
				tasksNames.remove(taskName);
			}
		})));
		return taskId++;
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					tasks.remove(taskId);
				}
			}
		};
		tasks.put(taskId, new UniversalTask(task));
		timer.schedule(task, timeUnit.toMillis(delay), timeUnit.toMillis(refresh));
		return taskId++;
	}

	@Override
	public int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					tasks.remove(taskId);
					tasksNames.remove(taskName);
				}
			}
		};
		tasksNames.put(taskName, taskId);
		tasks.put(taskId, new UniversalTask(task));
		timer.schedule(task, timeUnit.toMillis(delay), timeUnit.toMillis(refresh));
		return taskId++;
	}

	@Override
	public UniversalTask getTask(int id) {
		return tasks.get(id);
	}

	private class UniversalTask {
		CompletableFuture<?> completableFuture;
		TimerTask timerTask;

		public UniversalTask(CompletableFuture<?> completableFuture) {
			this.completableFuture = completableFuture;
		}

		public UniversalTask(TimerTask timerTask) {
			this.timerTask = timerTask;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			if (completableFuture != null)
				return completableFuture.cancel(mayInterruptIfRunning);
			else if (timerTask != null)
				return timerTask.cancel();
			return false;

		}
	}
}
