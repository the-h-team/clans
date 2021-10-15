package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ClanRosterElement extends UniformedComponents<Clan> {

	private List<Clan> clans;

	public ClanRosterElement(List<Clan> clans) {
		super();
		this.clans = new LinkedList<>(clans);
	}

	private static final long serialVersionUID = -5158318648096932311L;

	@Override
	public List<Clan> list() {
		return clans;
	}

	@Override
	public List<Clan> sort() {
		return list();
	}

	@Override
	public List<Clan> sort(Comparator<? super Clan> comparable) {
		clans.sort(comparable);
		return clans;
	}

	@Override
	public Collection<Clan> collect() {
		return clans;
	}

	@Override
	public Clan[] array() {
		return (Clan[]) clans.toArray();
	}

	@Override
	public <R> Stream<R> map(Function<? super Clan, ? extends R> mapper) {
		return clans.stream().map(mapper);
	}

	@Override
	public Stream<Clan> filter(Predicate<? super Clan> predicate) {
		return clans.stream().sequential().filter(predicate);
	}

	@Override
	public Clan getFirst() {
		return clans.get(0);
	}

	@Override
	public Clan getLast() {
		return clans.get(Math.max(list().size() - 1, 0));
	}

	@Override
	public Clan get(int index) {
		return clans.get(index);
	}

	public UniformedComponents<Clan> update(List<Clan> clans) {
		this.clans = clans;
		return this;
	}
}
