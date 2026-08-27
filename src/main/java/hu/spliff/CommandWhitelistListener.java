package hu.spliff;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandWhitelistListener implements Listener {
    private final SpliffPlugin plugin;

    public CommandWhitelistListener(SpliffPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("command-whitelist.enabled", false)) return;

        GameState state = plugin.getGameManager().getState();
        if (state == GameState.LOBBY || state == GameState.ENDING) return;

        Player player = event.getPlayer();
        if (player.hasPermission("spliff.admin")) return;

        String fullCmd = event.getMessage().toLowerCase();
        String cmd = fullCmd.split(" ")[0];

        // Spliff saját parancsai mindig engedélyezettek
        if (cmd.equals("/spliff") || fullCmd.startsWith("/spliff ")) {
            return;
        }

        List<String> allowed = plugin.getConfig().getStringList("command-whitelist.allowed-commands");

        for (String a : allowed) {
            String check = a.toLowerCase();
            if (cmd.equals(check) || fullCmd.startsWith(check + " ")) {
                return;
            }
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getMessageManager().get("command-not-allowed"));
    }
}