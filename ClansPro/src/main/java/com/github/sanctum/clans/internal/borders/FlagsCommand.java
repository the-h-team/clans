package com.github.sanctum.clans.internal.borders;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.library.Message;
import java.util.Collections;
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
		Message msg = Message.form(p).setPrefix(prefix);
		msg.send("&f&oCurrent flags: [ &6base&f, &6player&f, &6spawn &f]");

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return TabCompletion.build(getLabel()).forArgs(args).level(1).completeAt(getLabel()).filter(() -> Collections.singletonList("flags")).collect().get(args.length);
	}
}
