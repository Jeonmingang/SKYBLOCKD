package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.feature.FeatureService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.Random;

/**
 * Farm & Mine runtime listeners.
 * <p>
 * This listener implements the "seed must exist to replant" rule and
 * prevents infinite seeds when the player breaks a crop without the seed
 * in inventory. It also supports the configured growth bonus and uses
 * FeatureService to resolve farm level by location.
 */
public class FeatureListener implements Listener {

    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();

    public FeatureListener(Main plugin, FeatureService features) {
        this.plugin = plugin;
        this.features = features;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        // Only care about designated farm regions.
        try {
            if (!features.isInAnyFarmRegion(b.getLocation())) {
                return;
            }
        } catch (Throwable t) {
            // If the running build uses a different method name, keep runtime safe.
            return;
        }

        // Fully-grown only
        if (!(b.getBlockData() instanceof Ageable)) {
            return;
        }
        Ageable age = (Ageable) b.getBlockData();
        if (age.getAge() < age.getMaximumAge()) {
            e.setCancelled(true);
            p.sendMessage(Text.color("&c아직 익지 않았습니다."));
            return;
        }

        // Which crop are we (after level rules)?
        Material current = b.getType();
        Material plantType = current;
        try {
            plantType = features.pickCropFor(p, current);
            if (plantType == null) plantType = current;
        } catch (Throwable ignore) { /* old builds may not have this hook */ }

        // Seed consumption BEFORE replanting to avoid dupes.
        final Material seed = seedOf(plantType);
        if (seed != null && !consumeOne(p, seed)) {
            e.setCancelled(true);
            p.sendMessage(Text.color("&c씨앗이 없습니다."));
            return;
        }

        // Schedule replant shortly after the block break is processed by Bukkit.
        final Material replantType = plantType;
        final Block target = b;
        long delay = Math.max(1L, plugin.getConfig().getLong("features.farm.replant.delay-ticks", 4L));
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override public void run() { 
                // Safeguard: if user placed something else, do not overwrite
                if (target.getType() == Material.AIR || target.getType() == current) {
                    target.setType(replantType, false);
                }
            }
        }, delay);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        Block b = e.getBlock();

        boolean insideFarm;
        try {
            insideFarm = features.isInAnyFarmRegion(b.getLocation());
        } catch (Throwable t) {
            insideFarm = false;
        }
        if (!insideFarm) return;

        if (!(e.getNewState().getBlockData() instanceof Ageable)) return;
        Ageable age = (Ageable) e.getNewState().getBlockData();

        int lvl = 0;
        try {
            lvl = features.getFarmLevelByLoc(b.getLocation());
        } catch (Throwable ignore) {}

        double base = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-base", 0.0);
        double per = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-per-level", 0.05);
        if (rng.nextDouble() < (base + per * Math.max(0, lvl))) {
            int next = Math.min(age.getMaximumAge(), age.getAge() + 1);
            age.setAge(next);
            e.getNewState().setBlockData(age);
        }
    }

    private Material seedOf(Material crop) {
        if (crop == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (crop == Material.CARROTS) return Material.CARROT;
        if (crop == Material.POTATOES) return Material.POTATO;
        if (crop == Material.BEETROOTS) return Material.BEETROOT_SEEDS;
        return null;
    }

    private boolean consumeOne(Player p, Material mat) {
        ItemStack[] arr = p.getInventory().getContents();
        for (int i = 0; i < arr.length; i++) {
            ItemStack it = arr[i];
            if (it != null && it.getType() == mat && it.getAmount() > 0) {
                it.setAmount(it.getAmount() - 1);
                arr[i] = it.getAmount() > 0 ? it : null;
                p.getInventory().setContents(arr);
                p.updateInventory();
                return true;
            }
        }
        return false;
    }
}
