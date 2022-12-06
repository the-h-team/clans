package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.reload.PrintManager;
import com.github.sanctum.labyrinth.library.Mailer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReloadUtility {

	public static void reload() {
		final ClansAPI api = ClansAPI.getInstance();
		PrintManager printManager = LabyrinthProvider.getInstance().getLocalPrintManager();
		FileManager config = api.getFileList().get("Config", "Configuration");
		FileManager messages = api.getFileList().get("Messages", "Configuration");
		config.getRoot().reload();
		messages.getRoot().reload();
		FileManager main = ClansAPI.getDataInstance().getConfig();
		((ClansJavaPlugin) api.getPlugin()).setPrefix(new MessagePrefix(main.getRoot().getString("Formatting.prefix.prefix"), main.getRoot().getString("Formatting.prefix.text"), main.getRoot().getString("Formatting.prefix.suffix")));
		api.getClanManager().refresh();
		api.getClaimManager().refresh();
		printManager.getPrint(api.getLocalPrintKey()).reload().deploy();
	}

	public static void reload(@NotNull Lang lang, @Nullable Player playerReloading) {
		final ClansAPI api = ClansAPI.getInstance();
		PrintManager printManager = LabyrinthProvider.getInstance().getLocalPrintManager();
		FileManager config = api.getFileList().get("Config", "Configuration");
		config.getRoot().delete();
		FileManager messages = api.getFileList().get("Messages", "Configuration");
		messages.getRoot().delete();
		FileList list = FileList.search(api.getPlugin());
		FileManager nc = list.get("Config", "Configuration");
		FileManager nm = list.get("Messages", "Configuration");
		String type = config.read(f -> f.getNode("Formatting").getNode("file-type").toPrimitive().getString());
		switch (lang) {
			case EN_US:
				if (type != null) {
					if (type.equals("JSON")) {
						list.copyYML("Config_json", nc);
					} else {
						list.copyYML("Config", nc);
					}
				} else {
					list.copyYML("Config", nc);
				}
				nc.getRoot().reload();
				list.copyYML("Messages", nm);
				nm.getRoot().reload();
				if (playerReloading != null) {
					Mailer.empty(playerReloading).prefix().start(api.getPrefix().toString()).finish().chat("&a&oWaddup g, you ready to get blocky.").deploy();
				} else {
					Mailer.empty().prefix().start(api.getPrefix().toString()).finish().info("&a&oWaddup g, you ready to get blocky.").deploy();
				}
				FileManager main = ClansAPI.getDataInstance().getConfig();

				((ClansJavaPlugin) api.getPlugin()).setPrefix(new MessagePrefix(main.getRoot().getString("Formatting.prefix.prefix"), main.getRoot().getString("Formatting.prefix.text"), main.getRoot().getString("Formatting.prefix.suffix")));
				api.getClanManager().refresh();
				api.getClaimManager().refresh();
				printManager.getPrint(api.getLocalPrintKey()).reload().deploy();
				break;
			case ES_ES:
				if (type != null) {
					if (type.equals("JSON")) {
						list.copyYML("Config_es_json", nc);
					} else {
						list.copyYML("Config_es", nc);
					}
				} else {
					list.copyYML("Config_es", nc);
				}
				nc.getRoot().reload();
				list.copyYML("Messages_es", nm);
				nm.getRoot().reload();
				if (playerReloading != null) {
					Mailer.empty(playerReloading).prefix().start(api.getPrefix().toString()).finish().chat("&a&oHola mi amigo!").deploy();
				} else {
					Mailer.empty().prefix().start(api.getPrefix().toString()).finish().info("&a&oHola mi amigo!").deploy();
				}
				FileManager main2 = ClansAPI.getDataInstance().getConfig();

				((ClansJavaPlugin) api.getPlugin()).setPrefix(new MessagePrefix(main2.getRoot().getString("Formatting.prefix.prefix"), main2.getRoot().getString("Formatting.prefix.text"), main2.getRoot().getString("Formatting.prefix.suffix")));
				api.getClanManager().refresh();
				api.getClaimManager().refresh();
				printManager.getPrint(api.getLocalPrintKey()).reload().deploy();
				break;
			case PT_BR:
				if (type != null) {
					if (type.equals("JSON")) {
						list.copyYML("Config_pt_br_json", nc);
					} else {
						list.copyYML("Config_pt_br", nc);
					}
				} else {
					list.copyYML("Config_pt_br", nc);
				}
				nc.getRoot().reload();
				list.copyYML("Messages_pt_br", nm);
				nm.getRoot().reload();
				if (playerReloading != null) {
					Mailer.empty(playerReloading).prefix().start(api.getPrefix().toString()).finish().chat("&a&oAgora traduzido para o brasil!").deploy();
				} else {
					Mailer.empty().prefix().start(api.getPrefix().toString()).finish().info("&a&oAgora traduzido para o brasil!").deploy();
				}

				FileManager main3 = ClansAPI.getDataInstance().getConfig();

				((ClansJavaPlugin) api.getPlugin()).setPrefix(new MessagePrefix(main3.getRoot().getString("Formatting.prefix.prefix"), main3.getRoot().getString("Formatting.prefix.text"), main3.getRoot().getString("Formatting.prefix.suffix")));
				api.getClanManager().refresh();
				api.getClaimManager().refresh();
				printManager.getPrint(api.getLocalPrintKey()).reload().deploy();
				break;
		}
	}

	public enum Lang {
		EN_US,
		ES_ES,
		PT_BR;
	}

}
