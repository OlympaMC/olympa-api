package fr.olympa.api.spigot.economy.fluctuating;

import java.util.concurrent.TimeUnit;

public class FixedFluctuatingEconomy extends FluctuatingEconomy {
	
	private double min;
	private double downFactor;
	private double upValue;
	private long delayMillis;
	
	public FixedFluctuatingEconomy(String id, double base, double min, long timeBetween, TimeUnit timeUnit, double downFactor, double upValue) {
		super(id, base);
		this.min = min;
		this.downFactor = downFactor;
		this.upValue = upValue;
		delayMillis = timeUnit.toMillis(timeBetween);
	}
	
	@Override
	public double getMin() {
		return min;
	}
	
	@Override
	protected long nextUpdateDelayMillis() {
		return delayMillis;
	}
	
	@Override
	protected double processUpValue() {
		return value.get() + upValue;
	}
	
	@Override
	protected double processNewValue(double used) {
		return value.get() - used * downFactor;
	}
	
	public void use(double amount) {
		if (value.get() == min) return;
		value.set(Math.max(min, value.get() - amount * downFactor));
	}
	
}
