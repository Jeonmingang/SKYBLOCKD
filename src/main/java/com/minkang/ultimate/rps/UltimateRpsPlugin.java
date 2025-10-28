
package com.minkang.ultimate.rps;

import com.minkang.ultimate.rps.command.RpsCommand;
import com.minkang.ultimate.rps.data.StatsManager;
import com.minkang.ultimate.rps.data.AttemptsManager;
import com.minkang.ultimate.rps.holo.HologramManager;
import com.minkang.ultimate.rps.leader.LeaderboardManager;
import com.minkang.ultimate.rps.station.StationManager;
import com.minkang.ultimate.rps.gui.GuiListener;
import com.minkang.ultimate.rps.guard.PickupGuardListener;
import com.minkang.ultimate.rps.sweep.HoloSweepListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateRpsPlugin extends JavaPlugin {

    private static UltimateRpsPlugin instance;
    private StationManager stationManager;
    private StatsManager statsManager;
    private AttemptsManager attemptsManager;
    private HologramManager hologramManager;
    private LeaderboardManager leaderboardManager;
    private GuiListener guiListener;

    public static UltimateRpsPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.stationManager = new StationManager(this);
        this.stationManager.load();

        this.statsManager = new StatsManager(this);
        this.statsManager.load();

        this.hologramManager = new HologramManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.leaderboardManager.load();
        try { this.leaderboardManager.spawnOrRefresh(); } catch (Throwable ignored) {}
        // Spawn holograms for stations marked as enabled
        for (String n : stationManager.getNames()) {
            com.minkang.ultimate.rps.station.Station st = stationManager.getByName(n);
            if (st != null && st.isHologram()) hologramManager.spawnOrRefresh(st);
        }

        this.guiListener = new GuiListener(this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);
        Bukkit.getPluginManager().registerEvents(stationManager, this); // interact listener

        getCommand("rps").setExecutor(new RpsCommand(this));
        getCommand("rps").setTabCompleter(new RpsCommand(this));
        getLogger().info("[UltimateRPS] Enabled");
    }

    @Override
    public void onDisable() {
        try {
            stationManager.save();
            statsManager.save();
            hologramManager.despawnAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("[UltimateRPS] Disabled");
    }

    public StationManager stations() { return stationManager; }
    public StatsManager stats() { return statsManager; }
    public AttemptsManager attempts() { return attemptsManager; }
    public HologramManager holograms() { return hologramManager; }
    public LeaderboardManager leaderboard() { return leaderboardManager; }
}
