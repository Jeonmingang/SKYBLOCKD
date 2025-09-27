package com.signition.samskybridge.feature;

import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.*;

public class FarmGrowthBooster implements Listener {
    private final com.signition.samskybridge.upgrade.UpgradeService upgrades;
    private final org.bukkit.plugin.Plugin plugin;
    private final BentoSync bento;
    public FarmGrowthBooster(org.bukkit.plugin.Plugin plugin, BentoSync bento, com.signition.samskybridge.upgrade.UpgradeService upgrades){
        this.plugin = plugin; this.bento = bento; this.upgrades = upgrades;
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e){
        if (!plugin.getConfig().getBoolean("features.farm.growth.enabled", true)) return;
        Block b = e.getNewState().getBlock();
        // Determine island owner at this location
        java.util.UUID owner = bento.getOwnerAt(b.getLocation());
        if (owner == null) return;
        int lv =  upgrades.getLevel(owner, "farm");
        // Prefer upgrades section if present
        double mult = 1.0;
        try {
            // scan upgrades.farm.levels for the highest 'growth-multiplier' <= current lv
            org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades.farm.levels");
            if (sec != null){
                for (String k : sec.getKeys(false)){
                    int n = Integer.parseInt(k);
                    if (n <= lv) mult = Math.max(mult, sec.getDouble(k+".growth-multiplier", 1.0));
                }
            }
        } catch (Throwable ignored){}
        if (mult <= 1.0) return;
        // Probability-based extra growth step
        if (b.getBlockData() instanceof Ageable){
            Ageable ag = (Ageable) b.getBlockData();
            int extra = (int)Math.floor(mult - 1.0);
            double frac = (mult - 1.0) - extra;
            int inc = extra;
            if (Math.random() < frac) inc++;
            if (inc <= 0) return;
            int newAge = Math.min(ag.getMaximumAge(), ag.getAge() + inc);
            ag.setAge(newAge);
            b.setBlockData(ag, true);
        }
    }
}
