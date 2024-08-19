package dev.mcloudtw.rf.commands;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.mcloudtw.rf.Main;
import dev.mcloudtw.rf.PlayerFlightManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class ResidenceFlyAdminCommand {
    public static CommandAPICommand command() {
        return new CommandAPICommand("resflya")
                .withPermission(CommandPermission.OP)
                .withSubcommand(ResidenceFlyAdminCommand.scale())
                .withSubcommand(ResidenceFlyAdminCommand.reset())
                .withSubcommand(ResidenceFlyAdminCommand.add())
                .withSubcommand(ResidenceFlyAdminCommand.import_from_tempfly());
    }

    private static CommandAPICommand scale() {
        return new CommandAPICommand("scale")
                .withArguments(new DoubleArgument("scale"))
                .executesPlayer((player, args) -> {
                    double scale = (double) args.get("scale");
                    Main.plugin.scaleEdit(scale);
                    Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<gradient:red:gold>全服飛行時間倍率已更改為 " + scale + "x</gradient>"
                    ));
                });
    }

    private static CommandAPICommand import_from_tempfly() {
        return new CommandAPICommand("import_from_tempfly")
                .executesPlayer((player, args) -> {
                    File tempFlyData = Path.of("plugins/TempFly/data.yml").toFile();
                    if (!tempFlyData.exists()) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<red>TempFly 資料檔案不存在</red>"
                        ));
                        return;
                    }
                    YamlConfiguration tempFlyDB = YamlConfiguration.loadConfiguration(tempFlyData);
                    ConfigurationSection players = tempFlyDB.getConfigurationSection("players");
                    players.getKeys(false).forEach(uuid -> {
                        ConfigurationSection playerData = players.getConfigurationSection(uuid);
                        if (!playerData.contains("time")) return;
                        int time = (int) Math.round(
                                players.getConfigurationSection(uuid).getDouble("time")
                        );
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                        "<white>正在匯入 " + uuid + " 的飛行時間 " + time + " 秒</white>"
                        ));
                        OfflinePlayer playerToOperate = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(playerToOperate);
                        pfm.additionalSecondsLeft += time;
                        pfm.saveToFile();
                    });
                });
    }

    private static CommandAPICommand reset() {
        return new CommandAPICommand("reset")
                .executesPlayer((player, args) -> {
                    Main.plugin.scaleReset();
                    Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<gradient:red:gold>全服飛行時間倍率已重置為 1.0x</gradient>"
                    ));
                });

    }

    private static CommandAPICommand add() {
        return new CommandAPICommand("add")
                .withArguments(new IntegerArgument("time"))
                .withArguments(new StringArgument("player"))
                .executesPlayer((player, args) -> {
                    String playerName = (String) args.get("player");
                    assert playerName != null;
                    Player playerToOperate = Bukkit.getPlayer(playerName);
                    PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(playerToOperate);
                    pfm.additionalSecondsLeft += (int) args.get("time");
                    pfm.saveToFile();
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                    "<white>" + playerName + " 的飛行時間已增加 " + args.get("time") + "秒</white>"
                    ));
                });

    }
}
