package hu.spliff;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {
    private final SpliffPlugin plugin;
    private String prefix;

    public MessageManager(SpliffPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();
        this.prefix = colorize(cfg.getString("messages.prefix", "&8[&bSpliff&8] &r"));
    }

    public String get(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cHianyzo uzenet: " + key);
        return prefix + colorize(msg);
    }

    public String getRaw(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cHianyzo uzenet: " + key);
        return colorize(msg);
    }

    public String get(String key, String... replacements) {
        String msg = getRaw(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return prefix + msg;
    }

    private String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}