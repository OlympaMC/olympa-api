package fr.olympa.api.common.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import fr.olympa.api.common.chat.Chat;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.OlympaStatementBuilder;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public abstract class PaginatorDatabase<T> extends Paginator<T> {

	private SQLTable<T> table;
	private SQLColumn<?> orderColumn;
	private OlympaStatementBuilder olympaStatementBuilder;

	private int limit;
	private int offset;
	private Map<SQLColumn<?>, Object> getFromColumnWithObject;

	public PaginatorDatabase(int pageSize, String title, SQLTable<T> table, SQLColumn<?> orderColumn, boolean async) {
		super(pageSize, title);
		this.table = table;
		this.orderColumn = orderColumn;
		olympaStatementBuilder = new OlympaStatementBuilder();
		olympaStatementBuilder.type(StatementType.SELECT);
		olympaStatementBuilder.tableName(this.table.getName());
		olympaStatementBuilder.orderCollumn(this.orderColumn.getName());
		olympaStatementBuilder.asc(async);
	}

	public void setWhat(Map<SQLColumn<?>, Object> what) {
		Validate.notNull(what, "what can't be null");
		this.getFromColumnWithObject = what;
		setWhat();
	}

	public void setWhat(SQLColumn<?> collumn, Object object) {
		Validate.notNull(collumn, "collumn can't be null");
		Validate.notNull(object, "object can't be null");
		this.getFromColumnWithObject = Map.of(collumn, object);
		setWhat();
	}

	private void setWhat() {
		olympaStatementBuilder.what(getFromColumnWithObject.keySet().stream().map(SQLColumn::getName).toArray(String[]::new));
	}

	private void setForPage(int page) {
		this.offset = (page - 1) * 10;
		this.limit = 10;
		if (this.offset > 0)
			olympaStatementBuilder.offset(this.offset);
		if (this.limit > 0)
			olympaStatementBuilder.limit(this.limit);
	}

	/**
	 * Should be in async
	 */
	@Override
	protected List<T> getObjects() {
		List<T> list = new ArrayList<>();
		OlympaStatement olympaStatement = olympaStatementBuilder.build();
		boolean isWhatNotEmpty = getFromColumnWithObject != null && !getFromColumnWithObject.isEmpty();
		long valuesNeedToBeSet = olympaStatement.getStatementCommand().chars().filter(c -> c == '?').count();
		if (isWhatNotEmpty && (valuesNeedToBeSet == 0 || valuesNeedToBeSet != getFromColumnWithObject.size()) || !isWhatNotEmpty && valuesNeedToBeSet != 0)
			throw new IllegalAccessError(String.format("%s has been poorly defined. What (key for column) is %d and ValueNeedToBeSet (value of key of column) is %d",
					this.getClass().getSimpleName(), isWhatNotEmpty ? getFromColumnWithObject.size() : 0, valuesNeedToBeSet));
		try (PreparedStatement statement = olympaStatement.createStatement()) {
			if (getFromColumnWithObject != null && !getFromColumnWithObject.isEmpty()) {
				Iterator<Object> it = getFromColumnWithObject.values().iterator();
				int i = 1;
				while (it.hasNext())
					statement.setObject(i++, it.next());
			}
			ResultSet resultSet = olympaStatement.executeQuery(statement);
			while (resultSet.next())
				list.add(table.initializeFromRow.initialize(resultSet));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Should be in async
	 */
	@Override
	public BaseComponent getPage(int page) {
		if (page < 1)
			return getPageDidntExist(page);
		this.setForPage(page);
		return getTemplatePage(page, getObjects(), 0);
	}

	public BaseComponent getPageSAVED(int page) {
		if (page >= 1) {
			List<T> objects = getObjects();
			TextComponent compo = new TextComponent();
			int length = title.length();
			int bars = Math.min(length, 10);
			String bar = "§e§m" + " ".repeat(bars);
			compo.addExtra(new TextComponent(bar + "§6 " + title + " " + bar + "§a"));
			compo.addExtra("\n");
			for (int i = page * pageSize; i < Math.min(page * pageSize, objects.size()); i++) {
				compo.addExtra(getObjectDescription(objects.get(i)));
				compo.addExtra("\n");
			}
			TextComponent pageCompo;
			if (page >= 3) {
				pageCompo = new TextComponent("⏪ ");
				pageCompo.setColor(ChatColor.GOLD);
				pageCompo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eRevenir à la premère page")));
			} else
				pageCompo = new TextComponent("  ");
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
			pageCompo.addExtra("§e" + " ".repeat(bars) + "Page " + page + " ".repeat(bars));
			TextComponent nextPage = new TextComponent("▶");

			// Instresting if we know the last page
			//			if (page == max) {
			//				nextPage.setColor(ChatColor.RED);
			//				nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTu es à la dernière page.")));
			//			} else {
			nextPage.setColor(ChatColor.GOLD);
			nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eAller à la page " + (page + 1))));
			nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getCommand(page + 1)));
			//			}
			pageCompo.addExtra(nextPage);

			// Instresting if we know the last page
			//			if (false) {
			//				TextComponent lastPage = new TextComponent(" ⏩");
			//				lastPage.setColor(ChatColor.GOLD);
			//				lastPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eAller à la dernière page")));
			//				pageCompo.addExtra(lastPage);
			//			}
			compo.addExtra(pageCompo);
			compo.addExtra("\n");
			int pxSize = Chat.getPxSize(title, true);
			int spaces = pxSize / (Chat.SPACE.getLength() + 1) + 2;
			compo.addExtra(new TextComponent("§e§m" + " ".repeat(2 * bars + spaces)));
			return compo;

		}
		return new TextComponent(Prefix.DEFAULT_BAD.formatMessage("La page §4%d §cn'existe pas.", page));
	}
}