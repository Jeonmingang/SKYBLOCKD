package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockXPListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    private final LevelService levels;
    private final DropPickupTracker tracker;

    public BlockXPListener(Main plugin, DataStore store, LevelService levels, DropPickupTracker tracker){
        this.plugin = plugin;
        this.store = store;
        this.levels = levels;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        int amount = levels.getXpFor(e.getBlock().getType());
        if (amount <= 0) return;

        // simple anti-recycle: if recently picked up & same location, skip
        if (tracker.isRecycleLoop(p, e.getBlock().getLocation())) return;

        levels.grantXp(p, amount);
        int level = levels.levelOf(p);
        int xp = levels.xpOf(p);
        int next = levels.nextRequired(level);
        p.sendMessage(Text.col("&7[ &a섬 &7]&r &a+" + amount + "xp &7(현재 &f" + xp + "&7/&f" + next + "&7, Lv.&b" + level + "&7)"));

        // update tab list prefix
        String pref = plugin.getRankingService().tabPrefixFor(p);
        p.setPlayerListName(Text.col(pref + p.getName()));
    }
}
