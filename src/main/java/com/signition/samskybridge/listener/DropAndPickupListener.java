package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.xpguard.RecycleGuardService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class DropAndPickupListener implements Listener {
    private final PlacedTracker placed;
    private final RecycleGuardService recycleGuard;
    private final NamespacedKey RECYCLED_KEY;

    public DropAndPickupListener(Main plugin, PlacedTracker placedTracker, RecycleGuardService recycleGuard){
        this.placed = placedTracker;
        this.recycleGuard = recycleGuard;
        this.RECYCLED_KEY = new NamespacedKey(plugin, "recycled");
    }

    /** Tag drops from player-placed blocks so we can recognize them on pickup. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e){
        if (!placed.wasPlaced(e.getBlock().getLocation())) return;
        for (Item it : e.getItems()){
            ItemStack st = it.getItemStack();
            ItemMeta meta = st.getItemMeta();
            if (meta == null) continue;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(RECYCLED_KEY, PersistentDataType.BYTE, (byte)1);
            st.setItemMeta(meta);
            it.setItemStack(st);
        }
        // Consume the placed mark AFTER tagging, so the location is now considered natural again.
        placed.consume(e.getBlock().getLocation());
    }

    /** When the player picks up a tagged item, record a recycled token and strip the tag so it does not contaminate stacks. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e){
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        ItemStack st = e.getItem().getItemStack();
        ItemMeta meta = st.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte flagged = pdc.get(RECYCLED_KEY, PersistentDataType.BYTE);
        if (flagged == null || flagged.byteValue() != (byte)1) return;

        // Record recycled count and remove tag
        recycleGuard.recordPickup(p.getUniqueId(), st.getType(), st.getAmount());
        pdc.remove(RECYCLED_KEY);
        st.setItemMeta(meta);
        e.getItem().setItemStack(st);
    }
}