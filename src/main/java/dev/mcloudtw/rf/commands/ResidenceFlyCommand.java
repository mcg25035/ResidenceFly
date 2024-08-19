package dev.mcloudtw.rf.commands;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.mcloudtw.rf.Main;
import dev.mcloudtw.rf.PlayerFlightManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;


public class ResidenceFlyCommand {
    public static CommandAPICommand command() {
        return new CommandAPICommand("resfly")
                .withPermission(CommandPermission.NONE)
                .withSubcommand(ResidenceFlyCommand.info())
                .executesPlayer((player, args) -> {
                    Location playerLocation = player.getLocation();
                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(playerLocation);
                    if (res == null) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>你不在領地內，無法開啟飛行</red>"
                        ));
                        return;
                    }

                    boolean hasFlightPermission = res.getPermissions().playerHas(player, Flags.fly, FlagPermissions.FlagCombo.TrueOrNone);
                    hasFlightPermission = hasFlightPermission || res.getPermissions().playerHas(player, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue);
                    hasFlightPermission = hasFlightPermission || res.isOwner(player);

                    if (!hasFlightPermission) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>你沒有本領地的飛行權限</red>"
                        ));
                        return;
                    }

                    PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
                    if (pfm.enabled) {
                        pfm.disableFlight();
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>飛行已關閉</red>"
                        ));
                        return;
                    }
                    pfm.enableFlight();
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<green>飛行已開啟</green>"
                    ));

                });
    }

    private static CommandAPICommand info() {
        return new CommandAPICommand("info")
                .executesPlayer((player, args) -> {
                    PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
                    int leftTime = pfm.defaultSecondsLeft + pfm.additionalSecondsLeft;
                    int maxTime = Main.plugin.defaultPlayerFlightSeconds + pfm.additionalSecondsLeft;
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>飛行狀態: " + (pfm.enabled ? "<green>開啟</green>" : "<red>關閉</red>") +"</white>"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>你還有 <yellow>" + leftTime + "</yellow> (秒)，最大 <yellow>" + maxTime + "</yellow> (秒)的飛行時間</white>"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>飛行時間倍率為 <yellow>" + Main.plugin.currentBasePlayerFlightSecondsScale + "x</yellow></white>"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>飛行時間將在每日 <yellow>" + Main.plugin.dailyResetTime + "</yellow> 重置</white>"
                    ));
                });

    }
}
