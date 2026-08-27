package hu.spliff;

import org.bukkit.plugin.java.JavaPlugin;

public class SpliffPlugin extends JavaPlugin {
    private static SpliffPlugin instance;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private MessageManager messageManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.messageManager = new MessageManager(this);
        this.rewardManager = new RewardManager(this);
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this);
        getCommand("spliff").setExecutor(new SpliffCommand(this));
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandWhitelistListener(this), this);
        gameManager.startAutoStart();
        getLogger().info(messageManager.getRaw("plugin-enabled")
            .replace("{status}", String.valueOf(getConfig().getBoolean("auto-start.enabled", false))));
    }

    @Override
    public void onDisable() {
        if (gameManager.getState() == GameState.RUNNING || gameManager.getState() == GameState.WARMUP) {
            gameManager.stopGame();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        messageManager.load();
        rewardManager.load();
        arenaManager.loadConfig();
        gameManager.startAutoStart();
    }

    public static SpliffPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public GameManager getGameManager() { return gameManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}