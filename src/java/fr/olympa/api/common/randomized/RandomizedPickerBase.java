package fr.olympa.api.common.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public interface RandomizedPickerBase<T> extends RandomValueProvider<T> {
	
	public static <T> T pickWithEmpty(Random random, Map<T, Double> objects, double emptyChance) {
		return pick(random, objects, objects.values().stream().mapToDouble(Double::doubleValue).sum() + emptyChance);
	}
	
	public static <T> T pick(Random random, Map<T, Double> objects, double objectsChanceSum) {
		double hitValue = objectsChanceSum * random.nextDouble();
		double runningValue = 0;
		for (Entry<T, Double> obj : objects.entrySet()) {
			runningValue += obj.getValue().doubleValue();
			if (hitValue < runningValue) return obj.getKey();
		}
		return null;
	}
	
	@Override
	public default T pickOne(Random random) {
		return pickWithEmpty(random, getObjectList(), getEmptyChance());
	}
	
	public abstract Map<T, Double> getObjectList();
	
	public default double getEmptyChance() {
		return 0;
	}
	
	public abstract RandomizedPickerBuilder.IBuilder<T> createBuilder();
	
	public interface RandomizedMultiPickerBase<T> extends RandomizedPickerBase<T> {
		
		public default List<T> pickMulti(Random random) {
			int itemAmount = getAmountProvider().pickOne(random);
			
			List<T> pickeds = new ArrayList<>(itemAmount);
			
			for (T obj : getAlwaysObjectList()) {
				pickeds.add(obj);
				itemAmount--;
			}
			if (itemAmount <= 0) return pickeds;
			
			Map<T, Double> objects = getObjectList();
			double objectsChanceSum = objects.values().stream().mapToDouble(Double::doubleValue).sum() + getEmptyChance();
			for (int dropCount = 0; dropCount < itemAmount; dropCount++) {
				T picked = pick(random, objects, objectsChanceSum);
				if (picked != null) pickeds.add(picked);
			}
			return pickeds;
		}
		
		public abstract List<T> getAlwaysObjectList();
		
		public abstract RandomValueProvider<Integer> getAmountProvider();
		
	}
	
	public interface RandomizedPicker<T> extends RandomizedPickerBase<T> {
		
		@Override
		default RandomizedPickerBuilder.INormalBuilderBase<T> createBuilder() {
			return new RandomizedPickerBuilder.PickerBuilder<>(getObjectList());
		}
		
	}
	
	public interface RandomizedMultiPicker<T> extends RandomizedPicker<T>, RandomizedMultiPickerBase<T> {
		
		@Override
		default RandomizedPickerBuilder.INormalBuilderBase<T> createBuilder() {
			return new RandomizedPickerBuilder.PickerMultiBuilder<>(getObjectList(), getAlwaysObjectList());
		}
		
	}
	
	public interface Conditioned<T, C extends ConditionalContext<T>> {
		
		public T getObject();
		
		public boolean isValid(C context);
		
		public boolean isValidWithNoContext();
		
		public default boolean mustMultiBeRecomputed() {
			return false;
		}
		
		class EmptyConditioned<T, C extends ConditionalContext<T>> implements Conditioned<T, C> {
			
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
			
			@Override
			public int hashCode() {
				return object.hashCode();
			}
			
			@Override
			public boolean equals(Object o) {
				if (!(o instanceof EmptyConditioned)) return false;
				return Objects.equals(object, ((EmptyConditioned<T, C>) o).object);
			}
			
		}
		
	}
	
	public class ConditionalContext<T> {
		
		protected List<T> picked = new ArrayList<>();
		
		public void addPicked(T picked) {
			this.picked.add(picked);
		}
		
	}
	
	public interface ConditionalPickerBase<T, C extends ConditionalContext<T>> extends RandomizedPickerBase<T> {
		
		@Override
		public abstract RandomizedPickerBuilder.IConditionalBuilderBase<T, C> createBuilder();
		
	}
	
	public interface ConditionalPicker<T, C extends ConditionalContext<T>> extends RandomizedPickerBase<T> {
		
		@Override
		default T pickOne(Random random) {
			return pickOne(random, null);
		}
		
		public default T pickOne(Random random, C context) {
			return pickWithEmpty(random, getAppliedObjects(context), getEmptyChance());
		}
		
		public default Map<T, Double> getAppliedObjects(C context) {
			return getConditionedObjectsList().entrySet().stream().filter(entry -> {
				Conditioned<T, C> conditioned = entry.getKey();
				
				return context == null ? conditioned.isValidWithNoContext() : conditioned.isValid(context);
			}).collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue(), Double::sum));
		}
		
		public abstract Map<Conditioned<T, C>, Double> getConditionedObjectsList();
		
		/**
		 * @deprecated {@link #getConditionedObjectsList()}
		 */
		@Override
		@Deprecated (forRemoval = false)
		default Map<T, Double> getObjectList() {
			return getConditionedObjectsList().entrySet().stream().collect(Collectors.toMap(x -> x.getKey().getObject(), x -> x.getValue()));
		}
		
		@Override
		default RandomizedPickerBuilder.IConditionalBuilderBase<T, C> createBuilder() {
			return new RandomizedPickerBuilder.ConditionalPickerBuilder<>(getConditionedObjectsList());
		}
		
	}
	
	public interface ConditionalMultiPicker<T, C extends ConditionalContext<T>> extends ConditionalPicker<T, C>, RandomizedMultiPickerBase<T> {
		
		@Deprecated
		@Override
		default List<T> pickMulti(Random random) {
			return pickMulti(random, null);
		}
		
		default List<T> pickMulti(Random random, C context) {
			int itemAmount = getAmountProvider().pickOne(random);
			
			List<T> pickeds = new ArrayList<>(itemAmount);
			
			for (T obj : getAlwaysObjectList()) {
				pickeds.add(obj);
				itemAmount--;
			}
			if (itemAmount <= 0) return pickeds;
			
			Map<T, Double> objects = null;
			double objectsChanceSum = 0;
			boolean mustRecompute = getConditionedObjectsList().keySet().stream().anyMatch(Conditioned::mustMultiBeRecomputed);
			for (int dropCount = 0; dropCount < itemAmount; dropCount++) {
				if (dropCount == 0 || mustRecompute) {
					objects = getAppliedObjects(context);
					objectsChanceSum = objects.values().stream().mapToDouble(Double::doubleValue).sum() + getEmptyChance();
				}
				T picked = pick(random, objects, objectsChanceSum);
				if (picked != null) pickeds.add(picked);
				if (mustRecompute && context != null) context.addPicked(picked);
			}
			return pickeds;
		}
		
		public abstract List<Conditioned<T, C>> getConditionedAlwaysObjects();
		
		@Override
		default List<T> getAlwaysObjectList() {
			return getConditionedAlwaysObjects().stream().map(Conditioned<T, C>::getObject).collect(Collectors.toList());
		}
		
		@Override
		default RandomizedPickerBuilder.IConditionalMultiBuilder<T, C> createBuilder() {
			return new RandomizedPickerBuilder.ConditionalPickerMultiBuilder<>(getConditionedObjectsList(), getConditionedAlwaysObjects());
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
		private RandomValueProvider<Integer> amountProvider;
		
		public FixedMultiPicker(Map<T, Double> values, List<T> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.amountProvider = amountProvider;
		}
		
		@Override
		public List<T> getAlwaysObjectList() {
			return valuesAlways;
		}
		
		@Override
		public RandomValueProvider<Integer> getAmountProvider() {
			return amountProvider;
		}
		
	}
	
	public class FixedConditionalPicker<T, C extends ConditionalContext<T>> implements ConditionalPicker<T, C> {
		
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
	
	public class FixedConditionalMultiPicker<T, C extends ConditionalContext<T>> extends FixedConditionalPicker<T, C> implements ConditionalMultiPicker<T, C> {
		
		private List<Conditioned<T, C>> valuesAlways;
		private RandomValueProvider<Integer> amountProvider;
		
		public FixedConditionalMultiPicker(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.amountProvider = amountProvider;
		}
		
		@Override
		public List<Conditioned<T, C>> getConditionedAlwaysObjects() {
			return valuesAlways;
		}
		
		@Override
		public RandomValueProvider<Integer> getAmountProvider() {
			return amountProvider;
		}
		
	}
	
}
