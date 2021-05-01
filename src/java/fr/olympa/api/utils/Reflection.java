package fr.olympa.api.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Reflection {

	public enum ClassEnum {
		NMS("net.minecraft.server." + getNmsVersion() + "."),
		NM("net.minecraft." + getNmsVersion() + "."),
		CB("org.bukkit.craftbukkit." + getNmsVersion() + ".");

		private String classType;

		ClassEnum(String classType) {
			this.classType = classType;
		}

		@Override
		public String toString() {
			return classType;
		}
	}

	public interface ConstructorInvoker {

		Object invoke(Object... p0);
	}

	public interface FieldAccessor<T> {

		T get(Object p0);

		boolean hasField(Object p0);

		void set(Object p0, Object p1);
	}

	public interface MethodInvoker {
		Object invoke(Object p0, Object... p1);
	}

	private static Cache<String, Class<?>> classCache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(5, TimeUnit.MINUTES).build();

	static {
		CacheStats.addCache("CLASS", classCache);
	}

	@SuppressWarnings("unchecked")
	public static <T> T callMethod(Method method, Object instance, Object... paramaters) {
		if (method == null)
			throw new RuntimeException("No such method");
		method.setAccessible(true);
		try {
			return (T) method.invoke(instance, paramaters);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// Get a class as Array
	public static Class<?> getArrayClass(String classname, int arraySize) {
		try {
			return Array.newInstance(getClass(classname), arraySize).getClass();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	// Get a Fields who is an Array
	public static ArrayList<Field> getArraysFields(Object instance, Class<?> fieldType) throws ReflectiveOperationException {
		String[] values = fieldType.toString().split(" ");
		String fieldName = values[values.length - 1];
		Field[] fields = instance.getClass().getDeclaredFields();
		ArrayList<Field> fieldArrayList = new ArrayList<>();
		for (Field field : fields)
			if (field.getType().isArray())
				if (field.getType().toString().contains(fieldName)) {
					field.setAccessible(true);
					fieldArrayList.add(field);
				}
		return fieldArrayList;
	}

	public static Class<?> getClass(ClassEnum classEnum, String classname) {
		return getClass(classEnum + classname);
	}

	public static Class<?> getClass(String classname) {
		if (classCache.asMap().containsKey(classname))
			return classCache.asMap().get(classname);

		Class<?> clazz = getClassWithoutCache(classname);
		classCache.put(classname, clazz);
		return clazz;

	}

	public static Class<?> getClassWithoutCache(ClassEnum classEnum, String classname) {
		return getClassWithoutCache(classEnum + classname);
	}

	public static Class<?> getClassWithoutCache(String classname) {
		try {
			return Class.forName(classname);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}

	}

	public static Field getField(Class<?> clazz, String fieldName) {
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return field;
	}

	public static ArrayList<Field> getFields(Object instance, Class<?> fieldType) throws ReflectiveOperationException {
		Field[] fields = instance.getClass().getDeclaredFields();
		ArrayList<Field> fieldArrayList = new ArrayList<>();
		for (Field field : fields)
			if (field.getType() == fieldType) {
				field.setAccessible(true);
				fieldArrayList.add(field);
			}

		return fieldArrayList;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Field field, Object obj) {
		try {
			return (T) field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object getFieldValue(Object instance, Class clazz, String fieldName) throws ReflectiveOperationException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	public static Object getFieldValue(Object instance, String fieldName) throws ReflectiveOperationException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

	public static Field getFirstFieldByType(Class<?> clazz, Class<?> type) {
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType() == type)
				return field;
		}
		return null;
	}

	public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		return getTypedMethod(clazz, methodName, null, params);
	}

	public static MethodInvoker getMethod(String className, String methodName, Class<?>... params) {
		return getTypedMethod(getClass(className), methodName, null, params);
	}

	public static Object getNmsPlayer(Player p) throws ReflectiveOperationException {
		Method getHandle = p.getClass().getMethod("getHandle");
		return getHandle.invoke(p);
	}

	public static Object getNmsScoreboard(Scoreboard s) throws ReflectiveOperationException {
		Method getHandle = s.getClass().getMethod("getHandle");
		return getHandle.invoke(s);
	}

	public static String getNmsVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static Object getPlayerConnection(Player player) throws ReflectiveOperationException {
		Object nmsPlayer = getNmsPlayer(player);
		return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
	}

	public static MethodInvoker getTypedMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... params) {
		Method[] declaredMethods;
		for (int length = (declaredMethods = clazz.getDeclaredMethods()).length, i = 0; i < length; ++i) {
			Method method = declaredMethods[i];
			if ((methodName == null || method.getName().equals(methodName)) && returnType == null || method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), params)) {
				method.setAccessible(true);
				return (target, arguments) -> {
					try {
						return method.invoke(target, arguments);
					} catch (Exception e) {
						throw new RuntimeException("Cannot invoke method " + method, e);
					}
				};
			}
		}
		if (clazz.getSuperclass() != null)
			return getMethod(clazz.getSuperclass(), methodName, params);
		throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
	}

	public static Field makeField(Class<?> clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException ex) {
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Method makeMethod(Class<?> clazz, String methodName, Class<?>... paramaters) {
		try {
			return clazz.getDeclaredMethod(methodName, paramaters);
		} catch (NoSuchMethodException ex) {
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static int ping(Player p) {
		try {
			Object nmsPlayer = Reflection.getNmsPlayer(p);
			return Integer.valueOf(getFieldValue(nmsPlayer, "ping").toString());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/*public static void sendMessage(Player p, Object message) throws Exception { // quel intérêt ??
		Object nmsPlayer = getNmsPlayer(p);
		nmsPlayer.getClass().getMethod("sendMessage", Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent")).invoke(nmsPlayer, message);
	
	}*/

	public static void sendPacket(Collection<? extends Player> players, Object packet) {
		if (packet == null)
			return;
		try {
			for (Player p : players)
				sendPacket(getPlayerConnection(p), packet);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void sendPacket(Object packet) {
		sendPacket(Bukkit.getOnlinePlayers(), packet);
	}

	public static void sendPacket(Object connection, Object packet) throws ReflectiveOperationException {
		connection.getClass().getMethod("sendPacket", Reflection.getClass(ClassEnum.NMS, "Packet")).invoke(connection, packet);
	}

	public static void sendPacket(Player p, Object packet) {
		ArrayList<Player> list = new ArrayList<>();
		list.add(p);
		sendPacket(list, packet);
	}

	public static void setField(Object obj, String field, Object value) {
		try {
			Field maxUsesField = obj.getClass().getDeclaredField(field);
			maxUsesField.setAccessible(true);
			maxUsesField.set(obj, value);
			maxUsesField.setAccessible(!maxUsesField.isAccessible());
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().severe("Reflection failed for changeField " + obj.getClass().getName() + " field > " + field + " value > " + value);
			Bukkit.getServer().shutdown();
		}
	}

	public static void setFieldValue(Object instance, Field field, Object value) {
		field.setAccessible(true);
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setFieldValue(Object instance, String field, Object value) {
		try {
			Field f = instance.getClass().getDeclaredField(field);
			f.setAccessible(true);
			f.set(instance, value);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}