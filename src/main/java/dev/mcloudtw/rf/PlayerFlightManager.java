package dev.mcloudtw.rf;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerFlightManager {
    public static HashMap<Player, PlayerFlightManager> playerFlightManagerHashMap = new HashMap<>();
    public static Path playerFlightDataPath = Main.plugin.getDataFolder().toPath().resolve("playerFlightData");
    public static int defaultSecondsLeftEveryDay = 4000;

    public int defaultSecondsLeft;
    public int additionalSecondsLeft;
    public boolean enabled;
    public Date lastResetTime;
    public Player player;
    public BukkitTask task;

    private PlayerFlightManager(int defaultSecondsLeft, int additionalSecondsLeft, long timestamp, Player player) {
        this.defaultSecondsLeft = defaultSecondsLeft;
        this.additionalSecondsLeft = additionalSecondsLeft;
        this.lastResetTime = new Date(timestamp);
        this.enabled = false;
        this.player = player;
        playerFlightManagerHashMap.put(player, this);
    }

    public static PlayerFlightManager loadPlayerFlightData(Player player) {
        if (playerFlightManagerHashMap.containsKey(player)) {
            return playerFlightManagerHashMap.get(player);
        }
        File file = playerFlightDataPath.resolve(player.getUniqueId() + ".yml").toFile();
        if (!file.exists()) {
            return new PlayerFlightManager(defaultSecondsLeftEveryDay, 0, System.currentTimeMillis(), player);
        }
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        int defaultSecondsLeft = fileConfiguration.getInt("defaultSecondsLeft");
        int additionalSecondsLeft = fileConfiguration.getInt("additionalSecondsLeft");
        long timestamp = fileConfiguration.getLong("lastResetTime");
        PlayerFlightManager result = new PlayerFlightManager(defaultSecondsLeft, additionalSecondsLeft, timestamp, player);
        result.saveToFile();
        return result;
    }

    public void saveToFile() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            File file = playerFlightDataPath.resolve(player.getUniqueId() + ".yml").toFile();
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.set("defaultSecondsLeft", defaultSecondsLeft);
            fileConfiguration.set("additionalSecondsLeft", additionalSecondsLeft);
            fileConfiguration.set("lastResetTime", lastResetTime.getTime());
            try {
                fileConfiguration.save(file);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to save player flight data for " + player.getName());
                e.printStackTrace();
            }
        });
    }

    public void enableFlight(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        enabled = true;
        AtomicInteger autoSave = new AtomicInteger(0);
        task = Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
            if (autoSave.getAndIncrement() % 60 == 0) {
                saveToFile();
            }
            if (defaultSecondsLeft + additionalSecondsLeft <= 0) {
                disableFlight(player);
                return;
            }
            if (defaultSecondsLeft <= 0) {
                additionalSecondsLeft--;
            } else {
                defaultSecondsLeft--;
            }
            saveToFile();
        }, 0, 20);
    }

    public void disableFlight(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
        enabled = false;
        task.cancel();
        saveToFile();

    }

}
