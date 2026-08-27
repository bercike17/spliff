package hu.spliff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class RewardManager {
    private final SpliffPlugin plugin;
    private boolean enabled;
    private boolean autoGive;
    private List<String> commands;
    private List<String> messages;

    public RewardManager(SpliffPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        enabled = plugin.getConfig().getBoolean("rewards.enabled", true);
        autoGive = plugin.getConfig().getBoolean("rewards.auto-give", true);
        commands = plugin.getConfig().getStringList("rewards.commands");
        messages = plugin.getConfig().getStringList("rewards.messages");
    }

    public void giveRewards(Player player) {
        if (!enabled) return;

        for (String cmd : commands) {
            String parsed = cmd.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        for (String msg : messages) {
            String parsed = msg.replace("{player}", player.getName());
            player.sendMessage(plugin.getMessageManager().getRaw("prefix") + colorize(parsed));
        }
    }

    public boolean isEnabled() { return enabled; }
    public boolean isAutoGive() { return autoGive; }

    private String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}