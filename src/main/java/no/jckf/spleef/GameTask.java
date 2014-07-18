package no.jckf.spleef;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class GameTask implements Runnable {
    private final Spleef plugin;
    public static int am = 0;
    private final Arena arena;
    private final Game game;

    public GameTask(Spleef plugin, Arena arena, Game game) {
        this.plugin = plugin;

        this.arena = arena;
        this.game = game;
    }

    @Override
    public void run() {
        // Clone to avoid concurrent modification.
        for (Player p : (List<Player>) this.game.players.clone()) {
            Location l = p.getLocation();
            l.setY(l.getY() + 3);

            if (this.arena.contains(l)) {
                if (this.game.players.size() > 1) {
                    this.game.players.remove(p);
                    p.sendMessage(ChatColor.RED + this.plugin.lang.getString("game.lost"));
                    this.game.broadcast(ChatColor.GREEN + p.getDisplayName() + ChatColor.GREEN + this.plugin.lang.getString("game.out"));
                }
            }
            if (this.game.players.size() == 1 && this.game.players.contains(p)) {
                this.plugin.getServer().broadcastMessage(ChatColor.GREEN + p.getDisplayName() + ChatColor.GREEN + this.plugin.lang.getString("game.won"));
                if(plugin.hwEnabled) {
                    plugin.hw.getBankHandler().insertAmount(p, am);
                    plugin.getServer().broadcastMessage(ChatColor.GREEN + "Som også vant " + ChatColor.WHITE + am + ChatColor.GREEN + " gull!");
                    am = 0;
                }
                this.game.players.remove(p);

                this.arena.restore();
            }
        }
    }
}
