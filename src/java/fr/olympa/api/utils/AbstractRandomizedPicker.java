package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.olympa.api.utils.AbstractRandomizedPicker.Chanced;

public interface AbstractRandomizedPicker<T extends Chanced> {

	public default List<T> pick(Random random) {
		List<T> objects = new ArrayList<>(getMaxItems());

		int itemAmount = getMinItems() + random.nextInt(getMaxItems() - getMinItems() + 1);
		for (T obj : getAlwaysObjectList()) {
			objects.add(obj);
			itemAmount--;
		}
		if (itemAmount <= 0) return objects;

		int objectsChanceSum = 0;
		for (T obj : getObjectList()) objectsChanceSum += obj.getChance();
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

	public interface Chanced {
		/**
		 * @return chance que l'évenement soit arrive. <tt>-1</tt> si c'est un évenement obligatoire.
		 */
		public abstract double getChance();
	}

}
