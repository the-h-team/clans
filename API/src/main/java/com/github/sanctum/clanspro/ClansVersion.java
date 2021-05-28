package com.github.sanctum.clanspro;

import com.youtube.hempfest.clans.HempfestClans;

public class ClansVersion {

	public static String get() {
		return HempfestClans.getInstance().getDescription().getVersion();
	}

}
