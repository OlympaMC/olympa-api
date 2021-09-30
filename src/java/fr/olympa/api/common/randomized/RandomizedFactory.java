package fr.olympa.api.common.randomized;

import java.util.List;
import java.util.Map;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedPicker;

public interface RandomizedFactory {
	
	<T> RandomizedPicker<T> newPicker(Map<T, Double> values, double emptyChance);
	
	<T> RandomizedMultiPicker<T> newMultiPicker(Map<T, Double> values, List<T> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance);
	
	<T, C extends ConditionalContext<T>> ConditionalPicker<T, C> newConditionalPicker(Map<Conditioned<T, C>, Double> values, double emptyChance);
	
	<T, C extends ConditionalContext<T>> ConditionalMultiPicker<T, C> newConditionalMultiPicker(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance);
	
	static RandomizedFactory getDefaultFactory() {
		return DefaultFactoryProvider.FACTORY;
	}
	
	static void setDefaultFactory(RandomizedFactory factory) {
		DefaultFactoryProvider.FACTORY = factory;
	}
	
	static final class DefaultFactoryProvider {
		public static RandomizedFactory FACTORY;
		
		private DefaultFactoryProvider() {}
	}
	
}
