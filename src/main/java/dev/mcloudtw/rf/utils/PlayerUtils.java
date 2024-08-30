package dev.mcloudtw.rf.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import dev.mcloudtw.rf.PlayerFlightManager;
import dev.mcloudtw.rf.exceptions.NoResFlyPermissionException;
import dev.mcloudtw.rf.exceptions.NotInResidenceException;
import dev.mcloudtw.rf.exceptions.WrongGamemodeException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerUtils {
    public static void safeLandPlayer(Player player) throws WrongGamemodeException {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            throw new WrongGamemodeException();
        boolean isFlying = player.isFlying();
        player.setAllowFlight(false);
        player.setFlying(false);
        if (player.isGliding()) return;
        if (player.isOnGround()) return;
        if (!isFlying) return;
        Vector direction = player.getLocation().getDirection();
        Location safeLanding = player.getWorld().getHighestBlockAt(player.getLocation()).getLocation();
        if (safeLanding.getBlockY() <= player.getLocation().getBlockY()) {
            if (safeLanding.getBlock().isEmpty()) return;
            safeLanding.setDirection(direction);
            player.teleport(safeLanding.add(0, 1, 0));
            return;
        }

        safeLanding = player.getLocation();
        while (safeLanding.getBlock().isPassable() && !safeLanding.getBlock().isLiquid()) {
            safeLanding.subtract(0, 1, 0);
            if (safeLanding.getBlockY() < -64) return;
        }
        safeLanding.add(0, 1, 0);
        safeLanding.setDirection(direction);
        player.teleport(safeLanding);
    }

    public static boolean canPlayerFly(Player player, ClaimedResidence res) {
        boolean hasFlightPermission = res.getPermissions().playerHas(player, Flags.fly, FlagPermissions.FlagCombo.TrueOrNone);
        hasFlightPermission = hasFlightPermission && res.getPermissions().playerHas(player, Flags.move, FlagPermissions.FlagCombo.TrueOrNone);
        hasFlightPermission = hasFlightPermission || res.getPermissions().playerHas(player, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue);
        hasFlightPermission = hasFlightPermission || res.isOwner(player);
        return hasFlightPermission;
    }

    public static boolean playerToggleFly(Player player) throws NotInResidenceException, NoResFlyPermissionException {
        Location playerLocation = player.getLocation();
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(playerLocation);
        if (res == null) {
            throw new NotInResidenceException();
        }

        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled) {
            pfm.disableFlight();
            return false;
        }
        if (!canPlayerFly(player, res)) {
            throw new NoResFlyPermissionException();
        }
        pfm.enableFlight();
        return true;
    }

}
