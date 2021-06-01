package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

public interface RandomizedPickerBase<T> {
	
	public static <T> INormalBuilder<T> newBuilder() {
		return new PickerBuilder<>();
	}
	
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
	
	public default double getEmptyChance() {
		return 0;
	}
	
	public abstract IBuilder<T> createBuilder();
	
	public interface RandomizedMultiPickerBase<T> extends RandomizedPickerBase<T> {
		
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
		
		public abstract List<T> getAlwaysObjectList();
		
		public abstract int getMinItems();
		
		public abstract int getMaxItems();
		
	}
	
	public interface RandomizedPicker<T> extends RandomizedPickerBase<T> {
		
		@Override
		default INormalBuilderBase<T> createBuilder() {
			return new PickerBuilder<>(getObjectList());
		}
		
	}
	
	public interface RandomizedMultiPicker<T> extends RandomizedPicker<T>, RandomizedMultiPickerBase<T> {
		
		@Override
		default INormalBuilderBase<T> createBuilder() {
			return new PickerMultiBuilder<>(getObjectList(), getAlwaysObjectList());
		}
		
	}
	
	public interface Conditioned<T, C extends ConditionalContext> {
		
		public T getObject();
		
		public boolean isValid(C context);
		
		public boolean isValidWithNoContext();
		
		class EmptyConditioned<T, C extends ConditionalContext> implements Conditioned<T, C> {
			
			private T object;
			
			public EmptyConditioned(T object) {
				this.object = object;
			}
			
			@Override
			public T getObject() {
				return object;
			}
			
			@Override
			public boolean isValid(C context) {
				return true;
			}
			
			@Override
			public boolean isValidWithNoContext() {
				return true;
			}
			
		}
		
	}
	
	public class ConditionalContext {
		
	}
	
	public interface ConditionalPickerBase<T, C extends ConditionalContext> extends RandomizedPickerBase<T> {
		
		@Override
		public abstract IConditionalBuilderBase<T, C> createBuilder();
		
	}
	
	public interface ConditionalPicker<T, C extends ConditionalContext> extends RandomizedPickerBase<T> {
		
		@Override
		default T pickOne(Random random) {
			return pickOne(random, null);
		}
		
