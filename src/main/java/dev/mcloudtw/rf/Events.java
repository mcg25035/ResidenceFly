package dev.mcloudtw.rf;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import dev.mcloudtw.rf.exceptions.WrongGamemodeException;
import dev.mcloudtw.rf.utils.PlayerUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

public class Events implements Listener {
    @EventHandler
    public void ResidenceChangedEvent(ResidenceChangedEvent event) {

        Player player = event.getPlayer();
        ClaimedResidence resDest = event.getTo();

        boolean hasFlightPermission = false;
        if (resDest != null) {
            hasFlightPermission = resDest.getPermissions().playerHas(player, Flags.fly, FlagPermissions.FlagCombo.TrueOrNone);
            hasFlightPermission = hasFlightPermission || resDest.getPermissions().playerHas(player, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue);
            hasFlightPermission = hasFlightPermission || resDest.isOwner(player);
        }
        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled && !hasFlightPermission) {
            pfm.disableFlight();
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                            "<red>你已經離開了領地，飛行已經被關閉 </red>"
            ));
        }
    }

    public static HashSet<Player> disableFlyShortcut = new HashSet<>();
    public HashMap<Player, Instant> playerLastSneak = new HashMap<>();

    @EventHandler
    public void PlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) return;
        if (disableFlyShortcut.contains(event.getPlayer())) return;

        Instant now = Instant.now();
        Player player = event.getPlayer();
        if (!playerLastSneak.containsKey(player)) {
            playerLastSneak.put(player, Instant.now());
            return;
        }

        Instant lastSneak = playerLastSneak.get(player);
        if (now.toEpochMilli() - lastSneak.toEpochMilli() > 200) {
            playerLastSneak.put(player, now);
            return;
        }

        playerLastSneak.put(player, now);
        try {
            if (PlayerUtils.playerToggleFly(player)){
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
        catch (Exception ignored) {}
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) throws WrongGamemodeException {
        if (!event.getPlayer().getAllowFlight()) return;
        PlayerUtils.safeLandPlayer(event.getPlayer());
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        if (pfm.enabled) {
            pfm.disableFlight();
        }
    }
}
