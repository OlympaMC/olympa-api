package fr.olympa.api.trades;

import java.util.List;

import fr.olympa.api.economy.MoneyPlayerInterface;

public interface TradePlayerInterface extends MoneyPlayerInterface {
	
	
	default List<UniqueTradeHistory> getTradesHistory() {
		throw new IllegalAccessError("Trades history hasn't been yet implemented!");
	}
	
	public TradeBag<? extends TradePlayerInterface> getTradeBag();
}
