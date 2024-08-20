package dev.mcloudtw.rf.placeholder;

import dev.mcloudtw.rf.Main;
import dev.mcloudtw.rf.PlayerFlightManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerFlyInfoExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "resfly";
    }

    @Override
    public String getAuthor() {
        return "mcloudtw";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    public String formatTime(int seconds) {
//        int hours = seconds / 3600;
//        int minutes = (seconds % 3600) / 60;
//        int sec = seconds % 60;
//        return String.format("%02d小時 %02d分鐘 %02d秒", hours, minutes, sec).replaceAll("00小時 ", "").replaceAll("00分鐘 ", "");
        return String.format("%02d", seconds);
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        int totalTimeDefault = pfm.defaultSecondsLeft;
        int totalTimeAdditional = pfm.additionalSecondsLeft;
        int maxDefaultTime = Main.plugin.defaultPlayerFlightSeconds;
        if (identifier.equals("total_time")) {
            return String.valueOf(formatTime(totalTimeAdditional + totalTimeDefault));
        }
        if (identifier.equals("default_time")) {
            return String.valueOf(formatTime(totalTimeDefault));
        }
        if (identifier.equals("additional_time")) {
            return String.valueOf(formatTime(totalTimeAdditional));
        }
        if (identifier.equals("max_default_time")) {
            return String.valueOf(formatTime(maxDefaultTime));
        }
        if (identifier.equals("scale")) {
            return String.valueOf(Main.plugin.currentBasePlayerFlightSecondsScale)+"x";
        }
        return null;
    }
}
