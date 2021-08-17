package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.bukkit.OfflinePlayer;

public class MemberPlayerWrapper extends UniformedComponents<OfflinePlayer> implements Serializable {

	private static final long serialVersionUID = -6713773767070009087L;
	private final UniformedComponents<ClanAssociate> wrapper;

	public MemberPlayerWrapper(UniformedComponents<ClanAssociate> wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public List<OfflinePlayer> list() {
		List<OfflinePlayer> list = new ArrayList<>();
		for (ClanAssociate a : wrapper.list()) {
			list.add(a.getPlayer());
		}
		return list;
	}

	@Override
	public List<OfflinePlayer> sort() {
		list().sort(Comparator.comparingLong(OfflinePlayer::getLastPlayed));
		return list();
	}

	@Override
	public List<OfflinePlayer> sort(Comparator<? super OfflinePlayer> comparable) {
		return sort();
	}

	@Override
	public Collection<OfflinePlayer> collect() {
		return list();
	}

	@Override
	public OfflinePlayer[] array() {
		return collect().toArray(new OfflinePlayer[0]);
	}

	@Override
	public <R> Stream<R> map(Function<? super OfflinePlayer, ? extends R> mapper) {
		return list().stream().map(mapper);
	}

	@Override
	public Stream<OfflinePlayer> filter(Predicate<? super OfflinePlayer> predicate) {
		return list().stream().filter(predicate);
	}

	@Override
	public OfflinePlayer getFirst() {
		return list().get(0);
	}

	@Override
	public OfflinePlayer getLast() {
		return list().get(Math.max(list().size() - 1, 0));
	}

	@Override
	public OfflinePlayer get(int index) {
		return list().get(index);
	}
}
