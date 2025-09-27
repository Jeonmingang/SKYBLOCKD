package com.signition.samskybridge.feature;

import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FarmReplantListener implements Listener {
    private final org.bukkit.plugin.Plugin plugin;
    private final BentoSync bento;
    private final UpgradeService upgrades;

    public FarmReplantListener(org.bukkit.plugin.Plugin plugin, BentoSync bento, UpgradeService upgrades){
        this.plugin = plugin; this.bento = bento; this.upgrades = upgrades;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Block b = e.getBlock();
        Material type = b.getType();
        if (!isCrop(type)) return;

        // world filter
        java.util.List<String> worlds = plugin.getConfig().getStringList("features.farm.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(b.getWorld().getName())) return;

        Player p = e.getPlayer();
        java.util.UUID owner = bento.getOwnerAt(b.getLocation());
        if (owner == null) return;

        // level & allowed-crops at level
        int lv = upgrades.getLevel(owner, "farm");
        if (!isAllowedAtLevel(type, lv)) return;

        // only mature triggers replant
        if (!(b.getBlockData() instanceof Ageable)) return;
        Ageable ag = (Ageable) b.getBlockData();
        if (ag.getAge() < ag.getMaximumAge()) return;

        boolean requireSeed = plugin.getConfig().getBoolean("features.farm.replant.require-seed", true);
        boolean denyWhenMissing = plugin.getConfig().getBoolean("features.farm.replant.deny-when-missing", true);
        int delay = plugin.getConfig().getInt("features.farm.replant.delay-ticks", 4);

        Material seed = seedFor(type);
        if (requireSeed){
            if (!consumeSeed(p, seed, 1)){
                if (denyWhenMissing){
                    e.setCancelled(true);
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.need-seed","&c씨앗이 없습니다.")));
                }
                return;
            }
        }

        // schedule replant
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (b.getType() == Material.AIR){
                b.setType(type, false);
                // set age to 0
                if (b.getBlockData() instanceof Ageable){
                    Ageable a2 = (Ageable) b.getBlockData();
                    a2.setAge(0);
                    b.setBlockData(a2, true);
                }
            }
        }, Math.max(1, delay));
    }

    private boolean isAllowedAtLevel(Material crop, int lv){
        // Allowed if listed in features.farm.crops.<<=lv>.*
        for (int i = lv; i >= 1; i--){
            org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("features.farm.crops."+i);
            if (sec == null) continue;
            for (String key : sec.getKeys(false)){
                Material m = Material.matchMaterial(key);
                if (m == crop) return true;
            }
        }
        // default allow wheat
        return crop == Material.WHEAT;
    }

    private boolean isCrop(Material m){
        switch (m){
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case NETHER_WART:
                return true;
            default:
                return false;
        }
    }

    private Material seedFor(Material crop){
        switch (crop){
            case WHEAT: return Material.WHEAT_SEEDS;
            case CARROTS: return Material.CARROT;
            case POTATOES: return Material.POTATO;
            case BEETROOTS: return Material.BEETROOT_SEEDS;
            case NETHER_WART: return Material.NETHER_WART;
            default: return Material.WHEAT_SEEDS;
        }
    }

    private boolean consumeSeed(Player p, Material seed, int amount){
        if (seed == null) return true;
        if (amount <= 0) return true;
        org.bukkit.inventory.PlayerInventory inv = p.getInventory();
        for (int i=0;i<inv.getSize();i++){
            ItemStack it = inv.getItem(i);
            if (it != null && it.getType() == seed && it.getAmount() >= amount){
                it.setAmount(it.getAmount()-amount);
                inv.setItem(i, it.getAmount() > 0 ? it : null);
                return true;
            }
        }
        return false;
    }
}
