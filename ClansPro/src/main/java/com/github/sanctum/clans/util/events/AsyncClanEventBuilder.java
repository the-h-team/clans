package com.github.sanctum.clans.util.events;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import org.bukkit.event.Event;

public abstract class AsyncClanEventBuilder extends Event {


    protected AsyncClanEventBuilder() {
        this(true);
    }

    protected AsyncClanEventBuilder(boolean isAsync) {
        super(isAsync);
    }

    public abstract ClanAction getUtil();

    public abstract StringLibrary stringLibrary();


}
