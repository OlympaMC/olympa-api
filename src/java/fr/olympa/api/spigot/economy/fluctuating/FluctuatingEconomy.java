package fr.olympa.api.spigot.economy.fluctuating;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.observable.ObservableDouble;
import fr.olympa.api.common.observable.ObservableLong;

public abstract class FluctuatingEconomy {
	
	private final String id;
	private final double base;
	
	protected final ObservableDouble value;
	protected final ObservableLong nextUp;
	
	private BukkitTask task;
	
	protected FluctuatingEconomy(String id, double base) {
		this.id = id;
		this.base = base;
		
		value = new ObservableDouble(base);
		nextUp = new ObservableLong(0);
	}
	
	protected void start(Plugin plugin) {
		long timeLeft = nextUp.get() - System.currentTimeMillis();
		long delay = timeLeft < 0 ? 0 : timeLeft / 50;
		start(plugin, delay);
	}
	
	private void start(Plugin plugin, long delay) {
		Runnable runnable = () -> {
			task = null;
			if (value.get() < base) {
				value.set(processUpValue());
				if (value.get() < base) {
					long delayMillis = nextUpdateDelayMillis();
					nextUp.set(System.currentTimeMillis() + delayMillis);
					start(plugin, delayMillis / 50);
				}else nextUp.set(0);
			}
		};
		if (delay == 0) {
			runnable.run();
		}else task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
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
	
	public synchronized void use(double amount) {
		if (value.get() <= getMin()) return;
		value.set(Math.max(getMin(), processNewValue(amount)));
	}
	
	public abstract double getMin();
	
	protected abstract long nextUpdateDelayMillis();
	
	protected abstract double processUpValue();
	
	protected abstract double processNewValue(double used);
	
}