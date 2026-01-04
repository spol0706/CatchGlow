package CatchGlow.catchGlow;

import org.bukkit.plugin.java.JavaPlugin;

public final class CatchGlow extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerManager playerManager;
    private ItemManager itemManager;
    private GameManager gameManager;
    private EventListener eventListener;
    private Commands commands;

    @Override
    public void onEnable() {
        // Initialize Managers
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        playerManager = new PlayerManager();
        itemManager = new ItemManager();
        gameManager = new GameManager(this, playerManager, configManager, itemManager);
        eventListener = new EventListener(gameManager, playerManager, configManager, itemManager);
        commands = new Commands(gameManager, configManager);

        // Register Events
        getServer().getPluginManager().registerEvents(eventListener, this);

        // Register Commands
        getCommand("시작").setExecutor(commands);
        getCommand("중지").setExecutor(commands);
        getCommand("cgreload").setExecutor(commands);
        
        getLogger().info("CatchGlow Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.isRunning()) {
            gameManager.forceStop();
        }
        getLogger().info("CatchGlow Plugin Disabled!");
    }
}
