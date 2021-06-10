package fr.olympa.api.spigot.editor.parsers;

import java.math.BigDecimal;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;

public class NumberParser<T extends Number> implements TextParser<T> {

	public static final NumberParser<Integer> INTEGER_PARSER = new NumberParser<>(Integer.class, false, false);
	public static final NumberParser<Integer> INTEGER_PARSER_POSITIVE = new NumberParser<>(Integer.class, true, false);
	public static final NumberParser<Integer> INTEGER_PARSER_STRICT_POSITIVE = new NumberParser<>(Integer.class, true, true);
	
	private Class<T> numberType;
	private boolean positive;
	private boolean noZero;
	
	public NumberParser(Class<T> numberType, boolean positive) {
		this(numberType, positive, false);
	}
	
	public NumberParser(Class<T> numberType, boolean positive, boolean noZero) {
		this.numberType = numberType;
		this.positive = positive;
		this.noZero = noZero;
	}
	
	public T parse(Player p, String msg) {
		try{
			String tname = numberType != Integer.class ? numberType.getSimpleName() : "Int";
			T number = (T) numberType.getDeclaredMethod("parse" + tname, String.class).invoke(null, msg);
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
