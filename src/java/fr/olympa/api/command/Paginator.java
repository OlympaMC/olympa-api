package fr.olympa.api.command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.olympa.api.command.complex.ArgumentParser;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public abstract class Paginator<T> {

	private final int pageSize;
	private final String title;

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

	public BaseComponent getPageFromString(String page) {
		Integer pageInt = RegexMatcher.INT.parse(page);
		if (pageInt == null)
			return getPage(0);
		return getPage(pageInt);
	}

	public BaseComponent getPage(int page) {
		if (page >= 1) {
			List<T> objects = getObjects();
			int max = Math.max(1, (int) Math.ceil(objects.size() * 1D / pageSize));
			if (page <= max) {
				page--;
				TextComponent compo = new TextComponent();
				int length = title.length();
				int bars = 25 - length;
				bars = Math.min(length, 10);
				String bar = "§e§m" + " ".repeat(bars);
				compo.addExtra(new TextComponent(bar + "§6 " + title + " " + bar + "§a"));
				compo.addExtra("\n");
				for (int i = page * pageSize; i < Math.min((page + 1) * pageSize, objects.size()); i++) {
					compo.addExtra(getObjectDescription(objects.get(i)));
					compo.addExtra("\n");
				}
				TextComponent pageCompo = new TextComponent("  ");
				TextComponent previousPage = new TextComponent("◀");
				if (page == 0) {
					previousPage.setColor(ChatColor.RED);
					previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTu es à la première page.")));
				} else {
					previousPage.setColor(ChatColor.GOLD);
					previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eRevenir à la page " + page)));
					previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(page)));
				}
				pageCompo.addExtra(previousPage);
				pageCompo.addExtra("§e" + " ".repeat(bars) + "Page " + (page + 1) + "/" + max + " ".repeat(bars));
				TextComponent nextPage = new TextComponent("▶");
				if (page == max - 1) {
					nextPage.setColor(ChatColor.RED);
					nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTu es à la dernière page.")));
				} else {
					nextPage.setColor(ChatColor.GOLD);
					nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eAller à la page " + (page + 2))));
					nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(page + 2)));
				}
				pageCompo.addExtra(nextPage);
				compo.addExtra(pageCompo);
				compo.addExtra("\n");
				compo.addExtra(new TextComponent("§e§m" + " ".repeat(length + 2 * bars + 5)));
				return compo;
			}
		}
		return new TextComponent(Prefix.DEFAULT_BAD.formatMessage("La page §4%d §cn'existe pas.", page));
	}

}