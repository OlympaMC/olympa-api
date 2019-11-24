package fr.olympa.api.editor.parsers;

import java.math.BigDecimal;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;

public class NumberParser implements TextParser<Number> {
	
	private Class<? extends Number> numberType;
	private boolean positive;
	private boolean noZero;
	
	public NumberParser(Class<? extends Number> numberType, boolean positive){
		this(numberType, positive, false);
	}
	
	public NumberParser(Class<? extends Number> numberType, boolean positive, boolean noZero){
		this.numberType = numberType;
		this.positive = positive;
		this.noZero = noZero;
	}
	
	
	public Number parse(Player p, String msg) {
		try{
			String tname = numberType != Integer.class ? numberType.getSimpleName() : "Int";
			Number number = (Number) numberType.getDeclaredMethod("parse" + tname, String.class).invoke(null, msg);
			if (positive || noZero){
				int compare = new BigDecimal(msg).compareTo(new BigDecimal(0));
				if (positive && compare < 0){
					Prefix.BAD.sendMessage(p, "Vous devez entrer un nombre positif.");
					return null;
				}else if (noZero && compare == 0) {
					Prefix.BAD.sendMessage(p, "Vous devez entrer un nombre autre que zéro.");
					return null;
				}
			}
			return number;
		}catch (Exception ex) {}
		Prefix.BAD.sendMessage(p, "Le nombre que vous avez entré est incorrect.");
		return null;
	}

}
