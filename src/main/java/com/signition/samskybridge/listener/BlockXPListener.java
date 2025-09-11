
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.antidupe.AntiDupService;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.xpguard.SlotGuardService;
import com.signition.samskybridge.place.PlacedTracker;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class BlockXPListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final AntiDupService antiDup = new AntiDupService();
    private final SlotGuardService slotGuard = new SlotGuardService();
    private final PlacedTracker placedTracker = new PlacedTracker();
    private final NamespacedKey RECYCLED_KEY;

    public BlockXPListener(Main plugin, DataStore store, LevelService level){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        int ttl = plugin.getConfig().getInt("xp_anti_dup.ttl_seconds", 5);
        int radius = plugin.getConfig().getInt("xp_anti_dup.radius", 2);
        boolean sameY = plugin.getConfig().getBoolean("xp_anti_dup.only_same_y", true);
        antiDup.configure(ttl, radius, sameY);
        this.RECYCLED_KEY = new NamespacedKey(plugin, "recycled");

        int slotTtl = plugin.getConfig().getInt("xp_once.per_slot_ttl_seconds", -1);
        slotGuard.configureSeconds(slotTtl);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        antiDup.recordBreak(e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getType());
        // mark consumed if it was a player-placed block (used by drop tagger)
        placedTracker.consume(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        // If player's hand item is tagged as recycled(from placed block drop), deny XP
        ItemStack hand = e.getItemInHand();
        if (hand != null){
            ItemMeta meta = hand.getItemMeta();
            if (meta != null){
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(RECYCLED_KEY, PersistentDataType.BYTE)){
                    // allow placement but do not award XP
                    return;
                }
            }
        }
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        boolean suspicious = antiDup.isSuspiciousPlace(e.getPlayer(), e.getBlockPlaced().getLocation(), e.getItemInHand().getType());
        if (suspicious){
            return;
        }

        if (slotGuard.alreadyAwarded(e.getBlockPlaced().getLocation())){
            return;
        }

        level.onPlace(e);
        slotGuard.markAwarded(e.getBlockPlaced().getLocation());
        // mark location as player-placed for future break
        placedTracker.markPlaced(e.getBlockPlaced().getLocation());
    }
}
