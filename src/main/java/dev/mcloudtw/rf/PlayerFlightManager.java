package dev.mcloudtw.rf;

import dev.mcloudtw.rf.utils.PlayerUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
    public static HashMap<OfflinePlayer, PlayerFlightManager> playerFlightManagerHashMap = new HashMap<>();
    public static Path playerFlightDataPath = Main.plugin.getDataFolder().toPath().resolve("playerFlightData");

    public int defaultSecondsLeft;
    public int additionalSecondsLeft;
    public boolean enabled;
    public Date lastResetTime;
    public OfflinePlayer player;
    public BukkitTask task;

    private PlayerFlightManager(int defaultSecondsLeft, int additionalSecondsLeft, long timestamp, OfflinePlayer player) {
        this.defaultSecondsLeft = defaultSecondsLeft;
        this.additionalSecondsLeft = additionalSecondsLeft;
        this.lastResetTime = new Date(timestamp);
        this.enabled = false;
        this.player = player;
        playerFlightManagerHashMap.put(player, this);
        checkReset();
    }

    public void checkReset() {
        if (defaultSecondsLeft > Main.plugin.defaultPlayerFlightSeconds) {
            defaultSecondsLeft = Main.plugin.defaultPlayerFlightSeconds;
            saveToFile();
        }
        if (lastResetTime.after(Main.plugin.lastResetTime)) return;
        defaultSecondsLeft += Main.plugin.defaultPlayerFlightSeconds;
        if (defaultSecondsLeft > Main.plugin.defaultPlayerFlightSeconds) {
            defaultSecondsLeft = Main.plugin.defaultPlayerFlightSeconds;
        }
        lastResetTime = new Date();
        saveToFile();
    }

    public static PlayerFlightManager loadPlayerFlightData(OfflinePlayer player) {
        if (playerFlightManagerHashMap.containsKey(player)) {
            return playerFlightManagerHashMap.get(player);
        }
        File file = playerFlightDataPath.resolve(player.getUniqueId() + ".yml").toFile();
        if (!file.exists()) {
            return new PlayerFlightManager(Main.plugin.defaultPlayerFlightSeconds, 0, System.currentTimeMillis(), player);
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

    public void enableFlight() {
        if (!this.player.isOnline()) return;
        Player player = this.player.getPlayer();
        player.setAllowFlight(true);
        player.setFlying(true);
        enabled = true;
        AtomicInteger autoSave = new AtomicInteger(0);
        task = Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
            checkReset();

            if (!player.isOnline()) {
                disableFlight();
                return;
            }

            if (autoSave.getAndIncrement() % 60 == 0) {
                saveToFile();
            }
            if (defaultSecondsLeft + additionalSecondsLeft <= 0) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                "<red>飛行時間已用完</red>"
                ));
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                "<white>飛行已關閉</white>"
                ));
                disableFlight();
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

    public void disableFlight() {
        enabled = false;
        task.cancel();
        saveToFile();

        if (!this.player.isOnline()) return;
        Player player = this.player.getPlayer();
        assert player != null;
        PlayerUtils.safeLandPlayer(player);

    }

}
