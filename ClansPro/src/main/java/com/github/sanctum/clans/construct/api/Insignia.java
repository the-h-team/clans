package com.github.sanctum.clans.construct.api;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public abstract class Insignia implements Serializable {

	private static final List<Insignia> CACHE = new LinkedList<>();

	private final int z;

	private final int x;

	private final String key;

	private final LinkedList<Insignia.Line> list;

	private String selection = "&4";

	private char symbol = '⬛';

	protected Insignia(String key, int z, int x) {
		this.key = key;
		this.list = new LinkedList<>();
		this.z = z;
		this.x = x;

		CACHE.removeIf(c -> c.key.equalsIgnoreCase(key));

		CACHE.add(this);
	}

	protected Insignia(String key, Insignia insignia) {
		this.selection = insignia.selection;
		this.symbol = insignia.symbol;
		this.key = key;
		for (Line l : insignia.list) {
			l.setInsignia(this);
		}
		this.list = insignia.list;
		this.z = insignia.z;
		this.x = insignia.x;

		CACHE.removeIf(c -> c.key.equalsIgnoreCase(key));

		CACHE.add(this);
	}

	protected Insignia(Insignia insignia) {
		this.selection = insignia.selection;
		this.symbol = insignia.symbol;
		this.key = insignia.key;
		for (Line l : insignia.list) {
			l.setInsignia(this);
		}
		this.list = insignia.list;
		this.z = insignia.z;
		this.x = insignia.x;

		CACHE.removeIf(c -> c.key.equalsIgnoreCase(key));

		CACHE.add(this);
	}

	public static Insignia copy(String id, Insignia insignia) {

		if (insignia == null) return null;

		return new LabyrinthImpl(id, insignia);
	}

	public static Insignia copy(Insignia insignia) {

		if (insignia == null) return null;

		return new LabyrinthImpl(insignia);
	}

	public static Insignia get(String key) {
		for (Insignia i : CACHE) {
			if (i.getKey().equals(key)) {
				return i;
			}
		}
		return null;
	}

	public static Insignia newInsignia(String key) throws InsigniaError {
		if (get(key) == null) {
			return new LabyrinthImpl(key, 12, 12);
		} else {
			throw new InsigniaError(key, "An insignia under this id already exists!");
		}
	}

	public static Insignia newInsignia(String key, int width, int height) throws InsigniaError {
		if (get(key) == null) {
			return new LabyrinthImpl(key, height, width);
		} else {
			throw new InsigniaError(key, "An insignia under this id already exists!");
		}
	}

	public String getKey() {
		return key;
	}

	public int getWidth() {
		return x;
	}

	public int getHeight() {
		return z;
	}

	public void setSymbol(char symbol) {
		this.symbol = symbol;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public char getSymbol() {
		return symbol;
	}

	public String getSelection() {
		return selection;
	}

	public Insignia input(Insignia.Line line) {
		if (this.list.size() < z) {
			this.list.add(line);
		}
		return this;
	}

	public void remove() {
		CACHE.remove(this);
	}

	public LinkedList<Insignia.Line> getLines() {
		return this.list;
	}

	public List<BaseComponent[]> get() {
		return getLines().stream().map(Line::toArray).collect(Collectors.toList());
	}

	public static class Line implements Serializable {

		private final Map<Integer, Line.Symbol> position;

		private final int line;

		private Insignia insignia;

		public Line(int line, Insignia insignia) {
			this.insignia = insignia;
			this.line = line;
			this.position = new HashMap<>();
		}

		protected final void setInsignia(Insignia insignia) {
			this.insignia = insignia;
		}

		public Line finish(Consumer<Symbol> builder) {
			Symbol s = new Symbol('⬛');
			builder.accept(s);
			this.position.put(this.position.values().size() + 1, s);
			return this;
		}

		public Line add(int slot, char character) {
			if (this.position.values().size() < insignia.x) {
				this.position.put(slot, new Symbol(character));
			}
			return this;
		}

		public int getLine() {
			return this.line;
		}

		public Collection<Symbol> getSymbols() {
			return this.position.values();
		}

		public BaseComponent[] toArray() {
			LinkedList<BaseComponent> list = new LinkedList<>();
			TextLib lib = TextLib.getInstance();
			for (Map.Entry<Integer, Symbol> entry : position.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).collect(Collectors.toList())) {
				if (!entry.getValue().isHidden()) {
					if (entry.getValue().getAction() != null) {
						list.add(lib.execute(entry.getValue().getAction(), lib.textHoverable("", entry.getValue().getColor() + entry.getValue().getCharacter() + "", entry.getValue().getHoverMsg())));
					} else {
						list.add(lib.textHoverable("", entry.getValue().getColor() + entry.getValue().getCharacter() + "", entry.getValue().getHoverMsg()));
					}
				} else {
					if (entry.getValue().isShowMap()) {
						if (entry.getValue().getAction() != null) {
							list.add(lib.execute(entry.getValue().getAction(), lib.textHoverable("", entry.getValue().getColor() + entry.getValue().getCharacter() + "", entry.getValue().getHoverMsg())));
						} else {
							list.add(lib.textHoverable("", entry.getValue().getColor() + entry.getValue().getCharacter() + "", entry.getValue().getHoverMsg()));
						}
					}
				}
			}
			return list.toArray(new BaseComponent[0]);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<Integer, Symbol> entry : position.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).collect(Collectors.toList())) {
				if (!entry.getValue().isHidden()) {
					builder.append(entry.getValue().getColor()).append(entry.getValue().getCharacter());
				}
			}
			return builder.toString().trim();
		}

		public static class Symbol implements Serializable {

			private char character;

			private Applicable applicable;

			private boolean hidden;

			private boolean showMap;

			private String color = "&f";

			private String hoverMsg = "";

			public Symbol(char character) {
				this.character = character;
			}

			public void setCharacter(char character) {
				this.character = character;
			}

			public String getHoverMsg() {
				return hoverMsg;
			}

			public String getColor() {
				return color;
			}

			public void setColor(String color) {
				this.color = color;
			}

			public void setHoverMsg(String hoverMsg) {
				this.hoverMsg = hoverMsg;
			}

			public void setAction(Applicable applicable) {
				this.applicable = applicable;
			}

			public boolean isHidden() {
				return hidden;
			}

			public boolean isShowMap() {
				return showMap;
			}

			public void setHidden(boolean hidden, boolean showMap) {
				this.hidden = hidden;
				this.showMap = showMap;
			}

			public Applicable getAction() {
				return applicable;
			}

			public char getCharacter() {
				return character;
			}
		}

	}

	protected static final class LabyrinthImpl extends Insignia implements Serializable {

		public LabyrinthImpl(String key, int height, int width) {
			super(key, height, width);
		}

		protected LabyrinthImpl(String key, Insignia insignia) {
			super(key, insignia);
		}

		protected LabyrinthImpl(Insignia insignia) {
			super(insignia);
		}

	}

	public static class Builder {

		private final String key;

		private String color;

		private String border = "&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

		private int height;

		private int width;

		public Builder(String key) {
			this.key = key;
		}

		public Builder setHeight(int height) {
			this.height = height;
			return this;
		}

		public Builder setWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder setColor(String color) {
			this.color = color;
			return this;
		}

		public Builder setBorder(String border) {
			this.border = border;
			return this;
		}

		public Insignia draw(Player p) {
			try {
				Insignia insignia = newInsignia(this.key, this.width, this.height);

				if (this.color != null) {
					insignia.setSelection(this.color);
				}
				insignia.setSymbol('█');

				for (int j = 1; j < this.height + 1; j++) {

					Line line = new Line(j, insignia);

					for (int k = 1; k < this.width + 1; k++) {
						line.add(k, '█');
					}

					insignia.input(line);

				}

				//Message msg = Message.form(p);

				for (Line line : insignia.getLines()) {
					for (Line.Symbol symbol : line.getSymbols()) {

						symbol.setAction(() -> {

							symbol.setColor(insignia.getSelection());
							symbol.setCharacter(insignia.getSymbol());

							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(" ");
							p.sendMessage(StringUtils.use(this.border).translate());

							for (BaseComponent[] components : insignia.get()) {
								p.spigot().sendMessage(components);
							}

							p.sendMessage(StringUtils.use(this.border).translate());

						});

					}
				}

				for (int i = 1; i < 7; i++) {
					switch (i) {
						case 1:
						case 3:
						case 5:
							for (int j = 0; j < 6; j++) {
								insignia.getLines().get(j).finish(symbol -> {
									symbol.setCharacter(' ');
									symbol.setHidden(true, true);
								});
							}
							break;
						case 2:
							insignia.getLines().get(0).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for #0735dbblue");
								symbol.setColor("#0735db");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#0735db");
								});
							});
							insignia.getLines().get(1).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07dbb1}teal");
								symbol.setColor("{#07dbb1}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07dbb1}");
								});
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07db38}green");
								symbol.setColor("{#07db38}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07db38}");
								});
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for #dbeb34yellow");
								symbol.setColor("#dbeb34");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#dbeb34");
								});
							});
							insignia.getLines().get(4).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for &4red");
								symbol.setColor("&4");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("&4");
								});
							});
							insignia.getLines().get(5).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setColor("&5");
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Click for &5purple");
								symbol.setAction(() -> {
									insignia.setSelection("&5");
								});
							});
							break;

						case 4:
							insignia.getLines().get(0).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for &#5f07dbkinda blue");
								symbol.setColor("#5f07db");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#5f07db");
								});
							});
							insignia.getLines().get(1).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07d0db}straight neon");
								symbol.setColor("{#07d0db}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07d0db}");
								});
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07db9b}whoa green");
								symbol.setColor("{#07db9b}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07db9b}");
								});
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#a4f542}ooz green");
								symbol.setColor("#a4f542");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#a4f542");
								});
							});
							insignia.getLines().get(4).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#eb6834}pumpkin red");
								symbol.setColor("#eb6834");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#eb6834");
								});
							});
							insignia.getLines().get(5).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setColor("#a6128d");
								symbol.setHoverMsg("Click for {#a6128d}sexy purple");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#a6128d");
								});
							});
							break;

						case 6:
							insignia.getLines().get(0).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg(StringUtils.use("Click for {#a58bb0}what happened purple").translate());
								symbol.setColor("#a58bb0");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#a58bb0");
								});
							});
							insignia.getLines().get(1).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07a6db}ocean blue");
								symbol.setColor("{#07a6db}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07a6db}");
								});
							});
							insignia.getLines().get(1).finish(symbol -> {
								symbol.setCharacter('|');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#07db0b}mean green");
								symbol.setColor("{#07db0b}");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("{#07db0b}");
								});
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter('|');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter(' ');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter('▓');
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Change the symbol");
								symbol.setAction(() -> {
									insignia.setSymbol('▓');
								});
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter(' ');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(2).finish(symbol -> {
								symbol.setCharacter('▒');
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Change the symbol");
								symbol.setAction(() -> {
									insignia.setSymbol('▒');
								});
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Click for {#965806}turd");
								symbol.setColor("#965806");
								symbol.setAction(() -> {
									insignia.setSelection("#965806");
								});
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter('|');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter(' ');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter('█');
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Change the symbol");
								symbol.setAction(() -> {
									insignia.setSymbol('█');
								});
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter(' ');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(3).finish(symbol -> {
								symbol.setCharacter('░');
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Change the symbol");
								symbol.setAction(() -> {
									insignia.setSymbol('░');
								});
							});
							insignia.getLines().get(4).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setHoverMsg("Click for {#f5425d}questionably red");
								symbol.setColor("#f5425d");
								symbol.setHidden(true, true);
								symbol.setAction(() -> {
									insignia.setSelection("#f5425d");
								});
							});
							insignia.getLines().get(4).finish(symbol -> {
								symbol.setCharacter('|');
								symbol.setHidden(true, true);
							});
							insignia.getLines().get(5).finish(symbol -> {
								symbol.setCharacter(insignia.getSymbol());
								symbol.setColor("#21094f");
								symbol.setHidden(true, true);
								symbol.setHoverMsg("Click for {#21094f}nightmare cake");
								symbol.setAction(() -> {
									insignia.setSelection("#21094f");
								});
							});
							break;
					}
				}

				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(StringUtils.use(this.border).translate());

				for (BaseComponent[] components : insignia.get()) {
					p.spigot().sendMessage(components);
				}

				p.sendMessage(StringUtils.use(this.border).translate());

				return insignia;

			} catch (InsigniaError e) {
				Insignia i = e.getRegistration();
				//Message msg = Message.form(p);

				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(" ");
				p.sendMessage(StringUtils.use(this.border).translate());

				for (BaseComponent[] components : i.get()) {
					p.spigot().sendMessage(components);
				}

				p.sendMessage(StringUtils.use(this.border).translate());
				return i;
			}
		}


	}
}
