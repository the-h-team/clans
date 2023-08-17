package com.github.sanctum.clans.construct.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ClearanceOverride {

	Data data;

	public ClearanceOverride() {
		this.data = new Data();
	}

	public ClearanceOverride(@NotNull Data data) {
		this.data = data;
	}

	public boolean add(@NotNull Clearance clearance, @NotNull Clan.Rank position) {
		return data.add(clearance, position);
	}

	public boolean remove(@NotNull Clearance clearance, @NotNull Clan.Rank position) {
		return data.remove(clearance, position);
	}

	/**
	 * @param child
	 * @param parent
	 * @return
	 */
	public boolean addInheritance(@NotNull Clan.Rank child, @NotNull Clan.Rank parent) {
		return data.addInheritance(child, parent);
	}

	/**
	 * @param child
	 * @param parent
	 * @return
	 */
	public boolean removeInheritance(@NotNull Clan.Rank child, @NotNull Clan.Rank parent) {
		return data.removeInheritance(child, parent);
	}

	/**
	 * @param position
	 * @return
	 */
	public @NotNull List<Clearance> get(@NotNull Clan.Rank position) {
		return data.getPermissions(position, true).stream().map(Clearance::valueOf).collect(Collectors.toList());
	}

	/**
	 * @param position
	 * @return
	 */
	public @NotNull List<Clearance> getRaw(@NotNull Clan.Rank position) {
		return data.getPermissions(position, false).stream().map(Clearance::valueOf).collect(Collectors.toList());
	}

	/**
	 * @param rank
	 * @return
	 */
	public @NotNull List<Clan.Rank> getInheritance(@NotNull Clan.Rank rank) {
		return data.getInheritanceNames(rank).stream().map(s -> RankRegistry.getInstance().getRank(s)).collect(Collectors.toList());
	}

	public static final class Data implements Serializable {
		private static final long serialVersionUID = -7497706686470009039L;
		Map<String, List<String>> rankMap = new HashMap<>();
		Map<String, List<String>> inheritanceMap = new HashMap<>();

		public Data() {
			RankRegistry registry = RankRegistry.getInstance();
			registry.getRanks().forEach(this::loadClearances); // called one time, once saved will never load again.
		}

		void loadClearances(Clan.Rank p) {// this loads default clearances
			List<String> clearances = new ArrayList<>();
			for (Clearance c : p.getDefaultPermissions()) {
				clearances.add(c.getName());
			}
			for (Clan.Rank pos : p.getInheritance()) {
				addInheritance(pos, p);
			}
			rankMap.put(p.getName(), clearances);
		}

		public boolean add(@NotNull Clearance clearance, @NotNull Clan.Rank position) {
			if (!rankMap.containsKey(position.getName())) {  // clan inherit [groupToInherit] [group] ->
				loadClearances(position);
			}
			List<String> perms = rankMap.get(position.getName());
			if (!perms.contains(clearance.getName())) {
				perms.add(clearance.getName());
				return true;
			}
			return false;
		}

		public boolean remove(@NotNull Clearance clearance, @NotNull Clan.Rank position) {
			if (!rankMap.containsKey(position.getName())) {  // clan inherit [groupToInherit] [group] ->
				loadClearances(position);
			}
			List<String> perms = rankMap.get(position.getName());
			if (perms.contains(clearance.getName())) {
				perms.remove(clearance.getName());
				return true;
			}
			return false;
		}

		public boolean addInheritance(@NotNull Clan.Rank child, @NotNull Clan.Rank parent) {
			List<String> inheritance = getInheritanceNames(parent);
			if (!inheritance.contains(child.getName())) {
				inheritance.add(child.getName());
				return true;
			}
			return false;
		}

		public boolean removeInheritance(@NotNull Clan.Rank child, @NotNull Clan.Rank parent) {
			List<String> inheritance = getInheritanceNames(parent);
			if (inheritance.contains(child.getName())) {
				inheritance.remove(child.getName());
				return true;
			}
			return false;
		}

		List<String> getInheritanceNames(Clan.Rank position) {
			if (!inheritanceMap.containsKey(position.getName())) {
				inheritanceMap.put(position.getName(), new ArrayList<>());
			}
			return inheritanceMap.get(position.getName());
		}

		List<String> getPermissions(Clan.Rank position, boolean combine) {// clan permit Owner MANAGE_LAND
			if (!rankMap.containsKey(position.getName())) {  // clan inherit [groupToInherit] [group] ->
				loadClearances(position);
			}
			RankRegistry registry = RankRegistry.getInstance();
			// make new list to live grab inheritance nodes from positions
			List<String> list = new ArrayList<>(rankMap.get(position.getName()));
			// quick check to remove old inheritance that doesnt exist.
			if (combine) {
				List<String> toRemove = new ArrayList<>();
				List<String> inheritance = getInheritanceNames(position);
				inheritance.forEach(rank -> {
					Clan.Rank asRank = registry.getRank(rank);
					if (asRank != null) {
						List<String> individualPermList = getPermissions(asRank, false);
						list.addAll(individualPermList);
					} else toRemove.add(rank);
				});
				for (String t : toRemove) {
					inheritance.remove(t);
				}
			}
			return list;
		}

	}

}
