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

/**
 * Tags drops from player-placed blocks, and counts them when the player picks them up.
 * The tag is applied on the ItemStack's PersistentDataContainer via ItemMeta.
 */
public final class DropAndPickupListener implements Listener {

    private final Main plugin;
    private final PlacedTracker placedTracker;
    private final RecycleGuardService recycleGuard;
    private final NamespacedKey recycledKey;

    public DropAndPickupListener(Main plugin, PlacedTracker placedTracker, RecycleGuardService recycleGuard){
        this.plugin = plugin;
        this.placedTracker = placedTracker;
        this.recycleGuard = recycleGuard;
        this.recycledKey = new NamespacedKey(plugin, "samskybridge_recycled");
    }

    /** When a player breaks a block, if that block was player-placed, tag all dropped items. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e){
        if (e.getPlayer() == null) return;
        // consume() returns true iff this location was previously marked as player-placed
        boolean wasPlaced = placedTracker.consume(e.getBlock().getLocation());
        if (!wasPlaced) return;

        for (Item entityItem : e.getItems()){
            ItemStack stack = entityItem.getItemStack();
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            // mark recycled = 1
            meta.getPersistentDataContainer().set(recycledKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte)1);
            stack.setItemMeta(meta);
            entityItem.setItemStack(stack);
        }
    }

    /** When a player picks up a tagged item, increment the recycled counter and remove the tag from the stack. */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e){
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        ItemStack stack = e.getItem().getItemStack();
        if (stack == null) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        Byte tagged = meta.getPersistentDataContainer().get(recycledKey, org.bukkit.persistence.PersistentDataType.BYTE);
        if (tagged == null || tagged == 0) return;

        // Count by stack amount, then strip the tag to avoid double counting later.
        int amt = Math.max(1, stack.getAmount());
        recycleGuard.add(p.getUniqueId(), stack.getType(), amt);

        meta.getPersistentDataContainer().remove(recycledKey);
        stack.setItemMeta(meta);
        e.getItem().setItemStack(stack);
    }
}