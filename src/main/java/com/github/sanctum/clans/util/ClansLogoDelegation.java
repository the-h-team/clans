package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.LogoHolder;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.util.HUID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ClansLogoDelegation implements LogoHolder, Iterable<String> {

    final HUID id = HUID.randomID();
    private final List<String> logo;

    public ClansLogoDelegation(List<String> logo) {
        this.logo = new ArrayList<>(logo);
    }

    public LogoHolder getHolder() {
        return null;
    }

    @Ordinal
    HUID getRealId() {
        return id;
    }

    public String getId() {
        return id.toString().replace("-", "").substring(8);
    }

    public int size() {
        return logo.size();
    }

    public String[] toRaw() {
        return logo.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "Carrier{" + "size=" + logo.size() + ",id=" + getRealId().toString() + '}';
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return logo.iterator();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        logo.forEach(action);
    }

    @Override
    public Spliterator<String> spliterator() {
        return logo.spliterator();
    }

    @Override
    public List<String> getLogo() {
        return this.logo;
    }

    @Override
    public void save() {

    }

    @Override
    public void remove() {

    }
}
