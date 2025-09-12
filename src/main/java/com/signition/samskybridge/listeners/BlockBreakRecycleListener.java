
package com.signition.samskybridge.listeners;

import com.signition.samskybridge.xpguard.RecycleGuardService;
import com.signition.samskybridge.xpguard.SlotGuardService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

/**
 * Marks recycled items when a player breaks a block that was previously placed by a player.
 * For simplicity (without persistent storage), we treat "recent placements" via SlotGuard.
 */
public final class BlockBreakRecycleListener implements Listener {

    private final Plugin plugin;
    private final RecycleGuardService recycle;
    private final SlotGuardService slots;

    public BlockBreakRecycleListener(Plugin plugin, RecycleGuardService recycle, SlotGuardService slots){
        this.plugin = plugin;
        this.recycle = recycle;
        this.slots = slots;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if (p == null) return;
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;

        Block b = e.getBlock();
        // If this location previously awarded XP for a placement, treat its drop as "recycled"
        if (slots.already(b.getLocation())){
            Material mat = b.getType();
            // assume typical drop count 1 for guard purposes
            recycle.add(p.getUniqueId(), mat, 1);
            // optional: clear slot record after break to allow fresh cycle
            slots.clear(b.getLocation());
        }
    }
}
