package fr.olympa.api.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.cache.Cache;
import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TableGenerator;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.chat.TableGenerator.Alignment;
import fr.olympa.api.common.chat.TableGenerator.Receiver;
import fr.olympa.api.common.command.IOlympaCommand;
import fr.olympa.api.common.command.complex.CommandContext;

public class CacheStats {

	private static final Map<String, Cache<?, ?>> caches = new HashMap<>();
	private static final Map<String, Collection<?>> debugList = new HashMap<>();
	private static final Map<String, Map<Object, Object>> debugMap = new HashMap<>();

	public static Map<String, Cache<?, ?>> getCaches() {
		return caches;
	}

	public static Map<String, Collection<?>> getDebugLists() {
		return debugList;
	}

	public static Map<String, Map<Object, Object>> getDebugMaps() {
		return debugMap;
	}

	public static Entry<String, Cache<?, ?>> getCache(String id) {
		id = id.replace(" ", "_").toUpperCase();
		return new AbstractMap.SimpleEntry<>(id, caches.get(id));
	}

	public static Entry<String, Collection<?>> getDebugList(String id) {
		id = id.replace(" ", "_").toUpperCase();
		return new AbstractMap.SimpleEntry<>(id, debugList.get(id));
	}

	public static Entry<String, Map<Object, Object>> getDebugMap(String id) {
		id = id.replace(" ", "_").toUpperCase();
		return new AbstractMap.SimpleEntry<>(id, debugMap.get(id));
	}

	public static void addCache(String key, Cache<?, ?> value) {
		CacheStats.caches.put(key.replace(" ", "_").toUpperCase(), value);
	}

	public static void addDebugList(String key, Collection<?> value) {
		CacheStats.debugList.put(key.replace(" ", "_").toUpperCase(), value);
	}

	@SuppressWarnings("unchecked")
	public static void addDebugMap(String key, Map<?, ?> value) {
		CacheStats.debugMap.put(key.replace(" ", "_").toUpperCase(), (Map<Object, Object>) value);
	}

