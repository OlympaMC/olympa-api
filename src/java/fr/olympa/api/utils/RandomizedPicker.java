package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

public interface RandomizedPicker<T> {
	
	public static <T> T pick(Random random, Map<T, Double> objects, double emptyChance) {
		return pick(random, objects, emptyChance, objects.values().stream().mapToDouble(Double::doubleValue).sum() + emptyChance);
	}
	
	public static <T> T pick(Random random, Map<T, Double> objects, double emptyChance, double objectsChanceSum) {
		double hitValue = objectsChanceSum * random.nextDouble();
		double runningValue = 0;
		for (Entry<T, Double> obj : objects.entrySet()) {
			runningValue += obj.getValue().doubleValue();
			if (hitValue < runningValue) return obj.getKey();
		}
		return null;
	}

	public default T pickOne(Random random) {
		return pick(random, getObjectList(), getEmptyChance());
	}

	public abstract Map<T, Double> getObjectList();

	public abstract List<T> getAlwaysObjectList();
	
	public default double getEmptyChance() {
		return 0;
	}
	
	public default IPickerBuilder<T> createBuilder(){
		return new PickerBuilder<>(getObjectList(), getAlwaysObjectList());
	}
	
	public interface RandomizedMultiPicker<T> extends RandomizedPicker<T> {
		
		public default List<T> pickMulti(Random random) {
			int itemAmount = Utils.getRandomAmount(random, getMinItems(), getMaxItems());
			
			List<T> pickeds = new ArrayList<>(itemAmount);
			
			for (T obj : getAlwaysObjectList()) {
				pickeds.add(obj);
				itemAmount--;
			}
			if (itemAmount <= 0) return pickeds;
			
			Map<T, Double> objects = getObjectList();
			double objectsChanceSum = objects.values().stream().mapToDouble(Double::doubleValue).sum() + getEmptyChance();
			for (int dropCount = 0; dropCount < itemAmount; dropCount++) {
				T picked = pick(random, objects, getEmptyChance(), objectsChanceSum);
				if (picked != null) pickeds.add(picked);
			}
			return pickeds;
		}
		
		public abstract int getMinItems();
		
		public abstract int getMaxItems();
		
	}
	
	public interface Conditioned<T> {
		
		public T getObject();
		
		public Class<?> getArgumentType();
		
		public boolean isValid(@Nullable Object arg);
		
		class EmptyConditioned<T> implements Conditioned<T> {
			
			private T object;
			
			public EmptyConditioned(T object) {
				this.object = object;
			}
			
			@Override
			public T getObject() {
				return object;
			}
			
			@Override
			public Class<?> getArgumentType() {
				return null;
			}
			
			@Override
			public boolean isValid(@Nullable Object arg) {
				return true;
			}
			
		}
		
	}
	
	public interface ConditionalPicker<T> extends RandomizedPicker<T>{
		
