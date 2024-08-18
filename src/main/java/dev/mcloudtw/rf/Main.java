package dev.mcloudtw.rf;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        if (!PlayerFlightManager.playerFlightDataPath.toFile().exists()) {
            PlayerFlightManager.playerFlightDataPath.toFile().mkdirs();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
