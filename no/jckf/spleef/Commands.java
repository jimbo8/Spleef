package no.jckf.spleef;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	private final Spleef plugin;

	public Commands(Spleef _plugin) {
		plugin = _plugin;
	}

	public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used in-game.");
			return true;
		}

		Player player = (Player) sender;

		// TODO: Maybe implement arena admins?
		if (!player.isOp()) {
			return true;
		}

		if (args.length == 0 || args.length > 2) {
			return false;
		}

		switch (args[0].toLowerCase()) {
			case "create":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "Missing arena name.");
					return false;
				}
				return plugin.arenaCreate(player,args[1]);

			case "delete":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "Missing arena name.");
					return false;
				}
				return plugin.arenaDelete(player,args[1]);

			case "start":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "Missing arena name.");
					return false;
				}
				return plugin.arenaStart(player,args[1]);

			case "stop":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + "Missing arena name.");
					return false;
				}
				return plugin.arenaStop(player,args[1]);

			case "list":
				return plugin.arenaList(player);
		}

		return false;
	}
}
