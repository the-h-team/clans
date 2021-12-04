package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import com.github.sanctum.labyrinth.data.reload.FingerPrint;
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
	static final Map<FingerPrint, AbstractGameRule> SCANNER_MAP = new HashMap<>();
}
