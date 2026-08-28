package hu.spliff;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

public class ArenaManager {
    private final SpliffPlugin plugin;
    private Location spawn, lobby, end;
    private World regionWorld;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private boolean hasRegion = false;

    public ArenaManager(SpliffPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration cfg = plugin.getConfig();
        World world = plugin.getServer().getWorld(
            cfg.getString("arena.spawn.world", "world"));
        if (world == null) world = plugin.getServer().getWorlds().get(0);

        this.spawn = loc(cfg, "arena.spawn", world, 0, 105, 0);
        this.lobby = loc(cfg, "arena.lobby", world, 10, 105, 10);
        this.end = loc(cfg, "arena.end", world, 20, 100, 20);

        if (cfg.contains("arena.region.world")) {
            World rWorld = plugin.getServer().getWorld(cfg.getString("arena.region.world", "world"));
            if (rWorld == null) rWorld = world;
            this.regionWorld = rWorld;
            this.minX = cfg.getInt("arena.region.min-x");
            this.minY = cfg.getInt("arena.region.min-y");
            this.minZ = cfg.getInt("arena.region.min-z");
            this.maxX = cfg.getInt("arena.region.max-x");
            this.maxY = cfg.getInt("arena.region.max-y");
            this.maxZ = cfg.getInt("arena.region.max-z");
            this.hasRegion = true;
        }
    }

    private Location loc(FileConfiguration c, String path, World w, double dx, double dy, double dz) {
        return new Location(w,
            c.getDouble(path + ".x", dx),
            c.getDouble(path + ".y", dy),
            c.getDouble(path + ".z", dz));
    }

    public void saveRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.regionWorld = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.hasRegion = true;

        FileConfiguration cfg = plugin.getConfig();
        cfg.set("arena.region.world", world.getName());
        cfg.set("arena.region.min-x", minX);
        cfg.set("arena.region.min-y", minY);
        cfg.set("arena.region.min-z", minZ);
        cfg.set("arena.region.max-x", maxX);
        cfg.set("arena.region.max-y", maxY);
        cfg.set("arena.region.max-z", maxZ);
        plugin.saveConfig();
    }

    public void clearRegionConfig() {
        this.hasRegion = false;
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("arena.region", null);
        plugin.saveConfig();
    }

    public void buildArena() {
        if (!hasRegion || regionWorld == null) return;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    regionWorld.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    public void clearArena() {
        if (!hasRegion || regionWorld == null) return;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    regionWorld.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public boolean isInArena(Block block) {
        if (!hasRegion || regionWorld == null) return false;
        if (!block.getWorld().equals(regionWorld)) return false;
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        return bx >= minX && bx <= maxX &&
               by >= minY && by <= maxY &&
               bz >= minZ && bz <= maxZ;
    }

    public boolean hasRegion() { return hasRegion; }

    public Location getSpawn() { return spawn; }
    public Location getLobby() { return lobby; }
    public Location getEnd() { return end; }
}
