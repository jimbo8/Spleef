package no.jckf.spleef;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Spleef extends JavaPlugin {
	public WorldEditPlugin we;
	private HashMap<String,Arena> arenas;
	private HashMap<String,Game> games;

	public void onEnable() {
		arenas = new HashMap<String,Arena>();
		games = new HashMap<String,Game>();

		PluginManager pm = getServer().getPluginManager();

		we = (WorldEditPlugin) pm.getPlugin("WorldEdit");

		if (we == null) {
			getLogger().severe("Could not find WorldEdit!");
			pm.disablePlugin(this);
			return;
		}

		loadData();

		getCommand("spleef").setExecutor(new Commands(this));
	}

	public void onDisable() {
		// Nothing to see here, people.
	}

	public void loadData() {
		for (String name : getConfig().getConfigurationSection("arenas").getKeys(false)) {
			String[] data = getConfig().getString(name).split("\\|");

			World world = getServer().getWorld(data[0]);

			arenas.put(name,new Arena(
				this,
				world.getBlockAt(
					Integer.parseInt(data[1]),
					Integer.parseInt(data[2]),
					Integer.parseInt(data[3])
				).getLocation(),
				world.getBlockAt(
					Integer.parseInt(data[4]),
					Integer.parseInt(data[5]),
					Integer.parseInt(data[6])
				).getLocation()
			));
		}
	}

	public void saveData() {
		for (String name : getConfig().getKeys(false)) {
			getConfig().set(name,null);
		}

		for (String name : arenas.keySet()) {
			getConfig().set("arenas." + name,
				arenas.get(name).min.getWorld().getName() + "|" +

				arenas.get(name).min.getBlockX() + "|" +
				arenas.get(name).min.getBlockY() + "|" +
				arenas.get(name).min.getBlockZ() + "|" +

				arenas.get(name).max.getBlockX() + "|" +
				arenas.get(name).max.getBlockY() + "|" +
				arenas.get(name).max.getBlockZ()
			);
		}

		saveConfig();
	}

	public boolean arenaCreate(Player player,String name) {
		Selection selection = we.getSelection(player);

		if (selection == null) {
			player.sendMessage(ChatColor.RED + "No selection made.");
			return true;
		}

		if (arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "Arena name already taken.");
			return true;
		}

		arenas.put(name,new Arena(this,selection.getMinimumPoint(),selection.getMaximumPoint()));

		saveData();

		player.sendMessage(ChatColor.GREEN + "Arena created.");

		return true;
	}

	public boolean arenaDelete(Player player,String name) {
		if (!arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "Given arena does not exist.");
			return true;
		}

		arenas.remove(name);

		saveData();

		player.sendMessage(ChatColor.GREEN + "Arena deleted.");

		return true;
	}

	public boolean arenaStart(Player player,String name) {
		if (!arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "No such arena.");
			return true;
		}

		if (games.containsKey(name)) {
			if (games.get(name).hasPlayers()) {
				player.sendMessage(ChatColor.RED + "There is already a game in progress here.");
				return true;
			} else {
				games.remove(name);
			}
		}

		Game game = new Game(this,arenas.get(name));

		if (!game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + "No players were added.");
		} else {
			games.put(name,game);
		}

		return true;
	}

	public boolean arenaStop(Player player,String name) {
		if (!arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "No such arena.");
			return true;
		}

		Game game = games.get(name);

		if (!games.containsKey(name) || !game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + "No active game in that arena.");
			return true;
		}

		game.stop();
		games.remove(name);

		player.sendMessage(ChatColor.GREEN + "Game stopped.");

		return true;
	}

	public boolean arenaList(Player player) {
		String list = "";
		for (String name : arenas.keySet()) {
			list += name + ", ";
		}

		player.sendMessage(ChatColor.GREEN + "Arena list: " + ChatColor.WHITE + list.substring(0,Math.max(0,list.length() - 2)));

		return true;
	}
}
