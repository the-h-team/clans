package com.github.sanctum.clans.model;

import java.util.ArrayList;
import java.util.List;
import org.intellij.lang.annotations.MagicConstant;

public class Vote {

	public static final int YES = 1;
	public static final int NO = 2;
	private final List<Integer> numbers = new ArrayList<>();

	public void cast(@MagicConstant(valuesFromClass = Vote.class) int num) {
		numbers.add(num);
	}

	public int count(@MagicConstant(valuesFromClass = Vote.class) int num) {
		if (num == YES) {
			return (int) numbers.stream().filter(i -> i == 1).count();
		}
		if (num == NO) {
			return (int) numbers.stream().filter(i -> i == 2).count();
		}
		return -1;
	}

	public int getMajority() {
		int yes = count(Vote.YES);
		int no = count(Vote.NO);
		return yes > no ? Vote.YES : (yes == no ? -1 : Vote.NO);
	}

	public boolean isUnanimous() {
		int yes = count(Vote.YES);
		int no = count(Vote.NO);
		return yes == no;
	}

	public void clear() {
		numbers.clear();
	}


}
