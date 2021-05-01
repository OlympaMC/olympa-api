package fr.olympa.api.cache;

import java.lang.reflect.Field;

/**
 * Ici c'est pour essayer de lier l'api SQL & Cache au maximum
 * Le but est de résoudre le problème suivant : Si deux field sont du même type, il faut réussir à les différencier pour automatiser le get/set de ce même field
 * La solution serait d'utiliser l'ordre des fields dans la class ou des annotations
 */
public class TestCache<T> {

	public String getFieldName(T object, Object objectTargetField) throws SecurityException, IllegalArgumentException, IllegalAccessException {
		String fieldName = null;
		Class<? extends Object> objClass = object.getClass();
		Class<? extends Object> fieldClass = objectTargetField.getClass();

		for (Field field : objClass.getFields())
			if (field.getType().isAssignableFrom(fieldClass)) {
				Object tmpObjectField = field.get(object);
				if (objectTargetField.equals(tmpObjectField))
					if (fieldName != null)
						if (tmpObjectField.getClass().isPrimitive())
							throw new IllegalAccessException(String.format("%s has 2 (or more) field with same type & same value. It's a primitive field, you need to have only one.", object.getClass().getSimpleName()));
						else
							throw new IllegalAccessException(String.format("%s has 2 (or more) field with same type & same value. null include.", object.getClass().getSimpleName()));
					else
						fieldName = field.getName();
			}
		return fieldName;
	}
}
