package fr.olympa.api.report;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.item.OlympaItemBuild;

public enum ReportReason {

	CHAT(1, "Chat Abusif"),
	CHEAT_AURA(2, "Cheat Combat"),
	CHEAT_XRAY(3, "Cheat XRay"),
	CHEAT_FLY(4, "Cheat Fly"),
	OTHER(5, "Autre");

	private static boolean INIT = false;

	public static void initSpigot() {
		if (INIT)
			return;
		INIT = true;
		if (!LinkSpigotBungee.Provider.link.isSpigot())
			return;
		ReportReason.CHAT.item = new OlympaItemBuild(Material.BOOK, "&7Chat abusif").lore("", "&eSpam, Insulte, Provocations, Publicité ...");
		ReportReason.CHEAT_AURA.item = new OlympaItemBuild(Material.GOLDEN_SWORD, "&7Cheat Combat").lore("", "&eKillAura, Aimbot, TriggerBot, AutoClick ...").flag(ItemFlag.HIDE_ATTRIBUTES);
		ReportReason.CHEAT_XRAY.item = new OlympaItemBuild(Material.DIAMOND_ORE, "&7Cheat XRay").lore("", "&eMod &cinterdit&e qui permet de voir à travers les blocks.");
		ReportReason.CHEAT_FLY.item = new OlympaItemBuild(Material.FEATHER, "&7Cheat Fly").lore("", "&eMod &cinterdit&e qui permet de voler.");
		ReportReason.OTHER.item = new OlympaItemBuild(Material.CAULDRON, "&7Autre");
	}

	public static ReportReason get(int id) {
		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.getId() == id).findFirst().orElse(null);
	}

	//	public static ReportReason get(ItemStack itemStack) {
	//		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.getItem().build().isSimilar(itemStack)).findFirst().orElse(null);
	//	}

	public static ReportReason get(String name) {
		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.name().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static ReportReason getByReason(String reason) {
		return Arrays.stream(ReportReason.values()).filter(itemGui -> itemGui.getReason().equalsIgnoreCase(reason)).findFirst().orElse(null);
	}

	int id;
	String reason;
	OlympaItemBuild item;

	String note;

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

	public String getReasonClear() {
		return reason.replace(" ", "_");
	}
}
