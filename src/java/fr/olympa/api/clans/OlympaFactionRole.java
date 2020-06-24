package fr.olympa.api.clans;

import java.util.Arrays;

public enum OlympaFactionRole {
	
	LEADER(1, 10, "Leader", "**"),
	OFFICER(2, 5, "Officier", "*"),
	MEMBER(2, 2, "Membre", "+"),
	RECRUT(3, 0, "Recrue", "-");
	
	public static OlympaFactionRole get(int id) {
		return Arrays.stream(OlympaFactionRole.values()).filter(role -> role.getId() == id).findFirst().orElse(null);
	}
	
	int id;
	int power;
	String name;
	
	String tag;
	
	private OlympaFactionRole(int id, int power, String name, String tag) {
		this.id = id;
		this.power = power;
		this.name = name;
		this.tag = tag;
	}
	
	public int getId() {
		return id;
	}
	
	public OlympaFactionRole getLower() {
		OlympaFactionRole role = null;
		int i = id;
		while (i != 1 && role == null)
			role = get(--i);
		return role;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPower() {
		return power;
	}
	
	public String getTag() {
		return tag;
	}
	
	public OlympaFactionRole getUpper() {
		OlympaFactionRole role = null;
		int i = id;
		while (i != 3 && role == null)
			role = get(++i);
		return role;
	}
	
	public boolean hasPermission(OlympaFactionRole role) {
		return role.getPower() <= power;
	}
}
