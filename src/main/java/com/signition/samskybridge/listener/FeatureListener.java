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

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();

    public FeatureListener(Main plugin, FeatureService features){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        this.plugin = plugin;
        this.features = features;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        Player p = e.getPlayer();
        Block b = e.getBlock();
        // Mine: regen ore after delay if in mine region
        if (features.isInMineRegion(p, b.getLocation())){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

            final Material place = features.pickOreFor(p);
            long delay = plugin.getConfig().getLong("features.mine.regen.delay-ticks", 8L);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

                @Override public void run() {

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}
 b.setType(place, false); }
            }, delay);
            return;
        }
        // Farm: fully-grown only; consume seed and replant
        if (features.isInFarmRegion(p, b.getLocation())){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

    Material type = b.getType();
    if (!(b.getBlockData() instanceof Ageable)) return;
    Ageable age = (Ageable) b.getBlockData();
    if (age.getAge() < age.getMaximumAge()){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        e.setCancelled(true);
        p.sendMessage(Text.color("&c아직 익지 않았습니다."));
        return;
    }
    // decide crop to replant (may consider farm level)
    Material plantType = features.pickCropFor(p, type);
    Material seed = seedOf(plantType);
    if (seed == null || !consumeOne(p, seed)){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        // no seed in inventory: do NOT cancel, let vanilla break & drop; no auto-replant
        return;
    }
    long delay = Math.max(1L, plugin.getConfig().getLong("features.farm.replant.delay-ticks", 4L));
    final org.bukkit.Location loc = b.getLocation().clone();
    Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        @Override public void run(){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

            Block nb = loc.getBlock();
            nb.setType(plantType, false);
            if (nb.getBlockData() instanceof Ageable){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

                Ageable a = (Ageable) nb.getBlockData();
                a.setAge(0);
                nb.setBlockData(a, false);
            }
        }
    }, delay);
}

    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        Block b = e.getBlock();
        if (!features.isInAnyFarmRegion(b.getLocation())) return;
        if (!(e.getNewState().getBlockData() instanceof Ageable)) return;
        Ageable age = (Ageable) e.getNewState().getBlockData();
        int lvl = features.getFarmLevelByLoc(b.getLocation());
        double base = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-base", 0.0);
        double per = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-per-level", 0.05);
        if (rng.nextDouble() < (base + per * Math.max(0,lvl))) {

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

            int next = Math.min(age.getMaximumAge(), age.getAge() + 1);
            age.setAge(next);
            e.getNewState().setBlockData(age);
        }
    }

    private Material seedOf(Material crop){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

    if (crop == Material.WHEAT) return Material.WHEAT_SEEDS;
    if (crop == Material.CARROTS) return Material.CARROT;
    if (crop == Material.POTATOES) return Material.POTATO;
    if (crop == Material.BEETROOTS) return Material.BEETROOT_SEEDS;
    if (crop == Material.NETHER_WART) return Material.NETHER_WART;
    try {

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}
 if (crop == Material.COCOA) return Material.COCOA_BEANS; } catch (Throwable ignored) {

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}
}
    return null;
}

    private boolean consumeOne(Player p, Material mat){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

        ItemStack[] arr = p.getInventory().getContents();
        for (int i=0;i<arr.length;i++){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

            ItemStack it = arr[i];
            if (it != null && it.getType()==mat && it.getAmount()>0){

private boolean requireSeed(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.require-seed", false);
}
private boolean denyWhenMissing(){
    return this.plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", false);
}
private int replantDelay(){
    return this.plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);
}

                it.setAmount(it.getAmount()-1);
                arr[i] = it.getAmount()>0 ? it : null;
                p.getInventory().setContents(arr);
                p.updateInventory();
                return true;
            }
        }
        return false;
    }


// Generic farm break handler honoring require-seed / deny-when-missing
private void handleFarmBreak(org.bukkit.event.block.BlockBreakEvent e){
    org.bukkit.entity.Player p = e.getPlayer();
    org.bukkit.block.Block b = e.getBlock();
    if (!this.features.canUseFarm(p, b.getLocation())){
        e.setCancelled(true);
        return;
    }
    if (requireSeed()){
        org.bukkit.Material seed = com.signition.samskybridge.feature.CropUtil.findSeedFor(b.getType());
        if (seed != null){
            boolean hasSeed = p.getInventory().contains(seed);
            if (!hasSeed && denyWhenMissing()){
                e.setCancelled(true); // block break denied due to missing seed
                p.sendMessage(this.plugin.getConfig().getString("messages.need-seed","&c씨앗이 없습니다."));
                return;
            }
        }
    }
    // replant schedule
    int delay = replantDelay();
    if (delay > 0){
        org.bukkit.Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            com.signition.samskybridge.feature.CropUtil.replant(b);
        }, delay);
    }
}

}
