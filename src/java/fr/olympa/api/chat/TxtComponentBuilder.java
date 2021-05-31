package fr.olympa.api.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TxtComponentBuilder {

	//	private static boolean DEBUG = true; // need to test if cache is usefull

	public static TextComponent of(Prefix prefix, String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content content, Object... args) {
		return new TxtComponentBuilder(prefix, message, args).onClick(clickAction, clickActionValue).onHover(hoverAction, new Content[] { content }).build();
	}

	public static TextComponent of(Prefix prefix, String message, String runCommandValue, String hoverText, Object... args) {
		return new TxtComponentBuilder(prefix, message, args).onClick(ClickEvent.Action.RUN_COMMAND, runCommandValue).onHover(HoverEvent.Action.SHOW_TEXT, new Text(getStringColored(hoverText))).build();
	}

	public static TextComponent of(Prefix prefix, String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(prefix, message).onClick(clickAction, clickActionValue).onHover(hoverAction, contents).build();
	}

	public static TextComponent of(Prefix prefix, String message, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(prefix, message).onHover(hoverAction, contents).build();
	}

	public static TextComponent of(Prefix prefix, String message, Object... object) {
		return new TxtComponentBuilder(prefix, message, object).build();
	}

	public static TextComponent of(String message, HoverEvent.Action hoverAction, Content... contents) {
		return new TxtComponentBuilder(message).onHover(hoverAction, contents).build();
	}

	public static HoverEvent hoverEvent(HoverEvent.Action hoverAction, String message, Object... args) {
		return new HoverEvent(hoverAction, new Text(format(message, args)));
	}

	private static String getStringColored(String s) {
		return s != null ? ColorUtils.color(s) : null;
	}

	private static TextComponent getText(String format, Object... args) {
		return new TextComponent(TextComponent.fromLegacyText(format(format, args)));
	}

	private static String format(String format, Object... args) {
		return format != null ? ColorUtils.format(format, args) : null;
	}

	Prefix prefix;
	String msg;
	ClickEvent.Action clickAction;
	String clickActionValue;
	HoverEvent.Action hoverAction;
	Content[] contents;
	TxtComponentBuilder extrasSeparator;
	List<TxtComponentBuilder> extras;
	//	TextComponent cache;
	boolean isConsole = false;
	long timeInit = System.nanoTime();

	public TxtComponentBuilder() {
		this(null);
	}

	public TxtComponentBuilder(Prefix prefix, ChatColor color, String msg, Object... args) {
		this(prefix, color + msg, args);
	}

	public TxtComponentBuilder(String msg, Object... args) {
		this((Prefix) null, msg, args);
	}

	public TxtComponentBuilder(Prefix prefix, Map<String, ChatColor> msg) {
		this.prefix = prefix;
		this.msg = msg.entrySet().stream().map(e -> e.getValue() + getStringColored(e.getKey())).collect(Collectors.joining(" "));
	}

	public TxtComponentBuilder(Prefix prefix, String msg, Object... args) {
		this.prefix = prefix;
		this.msg = getStringColored(format(msg, args));
	}

	public TxtComponentBuilder onHover(HoverEvent.Action hoverAction, Content... contents) {
		clearCache();
		this.hoverAction = hoverAction;
		this.contents = contents;
		return this;
	}

	public boolean isEmpty() {
		return prefix == null && (msg == null || msg.isEmpty()) && clickAction == null && hoverAction == null && (extras == null || extras.isEmpty());
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

	public TxtComponentBuilder onHoverText(String contents, Object... args) {
		return onHover(HoverEvent.Action.SHOW_TEXT, new Text(format(contents, args)));
	}

	public TxtComponentBuilder onHoverText(BaseComponent... contents) {
		return onHover(HoverEvent.Action.SHOW_TEXT, new Text(contents));
	}

	public TxtComponentBuilder onClick(ClickEvent.Action clickAction, String clickActionValue) {
		clearCache();
		this.clickAction = clickAction;
		this.clickActionValue = getStringColored(clickActionValue);
		return this;
	}

	public TxtComponentBuilder onClickCommand(String clickActionValue, Object... args) {
		return onClick(ClickEvent.Action.RUN_COMMAND, format(clickActionValue, args));
	}

	public TxtComponentBuilder onClickSuggest(String clickActionValue, Object... args) {
		return onClick(ClickEvent.Action.SUGGEST_COMMAND, format(clickActionValue, args));
	}

	public TxtComponentBuilder onClickCopy(String clickActionValue, Object... args) {
		return onClick(ClickEvent.Action.COPY_TO_CLIPBOARD, format(clickActionValue, args));
	}

	public TxtComponentBuilder onClickUrl(String clickActionValue, Object... args) {
		return onClick(ClickEvent.Action.OPEN_URL, format(clickActionValue, args));
	}

	public TxtComponentBuilder extra(TxtComponentBuilder extra) {
		clearCache();
		if (extras == null)
			extras = new ArrayList<>();
		extras.add(extra);
		return this;
	}

	public TxtComponentBuilder extra(String text, Object... args) {
		clearCache();
		if (extras == null)
			extras = new ArrayList<>();
		extras.add(new TxtComponentBuilder(text, args));
		return this;
	}

	public TxtComponentBuilder extraSpliter(String text, Object... args) {
		clearCache();
		extrasSeparator = new TxtComponentBuilder(text, args);
		return this;
	}

	public TxtComponentBuilder extraSpliterBN() {
		clearCache();
		extrasSeparator = new TxtComponentBuilder("\n");
		return this;
	}

	private TextComponent clearCache() {
		//		return cache = null;
		return null;
	}

	public TextComponent build() {
		//		if (cache != null) {
		//			if (DEBUG)
		//				LinkSpigotBungee.Provider.link.sendMessage("§dTxtComponentBuilder builder CACHE took %s ms.", (System.nanoTime() - timeInit) / 1000000L);
		//			return cache;
		//		}
		TextComponent text;
		boolean isNotEmpty = !isEmpty();
		if (isNotEmpty) {
			StringBuilder txtBuilder = new StringBuilder();
			if (prefix != null)
				txtBuilder.append(prefix);
			if (msg != null)
				txtBuilder.append(msg);
			if (!txtBuilder.toString().isEmpty())
				text = getText(txtBuilder.toString());
			else
				text = new TextComponent();
			if (isConsole) {
				if (contents != null && contents.length != 0 && contents[0] instanceof Text)
					text.addExtra(getText(String.format("&r(%s&r) ", ((Text) contents[0]).getValue())));
				if (clickAction != null && clickActionValue != null && !clickActionValue.isBlank())
					text.addExtra(getText(String.format("&r(CLICK %s&r) ", clickActionValue)));
			} else {
				if (hoverAction != null && contents != null && contents.length > 0)
					text.setHoverEvent(new HoverEvent(hoverAction, contents));
				if (clickAction != null && clickActionValue != null && !clickActionValue.isBlank())
					text.setClickEvent(new ClickEvent(clickAction, clickActionValue));
			}
		} else
			text = new TextComponent();
		TextComponent extrasSeparatorBuild = null;
		if (extras != null) {
			if (extrasSeparator != null && !extrasSeparator.isEmpty()) {
				extrasSeparatorBuild = extrasSeparator.build();
				if (isNotEmpty)
					text.addExtra(extrasSeparatorBuild);
			}
			/*if (isNotEmpty && extrasSeparatorBuild != null)
				text.addExtra(extrasSeparatorBuild);*/ // pourquoi mettre le séparateur ici ? le but d'un séparateur c'est d'être entre, pas avant
			for (int i = 0; i < extras.size(); i++) {
				text.addExtra(extras.get(i).build());
				if (extrasSeparatorBuild != null && i < extras.size() - 1)
					text.addExtra(extrasSeparatorBuild);
			}
		}
		//		if (DEBUG)
		//			LinkSpigotBungee.Provider.link.sendMessage("§dTxtComponentBuilder builder took %s ms.", (System.nanoTime() - timeInit) / 1000000L);
		//		return cache = text;
		return text;
	}
}
