package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.file.Configurable;
import java.io.IOException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class FileTypeCalculator {

	final DataManager dataManager;
	final Configurable.Type type;

	public FileTypeCalculator(@NotNull DataManager dataManager) {
		this.dataManager = dataManager;
		FileManager file = ClansAPI.getInstance().getFileList().get("global", Configurable.Type.JSON);
		if (!file.getRoot().exists()) {
			try {
				file.getRoot().create();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (Arrays.stream(dataManager.getClanFolder().listFiles()).anyMatch(f -> f.getName().endsWith(".json"))) {
			if (!file.getRoot().isString("file-type")) {
				file.write(t -> t.set("file-type", "JSON"));
			}
		} else {
			if (!file.getRoot().isString("file-type")) {
				file.write(t -> t.set("file-type", "YAML"));
			}
		}
		this.type = Configurable.Type.valueOf(file.read(f -> f.getString("file-type")));
	}

	public Configurable.Type getType() {
		return type;
	}
}
