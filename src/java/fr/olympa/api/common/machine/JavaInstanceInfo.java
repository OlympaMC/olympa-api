package fr.olympa.api.common.machine;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.sun.management.OperatingSystemMXBean;

import fr.olympa.api.spigot.utils.TPSUtils;
import fr.olympa.api.utils.Utils;

public class JavaInstanceInfo {

	@Expose
	private Long memFree, memUsed, memeTotal, allThreadsCreated, cpuProcTime;
	@Expose
	private Double cpuProcUsage, cpuSysUsage, memUsage;
	@Expose
	private Integer cores, threads;
	@Expose
	private Long time;

	public JavaInstanceInfo() {
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

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threads = threadMXBean.getThreadCount();
		allThreadsCreated = threadMXBean.getTotalStartedThreadCount();
	}

	@Nullable
	public Long getAllThreadsCreated() {
		return allThreadsCreated;
	}

	@Nullable
	public Integer getCPUSysCore() {
		return cores;
	}

	@Nullable
	public String getCPUUsage() {
		if (cpuProcUsage == null)
			return null;
		return TPSUtils.getCPUUsageColor((int) Math.round(cpuProcUsage)) + "%";
	}

	@Nullable
	public String getCPUSysUsage() {
		if (cpuSysUsage == null)
			return null;
		return TPSUtils.getCPUUsageColor((int) Math.round(cpuSysUsage)) + "%";
	}

	@Nullable
	public Long getMemFree() {
		return memFree;
	}

	@Nullable
	public Long getMemTotal() {
		return memeTotal;
	}

	@Nullable
	public String getMemUsage() {
		if (memUsage == null)
			return null;
		return TPSUtils.getRamUsageColor((int) Math.round(memUsage)) + "%";
	}

	@Nullable
	public Long getRawMemUsage() {
		if (memUsage == null)
			return null;
		return Math.round(memUsage);
	}

	@Nullable
	public String getMemUse() {
		if (memeTotal == null || memUsed == null)
			return null;
		if (memeTotal >= 1024L) {
			DecimalFormat format = new DecimalFormat("0.#");
			return format.format(memUsed / 1024f) + "/" + format.format(memeTotal / 1024f) + "Go";
		}
		return memUsed + "/" + memeTotal + "Mo";
	}

	@Nullable
	public String getCPUProcTime() {
		if (cpuProcTime == null)
			return null;
		DecimalFormat format = new DecimalFormat("0.#");
		if (cpuProcTime > 3_600_000_000_000L)
			return format.format(cpuProcTime / 3_600_000_000_000f) + "heures";
		if (cpuProcTime > 60_000_000_000L)
			return format.format(cpuProcTime / 60_000_000_000f) + "minutes";
		if (cpuProcTime > 1_000_000_000L)
			return format.format(cpuProcTime / 1_000_000_000f) + "sec";
		if (cpuProcTime > 1_000_000L)
			return format.format(cpuProcTime / 1_000_000f) + "ms";
		return cpuProcTime + "ns";
	}

	@Nullable
	public Long getMemUsed() {
		return memUsed;
	}

	@Nullable
	public Integer getThreads() {
		return threads;
	}

	@Nullable
	public Long getTime() {
		return time;
	}

	@Nullable
	public Double getCpuProcUsage() {
		return cpuProcUsage;
	}

	public boolean hasInstanceInfo() {
		return memUsed != null && threads != null && cpuProcTime != null && cpuSysUsage != null && cores != null;
	}

}
