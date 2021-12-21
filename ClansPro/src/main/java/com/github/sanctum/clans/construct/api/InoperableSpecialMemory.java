package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SpecialCarrierAdapter;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthSet;
import com.github.sanctum.labyrinth.data.reload.FingerPrint;

abstract class InoperableSpecialMemory {
	static final LabyrinthCollection<SpecialCarrierAdapter> ADAPTERS = new LabyrinthSet<>();
	static final LabyrinthCollection<Channel.Filter> FILTERS = new LabyrinthSet<>();
	static final LabyrinthCollection<QnA> QNA = new LabyrinthSet<>();
	static final LabyrinthMap<String, InvasiveEntity> ENTITY_MAP = new LabyrinthEntryMap<>();
	static final LabyrinthMap<FingerPrint, AbstractGameRule> SCANNER_MAP = new LabyrinthEntryMap<>();
}
