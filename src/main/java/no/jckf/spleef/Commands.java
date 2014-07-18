package no.jckf.spleef;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	private final Spleef plugin;

	public Commands(Spleef plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used in-game.");
			return true;
		}

		Player player = (Player) sender;

		// TODO: Maybe implement arena admins?
		if (!player.isOp()) {
			return true;
		}
        if(plugin.hwEnabled){
            if (args.length == 0 || args.length > 3) {
                return false;
            }
        }else{
            if (args.length == 0 || args.length > 2) {
                return false;
            }
        }


		switch (args[0].toLowerCase()) {
			case "create":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + this.plugin.lang.getString("onCommand.argsLength"));
					return false;
				}
				return plugin.arenaCreate(player,args[1]);

			case "delete":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + this.plugin.lang.getString("onCommand.argsLength"));
					return false;
				}
				return plugin.arenaDelete(player,args[1]);

			case "start":
            if(plugin.hwEnabled){
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + this.plugin.lang.getString("onCommand.argsLength"));
                    return false;
                }
                if(plugin.isInt(args[2])) {
                    GameTask.am = Integer.parseInt(args[2]);
                    if (plugin.hw.getBankHandler().getAmount(player) > Integer.parseInt(args[2])) {
                        //plugin.amount.add(Game.am);
                        plugin.hw.getBankHandler().removeAmount(player, Integer.parseInt(args[2]));
                        System.out.println(player.getName() + " la til " + Integer.parseInt(args[2]) + " til spleef-potten");
                        return plugin.arenaStart(player, args[1]);
                    } else {
                        player.sendMessage(ChatColor.RED + "Du har ikke nok gull.");
                        return false;
                    }
                }else{
                    player.sendMessage(ChatColor.RED + "Du må skrive et tall - bokstaver gjelder ikke!");
                    return false;
                }
            }else {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + plugin.lang.getString("onCommand.argsLength"));
                    return false;
                }
                return plugin.arenaStart(player, args[1]);
            }
			case "stop":
				if (args.length < 2) {
					player.sendMessage(ChatColor.RED + this.plugin.lang.getString("onCommand.argsLength"));
					return false;
				}
				return plugin.arenaStop(player,args[1]);

			case "list":
				return plugin.arenaList(player);
		}

		return false;
	}
}
