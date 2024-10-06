package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.LogoHolder;
import org.bukkit.Location;

@FunctionalInterface
public interface SpecialCarrierAdapter {

	LogoHolder.Carrier accept(Location location);

}
