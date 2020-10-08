package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.olympa.api.utils.RandomizedPicker.Chanced;

public interface RandomizedPicker<T extends Chanced> {

	public default List<T> pick(Random random) {
		int itemAmount = Utils.getRandomAmount(random, getMinItems(), getMaxItems());

		List<T> objects = new ArrayList<>(itemAmount);

		for (T obj : getAlwaysObjectList()) {
			objects.add(obj);
			itemAmount--;
		}
		if (itemAmount <= 0) return objects;

		double objectsChanceSum = getObjectList().stream().mapToDouble(T::getChance).sum() + getEmptyChance();
		for (int dropCount = 0; dropCount < itemAmount; dropCount++) {
			double hitValue = objectsChanceSum * random.nextDouble();
			double runningValue = 0;
			for (T obj : getObjectList()) {
				runningValue += obj.getChance();
				if (hitValue < runningValue) {
					objects.add(obj);
					break;
				}
			}
		}
		return objects;
	}

	public abstract int getMinItems();

	public abstract int getMaxItems();

	public abstract List<T> getObjectList();

	public abstract List<T> getAlwaysObjectList();
	
	public default double getEmptyChance() {
		return 0;
	}

	public interface Chanced {
		/**
		 * @return chance que l'évenement soit arrive. <tt>-1</tt> si c'est un évenement obligatoire.
		 */
		public abstract double getChance();
	}
	
	public class FixedPicker<T extends Chanced> implements RandomizedPicker<T> {
		
		private int min, max;
		private double emptyChance;
		private List<T> objects = new ArrayList<>();
		private List<T> always = new ArrayList<>();
		
		public FixedPicker(int min, int max, double emptyChance, T... objects) {
			this.min = min;
			this.max = max;
			this.emptyChance = emptyChance;
			for (T obj : objects) {
				if (obj.getChance() == -1) {
					this.always.add(obj);
				}else this.objects.add(obj);
			}
		}
		
		@Override
		public int getMinItems() {
			return min;
		}
		
		@Override
		public int getMaxItems() {
			return max;
		}
		
		@Override
		public List<T> getObjectList() {
			return objects;
		}
		
		@Override
		public List<T> getAlwaysObjectList() {
			return always;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
	}

}
