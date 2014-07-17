package no.jckf.spleef;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Spleef extends JavaPlugin {
	public WorldEditPlugin we;

	private final Map<String, Arena> arenas = new HashMap<>();
	private final Map<String, Game> games = new HashMap<>();

    @Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();

		this.we = (WorldEditPlugin) pm.getPlugin("WorldEdit");

		if (this.we == null) {
			this.getLogger().severe("Could not find WorldEdit!");
			pm.disablePlugin(this);
			return;
		}

		this.loadData();

		this.getCommand("spleef").setExecutor(new Commands(this));
	}

	private void loadData() {
		for (String name : this.getConfig().getConfigurationSection("arenas").getKeys(false)) {
			String[] data = this.getConfig().getString(name).split("\\|");

			World world = this.getServer().getWorld(data[0]);

			this.arenas.put(name, new Arena(
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

	private void saveData() {
		for (String name : this.getConfig().getKeys(false)) {
			this.getConfig().set(name, null);
		}

		for (String name : this.arenas.keySet()) {
            Arena arena = this.arenas.get(name);

			this.getConfig().set("arenas." + name,
				arena.min.getWorld().getName() + "|" +

				arena.min.getBlockX() + "|" +
				arena.min.getBlockY() + "|" +
				arena.min.getBlockZ() + "|" +

				arena.max.getBlockX() + "|" +
				arena.max.getBlockY() + "|" +
				arena.max.getBlockZ()
			);
		}

		this.saveConfig();
	}

	public boolean arenaCreate(Player player, String name) {
		Selection selection = this.we.getSelection(player);

		if (selection == null) {
			player.sendMessage(ChatColor.RED + "No selection made.");
			return true;
		}

		if (this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "Arena name already taken.");
			return true;
		}

		this.arenas.put(name, new Arena(this, selection.getMinimumPoint(), selection.getMaximumPoint()));

		this.saveData();

		player.sendMessage(ChatColor.GREEN + "Arena created.");

		return true;
	}

	public boolean arenaDelete(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "Given arena does not exist.");
			return true;
		}

		this.arenas.remove(name);

		this.saveData();

		player.sendMessage(ChatColor.GREEN + "Arena deleted.");

		return true;
	}

	public boolean arenaStart(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "No such arena.");
			return true;
		}

		if (this.games.containsKey(name)) {
			if (this.games.get(name).hasPlayers()) {
				player.sendMessage(ChatColor.RED + "There is already a game in progress here.");
				return true;
			} else {
				this.games.remove(name);
			}
		}

		Game game = new Game(this, this.arenas.get(name));

		if (!game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + "No players were added.");
		} else {
			this.games.put(name,game);
		}

		return true;
	}

	public boolean arenaStop(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "No such arena.");
			return true;
		}

		Game game = this.games.get(name);

		if (game == null || !game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + "No active game in that arena.");
			return true;
		}

		game.stop();
		this.games.remove(name);

		player.sendMessage(ChatColor.GREEN + "Game stopped.");

		return true;
	}

	public boolean arenaList(Player player) {
		player.sendMessage(ChatColor.GREEN + "Arena list: " + ChatColor.WHITE + StringUtils.join(this.arenas.keySet(), ", "));

		return true;
	}
}
