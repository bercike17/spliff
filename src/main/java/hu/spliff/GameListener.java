package hu.spliff;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {
    private final SpliffPlugin plugin;

    public GameListener(SpliffPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getGameManager().getState() != GameState.RUNNING) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        if (!plugin.getGameManager().getPlayers().contains(p)) return;

        Block block = e.getBlock();
        if (!plugin.getArenaManager().isInArena(block)) return;

        if (plugin.getGameManager().getGameMode() == hu.spliff.GameMode.DIG) {
            if (block.getType() == Material.SNOW_BLOCK) {
                if (p.getInventory().getItemInMainHand().getType().toString().contains("SHOVEL")) {
                    e.setDropItems(false);
                    e.setExpToDrop(0);
                } else {
                    e.setCancelled(true);
                    p.sendMessage(plugin.getMessageManager().get("dig-only-shovel"));
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
            p.sendMessage(plugin.getMessageManager().get("auto-no-break"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        GameState state = plugin.getGameManager().getState();
        if (state != GameState.RUNNING && state != GameState.WARMUP) return;

        Player p = e.getPlayer();
        if (!plugin.getGameManager().getPlayers().contains(p)) return;

        int threshold = plugin.getConfig().getInt("game.fall-threshold", 80);
        if (p.getLocation().getY() < threshold) {
            if (state == GameState.WARMUP) {
                p.teleport(plugin.getArenaManager().getSpawn());
            } else {
                plugin.getGameManager().eliminatePlayer(p);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();

        if (!(damager instanceof Player) || !(victim instanceof Player)) return;

        if (plugin.getGameManager().getPlayers().contains((Player) damager) &&
            plugin.getGameManager().getPlayers().contains((Player) victim)) {
            e.setCancelled(true);
            damager.sendMessage(plugin.getMessageManager().get("pvp-disabled"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.getGameManager().getPlayers().contains(p) ||
            plugin.getGameManager().getState() != GameState.LOBBY) {
            plugin.getGameManager().eliminatePlayer(p);
            plugin.getGameManager().forceLeave(p);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (plugin.getGameManager().getPlayers().contains(p)) {
            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setDroppedExp(0);
            plugin.getGameManager().eliminatePlayer(p);
        }
    }
}
