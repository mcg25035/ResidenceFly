package dev.mcloudtw.rf;

import dev.mcloudtw.rf.commands.ResidenceFlyAdminCommand;
import dev.mcloudtw.rf.commands.ResidenceFlyCommand;
import dev.mcloudtw.rf.placeholder.PlayerFlyInfoExpansion;
import dev.mcloudtw.rf.utils.PlayerUtils;
import dev.mcloudtw.rf.utils.TimeUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;

public final class Main extends JavaPlugin {

    public static Main plugin;
    public double currentBasePlayerFlightSecondsScale;
    public int basePlayerFlightSeconds;
    public int defaultPlayerFlightSeconds;
    public String dailyResetTime;
    public Date lastResetTime;
    public Date nextResetTime;
    public BukkitTask resetTimeUpdateChecker;

    public void loadConfig() {
        // hh:mm:ss
        String dailyResetTime = getConfig().getString("dailyResetTime");
        if (dailyResetTime == null) {
            dailyResetTime = "00:00:00";
            getConfig().set("dailyResetTime", dailyResetTime);
        }
        this.dailyResetTime = dailyResetTime;

        long lastResetTimestamp = getConfig().getLong("lastResetTime");
        if (lastResetTimestamp == 0) {
            lastResetTimestamp = TimeUtils.getDateWithSpecifiedTime(dailyResetTime).getTime();
            getConfig().set("lastResetTime", lastResetTimestamp);
        }
        this.lastResetTime = new Date(lastResetTimestamp);
        //                                               mili * sec * min * hour
        this.nextResetTime = new Date(lastResetTimestamp + 1000 * 60 * 60 * 24);

        int basePlayerFlightSeconds = getConfig().getInt("basePlayerFlightSeconds");
        if (basePlayerFlightSeconds == 0) {
            basePlayerFlightSeconds = 4000;
            getConfig().set("basePlayerFlightSeconds", 4000);
        }
        this.basePlayerFlightSeconds = basePlayerFlightSeconds;

        double currentBasePlayerFlightSecondsScale = getConfig().getDouble("currentBasePlayerFlightSecondsScale");
        if (currentBasePlayerFlightSecondsScale == 0) {
            currentBasePlayerFlightSecondsScale = 1.0;
            getConfig().set("currentBasePlayerFlightSecondsScale", 1.0);
        }
        this.currentBasePlayerFlightSecondsScale = currentBasePlayerFlightSecondsScale;

        saveConfig();

        this.defaultPlayerFlightSeconds = (int) Math.round(basePlayerFlightSeconds * currentBasePlayerFlightSecondsScale);
    }

    public void resetTimeUpdateChecker() {
        Date now = new Date();
        if (now.before(nextResetTime)) return;
        lastResetTime = nextResetTime;
        nextResetTime = new Date(lastResetTime.getTime() + 1000 * 60 * 60 * 24);

        getConfig().set("lastResetTime", lastResetTime.getTime());
        saveConfig();
    }

    public void updateAllPlayerFlightData() {
        PlayerFlightManager.playerFlightManagerHashMap.forEach((player, pfm) -> {
            pfm.checkReset();
        });
    }

    public void scaleEdit(double scale) {
        currentBasePlayerFlightSecondsScale = scale;
        defaultPlayerFlightSeconds = (int) Math.round(basePlayerFlightSeconds * currentBasePlayerFlightSecondsScale);
        getConfig().set("currentBasePlayerFlightSecondsScale", currentBasePlayerFlightSecondsScale);
        saveConfig();
    }

    public void scaleReset() {
        currentBasePlayerFlightSecondsScale = 1.0;
        defaultPlayerFlightSeconds = (int) Math.round(basePlayerFlightSeconds * currentBasePlayerFlightSecondsScale);
        getConfig().set("currentBasePlayerFlightSecondsScale", currentBasePlayerFlightSecondsScale);
        saveConfig();
    }

    public void syncPlayerFlyStatus(Player player) {
        PlayerFlightManager pfm = PlayerFlightManager.loadPlayerFlightData(player);
        boolean bukkitFlyStatus = player.getAllowFlight();
        if (bukkitFlyStatus == pfm.enabled) return;
        if (pfm.enabled) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        if (bukkitFlyStatus) {
            try{
                PlayerUtils.safeLandPlayer(player);
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gray>[</gray><gold>領地飛行</gold><gray>]</gray> " +
                                "<red>你的飛行狀態異常，已關閉並同步飛行狀態。 </red>"
                ));
            }
            catch (Exception ignored) {}
        }

    }



    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        loadConfig();

        resetTimeUpdateChecker = Bukkit.getScheduler().runTaskTimer(this, this::resetTimeUpdateChecker, 20 * 30, 20);

        if (!PlayerFlightManager.playerFlightDataPath.toFile().exists()) {
            PlayerFlightManager.playerFlightDataPath.toFile().mkdirs();
        }

        ResidenceFlyCommand.command().register();
        ResidenceFlyAdminCommand.command().register();

        Bukkit.getPluginManager().registerEvents(new Events(), this);

        new PlayerFlyInfoExpansion().register();
        Bukkit.getScheduler().runTaskTimer(this, ()-> {
            Bukkit.getOnlinePlayers().forEach(this::syncPlayerFlyStatus);
        }, 0, 20);
        Bukkit.getScheduler().runTaskLater(this, this::updateAllPlayerFlightData, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
