package fr.olympa.api.report;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.item.OlympaItemStack;
import fr.olympa.core.spigot.OlympaCore;

public class ReportReason {

	public static final Map<String, ReportReason> reportReasons = new HashMap<>();

	public static void registerReason(Class<?> clazz) {
		try {
			int initialSize = reportReasons.size();
			for (Field f : clazz.getDeclaredFields())
				if (f.getType() == ReportReason.class && Modifier.isStatic(f.getModifiers())) {
					ReportReason reportReason = (ReportReason) f.get(null);
					Optional<Entry<String, ReportReason>> already = reportReasons.entrySet().stream().filter(rr -> rr.getValue().getId() == reportReason.getId()).findFirst();
					if (already.isPresent()) {
						OlympaCore.getInstance().sendMessage("&cCan't register ReportReason &4%s&c with id &4%d&c. Already used by &4%s&c.", reportReason.getName(), reportReason.getId(), already.get().getKey());
						continue;
					}
					reportReason.setName(f.getName());
					reportReasons.put(f.getName(), reportReason);
				}
			OlympaCore.getInstance().sendMessage("Registered " + (reportReasons.size() - initialSize) + " report reason from " + clazz.getName());
		} catch (ReflectiveOperationException e) {
			OlympaCore.getInstance().sendMessage("Error when registering permissions from class " + clazz.getName());
			e.printStackTrace();
		}
	}

	public static ReportReason CHAT = new ReportReason(1, "Chat Abusif");
	public static ReportReason PV_SPAM = new ReportReason(2, "MP Abusif");
	public static ReportReason INCORRECT_SKIN_OR_NAME = new ReportReason(3, "Pseudo/Skin incorrect");
	public static ReportReason CHEAT_AURA = new ReportReason(3, "Cheat Combat");
	public static ReportReason CHEAT_XRAY = new ReportReason(4, "Cheat XRay");
	public static ReportReason CHEAT_FLY = new ReportReason(5, "Cheat Fly");
	public static ReportReason ANTI_AFK = new ReportReason(6, "Anti AFK");
	public static ReportReason OTHER = new ReportReason(7, "Autre");

	static {
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			ReportReason.CHAT.setItem(new OlympaItemBuild(Material.BOOK, "&7Chat abusif").lore("", "&eSpam, Insulte, Provocations, Publicité ..."));
			ReportReason.PV_SPAM.setItem(new OlympaItemBuild(Material.WRITABLE_BOOK, "&7Message privés abusifs").lore("", "&eSpam, Insulte, Provocations, Publicité ..."));
			ReportReason.INCORRECT_SKIN_OR_NAME.setItem(new OlympaItemBuild(Material.PLAYER_HEAD, "&7Pseudo ou Skin incorrect").lore("", "&ePseudo insultant, provocant", "&eou skin choquant."));
			ReportReason.CHEAT_AURA.setItem(new OlympaItemBuild(Material.GOLDEN_SWORD, "&7Cheat Combat").lore("", "&eKillAura, Aimbot, TriggerBot, AutoClick ...").flag(ItemFlag.HIDE_ATTRIBUTES));
			ReportReason.CHEAT_XRAY.setItem(new OlympaItemBuild(Material.DIAMOND_ORE, "&7Cheat XRay").lore("", "&eMod &cinterdit&e qui permet de voir à travers les blocks."));
			ReportReason.CHEAT_FLY.setItem(new OlympaItemBuild(Material.FEATHER, "&7Cheat Fly").lore("", "&eMod &cinterdit&e qui permet de voler."));
			ReportReason.OTHER.setItem(new OlympaItemBuild(Material.CAULDRON, "&7Autre"));
		}
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

	int id;
	String reason;
	String name;

	String note;
	boolean active = true;
	OlympaItemStack item;

	public ReportReason(int id, String name, String reason) {
		this.id = id;
		this.name = name;
		this.reason = reason;
	}

	public ReportReason(int id, String reason) {
		this.id = id;
		this.reason = reason;
	}

	public OlympaItemStack getItem() {
		return item;
	}

	public void setItem(OlympaItemStack item) {
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
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

	public boolean hasItem() {
		return item != null;
	}

	public boolean isSame(ReportReason rr) {
		return id == rr.getId() || getName().equals(rr.getName());
	}

	public boolean needUpdate(ReportReason rr) {
		return id != rr.getId() || !getName().equals(rr.getName()) || !getReason().equals(rr.getReason());
	}
}
