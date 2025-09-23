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
import org.bukkit.inventory.ItemStack;

public class FeatureListener implements Listener {
    private final Main plugin;
    private final FeatureService features;

    public FeatureListener(Main plugin, FeatureService features){
        this.plugin = plugin;
        this.features = features;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        Block b = e.getBlock();
        // Mine nodes: regen as ore on delay if in mine region
        if (features.isInMineRegion(p, b.getLocation())){
            final Material regen = features.pickOreFor(p);
            long delay = plugin.getConfig().getLong("features.mine.regen.delay-ticks", 200L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (b.getType() == Material.AIR) b.setType(regen, false);
            }, delay);
            return;
        }
        // Farm: only fully-grown allowed; replant by consuming from inventory
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
            // consume seed from inventory
            Material replantType = features.pickCropFor(p, type);
            Material seed = Material.WHEAT_SEEDS;
            if (replantType == Material.CARROTS) seed = Material.CARROT;
            else if (replantType == Material.POTATOES) seed = Material.POTATO;
            boolean consumed = consumeOne(p, seed);
            if (!consumed){
                e.setCancelled(true);
                p.sendMessage(Text.color("&c씨앗이 없습니다."));
                return;
            }
            long delay = plugin.getConfig().getLong("features.farm.replant.delay-ticks", 100L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                b.setType(replantType, false);
            }, delay);
        }
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
