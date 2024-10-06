package com.github.sanctum.clans.util;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.annotation.Experimental;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

@Experimental
public class Book extends ItemStack {

	private final BookMeta bookMeta;
	private boolean title = false;
	int lines = 0;
	FormattedString string = new FormattedString("");

	@Experimental
	public Book() {
		this(1);
	}

	@Experimental
	public Book(int amount) {
		super(Material.WRITTEN_BOOK, amount);
		this.bookMeta = (BookMeta) getItemMeta();
	}

	@Experimental
	public Book(int amount, boolean titleAtTop) {
		super(Material.WRITTEN_BOOK, amount);
		this.bookMeta = (BookMeta) getItemMeta();
		this.title = titleAtTop;
	}

	public BookMeta getMeta() {
		return this.bookMeta;
	}

	public Book setAuthor(String author) {
		this.bookMeta.setAuthor(ChatColor.translateAlternateColorCodes('&', author + "&f"));
		setItemMeta(this.bookMeta);
		return this;
	}

	public Book setTitle(String title) {
		this.bookMeta.setTitle(ChatColor.translateAlternateColorCodes('&', title + "&f"));
		setItemMeta(this.bookMeta);
		return this;
	}

	void addPage(String... lines) {
		int offset = this.title ? 1 : 0;
		String[] array = new String[lines.length + offset];
		for (int i = Math.min(offset, lines.length - 1); i < lines.length; ++i) {
			int index = i + 1;
			if (index >= lines.length) {
				array[i] = StringUtils.use(lines[i] + "&f").translate();
			} else {
				array[index] = StringUtils.use(lines[i] + "&f").translate();
			}
		}
		if (offset == 1) {
			array[0] = this.bookMeta.getTitle();
		}
		this.bookMeta.addPage(String.join("\n", array));
		setItemMeta(this.bookMeta);
	}

	public String[] get(int page) {
		return this.bookMeta.getPage(page).split("\n");
	}

	public boolean remove(int page) {
		List<String> pages = new ArrayList<>(this.bookMeta.getPages());
		if (pages.size() >= page) {
			pages.remove(page);
			this.bookMeta.setPages(pages);
			setItemMeta(this.bookMeta);
			return true;
		}
		return false;
	}

	public Book append(String line) {
		if (lines >= 13) {
			newPage(string.get());
			this.lines = 0;
			this.string = new FormattedString("").append(line).append("\n");
		} else {
			this.string.append(line).append("\n");
		}
		lines += (Math.max(1, line.length() / 16));
		return this;
	}

	public void newPage(String... lines) {
		String[] copy = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			copy[i] = StringUtils.use(lines[i]).translate();
		}
		addPage(copy);
	}


	public void give(@NotNull Player player) {
		// check final line appendage and reset.
		if (lines > 0) {
			newPage(string.get());
			this.lines = 0;
			this.string = new FormattedString("");
		}
		LabyrinthProvider.getInstance().getItemComposter().add(this, player);
	}
}