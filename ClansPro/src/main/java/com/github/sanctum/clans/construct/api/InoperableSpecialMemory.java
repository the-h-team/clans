package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import com.github.sanctum.labyrinth.data.reload.FingerPrint;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;

abstract class InoperableSpecialMemory {
	static final PantherCollection<SpecialCarrierAdapter> ADAPTERS = new PantherSet<>();
	static final PantherCollection<Channel.Filter> FILTERS = new PantherSet<>();
	static final PantherCollection<QnA> QNA = new PantherSet<>();
	static final PantherMap<String, InvasiveEntity> ENTITY_MAP = new PantherEntryMap<>();
	static final PantherMap<FingerPrint, AbstractGameRule> SCANNER_MAP = new PantherEntryMap<>();
}
