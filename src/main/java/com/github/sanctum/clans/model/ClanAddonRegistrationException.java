package com.github.sanctum.clans.model;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.task.Procedure;
import java.io.File;
import java.io.IOException;

public class ClanAddonRegistrationException extends ClanError {

	private static final long serialVersionUID = 3461897427506958729L;

	public ClanAddonRegistrationException(String msg) {
		super(msg);
	}

	public static Procedure<ClansJavaPlugin> getLoadingProcedure() {
		return Procedure.request(() -> ClansJavaPlugin.class).next(instance -> {
			File file = FileList.search(instance).get("dummy", "Addons").getRoot().getParent().getParentFile();
			int amount = 0;
			for (File f : file.listFiles()) {
				if (f.isDirectory()) continue;
				try {
					Clan.Addon addon = new ClanAddonClassLoader(f).getMainClass();
					ClanAddonRegistry.getInstance().load(addon);
					instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					instance.getLogger().info("- Injected: " + addon.getName() + " v" + addon.getVersion());
					instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					amount++;
				} catch (IOException | InvalidAddonException | ClanAddonRegistrationException |
						 ClanAddonDependencyError e) {
					e.printStackTrace();
				}
			}
			instance.getLogger().info("- (" + amount + ") clan addon(s) were injected into cache.");
		});
	}

}
