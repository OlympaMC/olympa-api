package fr.olympa.api.editor;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

import fr.olympa.api.editor.parsers.TextParser;
import fr.olympa.api.utils.Prefix;

public class TextEditor<R> extends Editor {
	
	protected Consumer<R> run;
	public Runnable cancel;
	private boolean nullable;
	public TextParser<R> parser;
	
	public TextEditor(Player p, Consumer<R> end, Runnable cancel, boolean nullable) {
		this(p, end, cancel, nullable, null);
	}

	public TextEditor(Player p, Consumer<R> end, Runnable cancel, boolean nullable, TextParser<R> parser) {
		super(p);
		this.run = end;
		this.cancel = cancel;
		this.nullable = nullable;
		this.parser = parser;
	}

	public boolean chat(String msg){
		if (msg.equals("cancel")){
			if (cancel == null){
				Prefix.BAD.sendMessage(p, "Cet argument n'est pas supporté.");
				return false;
			}else {
				leave(p);
				cancel.run();
				return true;
			}
		}

		R returnment = null;
		boolean invalid = false;
		if (msg.equals("null")) {
			if (nullable) {
				Prefix.BAD.sendMessage(p, "Cet argument n'est pas supporté.");
				return false;
			}
		}else {
			if (parser != null) {
				try {
					returnment = parser.parse(p, msg);
					if (returnment == null) {
						invalid = true;
					}
				}catch (Exception ex) {
					Prefix.ERROR.sendMessage(p, "Une erreur est survenue durant le traitement de votre message. Veuillez prévenir un administrateur.");
					invalid = true;
					ex.printStackTrace();
				}
			}else returnment = (R) msg;
		}

		if (!invalid){
			leave(p);
			run.accept(returnment);
			return true;
		}
		return false;
	}
	
}