		public default T pickOne(Random random, C context) {
			return pick(random, getConditionedObjectsList().entrySet().stream().filter(entry -> {
				Conditioned<T, C> conditioned = entry.getKey();
				
				return context == null ? conditioned.isValidWithNoContext() : conditioned.isValid(context);
			}).collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue())), getEmptyChance());
		}
		
		public abstract Map<Conditioned<T, C>, Double> getConditionedObjectsList();
		
		@Override
		default Map<T, Double> getObjectList() {
			return getConditionedObjectsList().entrySet().stream().collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue()));
		}
		
		@Override
		default IConditionalBuilderBase<T, C> createBuilder() {
			return new ConditionalPickerBuilder<>(getConditionedObjectsList());
		}
		
	}
	
	public interface ConditionalMultiPicker<T, C extends ConditionalContext> extends ConditionalPicker<T, C>, RandomizedMultiPickerBase<T> {
		
		@Override
		default List<T> pickMulti(Random random) {
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
		
		public abstract List<Conditioned<T, C>> getConditionedAlwaysObjects();
		
		@Override
		default List<T> getAlwaysObjectList() {
			return getConditionedAlwaysObjects().stream().map(Conditioned<T, C>::getObject).collect(Collectors.toList());
		}
		
		@Override
		default IConditionalMultiBuilder<T, C> createBuilder() {
			return new ConditionalPickerMultiBuilder<>(getConditionedObjectsList(), getConditionedAlwaysObjects());
		}
		
	}
	
	public class FixedPicker<T> implements RandomizedPicker<T> {
		
		private Map<T, Double> values;
		private double emptyChance;
		
		public FixedPicker(Map<T, Double> values, double emptyChance) {
			this.values = values;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<T, Double> getObjectList() {
			return values;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
	}
	
	public class FixedMultiPicker<T> extends FixedPicker<T> implements RandomizedMultiPicker<T> {
		
		private List<T> valuesAlways;
		private int min;
		private int max;
		
		public FixedMultiPicker(Map<T, Double> values, List<T> valuesAlways, int min, int max, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.min = min;
			this.max = max;
		}
		
		@Override
		public List<T> getAlwaysObjectList() {
			return valuesAlways;
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
	
	public class FixedConditionalPicker<T, C extends ConditionalContext> implements ConditionalPicker<T, C> {
		
		private Map<Conditioned<T, C>, Double> values;
		private double emptyChance;
		
		public FixedConditionalPicker(Map<Conditioned<T, C>, Double> values, double emptyChance) {
			this.values = values;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<Conditioned<T, C>, Double> getConditionedObjectsList() {
			return values;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
	}
	
	public class FixedConditionalMultiPicker<T, C extends ConditionalContext> extends FixedConditionalPicker<T, C> implements ConditionalMultiPicker<T, C> {
		
		private List<Conditioned<T, C>> valuesAlways;
		private int min;
		private int max;
		
		public FixedConditionalMultiPicker(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, int min, int max, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.min = min;
			this.max = max;
		}
		
		@Override
		public List<Conditioned<T, C>> getConditionedAlwaysObjects() {
			return valuesAlways;
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
	
	public interface IBuilder<T> {}
	
	public interface IBuilderBase<T> extends IBuilder<T> {
		
		RandomizedPickerBase<T> build(double emptyChance);
		
		RandomizedPickerBase<T> build();
		
		IBuilderBase<T> clone();
		
	}
	
	public interface IMultiBuilderBase<T> extends IBuilder<T> {
		
		RandomizedMultiPickerBase<T> build(int min, int max, double emptyChance);
		
		RandomizedMultiPickerBase<T> build(int min, int max);
		
		IMultiBuilderBase<T> clone();
		
	}
	
	public interface INormalBuilderBase<T> extends IBuilder<T> {
		
		INormalBuilderBase<T> add(double chance, T value);
		
		INormalMultiBuilder<T> addAlways(T value);
		
		<C extends ConditionalContext> IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		<C extends ConditionalContext> IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value);
		
		INormalBuilderBase<T> clone();
		
	}
	
	public interface INormalMultiBuilder<T> extends INormalBuilderBase<T>, IMultiBuilderBase<T> {
		
		INormalMultiBuilder<T> add(double chance, T value);
		
		RandomizedMultiPicker<T> build(int min, int max, double emptyChance);
		
		RandomizedMultiPicker<T> build(int min, int max);
		
		INormalMultiBuilder<T> clone();
		
	}
	
	public interface INormalBuilder<T> extends INormalMultiBuilder<T>, IBuilderBase<T> {
		
		INormalBuilder<T> add(double chance, T value);
		
		RandomizedPicker<T> build(double emptyChance);
		
		RandomizedPicker<T> build();
		
		INormalBuilder<T> clone();
		
	}
	
	public interface IConditionalBuilderBase<T, C extends ConditionalContext> extends IBuilder<T> {
		
		IConditionalBuilderBase<T, C> add(double chance, T value);
		
		IConditionalBuilderBase<T, C> add(double chance, Conditioned<T, C> value);
		
		IConditionalMultiBuilder<T, C> addAlways(T value);
		
		IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value);
		
		IConditionalBuilderBase<T, C> clone();
		
	}
	
	public interface IConditionalMultiBuilder<T, C extends ConditionalContext> extends IConditionalBuilderBase<T, C>, IMultiBuilderBase<T> {
		
		IConditionalMultiBuilder<T, C> add(double chance, T value);
		
		IConditionalMultiBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		ConditionalMultiPicker<T, C> build(int min, int max, double emptyChance);
		
		ConditionalMultiPicker<T, C> build(int min, int max);
		
		IConditionalMultiBuilder<T, C> clone();
		
	}
	
	public interface IConditionalBuilder<T, C extends ConditionalContext> extends IConditionalMultiBuilder<T, C>, IBuilderBase<T> {
		
		IConditionalBuilder<T, C> add(double chance, T value);
		
		IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		ConditionalPicker<T, C> build(double emptyChance);
		
		ConditionalPicker<T, C> build();
		
		IConditionalBuilder<T, C> clone();
		
	}
	
	abstract class PickerBuilderBase<T> implements INormalBuilderBase<T> {
		
		protected Map<T, Double> values = new HashMap<>();
		
		public PickerBuilderBase() {}
		
		public PickerBuilderBase(Map<T, Double> values) {
			this.values.putAll(values);
		}
		
		@Override
		public INormalBuilderBase<T> add(double chance, T object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		@Override
		public <C extends ConditionalContext> IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> object) {
			ConditionalPickerBuilder<T, C> picker = new ConditionalPickerBuilder<>();
			values.forEach((obj, ochance) -> picker.add(ochance, obj));
			return picker.add(chance, object);
		}
		
		@Override
		public <C extends ConditionalContext> IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> object) {
			ConditionalPickerMultiBuilder<T, C> picker = new ConditionalPickerMultiBuilder<>();
			values.forEach((obj, ochance) -> picker.add(ochance, obj));
			return picker.addAlways(object);
		}
		
		public abstract PickerBuilderBase<T> clone();
		
	}
	
	public class PickerBuilder<T> extends PickerBuilderBase<T> implements INormalBuilder<T> {
		
		protected PickerBuilder() {}
		
		protected PickerBuilder(Map<T, Double> values) {
			super(values);
		}
		
		@Override
		public INormalBuilder<T> add(double chance, T object) {
			return (INormalBuilder<T>) super.add(chance, object);
		}
		
		@Override
		public INormalMultiBuilder<T> addAlways(T object) {
			return new PickerMultiBuilder<>(values, Collections.emptyList()).addAlways(object);
		}
		
		@Override
		public RandomizedPicker<T> build(double emptyChance) {
			return new FixedPicker<>(values, emptyChance);
		}
		
		@Override
		public RandomizedPicker<T> build() {
			return new FixedPicker<>(values, 0);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max, double emptyChance) {
			return new FixedMultiPicker<>(values, Collections.emptyList(), min, max, emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max) {
			return new FixedMultiPicker<>(values, Collections.emptyList(), min, max, 0);
		}
		
		@Override
		public PickerBuilder<T> clone() {
			return new PickerBuilder<>(values);
		}
		
	}
	
	public class PickerMultiBuilder<T> extends PickerBuilderBase<T> implements INormalMultiBuilder<T> {
		
		private List<T> valuesAlways = new ArrayList<>();
		
		public PickerMultiBuilder() {}
		
		public PickerMultiBuilder(Map<T, Double> values, List<T> valuesAlways) {
			super(values);
			this.valuesAlways.addAll(valuesAlways);
		}
		
		@Override
		public INormalMultiBuilder<T> add(double chance, T object) {
			return (INormalMultiBuilder<T>) super.add(chance, object);
		}
		
		@Override
		public INormalMultiBuilder<T> addAlways(T object) {
			valuesAlways.add(object);
			return this;
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max, double emptyChance) {
			return new FixedMultiPicker<>(values, valuesAlways, min, max, emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max) {
			return new FixedMultiPicker<>(values, valuesAlways, min, max, 0);
		}
		
		@Override
		public PickerMultiBuilder<T> clone() {
			return new PickerMultiBuilder<>(values, valuesAlways);
		}
		
	}
	
	abstract class ConditionalPickerBuilderBase<T, C extends ConditionalContext> implements IConditionalBuilderBase<T, C> {
		
		protected Map<Conditioned<T, C>, Double> values = new HashMap<>();
		
		public ConditionalPickerBuilderBase() {}
		
		public ConditionalPickerBuilderBase(Map<Conditioned<T, C>, Double> values) {
			this.values.putAll(values);
		}
		
		@Override
		public IConditionalBuilderBase<T, C> add(double chance, T object) {
			values.put(new Conditioned.EmptyConditioned<>(object), chance);
			return this;
		}
		
		@Override
		public IConditionalBuilderBase<T, C> add(double chance, Conditioned<T, C> object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		public abstract ConditionalPickerBuilderBase<T, C> clone();
		
	}
	
	public class ConditionalPickerBuilder<T, C extends ConditionalContext> extends ConditionalPickerBuilderBase<T, C> implements IConditionalBuilder<T, C> {
		
		public ConditionalPickerBuilder() {}
		
		public ConditionalPickerBuilder(Map<Conditioned<T, C>, Double> values) {
			super(values);
		}
		
		@Override
		public IConditionalBuilder<T, C> add(double chance, T object) {
			return (IConditionalBuilder<T, C>) super.add(chance, object);
		}
		
		@Override
		public IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> object) {
			return (IConditionalBuilder<T, C>) super.add(chance, object);
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> addAlways(T object) {
			return new ConditionalPickerMultiBuilder<>(values, Collections.emptyList()).addAlways(object);
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value) {
			return new ConditionalPickerMultiBuilder<>(values, Collections.emptyList()).addAlways(value);
		}
		
		@Override
		public ConditionalPicker<T, C> build(double emptyChance) {
			return new FixedConditionalPicker<>(values, emptyChance);
		}
		
		@Override
		public ConditionalPicker<T, C> build() {
			return new FixedConditionalPicker<>(values, 0);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max, double emptyChance) {
			return new FixedConditionalMultiPicker<>(values, Collections.emptyList(), min, max, emptyChance);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max) {
			return new FixedConditionalMultiPicker<>(values, Collections.emptyList(), min, max, 0);
		}
		
		@Override
		public ConditionalPickerBuilder<T, C> clone() {
			return new ConditionalPickerBuilder<>(values);
		}
		
	}
	
	public class ConditionalPickerMultiBuilder<T, C extends ConditionalContext> extends ConditionalPickerBuilderBase<T, C> implements IConditionalMultiBuilder<T, C> {
		
		private List<Conditioned<T, C>> valuesAlways = new ArrayList<>();
		
		public ConditionalPickerMultiBuilder() {}
		
		public ConditionalPickerMultiBuilder(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways) {
			super(values);
			this.valuesAlways.addAll(valuesAlways);
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> add(double chance, T object) {
			return (IConditionalMultiBuilder<T, C>) super.add(chance, object);
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> add(double chance, Conditioned<T, C> object) {
			return (IConditionalMultiBuilder<T, C>) super.add(chance, object);
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> addAlways(T value) {
			valuesAlways.add(new Conditioned.EmptyConditioned<>(value));
			return this;
		}
		
		@Override
		public IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value) {
			valuesAlways.add(value);
			return this;
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max, double emptyChance) {
			return new FixedConditionalMultiPicker<>(values, valuesAlways, min, max, emptyChance);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max) {
			return new FixedConditionalMultiPicker<>(values, valuesAlways, min, max, 0);
		}
		
		@Override
		public ConditionalPickerMultiBuilder<T, C> clone() {
			return new ConditionalPickerMultiBuilder<>(values, valuesAlways);
		}
		
	}
	
}
