
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.xpguard.SlotGuardService;
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
    private final PlacedTracker placedTracker;
    private final NamespacedKey RECYCLED_KEY;

    public BlockXPListener(Main plugin, DataStore store, LevelService level, PlacedTracker tracker){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.placedTracker = tracker;
        this.RECYCLED_KEY = new NamespacedKey(plugin, "recycled");
        this.slotGuard = new SlotGuardService();
        this.slotGuard.configureSeconds(plugin.getConfig().getInt("xp_once.per_slot_ttl_seconds", -1));
        slotGuard.configureSeconds(plugin.getConfig().getInt("xp_once.per_slot_ttl_seconds", -1));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        // Allowed worlds check
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        // If player's hand item is a recycled drop from a previously placed block -> allow place but deny XP
        ItemStack hand = e.getItemInHand();
        if (hand != null){
            ItemMeta meta = hand.getItemMeta();
            if (meta != null){
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(RECYCLED_KEY, PersistentDataType.BYTE)){
                    return; // skip XP
                }
            }
        }

        // Award XP normally via LevelService
        level.onPlace(e);

        // Mark this location as player-placed for future drop tagging
        placedTracker.markPlaced(e.getBlockPlaced().getLocation());
    }
}
