
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/** Listeners for farm/mine features (Java 8, MC 1.16.5). */
public class FeatureListener implements Listener {
    private final Main plugin;
    private final FeatureService features;
    private final Random rng = new Random();
    public FeatureListener(Main plugin, FeatureService features){
        this.plugin = plugin;
        this.features = features;
    }

    /* =======================
     *  FARM: break + replant
     * ======================= */
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();

        // MINES: replace mined ore with weighted new ore after delay
        if (features.isInMineRegion(p, b.getLocation())){
            long delay = plugin.getConfig().getLong("features.mine.regen.delay-ticks", 8L);
            final Material place = features.pickOreFor(p);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override public void run() {
                    b.setType(place, false);
                }
            }, delay);
            return;
        }

        // FARM: only fully-grown allowed; replant by consuming from inventory
        if (features.isInFarmRegion(p, b.getLocation())){
            Material type = b.getType();
            if (!(type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES)) return;
            if (!(b.getBlockData() instanceof Ageable)) return;
            Ageable age = (Ageable) b.getBlockData();
            if (age.getAge() < age.getMaximumAge()){ // not grown
                e.setCancelled(true);
                p.sendMessage(Text.color("&c아직 익지 않았습니다."));
                return;
            }
            // choose crop according to level and player's seeds
            List<Material> bag = plugin.getConfig().getStringList("features.farm.crops."+features.getFarmLevelByLoc(b.getLocation())).isEmpty()
                    ? Arrays.asList(Material.WHEAT, Material.CARROTS, Material.POTATOES)
                    : new java.util.ArrayList<org.bukkit.Material>(features.parseWeighted(plugin.getConfig().getStringList("features.farm.crops."+features.getFarmLevelByLoc(b.getLocation()))).keySet()).getStringList("features.farm.crops."+features.getFarmLevelByLoc(b.getLocation())));
            // Build candidate list unique order randomized
            List<Material> candidates = new ArrayList<Material>();
            for (Material m : bag){ if (!candidates.contains(m)) candidates.add(m); }
            Collections.shuffle(candidates, rng);
            // Seed mapping
            Material chosen = null;
            Material seed = null;
            for (Material m : candidates){
                Material s = seedOf(m);
                if (s != null && countItem(p, s) > 0){ chosen = m; seed = s; break; }
            }
            if (chosen == null){
                // fallback to the same crop if they have seeds for it
                Material s = seedOf(type);
                if (s != null && countItem(p, s) > 0){ chosen = type; seed = s; }
            }
            if (chosen == null){
                p.sendMessage(Text.color("&c씨앗이 없습니다. 재심지기가 취소되었습니다."));
                return; // allow harvest, but no replant
            }
            final Material plantType = chosen;
            consumeOne(p, seed);
            long delay = Math.max(1L, plugin.getConfig().getLong("features.farm.replant.delay-ticks", 4L));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override public void run() {
                    b.setType(plantType, false);
                }
            }, delay);
        }
    }

    // Growth acceleration based on farm level
    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e){
        Block b = e.getBlock();
        if (!features.isInAnyFarmRegion(b.getLocation())) return;
        if (!(e.getNewState().getBlockData() instanceof Ageable)) return;
        Ageable age = (Ageable) e.getNewState().getBlockData();
        int level = Math.max(0, features.getFarmLevelByLoc(b.getLocation()));
        double base = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-base", 0.0);
        double per = plugin.getConfig().getDouble("features.farm.growth.extra-stage-chance-per-level", 0.05);
        if (rng.nextDouble() < base + per * level){
            int next = Math.min(age.getMaximumAge(), age.getAge()+1);
            age.setAge(next);
            e.getNewState().setBlockData(age);
        }
    }

    /* ===== Utilities (no Java 9+ features) ===== */
    private Material seedOf(Material crop){
        if (crop == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (crop == Material.CARROTS) return Material.CARROT;
        if (crop == Material.POTATOES) return Material.POTATO;
        return null;
    }
    private int countItem(Player p, Material mat){
        int n = 0;
        for (ItemStack it : p.getInventory().getContents()){
            if (it != null && it.getType()==mat) n += it.getAmount();
        }
        return n;
    }
    private boolean consumeOne(Player p, Material mat){
        ItemStack[] contents = p.getInventory().getContents();
        for (int i=0;i<contents.length;i++){
            ItemStack it = contents[i];
            if (it != null && it.getType()==mat && it.getAmount()>0){
                it.setAmount(it.getAmount()-1);
                contents[i] = it.getAmount()>0 ? it : null;
                p.getInventory().setContents(contents);
                p.updateInventory();
                return true;
            }
        }
        return false;
    }
}
