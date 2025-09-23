package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.feature.FeatureService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class FeatureListener implements Listener {
    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();

    public FeatureListener(Main plugin, FeatureService features){
        this.plugin = plugin;
        this.features = features;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        // Mine regen: if block is inside mine region, schedule regen to a weighted ore
        if (features.isInMineRegion(p, b.getLocation())){
            long delay = plugin.getConfig().getLong("features.mine.regen.delay-ticks", 200L);
            final Material regenTo = features.pickOreFor(p);
            if (regenTo != null){
                new BukkitRunnable(){
                    @Override public void run(){ b.setType(regenTo, false); }
                }.runTaskLater(plugin, Math.max(1L, delay));
            }
            return;
        }
        // Farm replant: if block is inside farm region and it's a crop, schedule replant
        if (features.isInFarmRegion(p, b.getLocation())){
            Material type = b.getType();
            if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES){
                long delay = plugin.getConfig().getLong("features.farm.replant.delay-ticks", 100L);
                final Material plant = features.pickCropFor(p, type);
                new BukkitRunnable(){
                    @Override public void run(){ b.setType(plant, false); }
                }.runTaskLater(plugin, Math.max(1L, delay));
            }
        }
    }
}
