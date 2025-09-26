package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.feature.FeatureService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

import java.util.Random;

/**
 * Farm-related fixes:
 *  - Fully grown check
 *  - Seed pre-consumption to prevent "infinite" harvest without seeds
 *  - Delayed replant at age=0
 *  - Growth bonus per farm level
 *
 * Drop-in replacement; if your project already has a FeatureListener, merge the handlers.
 */
public class FeatureListener implements Listener {

    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();

    public FeatureListener(Main plugin) {
        this.plugin = plugin;
        this.features = plugin.getFeatureService();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        Location loc = b.getLocation();

        // Only handle registered farm regions
        if (!features.isInFarmRegion(p, loc)) return;

        Material type = b.getType();
        if (!(type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES)) return;
        if (!(b.getBlockData() instanceof Ageable)) return;

        Ageable age = (Ageable) b.getBlockData();
        if (age.getAge() < age.getMaximumAge()) {
            e.setCancelled(true);
            p.sendMessage(Text.color("&c아직 익지 않았습니다."));
            return;
        }

        // Pick crop by level (fallback to current)
        Material plantType = type;
        try {
            Material picked = features.pickCropFor(p, type);
            if (picked != null) plantType = picked;
        } catch (Throwable ignore) { /* keep fallback */ }

        // Consume one seed BEFORE letting the break pass so there is no infinite loop
        Material seed = seedOf(plantType);
        if (seed != null) {
            if (!consumeOne(p, seed)) {
                e.setCancelled(true);
                p.sendMessage(Text.color("&c씨앗이 없습니다."));
                return;
            }
        }

        // Let the event succeed so normal drops happen,
        // then replant a baby crop after a short delay.
        final Material finalPlantType = plantType;
        long delay = Math.max(1L, plugin.getConfig().getLong("features.farm.replant.delay-ticks", 4L));
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override public void run() {
                Block target = loc.getBlock();
                // Safety: only set if the block is still air/dirt/tilled-ish or harvestable placeholder.
                if (target.getType() == Material.AIR || target.getType() == Material.FARMLAND) {
                    target.setType(finalPlantType, false);
                    if (target.getBlockData() instanceof Ageable) {
                        Ageable ag = (Ageable) target.getBlockData();
                        ag.setAge(0);
                        target.setBlockData(ag, false);
                    }
                } else {
                    // Last resort: set anyway (keeps legacy behavior)
                    target.setType(finalPlantType, false);
                }
            }
        }, delay);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        Block b = e.getBlock();
        Location loc = b.getLocation();
        if (!features.isInAnyFarmRegion(loc)) return;
        if (!(e.getNewState().getBlockData() instanceof Ageable)) return;

        int lvl;
        try {
            lvl = Math.max(0, features.getFarmLevelByLoc(loc));
        } catch (Throwable t) {
            lvl = 0;
        }

        double base = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-base", 0.0);
        double per = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-per-level", 0.05);

        if (rng.nextDouble() < (base + per * lvl)) {
            Ageable age = (Ageable) e.getNewState().getBlockData();
            int next = Math.min(age.getMaximumAge(), age.getAge() + 1);
            age.setAge(next);
            e.getNewState().setBlockData(age);
        }
    }

    // ---- helpers ----
    private Material seedOf(Material crop) {
        if (crop == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (crop == Material.CARROTS) return Material.CARROT;
        if (crop == Material.POTATOES) return Material.POTATO;
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