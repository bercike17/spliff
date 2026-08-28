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
            case "create":
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessageManager().getRaw("prefix") + "&cCsak jatekos hasznalhatja!");
                    return true;
                }
                Player creator = (Player) sender;
                if (plugin.getServer().getPluginManager().getPlugin("WorldEdit") == null) {
                    creator.sendMessage(plugin.getMessageManager().getRaw("prefix") + "&cWorldEdit nincs telepitve!");
                    return true;
                }
                try {
                    com.sk89q.worldedit.bukkit.WorldEditPlugin we = (com.sk89q.worldedit.bukkit.WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
                    com.sk89q.worldedit.LocalSession session = we.getSession(creator);
                    com.sk89q.worldedit.world.World weWorld = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(creator.getWorld());
                    com.sk89q.worldedit.regions.Region region = session.getSelection(weWorld);
                    
                    com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
                    com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint();
                    
                    plugin.getArenaManager().saveRegion(
                        creator.getWorld(),
                        min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                        max.getBlockX(), max.getBlockY(), max.getBlockZ()
                    );
                    plugin.getArenaManager().buildArena();
                    creator.sendMessage(plugin.getMessageManager().get("arena-created"));
                } catch (Exception e) {
                    creator.sendMessage(plugin.getMessageManager().getRaw("prefix") + "&cEloszor jelolj ki egy teruletet WorldEdit-tel! (&7//wand&c)");
                }
                break;
            case "delete":
                if (!checkAdmin(sender)) return true;
                plugin.getArenaManager().clearArena();
                plugin.getArenaManager().clearRegionConfig();
                sender.sendMessage(plugin.getMessageManager().get("arena-deleted"));
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
            case "setend":
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player)) return true;
                saveLoc((Player) sender, "arena.end");
                sender.sendMessage(plugin.getMessageManager().get("end-set"));
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
