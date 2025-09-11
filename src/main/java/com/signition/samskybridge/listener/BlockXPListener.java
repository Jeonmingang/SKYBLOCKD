package com.signition.samskybridge.listener;


import org.bukkit.persistence.PersistentDataType;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.util.Keys;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BlockXPListener implements Listener {
    private final Main plugin;
    private final LevelService level;
    private final PlacedTracker placed;
    private final Keys keys;

    public BlockXPListener(Main plugin, DataStore store, LevelService level, PlacedTracker tracker){
        this.plugin = plugin;
        this.level = level;
        this.placed = tracker;
        this.keys = new Keys(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        // world filter
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        // check only main hand to avoid double triggers from off-hand
        if (e.getHand() != EquipmentSlot.HAND) {
            // Still mark placed so break event knows origin
            placed.markPlaced(e.getBlockPlaced().getLocation());
            return;
        }

        // deny XP if item-in-hand is tagged as recycled
        ItemStack hand = e.getItemInHand();
        boolean recycled = false;
        if (hand != null && hand.getType() != Material.AIR){
            ItemMeta meta = hand.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(keys.RECYCLED, org.bukkit.persistence.PersistentDataType.BYTE)) {
                recycled = true;
            }
        }

        if (!recycled) {
            // fresh inventory block -> grant XP
            level.onPlace(e);
        }
        // regardless, mark the location as placed to propagate lineage
        placed.markPlaced(e.getBlockPlaced().getLocation());
    }
}
