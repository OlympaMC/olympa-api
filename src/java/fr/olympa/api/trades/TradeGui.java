package fr.olympa.api.trades;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.editor.Editor;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;

public class TradeGui<T extends TradePlayerInterface> implements InventoryHolder {
	private static int countdownInit = 5;

	private static int stepIndicatorSlot = 0;
	private static int countdownIndicatorSlot = 1;
	private static int nextStepButtonSlot = 2;
	private static int moneySelectButtonSlot = 3;

	private static int otherStepIndicatorSlot = 5;
	private static int otherMoneyIndicatorSlot = 6;

	private static ItemStack playerBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.LIGHT_GRAY), "§1§2§3§4§5§9§8§7§6"));
	private static ItemStack otherBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GRAY), "§1§2§3§4§5§9§8§7§6"));
	private static ItemStack separatorItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GREEN), "§1§2§3§4§5§9§8§7§6"));
	//private static ItemStack topBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GREEN), "§1§2§3§4§5§9§8§7§6"));

	private static ItemStack defaultPlayerMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_INGOT, "§eTu n'envoies pas d'argent", "§7Cliques pour sélectionner", "§7un montant à échanger"));
	private static ItemStack defaultOtherMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_INGOT, "§eTu ne reçois pas d'argent", "§7Ton partenaire n'a pas sélectionné", "§7d'argent à échanger avec toi"));

	private static ItemStack nextStepEnabledItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GREEN_CONCRETE, "§aPasser à l'étape suivante"));
	private static ItemStack nextStepDisabledItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.YELLOW_CONCRETE, "§cAttend que ton partenaire change d'étape"));
	//private static ItemStack lastStepReachedItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.RED_CONCRETE, "§cAttend que ton partenaire change d'étape"));
	
	private ItemStack countdownTimerItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.CLOCK, "§eFinalisation dans..."));
	
	private ItemStack playerMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_BLOCK, "§6Tu enverras : XXX", "§7Lorsque l'échange sera conclu ton", "§7compte sera débité de ce montant", "§7et sera crédité à ton partenaire"));
	private ItemStack otherMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_BLOCK, "§6Tu recevras : XXX", "§7Lorsque l'échange sera conclu ton", "§7compte sera crédité de ce montant"));
	
	@SuppressWarnings("unused")
	private UniqueTradeManager<T> manager;
	
	private Inventory inv;
	private double playerMoney = 0;
	
	private TradeStep otherStep = TradeStep.FILLING;
	private TradeStep step = TradeStep.FILLING;
	
	private int currentCountdown = countdownInit;
	
	private List<ItemStack> items = new ArrayList<ItemStack>();
	
	private T p;
	private TextEditor<Double> moneyEditor;
	
	@SuppressWarnings("deprecation")
	public TradeGui(UniqueTradeManager<T> trade, T p, T other) {
		this.inv = Bukkit.createInventory(this, 9 * 6, "Echange avec " + other.getName());

		this.p = p;
		this.manager = trade;
		moneyEditor = new TextEditor<Double>(p.getPlayer(), d -> {
			trade.getOtherTrade(this).setOtherMoney(d);
			setMoney(d);
			p.getPlayer().openInventory(inv);
			
		}, () -> trade.endTrade(false), false, (player, msg) -> {
			try {
				return Double.valueOf(msg);	
			}catch (Exception ex) {
				return 0d;
			}
		});
		
		for (int i = 0 ; i < 9 ; i++)
			inv.setItem(i, separatorItem);
		
		for (int i = 9 ; i < inv.getSize() ; i++)
			if (i % 9 < 4)
				inv.setItem(i, playerBlankItem);
			else if (i % 9 > 4)
				inv.setItem(i, otherBlankItem);
			else
				inv.setItem(i, separatorItem);
		
		inv.setItem(stepIndicatorSlot, step.indicator);
		inv.setItem(otherStepIndicatorSlot, step.indicatorOther);

		inv.setItem(moneySelectButtonSlot, defaultPlayerMoneyItem);
		inv.setItem(otherMoneyIndicatorSlot, defaultOtherMoneyItem);	
		
		inv.setItem(nextStepButtonSlot, nextStepEnabledItem);
	}

	
	
	boolean addItemOnPlayerSide(ItemStack it) {
		for (Entry<Integer, ItemStack> e : inv.addItem(it.clone()).entrySet()) 
			if (getNextFreePlayerSlot() == -1) {
				e.getValue().setAmount(e.getValue().getAmount() - it.getAmount());
				return false;	
			}else {
				inv.setItem(getNextFreePlayerSlot(), e.getValue());
				items.add(it);
			}
		return true;
	}
	
	void addItemOnOtherSide(ItemStack it) {
		for (Entry<Integer, ItemStack> e : inv.addItem(UniqueTradeManager.getAsLocked(it)).entrySet()) 
			if (getNextFreeOtherSlot() != -1)
				inv.setItem(getNextFreeOtherSlot(), e.getValue());
	}

	
	boolean removeItemOnPlayerSide(ItemStack it) {
		if (!items.contains(it))
			return false;
		
		inv.setItem(inv.first(it), playerBlankItem);
		items.remove(it);
		return true; 
	}
	
	void removeItemOnOtherSide(ItemStack it) {
		int i = inv.first(UniqueTradeManager.getAsLocked(it));
		if (i > -1)
			inv.setItem(i, otherBlankItem);
	}

	
	 
	void openMoneyEditor() {
		moneyEditor.enterOrLeave();
		p.getPlayer().closeInventory();
		Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "Sélectionnez le montant à ajouter à l'échange :");
	}
	
	

	private int getNextFreePlayerSlot() {
		return inv.first(playerBlankItem);
	}
	private int getNextFreeOtherSlot() {
		return inv.first(otherBlankItem);
	}
	
	
	private void setMoney(double money) {
		playerMoney = money;
		if (money == 0)
			inv.setItem(moneySelectButtonSlot, defaultPlayerMoneyItem);
		else
			inv.setItem(moneySelectButtonSlot, ItemUtils.name(playerMoneyItem, "§6Tu enverras " + money + OlympaMoney.OMEGA));
	}
	
	private void setOtherMoney(double money) {
		if (money == 0)
			inv.setItem(otherMoneyIndicatorSlot, defaultOtherMoneyItem);
		else
			inv.setItem(otherMoneyIndicatorSlot, ItemUtils.name(otherMoneyItem, "§6Tu recevras " + money + OlympaMoney.OMEGA));
	}
	
	public double getPlayerMoney() {
		return playerMoney;
	}
	
	
	
	boolean nextTradeStep() {		
		if (!step.isLastStep && (otherStep == step || otherStep == TradeStep.getNext(step))) {
			step = TradeStep.getNext(step);
			inv.setItem(stepIndicatorSlot, step.indicator);
			
			if (otherStep != step)
				inv.setItem(nextStepButtonSlot, nextStepDisabledItem);
			return true;
		}else
			return false;
	}
	
	void setNextStepOther(TradeStep newOtherStep) {
		inv.setItem(otherStepIndicatorSlot, newOtherStep.indicatorOther);
		inv.setItem(nextStepButtonSlot, nextStepEnabledItem);
		otherStep = newOtherStep;
	}
	
	public TradeStep getStep() {
		return step;
	}

	

	public static boolean isMoneySelectionButton(int rawSlot) {
		return rawSlot == moneySelectButtonSlot;
	}
	
	public static boolean isNextStepButton(int rawSlot) {
		return rawSlot == nextStepButtonSlot;
	}
	


	/**
	 * 
	 * @return true if timer has been updated, false if it has ended
	 */
	boolean updateTimer() {
		if (currentCountdown == 0)
			return false;
		
		inv.setItem(countdownIndicatorSlot, ItemUtils.lore(countdownTimerItem, "§7" + currentCountdown + (currentCountdown == 1 ? " seconde" : " secondes")));
		currentCountdown--;
		return true;
	}
	
	
	
	List<ItemStack> getPlayerItems() {
		return items;
	}
	
	void endTrade() {
		inv.clear();
		inv.getViewers().forEach(p -> p.closeInventory());
		Editor.leave(p.getPlayer());
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	
	public static enum TradeStep {
		FILLING(false, ItemUtils.item(Material.HOPPER, "§aSélection des objets", new String[]{"§7Ajoute les objets et l'argent", "§7que tu souhaites échanger"})),
		COMPARING(false, ItemUtils.item(Material.CHEST, "§aComparaison des objets", new String[]{"§7Tu ne peux plus modifier", "§7l'échange, vérifie que tu ne", "§7t'es pas trompé !"})),
		TIMER(true, ItemUtils.item(Material.MINECART, "§aEchange des objets", new String[]{"§7Les objets et l'argent vont être", "§7échangés dans quelques secondes", " ", "§cPour annuler l'échange ferme l'inventaire"})),
		;
		
		public boolean isLastStep;
		public ItemStack indicator;
		public ItemStack indicatorOther;
		
		private TradeStep(boolean isLastStep, ItemStack item) {
			this.isLastStep = isLastStep;
			this.indicator = UniqueTradeManager.getAsLocked(item);
			this.indicatorOther = ItemUtils.lore(indicator.clone());
		}



		public static TradeStep getNext(TradeStep step) {
			switch (step) {
			case FILLING:
				return COMPARING;
			case COMPARING:
				return TIMER;
			default:
				return null;
			}
		}
	}
}





