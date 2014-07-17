package no.jckf.spleef;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.HashMap;

public class Arena {
	private final Spleef plugin;

	public final Location min;
	public final Location max;

	private HashMap<Location, BlockState> snapshot;

	public Arena(Spleef plugin, Location min, Location max) {
		this.plugin = plugin;
		this.min = min;
		this.max = max;

		this.snapshot = new HashMap<>();
	}

	public boolean contains(Location l) {
		return
			l.getBlockX() >= min.getBlockX() && l.getBlockX() <= max.getBlockX() &&
			l.getBlockY() >= min.getBlockY() && l.getBlockY() <= max.getBlockY() &&
			l.getBlockZ() >= min.getBlockZ() && l.getBlockZ() <= max.getBlockZ()
		;
	}

	public void save() {
		World world = min.getWorld();

		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
				Block b = world.getBlockAt(x,max.getBlockY(),z);
				snapshot.put(b.getLocation(),b.getState());
			}
		}
	}

	public void restore() {
		for (BlockState s : snapshot.values()) {
			s.update(true);
		}
	}
}
