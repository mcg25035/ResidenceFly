package dev.mcloudtw.rf.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.mcloudtw.rf.Events;
import dev.mcloudtw.rf.Main;
import dev.mcloudtw.rf.PlayerFlightManager;
import dev.mcloudtw.rf.exceptions.NoResFlyPermissionException;
import dev.mcloudtw.rf.exceptions.NotInResidenceException;
import dev.mcloudtw.rf.utils.PlayerUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;


public class ResidenceFlyCommand {
    public static CommandAPICommand command() {
        return new CommandAPICommand("resfly")
                .withPermission(CommandPermission.NONE)
                .withSubcommand(ResidenceFlyCommand.info())
                .withSubcommand(ResidenceFlyCommand.toggle())
                .executesPlayer((player, args) -> {
                    try{
                        if (PlayerUtils.playerToggleFly(player)) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                    "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                            "<green>飛行已開啟</green>"
                            ));
                        }
                        else{
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                    "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                            "<red>飛行已關閉</red>"
                            ));
                        }
                    }
                    catch (NotInResidenceException e) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>你不在領地內，無法開啟飛行</red>"
                        ));
                    } catch (NoResFlyPermissionException e) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>你沒有本領地的飛行權限</red>"
                        ));
                    }
                });
    }

    private static CommandAPICommand info() {
        return new CommandAPICommand("info")
                .executesPlayer((player, args) -> {
                    PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>飛行狀態: " + (pfm.enabled ? "<green>開啟</green>" : "<red>關閉</red>") + "  " +
                                    "飛行快捷鍵: " + (Events.disableFlyShortcut.contains(player) ? "<green>開啟</green>" : "<red>關閉</red>") +"</white>"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>你的基本時間還有 <yellow>" + pfm.defaultSecondsLeft + "</yellow> (秒) 的剩餘，最大 <yellow>" + Main.plugin.defaultPlayerFlightSeconds + "</yellow> (秒)</white>"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>你的額外時間還有 <yellow>" + pfm.additionalSecondsLeft + "</yellow> (秒) 的剩餘"
                    ));
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>你的總時間還有 <yellow>" + (pfm.additionalSecondsLeft + pfm.defaultSecondsLeft) + "</yellow> (秒) 的剩餘"
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

    private static CommandAPICommand toggle() {
        return new CommandAPICommand("toggle")
                .executesPlayer((player, args) -> {
                    if (Events.disableFlyShortcut.contains(player)) {
                        Events.disableFlyShortcut.remove(player);
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<green>飛行快捷鍵已開啟</green>"
                        ));
                    }
                    else {
                        Events.disableFlyShortcut.add(player);
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>飛行快捷鍵已關閉</red>"
                        ));
                    }
                });
    }
}
