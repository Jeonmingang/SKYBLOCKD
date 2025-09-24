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

import java.util.Random;

public class FeatureListener implements Listener {
    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();

    public FeatureListener(Main plugin, FeatureService features){
        this.plugin = plugin;
        this.features = features;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        // Mine: regen ore after delay if in mine region
        if (features.isInMineRegion(p, b.getLocation())){
            final Material place = features.pickOreFor(p);
            long delay = plugin.getConfig().getLong("features.mine.regen.delay-ticks", 8L);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override public void run() { b.setType(place, false); }
            }, delay);
            return;
        }
        // Farm: fully-grown only; consume seed and replant
        if (features.isInFarmRegion(p, b.getLocation())){
            Material type = b.getType();
            if (!(type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES)) return;
            if (!(b.getBlockData() instanceof Ageable)) return;
            Ageable age = (Ageable) b.getBlockData();
            if (age.getAge() < age.getMaximumAge()){
                e.setCancelled(true);
                p.sendMessage(Text.color("&c아직 익지 않았습니다."));
                return;
            }
            // choose crop according to level; fallback to current type
            Material plantType = features.pickCropFor(p, type);
            Material seed = seedOf(plantType);
            if (seed != null && !consumeOne(p, seed)){
                e.setCancelled(true);
                p.sendMessage(Text.color("&c씨앗이 없습니다."));
                return;
            }
            long delay = Math.max(1L, plugin.getConfig().getLong("features.farm.replant.delay-ticks", 4L));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override public void run() { b.setType(plantType, false); }
            }, delay);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e){
        Block b = e.getBlock();
        if (!features.isInAnyFarmRegion(b.getLocation())) return;
        if (!(e.getNewState().getBlockData() instanceof Ageable)) return;
        Ageable age = (Ageable) e.getNewState().getBlockData();
        int lvl = features.getFarmLevelByLoc(b.getLocation());
        double base = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-base", 0.0);
        double per = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-per-level", 0.05);
        if (rng.nextDouble() < (base + per * Math.max(0,lvl))) {
            int next = Math.min(age.getMaximumAge(), age.getAge() + 1);
            age.setAge(next);
            e.getNewState().setBlockData(age);
        }
    }

    private Material seedOf(Material crop){
        if (crop == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (crop == Material.CARROTS) return Material.CARROT;
        if (crop == Material.POTATOES) return Material.POTATO;
        return null;
    }
    private boolean consumeOne(Player p, Material mat){
        ItemStack[] arr = p.getInventory().getContents();
        for (int i=0;i<arr.length;i++){
            ItemStack it = arr[i];
            if (it != null && it.getType()==mat && it.getAmount()>0){
                it.setAmount(it.getAmount()-1);
                arr[i] = it.getAmount()>0 ? it : null;
                p.getInventory().setContents(arr);
                p.updateInventory();
                return true;
            }
        }
        return false;
    }
}
