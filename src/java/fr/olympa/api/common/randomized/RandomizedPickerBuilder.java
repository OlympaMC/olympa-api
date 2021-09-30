package fr.olympa.api.common.randomized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedMultiPickerBase;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedPicker;

public class RandomizedPickerBuilder {
	
	private RandomizedPickerBuilder() {}

	public static <T> INormalBuilder<T> newBuilder() {
		return new PickerBuilder<>();
	}
	
	public static <T, C extends ConditionalContext<T>> IConditionalBuilder<T, C> newConditionalBuilder() {
		return new ConditionalPickerBuilder<>();
	}
	
	public static interface IBuilder<T> {
		
		RandomizedFactory getFactory();
		
		void setFactory(RandomizedFactory factory);
		
	}

	public static interface IBuilderBase<T> extends IBuilder<T> {
		
		RandomizedPickerBase<T> build(double emptyChance);
		
		RandomizedPickerBase<T> build();
		
		IBuilderBase<T> clone();
		
	}

	public static interface IMultiBuilderBase<T> extends IBuilder<T> {
		
		RandomizedMultiPickerBase<T> build(int min, int max, double emptyChance);
		
		RandomizedMultiPickerBase<T> build(int min, int max);
		
		RandomizedMultiPickerBase<T> build(RandomValueProvider<Integer> amountProvider, double emptyChance);
		
		RandomizedMultiPickerBase<T> build(RandomValueProvider<Integer> amountProvider);
		
		IMultiBuilderBase<T> clone();
		
	}

	public static interface INormalBuilderBase<T> extends IBuilder<T> {
		
		INormalBuilderBase<T> add(double chance, T value);
		
		INormalMultiBuilder<T> addAlways(T value);
		
		<C extends ConditionalContext<T>> IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		<C extends ConditionalContext<T>> IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value);
		
