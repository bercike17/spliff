package hu.spliff;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {
    private final SpliffPlugin plugin;
    private GameState state = GameState.LOBBY;
    private final List<Player> players = new ArrayList<>();
    private final List<Player> spectators = new ArrayList<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private BukkitTask autoTask;
    private BukkitTask autoStartTask;
    private BukkitTask lobbyCountdownTask;
    private BukkitTask warmupTask;
    private int lobbyTimer = 0;
    private int warmupTimer = 0;

    public GameManager(SpliffPlugin plugin) { this.plugin = plugin; }

    public GameState getState() { return state; }
    public List<Player> getPlayers() { return new ArrayList<>(players); }

    public void startAutoStart() {
        if (!plugin.getConfig().getBoolean("auto-start.enabled", false)) return;
        if (autoStartTask != null) return;

        int interval = plugin.getConfig().getInt("auto-start.interval-minutes", 5) * 60 * 20;
        autoStartTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state == GameState.LOBBY && players.size() >= plugin.getConfig().getInt("auto-start.min-players", 2)) {
                startLobbyCountdown();
            }
        }, interval, interval);
    }

    public void startLobbyCountdown() {
        if (state != GameState.LOBBY) return;
        if (lobbyCountdownTask != null) return;

        int duration = plugin.getConfig().getInt("game.lobby-countdown-seconds", 30);
        lobbyTimer = duration;

        lobbyCountdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state != GameState.LOBBY) {
                cancelLobbyCountdown();
                return;
            }

            int min = plugin.getConfig().getInt("game.min-players", 2);
            if (players.size() < min) {
                abortLobbyStart();
                return;
            }

            if (lobbyTimer <= 0) {
                cancelLobbyCountdown();
                startWarmup();
                return;
            }

            if (lobbyTimer <= 5 || lobbyTimer == 10 || lobbyTimer == 15 || lobbyTimer == duration) {
                broadcast("lobby-countdown", "{seconds}", String.valueOf(lobbyTimer));
            }

            lobbyTimer--;
        }, 0L, 20L);
    }

    private void cancelLobbyCountdown() {
        if (lobbyCountdownTask != null) {
            lobbyCountdownTask.cancel();
            lobbyCountdownTask = null;
        }
    }

    private void abortLobbyStart() {
        cancelLobbyCountdown();
        broadcast("not-enough-players");
        for (Player p : new ArrayList<>(players)) {
            restoreInventory(p);
            p.teleport(p.getWorld().getSpawnLocation());
            p.setGameMode(GameMode.SURVIVAL);
        }
        players.clear();
    }

    public void startWarmup() {
        state = GameState.WARMUP;
        plugin.getArenaManager().buildArena();

        for (Player p : players) {
            p.teleport(plugin.getArenaManager().getSpawn());
            p.setGameMode(GameMode.ADVENTURE);
        }

        int duration = plugin.getConfig().getInt("game.warmup-seconds", 10);
        warmupTimer = duration;

        warmupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state != GameState.WARMUP) {
                if (warmupTask != null) warmupTask.cancel();
                return;
            }

            for (Player p : players) {
                p.sendActionBar(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<yellow>Elokeszules: <red>" + warmupTimer + " <yellow>mp"));
            }

            if (warmupTimer <= 0) {
                if (warmupTask != null) warmupTask.cancel();
                startGame();
                return;
            }

            warmupTimer--;
        }, 0L, 20L);
    }

    public void joinPlayer(Player player) {
        if (state != GameState.LOBBY) {
            player.sendMessage(plugin.getMessageManager().get("game-in-progress"));
            return;
        }

        if (players.size() >= plugin.getConfig().getInt("game.max-players", 16)) {
            player.sendMessage(plugin.getMessageManager().getRaw("prefix") + "&cA jatek tele van!");
            return;
        }

        saveInventory(player);
        players.add(player);
        player.teleport(plugin.getArenaManager().getLobby());
        player.setGameMode(GameMode.ADVENTURE);
        broadcast("player-joined", "{player}", player.getName(), "{count}", String.valueOf(players.size()),
            "{max}", String.valueOf(plugin.getConfig().getInt("game.max-players", 16)));

        int min = plugin.getConfig().getInt("game.min-players", 2);
        if (players.size() >= min && lobbyCountdownTask == null) {
            startLobbyCountdown();
        }
    }

    public void leavePlayer(Player player) {
        if (state != GameState.LOBBY) {
            player.sendMessage(plugin.getMessageManager().get("cannot-leave-now"));
            return;
        }

        players.remove(player);
        spectators.remove(player);
        restoreInventory(player);
        player.teleport(plugin.getArenaManager().getLobby());
        player.setGameMode(GameMode.SURVIVAL);
        broadcast("player-left", "{player}", player.getName());
    }

    public void forceLeave(Player player) {
        players.remove(player);
        spectators.remove(player);
        restoreInventory(player);
        player.setGameMode(GameMode.SURVIVAL);
        if (player.isOnline()) {
            player.teleport(plugin.getArenaManager().getLobby());
        }
    }

    public void startGame() {
        state = GameState.RUNNING;

        for (Player p : players) {
            p.teleport(plugin.getArenaManager().getSpawn());
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();

            if (getGameMode() == hu.spliff.GameMode.DIG && plugin.getConfig().getBoolean("game.give-shovel", true)) {
                try {
                    Material shovel = Material.valueOf(plugin.getConfig().getString("game.shovel-item", "DIAMOND_SHOVEL"));
                    int slot = plugin.getConfig().getInt("game.shovel-slot", 0);
                    p.getInventory().setItem(slot, new ItemStack(shovel));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ervenytelen shovel-item a configban!");
                }
            }
        }

        broadcast("game-started");
        broadcast("game-mode-info", "{mode}", getGameMode().name());

        if (getGameMode() == hu.spliff.GameMode.AUTO) {
            int interval = plugin.getConfig().getInt("game.auto-remove-interval", 10) * 20;
            autoTask = Bukkit.getScheduler().runTaskTimer(plugin, new AutoRemoveTask(plugin), interval, interval);
        }
    }

    public void stopGame() {
        state = GameState.LOBBY;
        if (autoTask != null) { autoTask.cancel(); autoTask = null; }
        cancelLobbyCountdown();
        if (warmupTask != null) { warmupTask.cancel(); warmupTask = null; }

        plugin.getArenaManager().clearArena();

        for (Player p : new ArrayList<>(players)) {
            forceLeave(p);
        }
        for (Player p : new ArrayList<>(spectators)) {
            forceLeave(p);
        }

        players.clear();
        spectators.clear();
        broadcast("game-stopped");
    }

    public void eliminatePlayer(Player player) {
        if (!players.contains(player)) return;
        players.remove(player);
        spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        broadcast("player-eliminated", "{player}", player.getName());
        checkWin();
    }

    public void respawnPlayer(Player player) {
        if (!players.contains(player)) return;
        player.teleport(plugin.getArenaManager().getRespawn());
        player.sendMessage(plugin.getMessageManager().get("respawn-fall"));
    }

    public void checkWin() {
        if (state != GameState.RUNNING && state != GameState.WARMUP) return;
        if (players.size() <= 1) {
            state = GameState.ENDING;
            if (players.size() == 1) {
                Player winner = players.get(0);
                broadcast("player-won", "{player}", winner.getName());
                restoreInventory(winner);
                if (plugin.getRewardManager().isAutoGive()) {
                    plugin.getRewardManager().giveRewards(winner);
                }
            } else {
                broadcast("no-winner");
            }
            Bukkit.getScheduler().runTaskLater(plugin, this::stopGame, 100);
        }
    }

    public hu.spliff.GameMode getGameMode() {
        try {
            return hu.spliff.GameMode.valueOf(plugin.getConfig().getString("game.mode", "DIG").toUpperCase());
        } catch (Exception e) { return hu.spliff.GameMode.DIG; }
    }

    private void saveInventory(Player player) {
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());
        player.getInventory().clear();
    }

    private void restoreInventory(Player player) {
        UUID id = player.getUniqueId();
        if (savedInventories.containsKey(id)) {
            player.getInventory().setContents(savedInventories.get(id));
            savedInventories.remove(id);
        }
        if (savedArmor.containsKey(id)) {
            player.getInventory().setArmorContents(savedArmor.get(id));
            savedArmor.remove(id);
        }
    }

    public void broadcast(String key, String... replacements) {
        Bukkit.broadcastMessage(plugin.getMessageManager().get(key, replacements));
    }
}