package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.util.BukkitColor;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.service.Constant;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.panther.annotation.Note;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public interface Channel {

	Channel CLAN = valueOf("CLAN");

	Channel ALLY = valueOf("ALLY");

	Channel GLOBAL = valueOf("GLOBAL");

	static Channel[] values() {
		return Constant.values(Channel.class, Channel.class).toArray(new Channel[0]);
	}

	/**
	 * @apiNote Safe to use for custom chat channel provision!
	 */
	static Channel valueOf(String name) {
		return Constant.values(Channel.class).stream().filter(c -> c.getName().equals(name)).map(Constant::getValue).findFirst().orElse(new Channel() {
			@Override
			public @NotNull String getId() {
				return name;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null) return false;
				if (!(obj instanceof Channel)) return false;
				Channel chatChannel = (Channel) obj;
				return chatChannel.getId().equals(getId());
			}

			@Override
			public String toString() {
				return getId();
			}
		});
	}

	@NotNull String getId();

	default Filter[] getFilters() {
		return InoperableSpecialMemory.FILTERS.stream().filter(f -> f.getChannel().equals(this)).toArray(Filter[]::new);
	}

	default void register(Filter filter) {
		InoperableSpecialMemory.FILTERS.add(filter);
	}

	default void register(Function<String, String> filter) {
		InoperableSpecialMemory.FILTERS.add(new Filter() {
			@Override
			public @NotNull Channel getChannel() {
				return Channel.this;
			}

			@Override
			public @NotNull String run(String context) {
				return filter.apply(context);
			}
		});
	}

	/**
	 * Get related associates that are in the same chat channel as this.
	 *
	 * @param parent The invasive parent to check.
	 * @return A set of all mutually known clan associates in this channel.
	 */
	default Set<Clan.Associate> getAudience(InvasiveEntity parent) {
		if (parent instanceof Clan) {
			Set<Clan.Associate> set = ((Clan) parent).getMembers().stream().filter(m -> m.getChannel().equals(this)).collect(Collectors.toSet());
			for (InvasiveEntity ent : parent.getAsClan().getRelation()) {
				if (ent.isClan()) {
					set.addAll(ent.getAsClan().getMembers().stream().filter(m -> m.getChannel().equals(this)).collect(Collectors.toSet()));
				}
			}
			return set;
		}
		if (!(parent instanceof Clan.Associate)) return null;
		Set<Clan.Associate> set = ((Clan.Associate) parent).getClan().getMembers().stream().filter(m -> m.getChannel().equals(this)).collect(Collectors.toSet());
		for (InvasiveEntity ent : parent.getAsClan().getRelation()) {
			if (ent.isClan()) {
				set.addAll(ent.getAsClan().getMembers().stream().filter(m -> m.getChannel().equals(this)).collect(Collectors.toSet()));
			}
		}
		return set;
	}

	/**
	 * Get all known associates participating within this chat channel.
	 *
	 * @return every known associate residing within this chat channel.
	 * @apiNote Don't use this method unless you want to get every clan associate online
	 * that is currently in this channel.
	 */
	@Note("Gets every known associate residing within this chat channel.")
	default Set<Clan.Associate> getAudience() {
		Set<Clan.Associate> associates = new HashSet<>();
		ClansAPI.getInstance().getClanManager().getClans().forEach(c -> c.getMembers().forEach(a -> {
			if (a.getChannel().equals(this)) {
				associates.add(a);
			}
		}));
		return associates;
	}

	default Message tryFormat(Clan.Associate associate) {
		FancyMessage message = new FancyMessage();
		message.then(MessageFormat.format("(" + (LabyrinthProvider.getInstance().isNew() ? new RandomHex().context(getId()).join() : BukkitColor.random().toCode().replace("0", "f") + getId()) + "&r) ", associate.getNickname(), associate.getName(), associate.getClan().getPalette(), associate.getRankFull(), associate.getClan().getName()));
		message.then(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.highlight"), associate.getNickname(), associate.getName(), associate.getClan().getPalette(), associate.getRankFull(), associate.getClan().getName()));
		message.then(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.divider") + "%MESSAGE%", associate.getNickname(), associate.getName(), associate.getClan().getPalette(), associate.getRankFull(), associate.getClan().getName()));
		message.hover(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.Chat.Channel.ally.hover"), associate.getNickname(), associate.getName(), associate.getClan().getPalette(), associate.getRankFull(), associate.getClan().getName()));
		return message;
	}

	default boolean isDefault() {
		return getId().equals(GLOBAL.toString()) || getId().equals(CLAN.toString()) || getId().equals(ALLY.toString());
	}

	interface Filter {

		@NotNull Channel getChannel();

		@NotNull String run(String context);

	}

}
