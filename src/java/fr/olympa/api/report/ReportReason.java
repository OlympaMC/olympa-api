package fr.olympa.api.report;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
	public static ReportReason CHEAT_AURA = new ReportReason(2, "Cheat Combat");
	public static ReportReason CHEAT_XRAY = new ReportReason(3, "Cheat XRay");
	public static ReportReason CHEAT_FLY = new ReportReason(4, "Cheat Fly");
	public static ReportReason OTHER = new ReportReason(5, "Autre");

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

	public ReportReason(int id, String reason) {
		this.id = id;
		this.reason = reason;
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
		return false;
	}
}
