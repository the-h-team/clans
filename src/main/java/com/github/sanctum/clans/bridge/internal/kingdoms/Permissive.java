package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.Clearance;

public interface Permissive {

	boolean test(Clearance clearance, Clan.Associate associate);

}
