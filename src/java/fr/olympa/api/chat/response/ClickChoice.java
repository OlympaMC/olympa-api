package fr.olympa.api.chat.response;

import java.util.LinkedHashMap;

import fr.olympa.api.chat.TxtComponentBuilder;

public class ClickChoice {

	public enum CheckEmoji {
		TRUE("&2&l✔"),
		FALSE("&4&l❌");

		String emoji;

		private CheckEmoji(String emoji) {
			this.emoji = emoji;
		}

		@Override
		public String toString() {
			return emoji;
		}
	}

	LinkedHashMap<String, String> emojiAndCommand;
	String message;
	Object[] args;

	public ClickChoice(LinkedHashMap<String, String> emojiAndCommand, String message) {
		this.emojiAndCommand = emojiAndCommand;
		this.message = message;
	}

	public ClickChoice(String emoji, String command, String message) {
		emojiAndCommand = new LinkedHashMap<>();
		emojiAndCommand.put(emoji, command);
		this.message = message;
	}

	public ClickChoice(String command, String message) {
		emojiAndCommand = new LinkedHashMap<>();
		emojiAndCommand.put(CheckEmoji.TRUE.toString(), command);
		this.message = message;
	}

	public ClickChoice(String commandTrue, String commandFalse, String message, Object... args) {
		emojiAndCommand = new LinkedHashMap<>();
		emojiAndCommand.put(CheckEmoji.FALSE.toString(), commandFalse);
		emojiAndCommand.put(CheckEmoji.TRUE.toString(), commandTrue);
		this.message = message;
		this.args = args;
	}

	public ClickChoice args(Object... args) {
		this.args = args;
		return this;
	}

	public TxtComponentBuilder build() {
		TxtComponentBuilder builder = new TxtComponentBuilder(message, args).extraSpliter(" ");
		emojiAndCommand.forEach((emoji, command) -> builder.extra(new TxtComponentBuilder(emoji).onHoverText("&eClique pour executer l'action " + emoji).onClickCommand(command)));
		return builder;
	}
}
