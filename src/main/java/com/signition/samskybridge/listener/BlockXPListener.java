
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BlockXPListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final PlacedTracker placed;
    private final NamespacedKey RECYCLED_KEY;

    public BlockXPListener(Main plugin, DataStore store, LevelService level, PlacedTracker tracker){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.placed = tracker;
        this.RECYCLED_KEY = new NamespacedKey(plugin, "recycled");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        // world allowlist
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        // If hand item has 'recycled' tag => allow place, but no XP
        ItemStack hand = e.getItemInHand();
        if (hand != null){
            ItemMeta meta = hand.getItemMeta();
            if (meta != null){
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(RECYCLED_KEY, PersistentDataType.BYTE)){
                    // still mark location as placed so next break will tag drops again
                    placed.markPlaced(e.getBlockPlaced().getLocation());
                    return; // deny XP
                }
            }
        }

        // Normal new inventory consumption => grant XP (even same coordinate)
        level.onPlace(e);

        // mark for later drop tagging
        placed.markPlaced(e.getBlockPlaced().getLocation());
    }
}
