package fr.olympa.api.common.randomized;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.IntFunction;

import fr.olympa.api.utils.Utils;

public interface RandomValueProvider<T> {
	
	public T pickOne(Random random);
	
	public class UniformProvider implements RandomValueProvider<Integer> {
		private int min;
		private int max;
		
		public UniformProvider(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		@Override
		public Integer pickOne(Random random) {
			return Utils.getRandomAmount(random, min, max);
		}
	}
	
	public class FunctionProvider implements RandomValueProvider<Integer> {
		
		private TreeMap<Double, Integer> values = new TreeMap<>();
		private double min, max;
		
		/**
		 * Constructs an integer provider
		 * @param min
		 * @param max
		 * @param chance a function asociating an integer from min to max (inclusive)
		 * 					to a double between 0 (inclusive) and 1 (exclusive)
		 */
		public FunctionProvider(int min, int max, IntFunction<Double> chance) {
			for (int i = min; i <= max + 1; i++) {
				values.put(chance.apply(i), i);
			}
			
			this.min = values.keySet().stream().mapToDouble(Double::doubleValue).min().getAsDouble();
			this.max = values.keySet().stream().mapToDouble(Double::doubleValue).max().getAsDouble();
		}
		
		@Override
		public Integer pickOne(Random random) {
			double randomValue = random.nextDouble() * (max - min) + min;
			int value;
			Iterator<Entry<Double, Integer>> iterator = values.entrySet().iterator();
			do {
				Entry<Double, Integer> entry = iterator.next();
				value = entry.getValue();
				if (entry.getKey().doubleValue() >= randomValue) break;
			}while (iterator.hasNext());
			return value;
		}
		
	}
	
}
