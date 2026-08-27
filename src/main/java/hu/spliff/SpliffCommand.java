package hu.spliff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpliffCommand implements CommandExecutor {
    private final SpliffPlugin plugin;

    public SpliffCommand(SpliffPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageManager().get("usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!checkAdmin(sender)) return true;
                plugin.getGameManager().startLobbyCountdown();
                break;
            case "stop":
                if (!checkAdmin(sender)) return true;
                plugin.getGameManager().stopGame();
                break;
            case "reload":
                if (!checkAdmin(sender)) return true;
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getMessageManager().get("reload-success"));
                break;
            case "reward":
                if (!checkAdmin(sender)) return true;
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().get("reward-usage"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessageManager().get("player-not-found"));
                    return true;
                }
                plugin.getRewardManager().giveRewards(target);
                sender.sendMessage(plugin.getMessageManager().get("reward-given", "{player}", target.getName()));
                target.sendMessage(plugin.getMessageManager().get("reward-received"));
                break;
            case "setspawn":
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player)) return true;
                saveLoc((Player) sender, "arena.spawn");
                sender.sendMessage(plugin.getMessageManager().get("spawn-set"));
                break;
            case "setlobby":
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player)) return true;
                saveLoc((Player) sender, "arena.lobby");
                sender.sendMessage(plugin.getMessageManager().get("lobby-set"));
                break;
            case "setrespawn":
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player)) return true;
                saveLoc((Player) sender, "respawn-location");
                sender.sendMessage(plugin.getMessageManager().get("respawn-set"));
                break;
            case "join":
                if (!(sender instanceof Player)) return true;
                plugin.getGameManager().joinPlayer((Player) sender);
                break;
            case "leave":
                if (!(sender instanceof Player)) return true;
                plugin.getGameManager().leavePlayer((Player) sender);
                break;
            default:
                sender.sendMessage(plugin.getMessageManager().get("unknown-command"));
        }
        return true;
    }

    private boolean checkAdmin(CommandSender s) {
        if (!s.hasPermission("spliff.admin")) {
            s.sendMessage(plugin.getMessageManager().get("no-permission"));
            return false;
        }
        return true;
    }

    private void saveLoc(Player p, String path) {
        plugin.getConfig().set(path + ".world", p.getWorld().getName());
        plugin.getConfig().set(path + ".x", p.getLocation().getX());
        plugin.getConfig().set(path + ".y", p.getLocation().getY());
        plugin.getConfig().set(path + ".z", p.getLocation().getZ());
        plugin.saveConfig();
    }
}