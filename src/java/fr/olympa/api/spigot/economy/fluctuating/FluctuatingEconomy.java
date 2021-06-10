package fr.olympa.api.spigot.economy.fluctuating;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.observable.ObservableDouble;
import fr.olympa.api.common.observable.ObservableLong;

public abstract class FluctuatingEconomy {
	
	private final String id;
	private final double base;
	private final double min;
	private final long timeBetween;
	private final TimeUnit timeUnit;
	private final double downFactor;
	private final double upValue;
	
	protected final ObservableDouble value;
	protected final ObservableLong nextUp;
	
	private BukkitTask task;
	
	protected FluctuatingEconomy(String id, double base, double min, long timeBetween, TimeUnit timeUnit, double downFactor, double upValue) {
		this.id = id;
		this.base = base;
		this.min = min;
		this.timeBetween = timeBetween;
		this.timeUnit = timeUnit;
		this.downFactor = downFactor;
		this.upValue = upValue;
		
		value = new ObservableDouble(base);
		nextUp = new ObservableLong(0);
	}
	
	protected void start(Plugin plugin) {
		long timeLeft = nextUp.get() - System.currentTimeMillis();
		long period = timeUnit.toMillis(timeBetween) / 50L;
		long delay = timeLeft < 0 ? 0 : timeLeft / 50;
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			if (value.get() < base) {
				value.set(Math.min(base, value.get() + upValue));
				if (value.get() < base) {
					nextUp.set(System.currentTimeMillis() + timeUnit.toMillis(timeBetween));
				}else nextUp.set(0);
			}
		}, delay, period);
	}
	
	public void stop() {
		if (task != null) task.cancel();
	}
	
	public String getId() {
		return id;
	}
	
	public double getBase() {
		return base;
	}
	
	public double getValue() {
		return value.get();
	}
	
	public void use(double amount) {
		if (value.get() == min) return;
		value.set(Math.max(min, value.get() - amount * downFactor));
	}
	
	protected abstract long processNextUpdate();
	
	protected abstract double processNewValue(double used);
	
}