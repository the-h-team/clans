package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.LogoHolder;
import org.bukkit.Location;

@FunctionalInterface
public interface SpecialCarrierAdapter {

	LogoHolder.Carrier accept(Location location);

}
