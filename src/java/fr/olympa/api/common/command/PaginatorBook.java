package fr.olympa.api.common.command;

import java.util.List;

import com.google.common.annotations.Beta;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.core.spigot.OlympaCore;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class PaginatorBook<T> extends Paginator<T>{

	public PaginatorBook(int pageSize, String title) {
		super(pageSize, title);
	}
	
	@Beta
	protected BaseComponent getBookPage(int page, List<T> objects, int maxPage) {
//		Book serverBook = Book.book(Component.text(title), Component.text("Olympa"), new TxtComponentBuilder(getTemplatePage(page, objects, maxPage)).buildBook());
		return null;
	}

}
