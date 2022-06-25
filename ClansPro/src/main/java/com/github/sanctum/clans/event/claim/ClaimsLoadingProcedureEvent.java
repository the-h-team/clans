package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.InvasiveEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Called any time all claims are re-loaded
 */
public class ClaimsLoadingProcedureEvent extends ClaimEvent {

	private final Map<InvasiveEntity.Tag, List<Claim>> set = new HashMap<>();

	public ClaimsLoadingProcedureEvent(Map<InvasiveEntity.Tag, List<Claim>> map) {
		super(true);
		map.entrySet().forEach(e -> {
			for (Claim claim : e.getValue()) {
				if (claim.getOwner() != null) {
					List<Claim> claims = set.get(e.getKey());
					if (claims == null) {
						claims = new ArrayList<>();
						claims.add(claim);
						set.put(e.getKey(), claims);
					} else {
						claims.add(claim);
					}
				}
			}
		});
	}

	public void insert(Claim claim) {
		if (set.get(claim.getOwner().getTag()) != null) {
			set.get(claim.getOwner().getTag()).add(claim);
		} else {
			List<Claim> list = new ArrayList<>();
			list.add(claim);
			set.put(claim.getOwner().getTag(), list);
		}
	}

	public Set<InvasiveEntity.Tag> getTags() {
		return set.keySet();
	}

	public List<Claim> getClaims(InvasiveEntity.Tag tag) {
		return set.get(tag);
	}

	public List<Claim> getClaims() {
		List<Claim> list = new ArrayList<>();
		set.forEach((tag, claims) -> list.addAll(claims));
		return list;
	}

}
