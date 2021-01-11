package fr.olympa.api.chat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TxtComponentBuilder {

	private static boolean DEBUG = true; // need to test if cache is usefull

	public static TextComponent of(Prefix prefix, String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(prefix, message).onClick(clickAction, clickActionValue).onHover(hoverAction, contents).build();
	}

	public static TextComponent of(Prefix prefix, String message, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(prefix, message).onHover(hoverAction, contents).build();
	}

	public static TextComponent of(String message, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(message).onHover(hoverAction, contents).build();
	}

	private static String getStringColored(String s) {
		return ColorUtils.color(s);
	}

	private static String join(CharSequence delimiter, CharSequence... elements) {
		return getStringColored(ColorUtils.join(delimiter, elements));
	}

	Prefix prefix;
	String msg;
	ClickEvent.Action clickAction;
	String clickActionValue;
	HoverEvent.Action hoverAction;
	Content[] contents;
	List<TxtComponentBuilder> extras;
	TextComponent cache;
	boolean isConsole = false;
	long timeInit = System.nanoTime();

	public TxtComponentBuilder(String msg, String... args) {
		this((Prefix) null, join(msg, args));
	}

	public TxtComponentBuilder(Prefix prefix, ChatColor color, String msg, String... args) {
		this(prefix, color + join(msg, args));
	}

	public TxtComponentBuilder(Prefix prefix, Map<String, ChatColor> msg) {
		this.prefix = prefix;
		this.msg = msg.entrySet().stream().map(e -> e.getValue() + getStringColored(e.getKey())).collect(Collectors.joining(" "));
	}

	public TxtComponentBuilder(Prefix prefix, String msg, String... args) {
		this.prefix = prefix;
		this.msg = getStringColored(join(msg, args));
	}

	public TxtComponentBuilder onHover(HoverEvent.Action hoverAction, Content... contents) {
		clearCache();
		this.hoverAction = hoverAction;
		this.contents = contents;
		return this;
	}

	public TxtComponentBuilder console() {
		clearCache();
		isConsole = true;
		return this;
	}

	public TxtComponentBuilder console(boolean isConsole) {
		clearCache();
		this.isConsole = isConsole;
		return this;
	}

	public TxtComponentBuilder onHoverText(String contents, String... args) {
		return onHover(HoverEvent.Action.SHOW_TEXT, new Text(join(contents, args)));
	}

	public TxtComponentBuilder onClick(ClickEvent.Action clickAction, String clickActionValue) {
		clearCache();
		this.clickAction = clickAction;
		this.clickActionValue = getStringColored(clickActionValue);
		return this;
	}

	public TxtComponentBuilder onClickCommand(String clickActionValue, String... args) {
		return onClick(ClickEvent.Action.RUN_COMMAND, join(clickActionValue, args));
	}

	public TxtComponentBuilder onClickSuggest(String clickActionValue, String... args) {
		return onClick(ClickEvent.Action.SUGGEST_COMMAND, join(clickActionValue, args));
	}

	public TxtComponentBuilder onClickCopy(String clickActionValue, String... args) {
		return onClick(ClickEvent.Action.COPY_TO_CLIPBOARD, join(clickActionValue, args));
	}

	public TxtComponentBuilder onClickUrl(String clickActionValue, String... args) {
		return onClick(ClickEvent.Action.OPEN_URL, join(clickActionValue, args));
	}

	public TxtComponentBuilder extra(TxtComponentBuilder extra) {
		clearCache();
		extras.add(extra);
		return this;
	}

	private TextComponent clearCache() {
		return cache = null;
	}

	public TextComponent build() {
		if (cache != null) {
			if (DEBUG)
				LinkSpigotBungee.Provider.link.sendMessage("§dTxtComponentBuilder builder CACHEtook %s ms.", (System.nanoTime() - timeInit) / 1000000L);
			return cache;
		}
		StringBuilder txtBuilder = new StringBuilder();
		if (prefix != null)
			txtBuilder.append(prefix);
		if (msg != null)
			txtBuilder.append(msg);
		TextComponent text = new TextComponent(TextComponent.fromLegacyText(txtBuilder.toString()));
		new Text("").getValue();
		if (isConsole) {
			if (contents != null && contents.length != 0 && contents instanceof Text[])
				text.addExtra(new TextComponent(TextComponent.fromLegacyText(String.format("&r(%s)", ((Text) contents[0]).getValue()))));
		} else {
			if (clickAction != null && clickActionValue != null)
				text.setClickEvent(new ClickEvent(clickAction, clickActionValue));
			if (hoverAction != null && contents != null)
				text.setHoverEvent(new HoverEvent(hoverAction, contents));
		}
		for (TxtComponentBuilder extra : extras)
			text.addExtra(extra.build());
		if (DEBUG)
			LinkSpigotBungee.Provider.link.sendMessage("§dTxtComponentBuilder builder took %s ms.", (System.nanoTime() - timeInit) / 1000000L);
		return cache = text;
	}
}
