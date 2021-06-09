package com.github.sanctum.clans.util;

public enum RankPriority {

	/**
	 * Rank = Member
	 */
	NORMAL(0),
	/**
	 * Rank = Moderator
	 */
	HIGH(1),
	/**
	 * Rank = Admin
	 */
	HIGHER(2),
	/**
	 * Rank = Owner
	 */
	HIGHEST(3);

	private final int priNum;

	RankPriority(int priNum) {
		this.priNum = priNum;
	}

	public int toInt() {
		int result;
		result = this.priNum;
		return result;
	}

}
