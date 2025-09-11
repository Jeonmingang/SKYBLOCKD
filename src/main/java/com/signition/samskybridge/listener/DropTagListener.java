
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.place.PlacedTracker;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DropTagListener implements Listener {
    private final NamespacedKey RECYCLED_KEY;
    private final PlacedTracker placedTracker;

    public DropTagListener(Main plugin, PlacedTracker tracker){
        this.RECYCLED_KEY = new NamespacedKey(plugin, "recycled");
        this.placedTracker = tracker;
    }

    // Fallback: if some plugins bypass BlockDropItemEvent, still mark the location on break
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e){
        if (placedTracker.wasPlaced(e.getBlock().getLocation())){
            // don't consume here; we'll consume after drops are produced
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onDrop(BlockDropItemEvent e){
        if (!placedTracker.wasPlaced(e.getBlock().getLocation())) return;
        for (Item it : e.getItems()){
            ItemStack st = it.getItemStack();
            ItemMeta meta = st.getItemMeta();
            if (meta == null) continue;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(RECYCLED_KEY, PersistentDataType.BYTE, (byte)1);
            st.setItemMeta(meta);
            it.setItemStack(st);
        }
        // consume the mark so the next placement using recycled item will be detected
        placedTracker.consume(e.getBlock().getLocation());
    }
}
