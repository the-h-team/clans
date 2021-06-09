package com.github.sanctum.clans.construct.extra.uniform;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.OfflinePlayer;

public class MemberWrapper extends UniformedComponents<ClanAssociate> implements Serializable {

	private static final long serialVersionUID = -1769055045640489378L;
	private final DefaultClan c;

	public MemberWrapper(DefaultClan clan) {
		this.c = clan;
	}

	@Override
	public List<ClanAssociate> list() {
		FileManager c = ClansAPI.getData().getClanFile(this.c);
		return c.getConfig().getStringList("members").stream().map(i -> ClansAPI.getInstance().getAssociate(UUID.fromString(i)).orElse(null)).collect(Collectors.toList());
	}

	@Override
	public List<ClanAssociate> sort() {
		List<ClanAssociate> list = list();
		list.sort(Comparator.comparingDouble(ClanAssociate::getKD));
		return list();
	}

	@Override
	public List<ClanAssociate> sort(Comparator<? super ClanAssociate> comparable) {
		return sort();
	}

	@Override
	public Collection<ClanAssociate> collect() {
		return list();
	}

	@Override
	public ClanAssociate[] array() {
		return list().toArray(new ClanAssociate[0]);
	}

	@Override
	public <R> Stream<R> map(Function<? super ClanAssociate, ? extends R> mapper) {
		return list().stream().map(mapper);
	}

	@Override
	public Stream<ClanAssociate> filter(Predicate<? super ClanAssociate> predicate) {
		return list().stream().filter(predicate);
	}

	@Override
	public ClanAssociate getFirst() {
		return list().get(0);
	}

	@Override
	public ClanAssociate getLast() {
		return list().get(Math.max(list().size() - 1, 0));
	}

	@Override
	public ClanAssociate get(int index) {
		return list().get(index);
	}

	public UniformedComponents<OfflinePlayer> asPlayer() {
		return new MemberPlayerWrapper(this);
	}

}
