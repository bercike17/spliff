package hu.spliff;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoRemoveTask implements Runnable {
    private final SpliffPlugin plugin;

    public AutoRemoveTask(SpliffPlugin plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        if (plugin.getGameManager().getState() != GameState.RUNNING) return;

        List<Block> snowBlocks = new ArrayList<>();
        ArenaManager a = plugin.getArenaManager();
        int half = a.getSize() / 2;
        int cx = a.getCenter().getBlockX();
        int cy = a.getCenter().getBlockY();
        int cz = a.getCenter().getBlockZ();

        for (int l = 0; l < a.getLayers(); l++) {
            int y = cy + l;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    Block b = a.getCenter().getWorld().getBlockAt(cx + x, y, cz + z);
                    if (b.getType() == Material.SNOW_BLOCK) snowBlocks.add(b);
                }
            }
        }

        if (snowBlocks.isEmpty()) return;

        double percent = plugin.getConfig().getDouble("game.auto-remove-percent", 15);
        int toRemove = Math.max(1, (int) (snowBlocks.size() * (percent / 100.0)));
        Collections.shuffle(snowBlocks);
        List<Block> selected = snowBlocks.subList(0, Math.min(toRemove, snowBlocks.size()));

        for (Block b : selected) b.setType(Material.RED_WOOL);

        int warning = plugin.getConfig().getInt("game.auto-remove-warning", 3) * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block b : selected) {
                if (b.getType() == Material.RED_WOOL) b.setType(Material.AIR);
            }
        }, warning);
    }
}