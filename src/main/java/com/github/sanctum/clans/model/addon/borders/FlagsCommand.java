package com.github.sanctum.clans.model.addon.borders;

import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlagsCommand extends ClanSubCommand {
	public FlagsCommand() {
		super("flags");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		int length = args.length;
		String prefix = "&f[&6&lX&f]&r";
		Mailer.empty(p).prefix().start(prefix).finish().chat("&f&oCurrent flags: [ &6base&f, &6player&f, &6spawn &f]").deploy();

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.empty().fillArgs(args).then(TabCompletionIndex.ONE, "flags").get();
	}
}