	public static void executeOnCache(IOlympaCommand commandClass, CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			commandClass.sendComponents(getAllDebugCaches(cmd.getSenderType()).build());
			return;
		}
		Entry<String, Cache<?, ?>> entry = cmd.getArgument(0);
		String cacheName = entry.getKey();
		Cache<?, ?> cache = entry.getValue();
		if (cmd.getArgumentsLength() == 1) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "Le cache &2%s&a contient &2%s&a entrées.\n%s", cacheName, cache.size(), getCacheStats(cacheName, cache, cmd.getSenderType()));
			return;
		}
		String action = cmd.getArgument(1);
		switch (action.toLowerCase()) {
		case "clear":
			long size = cache.size();
			cache.invalidateAll();
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "Le cache &2%s&a a été clear (de %d éléments).", cacheName, size);
			break;
		case "print":
			commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Voici les données de %s :\n", cacheName).extra(getContent(cache.asMap().entrySet(), cmd.getSenderType())).build());
			break;
		}
	}

	public static void executeOnList(IOlympaCommand commandClass, CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			commandClass.sendComponents(getAllDebugList(cmd.getSenderType()).build());
			return;
		}
		Entry<String, Collection<?>> entry = cmd.getArgument(0);
		String cacheName = entry.getKey();
		Collection<?> list = entry.getValue();
		if (cmd.getArgumentsLength() == 1) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La list &2%s&a contient &2%s&a objets.", cacheName, list.size());
			return;
		}
		String action = cmd.getArgument(1);
		switch (action.toLowerCase()) {
		case "clear":
			long size = list.size();
			list.clear();
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La list &2%s&a a été clear (de %d éléments).", cacheName, size);
			break;
		case "print":
			commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Voici les données de %s :\n", cacheName).extra(getContent(list, cmd.getSenderType())).build());
			break;
		}
	}

	public static void executeOnMap(IOlympaCommand commandClass, CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			commandClass.sendComponents(getAllDebugMap(cmd.getSenderType()).build());
			return;
		}
		Entry<String, Map<Object, Object>> entry = cmd.getArgument(0);
		String cacheName = entry.getKey();
		Map<Object, Object> map = entry.getValue();
		if (cmd.getArgumentsLength() == 1) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La map &2%s&a contient &2%s&a entrées.", cacheName, map.size());
			return;
		}
		String action = cmd.getArgument(1);
		switch (action.toLowerCase()) {
		case "clear":
			long size = map.size();
			map.clear();
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La map &2%s&a a été clear (de %d éléments).", cacheName, size);
			break;
		case "print":
			commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Voici les données de %s :\n", cacheName).extra(getContent(map, cmd.getSenderType())).build());
			break;
		case "remove":
			if (cmd.getArgumentsLength() < 2) {
				commandClass.sendError("Une clef doit être renseigné après &4remove&c.");
				return;
			}
			String clef = cmd.getArgument(2);
			Object objRemoved = map.remove(clef);
			if (objRemoved != null)
				commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "L'entrée avec la clef &2%s&a a été supprimé (passe ta souris pour voir les données).", cacheName).onHoverText(objectToString(objRemoved)).build());
			else
				commandClass.sendError("La clef &4%s&c n'existe pas. Petit de soucis de timing dit donc !", clef);
			break;
		}
	}

	private static TxtComponentBuilder getAllDebugCaches(Receiver receiver) {
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "Voici tous les cache de olympa côté %s.", LinkSpigotBungee.Provider.link.isSpigot() ? "Spigot" : "BungeeCord").extraSpliter("\n");
		builder.extra(new TxtComponentBuilder("&3ID       &bSize")).extraSpliter("\n");
		for (Entry<String, Cache<?, ?>> entry : caches.entrySet())
			builder.extra(new TxtComponentBuilder("&6%s &e%d", entry.getKey(), entry.getValue().size()).onHoverText(getCacheStats(entry.getKey(), entry.getValue(), receiver)));
		return builder;
	}

	public static TxtComponentBuilder getAllDebugList(Receiver receiver) {
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "List en mode DEBUG olympa côté %s.", LinkSpigotBungee.Provider.link.isSpigot() ? "Spigot" : "BungeeCord").extraSpliter("\n");
		builder.extra(new TxtComponentBuilder("&3ID          &bSize")).extraSpliter("\n");
		for (Entry<String, Collection<?>> entry : debugList.entrySet())
			builder.extra(getContent(entry.getValue(), receiver));
		return builder;
	}

	public static TxtComponentBuilder getAllDebugMap(Receiver receiver) {
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "Map en mode DEBUG olympa côté %s.", LinkSpigotBungee.Provider.link.isSpigot() ? "Spigot" : "BungeeCord").extraSpliter("\n");
		builder.extra(new TxtComponentBuilder("&3ID          &bSize"));
		for (Entry<String, Map<Object, Object>> entry : debugMap.entrySet())
			builder.extra(new TxtComponentBuilder(entry.getKey() + " " + entry.getValue().size()).onHoverText(getContent(entry.getValue(), receiver)));
		return builder;
	}

	private static String getCacheStats(String name, Cache<?, ?> cache, Receiver receiver) {
		Map<String, String> stats = Utils.jsonToHumainReadable(new Gson().toJson(cache.stats()));
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT, Alignment.CENTER, Alignment.LEFT, Alignment.LEFT);
		Iterator<Entry<String, String>> it = stats.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String info1 = objectToString(entry.getKey());
			String info2 = objectToString(entry.getValue());
			String info3 = null;
			String info4 = null;
			if (it.hasNext()) {
				Entry<String, String> entry2 = it.next();
				info3 = objectToString(entry2.getKey());
				info4 = objectToString(entry2.getValue());
			}
			table.addRow(info1, info2, "|", info3, info4);
		}
		return "&cStats de performances de &4" + name + "&r\n" + table.toString(receiver);
	}

	private static String getContent(Collection<?> info, Receiver receiver) {
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT, Alignment.LEFT, Alignment.LEFT);
		Iterator<?> it = info.iterator();
		while (it.hasNext()) {
			String info1 = objectToString(it.next());
			String info2 = null;
			String info3 = null;
			String info4 = null;
			if (it.hasNext()) {
				info2 = objectToString(it.next());
				if (it.hasNext()) {
					info3 = objectToString(it.next());
					if (it.hasNext())
						info4 = objectToString(it.next());
				}
			}
			table.addRow(info1, info2, info3, info4);
		}
		return "&cContenu de la list\n" + table.toString(receiver);
	}

	public static String getContent(Map<Object, Object> info, Receiver receiver) {
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT, Alignment.CENTER, Alignment.LEFT, Alignment.LEFT);
		table.addRow("Clef", "Valeur", "|", "Clef", "Valeur");
		Iterator<Entry<Object, Object>> it = info.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			String info1 = objectToString(entry.getKey());
			String info2 = objectToString(entry.getValue());
			String info3 = null;
			String info4 = null;
			if (it.hasNext()) {
				Entry<Object, Object> entry2 = it.next();
				info3 = objectToString(entry2.getKey());
				info4 = objectToString(entry2.getValue());
			}
			table.addRow(info1, info2, "|", info3, info4);
		}
		return table.toString(receiver);
	}

	private static String objectToString(Object o) {
		String key = o.toString();
		if (key.startsWith(o.getClass().getName() + "@"))
			try {
				String tmp = new Gson().toJson(o);
				key = tmp;
			} catch (Exception e) {
				LinkSpigotBungee.Provider.link.sendMessage("&4ERROR CacheStats &4" + o.getClass().getName() + "&c - Impossible de mettre &4" + o + "&c en JSON.");
			}
		return key;
	}

}
