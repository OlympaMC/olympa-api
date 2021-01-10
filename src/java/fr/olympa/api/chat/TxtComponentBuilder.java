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

	Prefix prefix;
	String msg;
	ClickEvent.Action clickAction;
	String clickActionValue;
	HoverEvent.Action hoverAction;
	Content[] contents;
	List<TxtComponentBuilder> extras;
	TextComponent cache;
	long timeInit = System.nanoTime();

	public TxtComponentBuilder(String msg) {
		this(null, msg);
	}

	public TxtComponentBuilder(Prefix prefix, ChatColor color, String msg) {
		this(prefix, color + msg);
	}

	public TxtComponentBuilder(Prefix prefix, Map<String, ChatColor> msg) {
		this.prefix = prefix;
		this.msg = msg.entrySet().stream().map(e -> e.getValue() + getStringColored(e.getKey())).collect(Collectors.joining(" "));
	}

	public TxtComponentBuilder(Prefix prefix, String msg) {
		this.prefix = prefix;
		this.msg = getStringColored(msg);
	}

	public TxtComponentBuilder onHover(HoverEvent.Action hoverAction, Content... contents) {
		clearCache();
		this.hoverAction = hoverAction;
		this.contents = contents;
		return this;
	}

	public TxtComponentBuilder onHoverText(String contents) {
		return onHover(HoverEvent.Action.SHOW_TEXT, new Text(getStringColored(contents)));
	}

	public TxtComponentBuilder onClick(ClickEvent.Action clickAction, String clickActionValue) {
		clearCache();
		this.clickAction = clickAction;
		this.clickActionValue = getStringColored(clickActionValue);
		return this;
	}

	public TxtComponentBuilder onClickCommand(String clickActionValue) {
		return onClick(ClickEvent.Action.RUN_COMMAND, clickActionValue);
	}

	public TxtComponentBuilder onClickSuggest(String clickActionValue) {
		return onClick(ClickEvent.Action.SUGGEST_COMMAND, clickActionValue);
	}

	public TxtComponentBuilder onClickCopy(String clickActionValue) {
		return onClick(ClickEvent.Action.COPY_TO_CLIPBOARD, clickActionValue);
	}

	public TxtComponentBuilder onClickUrl(String clickActionValue) {
		return onClick(ClickEvent.Action.OPEN_URL, clickActionValue);
	}

	public TxtComponentBuilder extra(TxtComponentBuilder extra) {
		clearCache();
		extras.add(extra);
		return this;
	}

	private String getStringColored(String s) {
		return ColorUtils.color(s);
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
		if (clickAction != null && clickActionValue != null)
			text.setClickEvent(new ClickEvent(clickAction, clickActionValue));
		if (hoverAction != null && contents != null)
			text.setHoverEvent(new HoverEvent(hoverAction, contents));
		for (TxtComponentBuilder extra : extras)
			text.addExtra(extra.build());
		if (DEBUG)
			LinkSpigotBungee.Provider.link.sendMessage("§dTxtComponentBuilder builder took %s ms.", (System.nanoTime() - timeInit) / 1000000L);
		return cache = text;
	}
}
