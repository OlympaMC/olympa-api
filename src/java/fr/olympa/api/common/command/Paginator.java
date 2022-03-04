package fr.olympa.api.common.command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.annotations.Beta;

import fr.olympa.api.common.chat.Chat;
import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public abstract class Paginator<T> {

	protected final int pageSize;
	protected final String title;

	public Paginator(int pageSize, String title) {
		this.pageSize = pageSize;
		this.title = title;
	}

	protected abstract List<T> getObjects();

	protected abstract BaseComponent getObjectDescription(T object);

	protected abstract String getCommand(int page);

	public ArgumentParser<T> getArgumentParser() {
		return new ArgumentParser<>((player, arg) -> this.getArgsPageSize(), arg -> this.getPageFromString(arg), x -> String.format("La page &4%s&c n'existe pas", x));
	}

	public List<String> getArgsPageSize() {
		int max = Math.max(1, (int) Math.ceil(getObjects().size() * 1D / pageSize));
		return IntStream.rangeClosed(1, max - 1).mapToObj(String::valueOf).collect(Collectors.toList());
	}

	public BaseComponent getPageDidntExist(int page) {
		if (page <= 1) {
			return new TextComponent(Prefix.DEFAULT_BAD.formatMessage("Il n'y a aucun résultat."));
		}
		return new TextComponent(Prefix.DEFAULT_BAD.formatMessage("La page §4%d §cn'existe pas.", page));
	}

	public BaseComponent getPageFromString(String page) {
		Integer pageInt = RegexMatcher.INT.parse(page);
		if (pageInt == null)
			return getPage(1);
		return getPage(pageInt);
	}

	public BaseComponent getPage(int page) {
		if (page < 1)
			return getPageDidntExist(page);
		List<T> objects = getObjects();
		if (objects == null || objects.isEmpty())
			return getPageDidntExist(page);
		int offset = (page - 1) * 10;
		List<T> pageObjects = objects.stream().skip(offset).limit(pageSize).toList();
		int maxPage = Math.max(1, (int) Math.ceil(objects.size() * 1D / pageSize));
		return getTemplatePage(page, pageObjects, maxPage);
	}
	
	protected BaseComponent getTemplatePage(int page, List<T> objects, int maxPage) {
		if (page < 1 || maxPage != 0 && maxPage < page || objects == null || objects.isEmpty())
			return getPageDidntExist(page);
		TextComponent compo = new TextComponent();
		int length = title.length();
		int bars = Math.min(length, 13);
		String bar = "§e§m" + " ".repeat(bars);
		compo.addExtra(new TextComponent(bar + "§6 " + title + " " + bar + "§a"));
		compo.addExtra("\n");
		for (T object : objects) {
			compo.addExtra(getObjectDescription(object));
			compo.addExtra("\n");
		}
		TextComponent pageCompo;
		if (page >= 3) {
			pageCompo = new TextComponent();
			TextComponent firstPage = new TextComponent("⏪ ");
			firstPage.setColor(ChatColor.GOLD);
			firstPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eRevenir à la premère page")));
			firstPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(1)));
			pageCompo.addExtra(firstPage);
		} else
			pageCompo = new TextComponent("   ");
		TextComponent previousPage = new TextComponent("◀");
		if (page == 1) {
			previousPage.setColor(ChatColor.RED);
			previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTu es à la première page.")));
		} else {
			previousPage.setColor(ChatColor.GOLD);
			previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eRevenir à la page " + (page - 1))));
			previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(page - 1)));
		}
		pageCompo.addExtra(previousPage);
		if (maxPage != 0)
			pageCompo.addExtra("§e" + " ".repeat(bars) + "Page " + page + "/" + maxPage + " ".repeat(bars));
		else
			pageCompo.addExtra("§e" + " ".repeat(bars) + "Page " + page + " ".repeat(bars));
		TextComponent nextPage = new TextComponent("▶");
		if (maxPage != 0 && page == maxPage) {
			nextPage.setColor(ChatColor.RED);
			nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTu es à la dernière page.")));
		} else {
			nextPage.setColor(ChatColor.GOLD);
			nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eAller à la page " + (page + 1))));
			nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(page + 1)));
		}
		pageCompo.addExtra(nextPage);
		if (maxPage != 0 && page <= maxPage - 2) {
			TextComponent lastPage = new TextComponent(" ⏩");
			lastPage.setColor(ChatColor.GOLD);
			lastPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eAller à la page " + maxPage)));
			lastPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(maxPage)));
			pageCompo.addExtra(lastPage);
		}
		compo.addExtra(pageCompo);
		compo.addExtra("\n");
		int pxSize = Chat.getPxSize(title, true);
		int spaces = pxSize / (Chat.SPACE.getLength() + 1) + 2;
		compo.addExtra(new TextComponent("§e§m" + " ".repeat(2 * bars + spaces)));
		return compo;
	}

}