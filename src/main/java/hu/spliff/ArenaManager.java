package hu.spliff;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

public class ArenaManager {
    private final SpliffPlugin plugin;
    private Location center, spawn, lobby, respawn;
    private int size, layers;

    public ArenaManager(SpliffPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        World world = plugin.getServer().getWorld(
            cfg.getString("arena.center.world", "world"));
        if (world == null) world = plugin.getServer().getWorlds().get(0);

        this.center = loc(cfg, "arena.center", world, 0, 100, 0);
        this.spawn = loc(cfg, "arena.spawn", world, 0, 105, 0);
        this.lobby = loc(cfg, "arena.lobby", world, 0, 100, 50);
        this.respawn = loc(cfg, "respawn-location", world, 0, 105, 0);
        this.size = cfg.getInt("arena.size", 25);
        this.layers = cfg.getInt("arena.layers", 4);
    }

    private Location loc(FileConfiguration c, String path, World w, double dx, double dy, double dz) {
        return new Location(w,
            c.getDouble(path + ".x", dx),
            c.getDouble(path + ".y", dy),
            c.getDouble(path + ".z", dz));
    }

    public void buildArena() {
        World w = center.getWorld();
        int half = size / 2;
        for (int l = 0; l < layers; l++) {
            int y = center.getBlockY() + l;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    w.getBlockAt(center.getBlockX() + x, y, center.getBlockZ() + z)
                     .setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    public void clearArena() {
        World w = center.getWorld();
        int half = size / 2;
        for (int l = 0; l < layers; l++) {
            int y = center.getBlockY() + l;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    Block b = w.getBlockAt(center.getBlockX() + x, y, center.getBlockZ() + z);
                    if (b.getType() == Material.SNOW_BLOCK || b.getType() == Material.RED_WOOL) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public boolean isInArena(Block block) {
        int half = size / 2;
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
        return bx >= cx - half && bx <= cx + half &&
               bz >= cz - half && bz <= cz + half &&
               by >= cy && by < cy + layers;
    }

    public Location getCenter() { return center; }
    public Location getSpawn() { return spawn; }
    public Location getLobby() { return lobby; }
    public Location getRespawn() { return respawn; }
    public int getSize() { return size; }
    public int getLayers() { return layers; }
}