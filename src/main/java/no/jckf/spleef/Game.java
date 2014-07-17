package no.jckf.spleef;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class Game implements Listener {
	private final Spleef plugin;

	private final Arena arena;
	public final ArrayList<Player> players;

	public Game(Spleef plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;

		this.players = new ArrayList<>();

		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

		this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            this.plugin,
            new GameTask(this.plugin, this.arena, this),
            0,
            20
        );

		start();
	}

	public boolean hasPlayers() {
		return !this.players.isEmpty();
	}

	public void start() {
		this.arena.save();

		ArrayList<String> list = new ArrayList<>();
		for (Player player : this.plugin.getServer().getOnlinePlayers()) {
			Location l = player.getLocation();
			l.setY(l.getY() - 2);

			if (this.arena.contains(l)) {
				this.players.add(player);
				list.add(player.getDisplayName());
			}
		}

		this.broadcast(ChatColor.GREEN + "The game has started! Players: " + ChatColor.WHITE + StringUtils.join(list, ", "));
	}

	public void stop() {
		ArrayList<String> list = new ArrayList<>();
		for (Player player : this.players) {
			list.add(player.getDisplayName());
		}

		this.broadcast(ChatColor.RED + "The game was stopped! Remaining players: " + ChatColor.WHITE + StringUtils.join(list, ", "));

		this.arena.restore();
	}

	public void broadcast(String msg) {
		for (Player player : this.players) {
			player.sendMessage(msg);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (!this.players.contains(event.getPlayer())) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null) {
			return;
		}

		if (!this.arena.contains(block.getLocation())) {
			return;
		}

		block.setType(Material.AIR);
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		this.players.remove(event.getPlayer());
	}
}
