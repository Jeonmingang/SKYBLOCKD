package com.signition.samskybridge.listener;

import com.signition.samskybridge.xpguard.RecycleGuardService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ClearOnQuitListener implements Listener {
    private final RecycleGuardService recycleGuard;

    public ClearOnQuitListener(RecycleGuardService recycleGuard){
        this.recycleGuard = recycleGuard;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        recycleGuard.clear(e.getPlayer().getUniqueId());
    }
}