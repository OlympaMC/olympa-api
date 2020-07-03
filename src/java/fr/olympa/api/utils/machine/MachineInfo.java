package fr.olympa.api.utils.machine;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

import fr.olympa.api.utils.spigot.TPSUtils;

public class MachineInfo {

	private long memFree, memUsed, memeTotal, allThreadsCreated;
	private double cpuUsage, memUsage;
	private int cores, threads, availableProcessors;

	public MachineInfo() {
		Runtime r = Runtime.getRuntime();
		memUsed = r.totalMemory() / 1048576L;
		memFree = r.freeMemory() / 1048576L;
		memeTotal = r.maxMemory() / 1048576L;
		memUsage = (double) memUsed / (double) memeTotal * 100.0;
		availableProcessors = r.availableProcessors();

		OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
		cpuUsage = osMXBean.getSystemLoadAverage();
		cores = osMXBean.getAvailableProcessors();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threads = threadMXBean.getThreadCount();

		allThreadsCreated = threadMXBean.getTotalStartedThreadCount();

	}

	public long getMemeTotal() {
		return memeTotal;
	}

	public long getAllThreadsCreated() {
		return allThreadsCreated;
	}

	public int getCores() {
		return cores;
	}

	public String getCPUUsage() {
		return TPSUtils.getCPUUsageColor((int) Math.round(cpuUsage)) + "%";
	}

	public long getMemFree() {
		return memFree;
	}

	public long getMemTotal() {
		return memeTotal;
	}

	public String getMemUsage() {
		return TPSUtils.getRamUsageColor((int) Math.round(memUsage)) + "%";
	}

	public String getMemUse() {
		return memUsed + "/" + memeTotal + "Mo";
	}

	public long getMemUsed() {
		return memUsed;
	}

	public int getThreads() {
		return threads;
	}

}
