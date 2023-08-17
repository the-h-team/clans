package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.util.InsigniaError;
import com.github.sanctum.clans.event.associate.AssociateChangeBrushColorEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.panther.util.Applicable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public abstract class Insignia {

	private static final List<Insignia> CACHE = new LinkedList<>();

	private final int z;

	private final int x;

	private final String key;

	private final LinkedList<Line> list;

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

	public Insignia input(Line line) {
		if (this.list.size() < z) {
			this.list.add(line);
		}
		return this;
	}

	public void remove() {
		CACHE.remove(this);
	}

	public LinkedList<Line> getLines() {
		return this.list;
	}

	public List<BaseComponent[]> get() {
		return getLines().stream().map(Insignia.Line::toArray).collect(Collectors.toList());
	}

	public static class Line {

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

		public static class Symbol {

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

	protected static final class LabyrinthImpl extends Insignia {

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
			final UUID user = p.getUniqueId();
			try {
				Insignia insignia = newInsignia(this.key, this.width, this.height);

				if (this.color != null) {
					AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(user).get(), this.color));
					if (!e.isCancelled()) {
						insignia.setSelection(e.getColor());
					}
				}
				insignia.setSymbol('█'); // set our starting symbol to solid

				for (int j = 1; j < this.height + 1; j++) { // populate the size of our grid.

					Line line = new Line(j, insignia);

					for (int k = 1; k < this.width + 1; k++) {
						line.add(k, '█');
					}

					insignia.input(line);

				}

				for (Line line : insignia.getLines()) {
					for (Line.Symbol symbol : line.getSymbols()) { // Setup what happens when we click a box (Default reaction is to resend the graph)

						symbol.setAction(() -> {

							symbol.setColor(insignia.getSelection());
							symbol.setCharacter(insignia.getSymbol());

							OfflinePlayer offline = Bukkit.getOfflinePlayer(user);
							if (offline.isOnline()) {
								Player t = offline.getPlayer();
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(" ");
								t.sendMessage(StringUtils.use(this.border).translate());

								for (BaseComponent[] components : insignia.get()) {
									t.spigot().sendMessage(components);
								}

								t.sendMessage(StringUtils.use(this.border).translate());
							}

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
							if (LabyrinthProvider.getInstance().isNew()) {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for #0735dbblue");
									symbol.setColor("#0735db");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#0735db}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#07dbb1}teal");
									symbol.setColor("{#07dbb1}");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07dbb1}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(2).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#07db38}green");
									symbol.setColor("{#07db38}");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07db38}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(3).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for #dbeb34yellow");
									symbol.setColor("#dbeb34");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#dbeb34}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(4).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &4red");
									symbol.setColor("&4");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&4"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(5).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setColor("&5");
									symbol.setHidden(true, true);
									symbol.setHoverMsg("Click for &5purple");
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&5"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
							} else {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &9blue");
									symbol.setColor("&9");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&9"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &baqua");
									symbol.setColor("&b");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&b"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(2).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &2green");
									symbol.setColor("&2");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&2"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(3).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &eyellow");
									symbol.setColor("&e");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&e"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(4).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &4red");
									symbol.setColor("&4");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&4"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(5).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setColor("&5");
									symbol.setHidden(true, true);
									symbol.setHoverMsg("Click for &5purple");
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&5"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
							}
							break;

						case 4:
							if (LabyrinthProvider.getInstance().isNew()) {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &#5f07dbkinda blue");
									symbol.setColor("#5f07db");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#5f07db}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#07d0db}straight neon");
									symbol.setColor("{#07d0db}");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07d0db}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(2).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#07db9b}whoa green");
									symbol.setColor("{#07db9b}");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07db9b}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(3).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#a4f542}ooz green");
									symbol.setColor("#a4f542");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#a4f542}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(4).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#eb6834}pumpkin red");
									symbol.setColor("#eb6834");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#eb6834}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(5).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setColor("#a6128d");
									symbol.setHoverMsg("Click for {#a6128d}sexy purple");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#a6128d}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
							} else {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &1dark blue");
									symbol.setColor("&1");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&1"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &3dark aqua");
									symbol.setColor("&3");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&3"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(2).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &alight green");
									symbol.setColor("&a");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&a"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(3).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHidden(true, true);
									symbol.setHoverMsg("Click for &6gold");
									symbol.setColor("&6");
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&6"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(4).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &clight red");
									symbol.setColor("&c");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&c"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(5).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setColor("&d");
									symbol.setHoverMsg("Click for &dlight purple");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&d"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
							}
							break;

						case 6:
							if (!LabyrinthProvider.getInstance().isNew()) {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg(StringUtils.use("Click for &0black").translate());
									symbol.setColor("&0");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&0"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> symbol.setCharacter(' '));
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter('|');
									symbol.setHidden(true, true);
								});
								insignia.getLines().get(2).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &fwhite");
									symbol.setColor("&f");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&f"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
									symbol.setHoverMsg("Change the brush to translucent 1");
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
									symbol.setHoverMsg("Change the brush to translucent 2");
									symbol.setAction(() -> {
										insignia.setSymbol('▒');
									});
								});
								insignia.getLines().get(3).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for &7gray");
									symbol.setColor("&7");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "&7"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
									symbol.setHoverMsg("Change the brush to solid");
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
									symbol.setHoverMsg("Change the brush to translucent 3");
									symbol.setAction(() -> {
										insignia.setSymbol('░');
									});
								});
								insignia.getLines().get(4).finish(symbol -> symbol.setCharacter(' '));
								insignia.getLines().get(4).finish(symbol -> {
									symbol.setCharacter('|');
									symbol.setHidden(true, true);
								});
								insignia.getLines().get(5).finish(symbol -> symbol.setCharacter(' '));
							} else {
								insignia.getLines().get(0).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg(StringUtils.use("Click for {#a58bb0}what happened purple").translate());
									symbol.setColor("#a58bb0");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#a58bb0}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
								insignia.getLines().get(1).finish(symbol -> {
									symbol.setCharacter(insignia.getSymbol());
									symbol.setHoverMsg("Click for {#07a6db}ocean blue");
									symbol.setColor("{#07a6db}");
									symbol.setHidden(true, true);
									symbol.setAction(() -> {
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07a6db}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#07db0b}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
									symbol.setHoverMsg("Change the brush to translucent 1");
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
									symbol.setHoverMsg("Change the brush to translucent 2");
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
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#965806}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
									symbol.setHoverMsg("Change the brush to solid");
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
									symbol.setHoverMsg("Change the brush to translucent 3");
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
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#f5425d}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
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
										AssociateChangeBrushColorEvent e = ClanVentBus.call(new AssociateChangeBrushColorEvent(ClansAPI.getInstance().getAssociate(p).get(), "{#21094f}"));
										if (!e.isCancelled()) {
											insignia.setSelection(e.getColor());
										}
									});
								});
							}
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
