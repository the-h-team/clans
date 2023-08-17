package com.github.sanctum.clans.construct.util;

import com.github.sanctum.labyrinth.library.DirectivePoint;

public interface Buildable {

	void build();

	static DirectivePoint getDirection(float yaw) {
		DirectivePoint result = null;
		float y = yaw;
		if (y < 0) {
			y += 360;
		}
		y %= 360;
		int i = (int) ((y + 8) / 22.5);
		if (i == 0) {
			result = DirectivePoint.South;
		}
		if (i == 1) {
			result = DirectivePoint.South_West;
		}
		if (i == 2) {
			result = DirectivePoint.South_West;
		}
		if (i == 3) {
			result = DirectivePoint.South_West;
		}
		if (i == 4) {
			result = DirectivePoint.West;
		}
		if (i == 5) {
			result = DirectivePoint.North_West;
		}
		if (i == 6) {
			result = DirectivePoint.North_West;
		}
		if (i == 7) {
			result = DirectivePoint.North_West;
		}
		if (i == 8) {
			result = DirectivePoint.North;
		}
		if (i == 9) {
			result = DirectivePoint.North_East;
		}
		if (i == 10) {
			result = DirectivePoint.North_East;
		}
		if (i == 11) {
			result = DirectivePoint.North_East;
		}
		if (i == 12) {
			result = DirectivePoint.East;
		}
		if (i == 13) {
			result = DirectivePoint.South_East;
		}
		if (i == 14) {
			result = DirectivePoint.South_East;
		}
		if (i == 15) {
			result = DirectivePoint.South_East;
		}
		if (result == null) {
			result = DirectivePoint.South;
		}
		return result;
	}


}
