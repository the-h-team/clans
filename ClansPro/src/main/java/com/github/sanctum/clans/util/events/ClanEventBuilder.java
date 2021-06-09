package com.github.sanctum.clans.util.events;

import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import org.bukkit.event.Event;

public abstract class ClanEventBuilder extends Event {

    public abstract ClanAction getUtil();

    public abstract StringLibrary stringLibrary();


}
