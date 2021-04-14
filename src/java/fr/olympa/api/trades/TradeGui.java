package fr.olympa.api.trades;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.item.ItemUtils;

public class TradeGui<T extends MoneyPlayerInterface> implements InventoryHolder {
	private static int countdownInit = 5;

	private static int stepIndicatorSlot = 0;
	private static int countdownIndicatorSlot = 1;
	private static int nextStepButtonSlot = 2;
	private static int moneySelectButtonSlot = 3;

	private static int otherStepIndicatorSlot = 4;
	private static int otherMoneyIndicatorSlot = 6;

	private static ItemStack playerBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.LIGHT_GRAY), "§1§2§3§4§5§9§8§7§6"));
	private static ItemStack otherBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GRAY), "§1§2§3§4§5§9§8§7§6"));
	private static ItemStack middleBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GREEN), "§1§2§3§4§5§9§8§7§6"));
	private static ItemStack topBlankItem = UniqueTradeManager.getAsLocked(ItemUtils.name(ItemUtils.itemSeparator(DyeColor.GREEN), "§1§2§3§4§5§9§8§7§6"));

	private static ItemStack defaultPlayerMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.COAL, "§eTu n'envoies pas d'argent", "§7Cliques pour sélectionner", "§7un montant à échanger"));
	private static ItemStack defaultOtherMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.COAL, "§eTu ne reçois pas d'argent", "§7Ton partenaire n'a pas sélectionné", "§7d'argent à échanger avec toi"));

	private static ItemStack nextStepEnabledItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GREEN_CONCRETE, "§aPasser à l'étape suivante"));
	private static ItemStack nextStepDisabledItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.YELLOW_CONCRETE, "§cAttend que ton partenaire change d'étape"));
	//private static ItemStack lastStepReachedItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.RED_CONCRETE, "§cAttend que ton partenaire change d'étape"));
	
	private ItemStack countdownTimerItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.CLOCK, "§eFinalisation dans..."));
	
	private ItemStack playerMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_INGOT, "§6Tu enverras : XXX", "§7Lorsque l'échange sera conclu ton", "§7compte sera débité de ce montant", "§7et sera crédité à ton partenaire"));
	private ItemStack otherMoneyItem = UniqueTradeManager.getAsLocked(ItemUtils.item(Material.GOLD_INGOT, "§6Tu recevras : XXX", "§7Lorsque l'échange sera conclu ton", "§7compte sera crédité de ce montant"));
	
	private UniqueTradeManager<T> manager;
	
	private Inventory inv;
	private double playerMoney = 0;
	
	private TradeStep otherStep = TradeStep.FILLING;
	private TradeStep step = TradeStep.FILLING;
	
	private int currentCountdown = countdownInit;
	
	@SuppressWarnings("deprecation")
	public TradeGui(UniqueTradeManager<T> manager, T p) {
		this.inv = Bukkit.createInventory(this, 9 * 6, "Echange avec " + manager.getOtherPlayer(p).getName());

		this.manager = manager;
		
		inv.setItem(stepIndicatorSlot, step.indicator);
		inv.setItem(otherStepIndicatorSlot, step.indicator);
		
		if (manager.getMoneySymbol() != null) {
			inv.setItem(moneySelectButtonSlot, defaultPlayerMoneyItem);
			inv.setItem(otherMoneyIndicatorSlot, defaultOtherMoneyItem);	
		}
		
		inv.setItem(nextStepButtonSlot, nextStepEnabledItem);
		
		for (int i = 0 ; i < 9 ; i++)
			inv.setItem(i, topBlankItem);
		
		for (int i = 9 ; i < inv.getSize() ; i++)
			if (i / 9 < 4)
				inv.setItem(i, playerBlankItem);
			else if (i / 9 > 4)
				inv.setItem(i, otherBlankItem);
			else
				inv.setItem(i, middleBlankItem);
	}

	
	
	public boolean addItemOnPlayerSide(ItemStack it) {
		for (Entry<Integer, ItemStack> e : inv.addItem(it.clone()).entrySet()) 
			if (getNextFreePlayerSlot() == -1) {
				e.getValue().setAmount(e.getValue().getAmount() - it.getAmount());
				return false;	
			}
			else
				inv.setItem(getNextFreePlayerSlot(), e.getValue());
		
		
		return true;
	}
	
	public void addItemOnOtherSide(ItemStack it) {
		for (Entry<Integer, ItemStack> e : inv.addItem(UniqueTradeManager.getAsLocked(it)).entrySet()) 
			if (getNextFreeOtherSlot() != -1)
				inv.setItem(getNextFreeOtherSlot(), e.getValue());
	}

	
	public boolean removeItemOnPlayerSide(ItemStack it) {
		int i = inv.first(it);
		if (i == -1)
			return false;
		
		inv.setItem(i, playerBlankItem);
		return true; 
	}
	
	public void removeItemOnOtherSide(ItemStack it) {
		int i = inv.first(UniqueTradeManager.getAsLocked(it));
		if (i > -1)
			inv.setItem(i, otherBlankItem);
	}

	

	private int getNextFreePlayerSlot() {
		return inv.first(playerBlankItem);
	}
	private int getNextFreeOtherSlot() {
		return inv.first(otherBlankItem);
	}
	
	
	public void setPlayerMoney(double money) {
		if (manager.getMoneySymbol() == null)
			return;
		
		playerMoney = money;
		if (money == 0)
			inv.setItem(moneySelectButtonSlot, defaultPlayerMoneyItem);
		else
			inv.setItem(moneySelectButtonSlot, ItemUtils.name(playerMoneyItem, "§6Tu enverras " + money + manager.getMoneySymbol()));
	}
	
	public void setOtherMoney(double money) {
		if (manager.getMoneySymbol() == null)
			return;
		
		if (money == 0)
			inv.setItem(otherMoneyIndicatorSlot, defaultOtherMoneyItem);
		else
			inv.setItem(otherMoneyIndicatorSlot, ItemUtils.name(otherMoneyItem, "§6Tu recevras " + money + manager.getMoneySymbol()));
	}
	
	public double getPlayerMoney() {
		return playerMoney;
	}
	
	
	
	public boolean nextTradeStep() {		
		if (!step.isLastStep && (otherStep == step || otherStep == TradeStep.getNext(step))) {
			step = TradeStep.getNext(step);
			inv.setItem(stepIndicatorSlot, step.indicator);
			
			if (otherStep != step)
				inv.setItem(otherStepIndicatorSlot, nextStepDisabledItem);
			return true;
		}else
			return false;
	}
	
	public void setNextStepOther(TradeStep newOtherStep) {
		inv.setItem(otherStepIndicatorSlot, newOtherStep.indicator);
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
	public boolean updateTimer() {
		if (currentCountdown == 0)
			return false;
		
		inv.setItem(countdownIndicatorSlot, ItemUtils.lore(countdownTimerItem, "§7" + currentCountdown + (currentCountdown == 1 ? " seconde" : " secondes")));
		currentCountdown--;
		return true;
	}
	
	
	
	public List<ItemStack> getPlayerItems() {
		return Stream.of(inv.getContents()).filter(it -> it != null && !UniqueTradeManager.isLocked(it)).collect(Collectors.toList());
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
		
		private TradeStep(boolean isLastStep, ItemStack item) {
			this.isLastStep = isLastStep;
			this.indicator = item;
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





