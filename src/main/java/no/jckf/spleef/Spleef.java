package no.jckf.spleef;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Spleef extends JavaPlugin {
	public WorldEditPlugin we;

    YamlConfiguration lang;

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

        this.saveDefaultConfig();

        File langFile = new File(this.getDataFolder(), this.getConfig().getString("language") + ".yml");

        if (!langFile.exists()) {
            this.getLogger().warning("Localization file for " + this.getConfig().getString("language") + " could not be found. Defaulting to English.");
            langFile = new File(this.getDataFolder(), "english.yml");
        }

        this.lang = YamlConfiguration.loadConfiguration(langFile);

		this.loadData();

		this.getCommand("spleef").setExecutor(new Commands(this));
	}

	private void loadData() {
        ConfigurationSection section = this.getConfig().getConfigurationSection("arenas");

        if (section == null || section.getKeys(false).isEmpty())
            return;

		for (String name : section.getKeys(false)) {
			String[] data = section.getString(name).split("\\|");

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
		this.getConfig().set("arenas", null);

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
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaCreate.selectionNull"));
			return true;
		}

		if (this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaCreate.containsKey"));
			return true;
		}

		this.arenas.put(name, new Arena(this, selection.getMinimumPoint(), selection.getMaximumPoint()));

		this.saveData();

		player.sendMessage(ChatColor.GREEN + this.lang.getString("arenaCreate.okay"));

		return true;
	}

	public boolean arenaDelete(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaDelete.notContainsKey"));
			return true;
		}

		this.arenas.remove(name);

		this.saveData();

		player.sendMessage(ChatColor.GREEN + this.lang.getString("arenaDelete.okay"));

		return true;
	}

	public boolean arenaStart(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaStart.notContainsKey"));
			return true;
		}

		if (this.games.containsKey(name)) {
			if (this.games.get(name).hasPlayers()) {
				player.sendMessage(ChatColor.RED + this.lang.getString("arenaStart.containsKey"));
				return true;
			} else {
				this.games.remove(name);
			}
		}

		Game game = new Game(this, this.arenas.get(name));

		if (!game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaStart.notHasPlayers"));
		} else {
			this.games.put(name, game);
		}

		return true;
	}

	public boolean arenaStop(Player player, String name) {
		if (!this.arenas.containsKey(name)) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaStop.notContainsKey"));
			return true;
		}

		Game game = this.games.get(name);

		if (game == null || !game.hasPlayers()) {
			player.sendMessage(ChatColor.RED + this.lang.getString("arenaStop.notHasPlayers"));
			return true;
		}

		game.stop();
		this.games.remove(name);

		player.sendMessage(ChatColor.GREEN + this.lang.getString("arenaStop.okay"));

		return true;
	}

	public boolean arenaList(Player player) {
		player.sendMessage(
            ChatColor.GREEN + this.lang.getString("arenaList.prefix") +
            ChatColor.WHITE + StringUtils.join(this.arenas.keySet(), this.lang.getString("arenaList.separator"))
        );

		return true;
	}
}
