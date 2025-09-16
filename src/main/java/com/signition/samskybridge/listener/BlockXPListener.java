package com.signition.samskybridge.listener;

import com.signition.samskybridge.level.LevelService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Location;

public class BlockXPListener implements Listener {
    private final LevelService level;
    public BlockXPListener(com.signition.samskybridge.Main plugin, LevelService level){
        this.level = level;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        Material mat = e.getBlock().getType();
        level.onPlace(mat, e.getBlock().getLocation(), e.getPlayer());
    }
}