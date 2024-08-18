package dev.mcloudtw.rf;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidencePlayerEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Events implements Listener {
    @EventHandler
    public void ResidenceChangedEvent(ResidenceChangedEvent event) {
        Player player = event.getPlayer();
        ClaimedResidence resDest = event.getTo();
        boolean hasFlightPermission = false;
        if (resDest != null) {
            hasFlightPermission = resDest.getPermissions().playerHas(player, Flags.fly, true);
            hasFlightPermission = hasFlightPermission || resDest.getPermissions().playerHas(player, Flags.admin, true);
            hasFlightPermission = hasFlightPermission || resDest.isOwner(player);
        }
        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled && !hasFlightPermission) {
            pfm.disableFlight(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                            "<gradient:white:gray> 你已經離開了領地，飛行已經被關閉 </gradient>" +
                            "</gradient>"
            ));
        }


    }
}
