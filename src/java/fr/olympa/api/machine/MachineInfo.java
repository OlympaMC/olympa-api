package fr.olympa.api.machine;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;

import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPSUtils;

public class MachineInfo {

	private long memFree, memUsed, memeTotal, allThreadsCreated, cpuProcTime;
	private double cpuProcUsage, cpuSysUsage, memUsage;
	private int cores, threads;
	private long time;

	public MachineInfo() {
		time = Utils.getCurrentTimeInSeconds();
		Runtime r = Runtime.getRuntime();
		//		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		//		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() ;
		memUsed = r.totalMemory() / 1048576L;
		memFree = r.freeMemory() / 1048576L;
		memeTotal = r.maxMemory() / 1048576L;
		memUsage = (double) memUsed / (double) memeTotal * 100.0;

		OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		//		cpuSysUsage = osMXBean.getSystemLoadAverage();
		cores = osMXBean.getAvailableProcessors();
		cpuProcTime = osMXBean.getProcessCpuTime();

		cpuProcUsage = osMXBean.getProcessCpuLoad() * 100;
		cpuSysUsage = osMXBean.getSystemCpuLoad() * 100;

		ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
		threads = threadMXBean.getThreadCount();
		allThreadsCreated = threadMXBean.getTotalStartedThreadCount();
	}

	public long getMemeTotal() {
		return memeTotal;
	}

	public long getAllThreadsCreated() {
		return allThreadsCreated;
	}

	public int getCPUSysCore() {
		return cores;
	}

	public String getCPUUsage() {
		return TPSUtils.getCPUUsageColor((int) Math.round(cpuProcUsage)) + "%";
	}

	public String getCPUSysUsage() {
		return TPSUtils.getCPUUsageColor((int) Math.round(cpuSysUsage)) + "%";
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

	public long getRawMemUsage() {
		return Math.round(memUsage);
	}

	public String getMemUse() {
		if (memeTotal > 1024d)
			return memUsed / 1024d + "/" + memeTotal / 1024d + "Go";
		return memUsed + "/" + memeTotal + "Mo";
	}

	public String getCPUProcTime() {
		if (cpuProcTime < 1000000)
			return cpuProcTime + "ns";
		if (cpuProcTime < 1000000000)
			return cpuProcTime + "ms";
		return cpuProcTime + "sec";
	}

	public long getMemUsed() {
		return memUsed;
	}

	public int getThreads() {
		return threads;
	}

	public long getTime() {
		return time;
	}
}
