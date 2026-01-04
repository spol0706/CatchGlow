package CatchGlow.catchGlow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final CatchGlow plugin;

    public ConfigManager(CatchGlow plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public int getPlayTime() {
        return plugin.getConfig().getInt("game-settings.play-time", 600);
    }

    public int getJailTime() {
        return plugin.getConfig().getInt("game-settings.jail-time", 40);
    }

    public int getItemCooldown() {
        return plugin.getConfig().getInt("game-settings.item-cooldown", 10);
    }

    public int getGlowDuration() {
        return plugin.getConfig().getInt("game-settings.glow-duration", 3);
    }

    public Location getLocation(String path) {
        FileConfiguration config = plugin.getConfig();
        String worldName = config.getString("locations." + path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = config.getDouble("locations." + path + ".x");
        double y = config.getDouble("locations." + path + ".y");
        double z = config.getDouble("locations." + path + ".z");
        return new Location(world, x, y, z);
    }

    public String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "");
    }
}
