package fr.olympa.api.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class Reflection {

	public enum ClassEnum {
		NMS("net.minecraft.server." + getNmsVersion() + "."),
		NM("net.minecraft." + getNmsVersion() + "."),
		CB("org.bukkit.craftbukkit." + getNmsVersion() + ".");

		private final String classType;

		ClassEnum(final String classType) {
			this.classType = classType;
		}

		@Override
		public String toString() {
			return this.classType;
		}
	}

	public interface ConstructorInvoker {

		Object invoke(final Object... p0);
	}

	public interface FieldAccessor<T> {

		T get(final Object p0);

		boolean hasField(final Object p0);

		void set(final Object p0, final Object p1);
	}

	public interface MethodInvoker {
		Object invoke(final Object p0, final Object... p1);
	}

	private static final Map<String, Class<?>> classCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T> T callMethod(final Method method, final Object instance, final Object... paramaters) {
		if (method == null) {
			throw new RuntimeException("No such method");
		}
		method.setAccessible(true);
		try {
			return (T) method.invoke(instance, paramaters);
		} catch (final InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause());
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// Get a class as Array
	public static Class<?> getArrayClass(final String classname, final int arraySize) {
		try {
			return Array.newInstance(getClass(classname), arraySize).getClass();
		} catch (final Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	// Get a Fields who is an Array
	public static ArrayList<Field> getArraysFields(final Object instance, final Class<?> fieldType) throws Exception {
		final String[] values = fieldType.toString().split(" ");
		final String fieldName = values[values.length - 1];
		final Field[] fields = instance.getClass().getDeclaredFields();
		final ArrayList<Field> fieldArrayList = new ArrayList<>();
		for (final Field field : fields) {
			if (field.getType().isArray()) {
				if (field.getType().toString().contains(fieldName)) {
					field.setAccessible(true);
					fieldArrayList.add(field);
				}
			}
		}
		return fieldArrayList;
	}

	public static Class<?> getClass(final ClassEnum classEnum, final String classname) {
		return getClass(classEnum + classname);
	}

	public static Class<?> getClass(final String classname) {

		if (classCache.containsKey(classname)) {
			return classCache.get(classname);
		}

		final Class<?> clazz = getClassWithoutCache(classname);
		classCache.put(classname, clazz);
		return clazz;

	}

	public static Class<?> getClassWithoutCache(final ClassEnum classEnum, final String classname) {
		return getClassWithoutCache(classEnum + classname);
	}

	public static Class<?> getClassWithoutCache(final String classname) {
		try {
			return Class.forName(classname);
		} catch (final Throwable t) {
			t.printStackTrace();
			return null;
		}

	}

	public static Field getField(final Class<?> clazz, final String fieldName) {
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
		}
		return field;
	}

	public static ArrayList<Field> getFields(final Object instance, final Class<?> fieldType) throws Exception {
		final Field[] fields = instance.getClass().getDeclaredFields();
		final ArrayList<Field> fieldArrayList = new ArrayList<>();
		for (final Field field : fields) {
			if (field.getType() == fieldType) {
				field.setAccessible(true);
				fieldArrayList.add(field);
			}
		}

		return fieldArrayList;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(final Field field, final Object obj) {
		try {
			return (T) field.get(obj);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object getFieldValue(final Object instance, final Class clazz, final String fieldName) throws Exception {
		final Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	public static Object getFieldValue(final Object instance, final String fieldName) throws Exception {
		final Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	public static Field getFirstFieldByType(final Class<?> clazz, final Class<?> type) {
		for (final Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type) {
				return field;
			}
		}
		return null;
	}

	public static MethodInvoker getMethod(final Class<?> clazz, final String methodName, final Class<?>... params) {
		return getTypedMethod(clazz, methodName, null, params);
	}

	public static MethodInvoker getMethod(final String className, final String methodName, final Class<?>... params) {
		return getTypedMethod(getClass(className), methodName, null, params);
	}

	public static Object getNmsPlayer(final Player p) throws Exception {
		final Method getHandle = p.getClass().getMethod("getHandle");
		return getHandle.invoke(p);
	}

	public static Object getNmsScoreboard(final Scoreboard s) throws Exception {
		final Method getHandle = s.getClass().getMethod("getHandle");
		return getHandle.invoke(s);
	}

	public static String getNmsVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static Object getPlayerConnection(final Player player) throws Exception {
		final Object nmsPlayer = getNmsPlayer(player);
		return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
	}

	public static MethodInvoker getTypedMethod(final Class<?> clazz, final String methodName, final Class<?> returnType, final Class<?>... params) {
		Method[] declaredMethods;
		for (int length = (declaredMethods = clazz.getDeclaredMethods()).length, i = 0; i < length; ++i) {
			final Method method = declaredMethods[i];
			if ((methodName == null || method.getName().equals(methodName)) && returnType == null || method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), params)) {
				method.setAccessible(true);
				return (target, arguments) -> {
					try {
						return method.invoke(target, arguments);
					} catch (final Exception e) {
						throw new RuntimeException("Cannot invoke method " + method, e);
					}
				};
			}
		}
		if (clazz.getSuperclass() != null) {
			return getMethod(clazz.getSuperclass(), methodName, params);
		}
		throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
	}

	public static Field makeField(final Class<?> clazz, final String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (final NoSuchFieldException ex) {
			return null;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Method makeMethod(final Class<?> clazz, final String methodName, final Class<?>... paramaters) {
		try {
			return clazz.getDeclaredMethod(methodName, paramaters);
		} catch (final NoSuchMethodException ex) {
			return null;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static int ping(final Player p) {
		try {
			final Object nmsPlayer = Reflection.getNmsPlayer(p);
			return Integer.valueOf(getFieldValue(nmsPlayer, "ping").toString());
		} catch (final Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void sendMessage(final Player p, final Object message) throws Exception {
		final Object nmsPlayer = getNmsPlayer(p);
		nmsPlayer.getClass().getMethod("sendMessage", Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent")).invoke(nmsPlayer, message);

	}

	public static void sendPacket(final Collection<? extends Player> players, final Object packet) {
		if (packet == null) {
			return;
		}
		try {
			for (final Player p : players) {
				sendPacket(getPlayerConnection(p), packet);
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	public static void sendPacket(final Object packet) {
		sendPacket(Bukkit.getOnlinePlayers(), packet);
	}

	public static void sendPacket(final Object connection, final Object packet) throws Exception {
		connection.getClass().getMethod("sendPacket", Reflection.getClass(ClassEnum.NMS, "Packet")).invoke(connection, packet);
	}

	public static void sendPacket(final Player p, final Object packet) {
		final ArrayList<Player> list = new ArrayList<>();
		list.add(p);
		sendPacket(list, packet);
	}

	public static void setField(final Object obj, final String field, final Object value) {
		try {
			final Field maxUsesField = obj.getClass().getDeclaredField(field);
			maxUsesField.setAccessible(true);
			maxUsesField.set(obj, value);
			maxUsesField.setAccessible(!maxUsesField.isAccessible());
		} catch (final Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().severe("Reflection failed for changeField " + obj.getClass().getName() + " field > " + field + " value > " + value);
			Bukkit.getServer().shutdown();
		}
	}

	public static void setFieldValue(final Object instance, final Field field, final Object value) {
		field.setAccessible(true);
		try {
			field.set(instance, value);
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setFieldValue(final Object instance, final String field, final Object value) {
		try {
			final Field f = instance.getClass().getDeclaredField(field);
			f.setAccessible(true);
			f.set(instance, value);
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}
}