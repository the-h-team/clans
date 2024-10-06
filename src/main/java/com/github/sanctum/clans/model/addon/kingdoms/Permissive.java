package com.github.sanctum.clans.model.addon.kingdoms;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.Clearance;

public interface Permissive {

	boolean test(Clearance clearance, Clan.Associate associate);

}
