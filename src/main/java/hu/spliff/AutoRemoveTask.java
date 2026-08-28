package hu.spliff;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoRemoveTask implements Runnable {
    private final SpliffPlugin plugin;

    public AutoRemoveTask(SpliffPlugin plugin) { 
        this.plugin = plugin; 
    }

    @Override
    public void run() {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        ArenaManager a = plugin.getArenaManager();
        Location spawn = a.getSpawn();
        if (spawn == null || spawn.getWorld() == null) return;

        List<Block> snowBlocks = new ArrayList<>();
        int radius = plugin.getConfig().getInt("game.arena-radius", 20);
        int cx = spawn.getBlockX();
        int cy = spawn.getBlockY();
        int cz = spawn.getBlockZ();

        // Keresünk hóblokkokat a spawn körüli területen és az alatta/felette lévő magasságokban
        for (int y = cy - 5; y <= cy + 10; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = spawn.getWorld().getBlockAt(cx + x, y, cz + z);
                    if (b.getType() == Material.SNOW_BLOCK) {
                        snowBlocks.add(b);
                    }
                }
            }
        }

        if (snowBlocks.isEmpty()) return;

        double percent = plugin.getConfig().getDouble("game.auto-remove-percent", 15);
        int toRemove = Math.max(1, (int) (snowBlocks.size() * (percent / 100.0)));
        Collections.shuffle(snowBlocks);
        List<Block> selected = snowBlocks.subList(0, Math.min(toRemove, snowBlocks.size()));

        // Piros gyapjúra váltás figyelmeztetésként
        for (Block b : selected) {
            b.setType(Material.RED_WOOL);
        }

        int warningTicks = plugin.getConfig().getInt("game.auto-remove-warning", 3) * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block b : selected) {
                if (b.getType() == Material.RED_WOOL) {
                    b.setType(Material.AIR);
                }
            }
        }, warningTicks);
    }
}
