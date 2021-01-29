package fr.olympa.api.report;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.item.OlympaItemBuild;

public class ReportReasonItem extends ReportReason {

	static {
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			((ReportReasonItem) ReportReason.CHAT).setItem(new OlympaItemBuild(Material.BOOK, "&7Chat abusif").lore("", "&eSpam, Insulte, Provocations, Publicité ..."));
			((ReportReasonItem) ReportReason.CHEAT_AURA).setItem(new OlympaItemBuild(Material.GOLDEN_SWORD, "&7Cheat Combat").lore("", "&eKillAura, Aimbot, TriggerBot, AutoClick ...").flag(ItemFlag.HIDE_ATTRIBUTES));
			((ReportReasonItem) ReportReason.CHEAT_XRAY).setItem(new OlympaItemBuild(Material.DIAMOND_ORE, "&7Cheat XRay").lore("", "&eMod &cinterdit&e qui permet de voir à travers les blocks."));
			((ReportReasonItem) ReportReason.CHEAT_FLY).setItem(new OlympaItemBuild(Material.FEATHER, "&7Cheat Fly").lore("", "&eMod &cinterdit&e qui permet de voler."));
			((ReportReasonItem) ReportReason.OTHER).setItem(new OlympaItemBuild(Material.CAULDRON, "&7Autre"));
		}
	}

	OlympaItemBuild item;

	public ReportReasonItem(int id, String reason) {
		super(id, reason);
	}

	public OlympaItemBuild getItem() {
		return item;
	}

	public void setItem(OlympaItemBuild item) {
		this.item = item;
	}

	@Override
	public boolean hasItem() {
		return item != null;
	}
}
