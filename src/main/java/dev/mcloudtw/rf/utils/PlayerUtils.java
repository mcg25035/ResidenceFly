package dev.mcloudtw.rf.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import dev.mcloudtw.rf.PlayerFlightManager;
import dev.mcloudtw.rf.exceptions.NoResFlyPermissionException;
import dev.mcloudtw.rf.exceptions.NotInResidenceException;
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

    public static boolean playerToggleFly(Player player) throws NotInResidenceException, NoResFlyPermissionException {
        Location playerLocation = player.getLocation();
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(playerLocation);
        if (res == null) {
            throw new NotInResidenceException();
        }

        boolean hasFlightPermission = res.getPermissions().playerHas(player, Flags.fly, FlagPermissions.FlagCombo.TrueOrNone);
        hasFlightPermission = hasFlightPermission || res.getPermissions().playerHas(player, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue);
        hasFlightPermission = hasFlightPermission || res.isOwner(player);

        if (!hasFlightPermission) {
            throw new NoResFlyPermissionException();
        }

        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled) {
            pfm.disableFlight();
            return false;
        }
        pfm.enableFlight();
        return true;
    }

    public static boolean playerToggleFly(Player player, boolean enable) throws NotInResidenceException, NoResFlyPermissionException {
        Location playerLocation = player.getLocation();
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(playerLocation);
        if (res == null) {
            throw new NotInResidenceException();
        }

        boolean hasFlightPermission = res.getPermissions().playerHas(player, Flags.fly, FlagPermissions.FlagCombo.TrueOrNone);
        hasFlightPermission = hasFlightPermission || res.getPermissions().playerHas(player, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue);
        hasFlightPermission = hasFlightPermission || res.isOwner(player);

        if (!hasFlightPermission) {
            throw new NoResFlyPermissionException();
        }

        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled == enable) return false;
        if (enable) {
            pfm.enableFlight();
        } else {
            pfm.disableFlight();
        }
        return true;
    }
}
