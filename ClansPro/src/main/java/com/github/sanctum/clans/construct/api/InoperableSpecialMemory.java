package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class InoperableSpecialMemory {
	static final Set<SpecialCarrierAdapter> ADAPTERS = new HashSet<>();
	static final List<Channel.Filter> FILTERS = new ArrayList<>();
	static final Map<String, InvasiveEntity> ENTITY_MAP = new HashMap<>();
}
