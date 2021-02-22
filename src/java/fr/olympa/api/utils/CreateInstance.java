package fr.olympa.api.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class CreateInstance<R> {
	@Nullable
	public R of(Class<?> clazz, Object... params) {
		try {
			Constructor<R>[] constructors = (Constructor<R>[]) clazz.getDeclaredConstructors();
			Class<?>[] paramsList = Arrays.stream(params).map(p -> p.getClass()).toArray(Class[]::new);
			int maxI = -1;
			int i = 0;
			Constructor<R> constructor = null;
			while (i < constructors.length) {
				Constructor<R> tmp = constructors[i++];
				int j = 0;
				for (Class<?> pa : tmp.getParameterTypes())
					if (pa.isAssignableFrom(paramsList[j++]) && maxI < j) {
						constructor = tmp;
						maxI = j;
					} else
						break;
			}
			if (constructor != null)
				return constructor.newInstance(Arrays.copyOfRange(params, 0, maxI));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Nullable
	public R of(Class<?> clazz) {
		try {
			Constructor<R>[] constructors = (Constructor<R>[]) clazz.getConstructors();
			Constructor<R> simpleConstructor = null;
			int i = 0;
			while (i < constructors.length && simpleConstructor == null) {
				Constructor<R> tmp = constructors[i++];
				if (tmp.getParameterCount() == 0)
					simpleConstructor = tmp;
			}
			if (simpleConstructor != null)
				return simpleConstructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}