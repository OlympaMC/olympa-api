package fr.olympa.api.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TableGenerator;
import fr.olympa.api.common.chat.TableGenerator.Alignment;
import fr.olympa.api.common.chat.TableGenerator.Receiver;
import fr.olympa.api.common.chat.TxtComponentBuilder;
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
		@Nullable
		Cache<?, ?> cache = entry.getValue();
		if (cache == null) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "Le cache %s est null.\n", cacheName);
			return;
		}
		if (cmd.getArgumentsLength() == 1) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "Le cache &2%s&a contient &2%s&a entrées.", cacheName, cache.size());
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
			commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Voici les données du cache %s :\n", cacheName).extra(getContent((Map<Object, Object>) cache.asMap(), cmd.getSenderType())).build());
			break;
		case "stats":
			commandClass.sendComponents(new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Voici les stats de %s :\n", cacheName).extra(getCacheStats(cacheName, cache, cmd.getSenderType())).build());
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
		if (list == null) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La list %s est null.\n%s", cacheName);
			return;
		}
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
		if (map == null) {
			commandClass.sendMessage(Prefix.DEFAULT_GOOD, "La map %s est null.\n%s", cacheName);
			return;
		}
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
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "Voici tous les cache de olympa côté %s.", LinkSpigotBungee.getInstance().isSpigot() ? "Spigot" : "BungeeCord").extraSpliterBN();
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		table.addRowTxtBuilder("&3Id", "&bTaille");
		for (Entry<String, Cache<?, ?>> entry : caches.entrySet())
			table.addRowTxtBuilder(new TxtComponentBuilder(entry.getKey())
					.onHoverText(getCacheStats(entry.getKey(), entry.getValue(), receiver).toLegacyText())
					.onClickCommand(getCommandToPrint("cache", entry.getKey())), new TxtComponentBuilder(entry.getValue().size()));
		builder.extra(table.toTxtComponentBuilder());
		return builder;
	}

	public static TxtComponentBuilder getAllDebugList(Receiver receiver) {
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "List en mode DEBUG olympa côté %s.", LinkSpigotBungee.getInstance().isSpigot() ? "Spigot" : "BungeeCord").extraSpliter("\n");
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		table.addRowTxtBuilder("&3Id", "&bTaille");
		for (Entry<String, Collection<?>> entry : debugList.entrySet())
			table.addRowTxtBuilder(new TxtComponentBuilder("&6%s &e%d", entry.getKey(), entry.getValue().size())
					.onHoverText(getContent(entry.getValue(), receiver).toLegacyText())
					.onClickCommand(getCommandToPrint("list", entry.getKey())));
		builder.extra(table.toTxtComponentBuilder());
		return builder;
	}

	public static TxtComponentBuilder getAllDebugMap(Receiver receiver) {
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT, "Map en mode DEBUG olympa côté %s.", LinkSpigotBungee.getInstance().isSpigot() ? "Spigot" : "BungeeCord").extraSpliter("\n");
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		table.addRowTxtBuilder("&3Id", "&bTaille");
		for (Entry<String, Map<Object, Object>> entry : debugMap.entrySet())
			table.addRowTxtBuilder(new TxtComponentBuilder(entry.getKey()).onHoverText(getContent(entry.getValue(), receiver).toLegacyText())
					.onClickCommand(getCommandToPrint("map", entry.getKey())), new TxtComponentBuilder(entry.getValue().size()));
		builder.extra(table.toTxtComponentBuilder());
		return builder;
	}

	private static TxtComponentBuilder getCacheStats(String name, Cache<?, ?> cache, Receiver receiver) {
		TxtComponentBuilder out = new TxtComponentBuilder("&cStats de performances de &4%s&r\n&7Si toutes les valeurs sont à 0, il est possible que les stats du cache n'ont pas activés\n", name);
		Map<String, String> stats = Utils.jsonToHumainReadable(new Gson().toJson(cache.stats()));
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		Iterator<Entry<String, String>> it = stats.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			table.addRowTxtBuilder(objectToTxtBuilder(entry.getKey(), receiver), objectToTxtBuilder(entry.getValue(), receiver));
		}
		out.extra(table.toTxtComponentBuilder());
		return out;
	}

	private static TxtComponentBuilder getContent(Collection<?> info, Receiver receiver) {
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		table.addRowTxtBuilder("Valeur de l'objet");
		Iterator<?> it = info.iterator();
		while (it.hasNext())
			table.addRowTxtBuilder(objectToTxtBuilder(it.next(), receiver));
		return table.toTxtComponentBuilder();
	}

	public static TxtComponentBuilder getContent(Map<Object, Object> info, Receiver receiver) {
		TableGenerator table = new TableGenerator(Alignment.LEFT, Alignment.LEFT).setReceiver(receiver);
		table.addRowTxtBuilder("Clef", "Valeur");
		Iterator<Entry<Object, Object>> it = info.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			table.addRowTxtBuilder(objectToTxtBuilder(entry.getKey(), receiver), objectToTxtBuilder(entry.getValue(), receiver));
		}
		return table.toTxtComponentBuilder();
	}

	private static String getCommandToPrint(String listOrMapOrCache, String name) {
		String commandName;
		if (LinkSpigotBungee.getInstance().isSpigot())
			commandName = "spig";
		else
			commandName = "bung";
		return String.format("/%s %s %s print", commandName, listOrMapOrCache, name);
	}

	private static String objectToString(Object o) {
		if (o instanceof String)
			return (String) o;
		String key = o.toString();
		if (key.startsWith(o.getClass().getName() + "@"))
			try {
				String tmp = new Gson().toJson(o);
				key = tmp;
			} catch (Exception e) {
				LinkSpigotBungee.getInstance().sendMessage("&4ERROR CacheStats &4" + o.getClass().getName() + "&c - Impossible de mettre &4" + o + "&c en JSON.");
			}
		return key;
	}

	private static TxtComponentBuilder objectToTxtBuilder(Object o, Receiver receiver) {
		if (o instanceof String)
			return new TxtComponentBuilder((String) o);
		TxtComponentBuilder out = new TxtComponentBuilder();
		String key = o.toString();
		if (key.startsWith(o.getClass().getName() + "@"))
			try {
				String tmp = new Gson().toJson(o);
				if (receiver == Receiver.CONSOLE || tmp.length() < 50)
					out.extra(tmp);
				else {
					out.extra(o.getClass().getSimpleName());
					out.onHoverText(tmp);
				}
			} catch (Exception e) {
				out.extra(key);
				out.onHoverText("&cImpossible de serialize en json la class &4%s&c.", o.getClass().getSimpleName());
				LinkSpigotBungee.getInstance().sendMessage("&4ERROR CacheStats &4" + o.getClass().getName() + "&c - Impossible de mettre &4" + o + "&c en JSON.");
			}
		else
			out.extra(key);
		return out;
	}

}
