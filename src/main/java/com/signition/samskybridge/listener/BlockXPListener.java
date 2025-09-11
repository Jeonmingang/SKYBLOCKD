
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockXPListener implements Listener {

    private final Main plugin;
    private final DataStore data;
    private final LevelService levels;
    private final DropPickupTracker tracker;

    private final Set<String> allowedWorlds = new HashSet<>();

    public BlockXPListener(Main plugin, DataStore data, LevelService levels, DropPickupTracker tracker){
        this.plugin = plugin;
        this.data = data;
        this.levels = levels;
        this.tracker = tracker;
        reloadWorlds();
    }

    private void reloadWorlds(){
        allowedWorlds.clear();
        List<String> ws = plugin.getConfig().getStringList("xp.allowed-worlds");
        allowedWorlds.addAll(ws);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        World w = e.getBlock().getWorld();
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(w.getName())) return;

        Material mat = e.getBlockPlaced().getType();
        int amount = levels.getXpFor(mat);
        if (amount <= 0) return;

        // Anti: place -> break -> pickup -> reinstall loop at SAME SLOT within TTL
        if (tracker.isRecycleLoop(p, e.getBlockPlaced().getLocation())) {
            // No XP
            return;
        }

        levels.grantXp(p, amount);

        int level = levels.levelOf(p);
        int xp = levels.xpOf(p);
        int next = levels.nextRequired(level);
        p.sendMessage(Text.col("&7[ &a섬 &7]&r &a+" + amount + " XP &7(현재 &f" + xp + "&7/&f" + next + "&7, Lv.&b" + level + "&7)"));
        // also update tablist name
        String pref = plugin.getRankingService().tabPrefixFor(p);
        p.setPlayerListName(Text.col(pref + p.getName()));
    }
}
