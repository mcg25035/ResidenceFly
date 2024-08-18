package dev.mcloudtw.rf.commands;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import dev.jorel.commandapi.CommandAPICommand;
import dev.mcloudtw.rf.PlayerFlightManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;


public class ResidenceFlyCommand {
    CommandAPICommand command() {
        return new CommandAPICommand("resfly")
                .executesPlayer((player, args) -> {
                    Location playerLocation = player.getLocation();
                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(playerLocation);
                    if (res == null) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<gradient:red> 你不在領地內，無法開啟飛行 </gradient>"
                        ));
                        return;
                    }

                    boolean hasFlightPermission = res.getPermissions().playerHas(player, Flags.fly, true);
                    hasFlightPermission = hasFlightPermission || res.getPermissions().playerHas(player, Flags.admin, true);
                    hasFlightPermission = hasFlightPermission || res.isOwner(player);

                    if (!hasFlightPermission) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<gradient:red> 你沒有本領地的飛行權限 </gradient>"
                        ));
                        return;
                    }

                    PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
                    if (pfm.enabled) {
                        pfm.disableFlight(player);
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<gradient:white:gray> 飛行已關閉 </gradient>"
                        ));
                        return;
                    }
                    pfm.enableFlight(player);
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<gradient:white:gray> 飛行已開啟 </gradient>"
                    ));

                });
    }
}
