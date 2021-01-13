package fr.olympa.api.report;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.spigot.OlympaCore;

public class ReportReason {

	public static ReportReason CHAT = new ReportReason(1, "Chat Abusif");
	public static ReportReason CHEAT_AURA = new ReportReason(2, "Cheat Combat");
	public static ReportReason CHEAT_XRAY = new ReportReason(3, "Cheat XRay");
	public static ReportReason CHEAT_FLY = new ReportReason(4, "Cheat Fly");
	public static ReportReason OTHER = new ReportReason(5, "Autre");

	private static boolean INIT = false;

	{
		registerReason(ReportReason.class);
	}

	public static void initSpigot() {
		if (INIT)
			return;
		INIT = true;
		if (!LinkSpigotBungee.Provider.link.isSpigot())
			return;
		ReportReason.CHAT.setItem(new OlympaItemBuild(Material.BOOK, "&7Chat abusif").lore("", "&eSpam, Insulte, Provocations, Publicité ..."));
		ReportReason.CHEAT_AURA.setItem(new OlympaItemBuild(Material.GOLDEN_SWORD, "&7Cheat Combat").lore("", "&eKillAura, Aimbot, TriggerBot, AutoClick ...").flag(ItemFlag.HIDE_ATTRIBUTES));
		ReportReason.CHEAT_XRAY.setItem(new OlympaItemBuild(Material.DIAMOND_ORE, "&7Cheat XRay").lore("", "&eMod &cinterdit&e qui permet de voir à travers les blocks."));
		ReportReason.CHEAT_FLY.setItem(new OlympaItemBuild(Material.FEATHER, "&7Cheat Fly").lore("", "&eMod &cinterdit&e qui permet de voler."));
		ReportReason.OTHER.setItem(new OlympaItemBuild(Material.CAULDRON, "&7Autre"));
	}

	public static ReportReason get(int id) {
		return ReportReason.values().stream().filter(itemGui -> itemGui.getId() == id).findFirst().orElse(null);
	}

	//	public static ReportReason get(ItemStack itemStack) {
	//		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	//	}

	public static ReportReason get(String name) {
		return ReportReason.values().stream().filter(itemGui -> itemGui.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static Collection<ReportReason> values() {
		return reportReasons.values();
	}

	public static ReportReason getByReason(String reason) {
		return ReportReason.values().stream().filter(itemGui -> itemGui.getReason().equalsIgnoreCase(reason)).findFirst().orElse(null);
	}

	public static final Map<String, ReportReason> reportReasons = new HashMap<>();

	public static void registerReason(Class<?> clazz) {
		try {
			int initialSize = reportReasons.size();
			for (Field f : clazz.getDeclaredFields())
				if (f.getType() == ReportReason.class && Modifier.isStatic(f.getModifiers())) {
					ReportReason reportReason = (ReportReason) f.get(null);
					reportReason.setName(f.getName());
					reportReasons.put(f.getName(), reportReason);
				}
			OlympaCore.getInstance().sendMessage("Registered " + (reportReasons.size() - initialSize) + " report reason from " + clazz.getName());
		} catch (ReflectiveOperationException ex) {
			OlympaCore.getInstance().sendMessage("Error when registering permissions from class " + clazz.getName());
			ex.printStackTrace();
		}
	}

	int id;
	String reason;
	OlympaItemBuild item;
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	String note;
	boolean active = true;

	private ReportReason(int id, String reason) {
		this.id = id;
		this.reason = reason;
	}

	public int getId() {
		return id;
	}

	public OlympaItemBuild getItem() {
		initSpigot();
		return item;
	}

	public String getReason() {
		return reason;
	}

	public String getReasonUpper() {
		return reason.toUpperCase();
	}

	public String getReasonClear() {
		return reason.replace(" ", "_");
	}

	public void disable() {
		active = false;
	}

	public void setItem(OlympaItemBuild item) {
		this.item = item;
	}
}
