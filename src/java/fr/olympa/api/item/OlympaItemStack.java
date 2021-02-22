package fr.olympa.api.item;

public interface OlympaItemStack {
	OlympaItemStack addLore(String... lore);

	OlympaItemStack addLoreBefore(String... lore);

	OlympaItemStack addName(String name);

	OlympaItemStack breakable();

	OlympaItemStack customModelData(int customModelData);

	OlympaItemStack durability(short durability);

	OlympaItemStack lore(String... lore);

	OlympaItemStack name(String name);

	OlympaItemStack setLore(int i, String lore);

	OlympaItemStack size(int size);

	OlympaItemStack skullowner(String player);

	OlympaItemStack unbreakable();

}