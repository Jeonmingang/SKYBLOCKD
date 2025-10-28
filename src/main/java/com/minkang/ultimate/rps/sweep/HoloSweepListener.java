
package com.minkang.ultimate.rps.sweep;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class HoloSweepListener implements Listener {
    private final UltimateRpsPlugin plugin;
    public HoloSweepListener(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk c = e.getChunk();
        for (Entity ent : c.getEntities()) {
            if (ent instanceof ArmorStand) {
                try {
                    plugin.holograms().evaluateAndMaybeRemove((ArmorStand) ent);
                } catch (Throwable ignored) {}
            }
        }
    }
}