		INormalBuilderBase<T> clone();
		
	}

	public static interface INormalMultiBuilder<T> extends INormalBuilderBase<T>, IMultiBuilderBase<T> {
		
		@Override
		INormalMultiBuilder<T> add(double chance, T value);
		
		@Override
		RandomizedMultiPicker<T> build(int min, int max, double emptyChance);
		
		@Override
		RandomizedMultiPicker<T> build(int min, int max);
		
		@Override
		RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider, double emptyChance);
		
		@Override
		RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider);
		
		@Override
		INormalMultiBuilder<T> clone();
		
	}

	public static interface INormalBuilder<T> extends INormalMultiBuilder<T>, IBuilderBase<T> {
		
		@Override
		INormalBuilder<T> add(double chance, T value);
		
		@Override
		RandomizedPicker<T> build(double emptyChance);
		
		@Override
		RandomizedPicker<T> build();
		
		@Override
		INormalBuilder<T> clone();
		
	}

	public static interface IConditionalBuilderBase<T, C extends ConditionalContext<T>> extends IBuilder<T> {
		
		IConditionalBuilderBase<T, C> add(double chance, T value);
		
		IConditionalBuilderBase<T, C> add(double chance, Conditioned<T, C> value);
		
		IConditionalMultiBuilder<T, C> addAlways(T value);
		
		IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> value);
		
		IConditionalBuilderBase<T, C> clone();
		
	}

	public static interface IConditionalMultiBuilder<T, C extends ConditionalContext<T>> extends IConditionalBuilderBase<T, C>, IMultiBuilderBase<T> {
		
		@Override
		IConditionalMultiBuilder<T, C> add(double chance, T value);
		
		@Override
		IConditionalMultiBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		@Override
		ConditionalMultiPicker<T, C> build(int min, int max, double emptyChance);
		
		@Override
		ConditionalMultiPicker<T, C> build(int min, int max);
		
		@Override
		ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider, double emptyChance);
		
		@Override
		ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider);
		
		@Override
		IConditionalMultiBuilder<T, C> clone();
		
	}

	public static interface IConditionalBuilder<T, C extends ConditionalContext<T>> extends IConditionalMultiBuilder<T, C>, IBuilderBase<T> {
		
		@Override
		IConditionalBuilder<T, C> add(double chance, T value);
		
		@Override
		IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> value);
		
		@Override
		ConditionalPicker<T, C> build(double emptyChance);
		
		@Override
		ConditionalPicker<T, C> build();
		
		@Override
		IConditionalBuilder<T, C> clone();
		
	}

	public abstract static class AbstractBuilder<T> implements IBuilder<T> {
		
		protected RandomizedFactory factory = RandomizedFactory.getDefaultFactory();
		
		@Override
		public RandomizedFactory getFactory() {
			return factory;
		}
		
		@Override
		public void setFactory(RandomizedFactory factory) {
			this.factory = factory;
		}
	}
	
	public abstract static class PickerBuilderBase<T> extends AbstractBuilder<T> implements INormalBuilderBase<T> {
		
		protected Map<T, Double> values = new HashMap<>();
		
		protected PickerBuilderBase() {}
		
		protected PickerBuilderBase(Map<T, Double> values) {
			this.values.putAll(values);
		}
		
		@Override
		public INormalBuilderBase<T> add(double chance, T object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		@Override
		public <C extends ConditionalContext<T>> IConditionalBuilder<T, C> add(double chance, Conditioned<T, C> object) {
			ConditionalPickerBuilder<T, C> picker = new ConditionalPickerBuilder<>();
			values.forEach((obj, ochance) -> picker.add(ochance, obj));
			return picker.add(chance, object);
		}
		
		@Override
		public <C extends ConditionalContext<T>> IConditionalMultiBuilder<T, C> addAlways(Conditioned<T, C> object) {
			ConditionalPickerMultiBuilder<T, C> picker = new ConditionalPickerMultiBuilder<>();
			values.forEach((obj, ochance) -> picker.add(ochance, obj));
			return picker.addAlways(object);
		}
		
		@Override
		public abstract PickerBuilderBase<T> clone();
		
	}

	public static class PickerBuilder<T> extends PickerBuilderBase<T> implements INormalBuilder<T> {
		
		public PickerBuilder() {}
		
		public PickerBuilder(Map<T, Double> values) {
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
			return factory.newPicker(values, emptyChance);
		}
		
		@Override
		public RandomizedPicker<T> build() {
			return build(0);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max, double emptyChance) {
			return build(new RandomValueProvider.UniformProvider(min, max), emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max) {
			return build(new RandomValueProvider.UniformProvider(min, max));
		}
		
		@Override
		public RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider, double emptyChance) {
			return factory.newMultiPicker(values, Collections.emptyList(), amountProvider, emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider) {
			return build(amountProvider, 0);
		}
		
		@Override
		public PickerBuilder<T> clone() {
			return new PickerBuilder<>(values);
		}
		
	}

	public static class PickerMultiBuilder<T> extends PickerBuilderBase<T> implements INormalMultiBuilder<T> {
		
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
			return build(new RandomValueProvider.UniformProvider(min, max), emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(int min, int max) {
			return build(min, max, 0);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider, double emptyChance) {
			return factory.newMultiPicker(values, valuesAlways, amountProvider, emptyChance);
		}
		
		@Override
		public RandomizedMultiPicker<T> build(RandomValueProvider<Integer> amountProvider) {
			return build(amountProvider, 0);
		}
		
		@Override
		public PickerMultiBuilder<T> clone() {
			return new PickerMultiBuilder<>(values, valuesAlways);
		}
		
	}

	public abstract static class ConditionalPickerBuilderBase<T, C extends ConditionalContext<T>> extends AbstractBuilder<T> implements IConditionalBuilderBase<T, C> {
		
		protected Map<Conditioned<T, C>, Double> values = new HashMap<>();
		
		protected ConditionalPickerBuilderBase() {}
		
		protected ConditionalPickerBuilderBase(Map<Conditioned<T, C>, Double> values) {
			this.values.putAll(values);
		}
		
		@Override
		public IConditionalBuilderBase<T, C> add(double chance, T object) {
			add(chance, new Conditioned.EmptyConditioned<>(object));
			return this;
		}
		
		@Override
		public IConditionalBuilderBase<T, C> add(double chance, Conditioned<T, C> object) {
			values.compute(object, (key, value) -> value == null ? chance : value.doubleValue() + chance);
			return this;
		}
		
		@Override
		public abstract ConditionalPickerBuilderBase<T, C> clone();
		
	}

	public static class ConditionalPickerBuilder<T, C extends ConditionalContext<T>> extends ConditionalPickerBuilderBase<T, C> implements IConditionalBuilder<T, C> {
		
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
			return factory.newConditionalPicker(values, emptyChance);
		}
		
		@Override
		public ConditionalPicker<T, C> build() {
			return build(0);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max, double emptyChance) {
			return build(new RandomValueProvider.UniformProvider(min, max), emptyChance);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max) {
			return build(min, max, 0);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider, double emptyChance) {
			return factory.newConditionalMultiPicker(values, Collections.emptyList(), amountProvider, emptyChance);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider) {
			return build(amountProvider, 0);
		}
		
		@Override
		public ConditionalPickerBuilder<T, C> clone() {
			return new ConditionalPickerBuilder<>(values);
		}
		
	}

	public static class ConditionalPickerMultiBuilder<T, C extends ConditionalContext<T>> extends ConditionalPickerBuilderBase<T, C> implements IConditionalMultiBuilder<T, C> {
		
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
			return build(new RandomValueProvider.UniformProvider(min, max));
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(int min, int max) {
			return build(min, max, 0);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider, double emptyChance) {
			return factory.newConditionalMultiPicker(values, valuesAlways, amountProvider, emptyChance);
		}
		
		@Override
		public ConditionalMultiPicker<T, C> build(RandomValueProvider<Integer> amountProvider) {
			return build(amountProvider, 0);
		}
		
		@Override
		public ConditionalPickerMultiBuilder<T, C> clone() {
			return new ConditionalPickerMultiBuilder<>(values, valuesAlways);
		}
		
	}
	
}
