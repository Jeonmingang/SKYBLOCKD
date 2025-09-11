package com.signition.samskybridge.listener;


import org.bukkit.entity.Item;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.util.Keys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DropTagListener implements Listener {
    private final PlacedTracker placedTracker;
    private final Keys keys;

    public DropTagListener(Main plugin, PlacedTracker tracker){
        this.placedTracker = tracker;
        this.keys = new Keys(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        // If it was one of our placed blocks, mark so that drops are tagged
        // (We don't remove here because BlockDropItemEvent needs to check again)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e){
        if (!placedTracker.wasPlaced(e.getBlock().getLocation())) return;
        // Tag every dropped item as recycled
        e.getItems().forEach(ent -> {
            ItemStack st = ent.getItemStack();
            if (st == null) return;
            ItemMeta meta = st.getItemMeta();
            if (meta == null) return;
            meta.getPersistentDataContainer().set(keys.RECYCLED, PersistentDataType.BYTE, (byte)1);
            st.setItemMeta(meta);
            ent.setItemStack(st);
        });
        // consume the placed marker after tagging
        placedTracker.consume(e.getBlock().getLocation());
    }
}
