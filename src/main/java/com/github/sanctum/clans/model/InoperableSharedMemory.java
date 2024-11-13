package com.github.sanctum.clans.model;

import com.github.sanctum.labyrinth.data.reload.FingerPrint;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;

abstract class InoperableSharedMemory {
	static final PantherCollection<ChatChannel.Filter> FILTERS = new PantherSet<>();
	static final PantherCollection<QnA> QNA = new PantherSet<>();
	static final PantherMap<String, InvasiveEntity> ENTITY_MAP = new PantherEntryMap<>();
	static final PantherMap<FingerPrint, ClanGameRule> SCANNER_MAP = new PantherEntryMap<>();
}
