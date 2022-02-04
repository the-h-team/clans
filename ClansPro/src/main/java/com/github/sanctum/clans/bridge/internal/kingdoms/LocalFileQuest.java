package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomQuestCompletionEvent;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.RoundTableQuestCompletionEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.formatting.ComponentChunk;
import com.github.sanctum.labyrinth.formatting.Message;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LocalFileQuest implements Quest, Message.Factory {


	private double progression;

	private boolean complete;

	private Progressive parent;

	private Reward<?> reward;

	private final double requirement;

	private final String title;

	private final Set<Player> players;

	private final String description;

	LocalFileQuest(String title, String description, double progression, double requirement, boolean done) {
		this.title = title;
		this.complete = done;
		this.players = new HashSet<>();
		this.description = description;
		this.progression = progression;
		this.requirement = requirement;
	}

	LocalFileQuest(String title, String description, double progression, double requirement) {
		this.title = title;
		this.players = new HashSet<>();
		this.description = description;
		this.progression = progression;
		this.requirement = requirement;
	}

	@Override
	public @NotNull String getTitle() {
		return this.title;
	}

	@Override
	public @NotNull String getDescription() {
		return this.description;
	}

	@Override
	public @Nullable Progressive getParent() {
		return parent;
	}

	@Override
	public double getRequirement() {
		return this.requirement;
	}

	@Override
	public double getProgression() {
		return this.progression;
	}

	@Override
	public LabyrinthUser getCompleter() {
		return null;
	}

	@Override
	public Reward<?> getReward() {
		return reward;
	}

	@Override
	public Set<Player> getActiveUsers() {
		return players;
	}

	@Override
	public double progress(double amount) {
		if (this.progression + amount > this.requirement) {
			return 0;
		}
		return this.progression += amount;
	}

	@Override
	public double unprogress(double amount) {
		double d = Math.max(0, this.progression - amount);
		this.progression = d;
		return d;
	}

	@Override
	public void save() {

		Node path = getNode(getTitle());
		path.getNode("complete").set(complete);
		path.getNode("info").set(getDescription());
		path.getNode("progression").set(getProgression());
		path.getNode("requirement").set(getRequirement());

		if (this.reward != null) {
			Node rew = path.getNode("reward");
			Node type = rew.getNode("type");
			Node value = rew.getNode("value");
			Node message = rew.getNode("message");
			String t = Double.class.isAssignableFrom(reward.get().getClass()) ? "MONEY" : "ITEM";
			type.set(t);
			if (ItemStack[].class.isAssignableFrom(reward.get().getClass())) {
				for (int i = 0; i < ((ItemStack[]) reward.get()).length; i++) {
					value.getNode(i + "").set(((ItemStack[]) reward.get())[i]);
				}
			} else {
				value.set(reward.get());
			}
			message.set(message().append(new ComponentChunk(reward.getMessage())).toJson());
			type.save();
			value.save();
			message.save();
		}
		path.save();
	}

	@Override
	public void delete() {
		Node path = getParentNode();
		path.set(null);
		path.save();
	}

	@Override
	public double getPercentage() {
		return Math.round(this.progression * 100 / this.requirement * 100.0) / 100.0;
	}

	@Override
	public boolean activated(Player p) {
		return this.players.contains(p);
	}

	@Override
	public boolean activate(Player p) {
		if (isComplete()) return false;
		if (activated(p)) return false;
		return this.players.add(p);
	}

	@Override
	public boolean deactivate(Player p) {
		if (!activated(p)) return false;
		return this.players.remove(p);
	}

	@Override
	public boolean isComplete() {

		if (getPercentage() >= 100) {
			if (!this.complete) {
				if (this.parent != null) {
					if (Kingdom.class.isAssignableFrom(this.parent.getClass())) {
						ClanVentBus.call(new KingdomQuestCompletionEvent((Kingdom) this.parent, this));
					}
					if (RoundTable.class.isAssignableFrom(this.parent.getClass())) {
						ClanVentBus.call(new RoundTableQuestCompletionEvent((RoundTable) this.parent, this));
					}
				}

				this.complete = true;
			}
		}

		return this.complete;
	}

	@Override
	public void setReward(Reward<?> type, Object reward) {
		if (type.get().getClass().isAssignableFrom(reward.getClass())) {
			Reward.assertReward(type);
			if (Double.class.isAssignableFrom(reward.getClass())) {
				this.reward = new Reward<Double>() {
					@Override
					public Double get() {
						return (Double) reward;
					}

					@Override
					public BaseComponent[] getMessage() {
						return message().append(text("[").color(Color.BLUE)).append(text("Money").color(Color.GREEN)).append(text("]").color(Color.BLUE)).append(text(" ")).append(text(get() + " has been received.").color(Color.ORANGE)).build();
					}

					@Override
					public void give(Kingdom kingdom) {
						kingdom.getMembers().forEach(this::give);
					}

					@Override
					public void give(Clan clan) {
						clan.getMembers().forEach(this::give);
					}

					@Override
					public void give(Clan.Associate associate) {
						if (EconomyProvision.getInstance().isValid()) {
							EconomyProvision.getInstance().deposit(BigDecimal.valueOf(get()), associate.getTag().getPlayer());
						}
						Optional.ofNullable(associate.getTag().getPlayer().getPlayer()).ifPresent(p -> {
							int random = new Random().nextInt(246);
							p.giveExp(random);
						});
					}
				};
			} else {
				if (ItemStack.class.isAssignableFrom(reward.getClass())) {
					this.reward = new Reward<ItemStack>() {
						@Override
						public ItemStack get() {
							return (ItemStack) reward;
						}

						@Override
						public BaseComponent[] getMessage() {
							return message().append(text("[").color(Color.BLUE)).append(text("Item").color(Color.GREEN).bind(hover(get()))).append(text("]").color(Color.BLUE)).append(text(" ")).append(text(get().getType() + " has been received.").color(Color.ORANGE)).build();
						}

						@Override
						public void give(Kingdom kingdom) {
							kingdom.getMembers().forEach(this::give);
						}

						@Override
						public void give(Clan clan) {
							clan.getMembers().forEach(this::give);
						}

						@Override
						public void give(Clan.Associate associate) {
							Optional.ofNullable(associate.getTag().getPlayer().getPlayer()).ifPresent(p -> {
								p.getWorld().dropItem(p.getLocation(), get());
								int random = new Random().nextInt(246);
								p.giveExp(random);
							});
						}
					};
				} else {
					this.reward = new Reward<ItemStack[]>() {
						@Override
						public ItemStack[] get() {
							return (ItemStack[]) reward;
						}

						@Override
						public BaseComponent[] getMessage() {
							return message().append(text("[").color(Color.BLUE)).append(text("Item's").color(Color.GREEN).bind(hover(get()[0]))).append(text("]").color(Color.BLUE)).append(text(" ")).append(text(get()[0].getType() + " + " + (get().length - 1) + " more items has been received.").color(Color.ORANGE)).build();
						}

						@Override
						public void give(Kingdom kingdom) {
							kingdom.getMembers().forEach(this::give);
						}

						@Override
						public void give(Clan clan) {
							clan.getMembers().forEach(this::give);
						}

						@Override
						public void give(Clan.Associate associate) {
							Optional.ofNullable(associate.getTag().getPlayer().getPlayer()).ifPresent(p -> {
								for (ItemStack item : get()) {
									p.getWorld().dropItem(p.getLocation(), item);
								}
								int random = new Random().nextInt(246);
								p.giveExp(random);
							});
						}
					};
				}
			}
		}
	}

	@Override
	public void setParent(Progressive k) {
		this.parent = k;
	}

	@Override
	public String getPath() {
		return getParent() instanceof Kingdom ? "memory.kingdom." + getParent().getName() : "memory.table";
	}

	@Override
	public boolean isNode(String key) {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile(FileType.JSON, "achievements", "data");
		return file.getRoot().getNode(getPath()).isNode(key);
	}

	Node getParentNode() {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile(FileType.JSON, "achievements", "data");
		return file.getRoot().getNode(getPath());
	}

	@Override
	public Node getNode(String key) {
		return getParentNode().getNode(key);
	}

	@Override
	public Set<String> getKeys(boolean deep) {
		return getNode(getPath()).getKeys(deep);
	}

	@Override
	public Map<String, Object> getValues(boolean deep) {
		return getNode(getPath()).getValues(deep);
	}
}