		public default T pickOne(Random random, Object... args) {
			return pick(random, getConditionedObjectsList().entrySet().stream().filter(entry -> {
				Conditioned<T> conditioned = entry.getKey();
				if (conditioned.getArgumentType() == null) return conditioned.isValid(null);
				
				for (Object arg : args) {
					if (conditioned.getArgumentType().isInstance(arg)) return conditioned.isValid(arg);
				}
				
				return conditioned.isValid(null);
			}).collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue())), getEmptyChance());
		}
		
		public abstract Map<Conditioned<T>, Double> getConditionedObjectsList();
		
		public abstract List<Conditioned<T>> getConditionedAlwaysObjects();
		
		@Override
		default Map<T, Double> getObjectList() {
			return getConditionedObjectsList().entrySet().stream().collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue()));
		}
		
		@Override
		default List<T> getAlwaysObjectList() {
			return getConditionedAlwaysObjects().stream().map(Conditioned<T>::getObject).collect(Collectors.toList());
		}
		
		@Override
		default IPickerBuilder<T> createBuilder() {
			return new ConditionalPickerBuilder<>(getConditionedObjectsList(), getConditionedAlwaysObjects());
		}
		
	}
	
	public class FixedPicker<T> implements RandomizedPicker<T> {
		
		private Map<T, Double> values;
		private List<T> valuesAlways;
		private double emptyChance;
		
		public FixedPicker(Map<T, Double> values, List<T> valuesAlways, double emptyChance) {
			this.values = values;
			this.valuesAlways = valuesAlways;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<T, Double> getObjectList() {
			return values;
		}
		
		@Override
		public List<T> getAlwaysObjectList() {
			return valuesAlways;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
	}
	
	public class FixedMultiPicker<T> extends FixedPicker<T> implements RandomizedMultiPicker<T> {
		
		private int min;
		private int max;
		
		public FixedMultiPicker(Map<T, Double> values, List<T> valuesAlways, int min, int max, double emptyChance) {
			super(values, valuesAlways, emptyChance);
			this.min = min;
			this.max = max;
		}
		
		@Override
		public int getMinItems() {
			return min;
		}
		
		@Override
		public int getMaxItems() {
			return max;
		}
		
	}
	
	public class FixedConditionalPicker<T> implements ConditionalPicker<T> {
		
		private Map<Conditioned<T>, Double> values;
		private List<Conditioned<T>> valuesAlways;
		private double emptyChance;
		
		public FixedConditionalPicker(Map<Conditioned<T>, Double> values, List<Conditioned<T>> valuesAlways, double emptyChance) {
			this.values = values;
			this.valuesAlways = valuesAlways;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<Conditioned<T>, Double> getConditionedObjectsList() {
			return values;
		}
		
		@Override
		public List<Conditioned<T>> getConditionedAlwaysObjects() {
			return valuesAlways;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
	}
	
	public interface IPickerBuilder<T> {
		
		IPickerBuilder<T> add(double chance, T value);
		
		ConditionalPickerBuilder<T> add(double chance, Conditioned<T> value);
		
		IPickerBuilder<T> addAlways(T value);
		
		ConditionalPickerBuilder<T> addAlways(Conditioned<T> value);
		
		RandomizedPicker<T> build(double emptyChance);
		
		RandomizedPicker<T> build();
		
	}
	
	public class PickerBuilder<T> implements IPickerBuilder<T> {
		private Map<T, Double> values = new HashMap<>();
		private List<T> valuesAlways = new ArrayList<>();
		
		public PickerBuilder() {}
		
		public PickerBuilder(Map<T, Double> values, List<T> valuesAlways) {
			this.values.putAll(values);
			this.valuesAlways.addAll(valuesAlways);
		}
		
		@Override
		public PickerBuilder<T> add(double chance, T object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		@Override
		public ConditionalPickerBuilder<T> add(double chance, Conditioned<T> object) {
			return toConditionalBuilder().add(chance, object);
		}
		
		@Override
		public PickerBuilder<T> addAlways(T object) {
			valuesAlways.add(object);
			return this;
		}
		
		@Override
		public ConditionalPickerBuilder<T> addAlways(Conditioned<T> object) {
			return toConditionalBuilder().addAlways(object);
		}
		
		public RandomizedMultiPicker<T> build(int min, int max, double emptyChance) {
			return new FixedMultiPicker<>(values, valuesAlways, min, max, emptyChance);
		}
		
		public RandomizedMultiPicker<T> build(int min, int max) {
			return new FixedMultiPicker<>(values, valuesAlways, min, max, 0);
		}
		
		@Override
		public RandomizedPicker<T> build(double emptyChance){
			return new FixedPicker<>(values, valuesAlways, emptyChance);
		}
		
		@Override
		public RandomizedPicker<T> build() {
			return new FixedPicker<>(values, valuesAlways, 0);
		}
		
		@Override
		public PickerBuilder<T> clone() {
			return new PickerBuilder<>(values, valuesAlways);
		}
		
		public ConditionalPickerBuilder<T> toConditionalBuilder() {
			ConditionalPickerBuilder<T> picker = new ConditionalPickerBuilder<>();
			values.forEach((obj, ochance) -> picker.add(ochance, obj));
			valuesAlways.forEach(obj -> picker.addAlways(obj));
			return picker;
		}
	}
	
	public class ConditionalPickerBuilder<T> implements IPickerBuilder<T> {
		private Map<Conditioned<T>, Double> values = new HashMap<>();
		private List<Conditioned<T>> valuesAlways = new ArrayList<>();
		
		public ConditionalPickerBuilder() {}
		
		public ConditionalPickerBuilder(Map<Conditioned<T>, Double> values, List<Conditioned<T>> valuesAlways) {
			this.values = values;
			this.valuesAlways = valuesAlways;
		}
		
		@Override
		public ConditionalPickerBuilder<T> add(double chance, T object) {
			values.put(new Conditioned.EmptyConditioned<>(object), chance);
			return this;
		}
		
		@Override
		public ConditionalPickerBuilder<T> add(double chance, Conditioned<T> object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		@Override
		public ConditionalPickerBuilder<T> addAlways(T object) {
			valuesAlways.add(new Conditioned.EmptyConditioned<>(object));
			return this;
		}
		
		@Override
		public ConditionalPickerBuilder<T> addAlways(Conditioned<T> object) {
			valuesAlways.add(object);
			return this;
		}
		
		@Override
		public ConditionalPicker<T> build(double emptyChance) {
			return new FixedConditionalPicker<>(values, valuesAlways, emptyChance);
		}
		
		@Override
		public ConditionalPicker<T> build() {
			return new FixedConditionalPicker<>(values, valuesAlways, 0);
		}
		
		@Override
		public ConditionalPickerBuilder<T> clone() {
			return new ConditionalPickerBuilder<>(values, valuesAlways);
		}
	}
	
}
