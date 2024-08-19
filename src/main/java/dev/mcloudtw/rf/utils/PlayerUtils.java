package dev.mcloudtw.rf.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerUtils {
    public static void safeLandPlayer(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
        Location safeLanding = player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 1, 0);
        if (safeLanding.getBlockY() <= player.getLocation().getBlockY()) player.teleport(safeLanding);

        safeLanding = player.getLocation();
        while (safeLanding.getBlock().isPassable() && !safeLanding.getBlock().isLiquid()) {
            safeLanding.subtract(0, 1, 0);
        }
        safeLanding.add(0, 1, 0);
        player.teleport(safeLanding);
    }
}
