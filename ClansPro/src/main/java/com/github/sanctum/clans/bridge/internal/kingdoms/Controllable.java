package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.Clearance;

public interface Controllable {

	boolean test(Clearance clearance, Clan.Associate associate);

}
