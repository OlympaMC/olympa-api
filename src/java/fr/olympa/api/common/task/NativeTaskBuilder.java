package fr.olympa.api.common.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.common.observable.Observable;

public class NativeTaskBuilder {

	private static NativeTask instanceNativeTask = NativeTask.getInstance();

	Runnable runnable;
	//Callback callBack;
	int delay;
	int refresh;
	TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	boolean isLinkedToPlugin = false;
	boolean async;

	String taskName;
	int taskId;

	List<?> listObserve;
	boolean isRunning;
	boolean manualStop = false;

	public NativeTaskBuilder(Runnable runnable) {
		this.runnable = runnable;
	}

	/**
	 * If list is not instance of Observable, don't foget to use {@link #valueChanged() when adding object to paramter {@code list}.
	 * @param list
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public NativeTaskBuilder toggleOnObserve(List list) {
		listObserve = list;
		if (list instanceof Observable)
			((Observable) list).observe(toString(), this::valueChanged);
		return this;
	}

	public void cancel() {
		isRunning = false;
	}

	public void cancelManual() {
		manualStop = true;
		cancel();
	}

	public void start() {
		manualStop = false;
		taskId = 0;
		if (async)
			if (taskName != null)
				taskId = instanceNativeTask.runTaskAsynchronously(taskName, runnable);
			else
				taskId = instanceNativeTask.runTaskAsynchronously(runnable);
		else if (taskName != null)
			taskId = instanceNativeTask.runTaskLater(taskName, runnable, delay, timeUnit);
		else
			taskId = instanceNativeTask.runTask(runnable);
		isRunning = taskId != 0;
	}

	public void valueChanged() {
		if (listObserve == null)
			return;
		int size = listObserve.size();
		if (size == 0)
			cancel();
		else if (size == 1 && !isRunning && !manualStop)
			start();
	}
}
