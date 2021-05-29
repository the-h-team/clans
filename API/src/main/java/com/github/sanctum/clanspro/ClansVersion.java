package com.github.sanctum.clanspro;

import com.github.sanctum.clans.ClansPro;

public class ClansVersion {

	public static String get() {
		return ClansPro.getInstance().getDescription().getVersion();
	}

}
