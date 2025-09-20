package com.signition.samskybridge.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalBlocker implements Listener {
    @EventHandler
    public void onPortal(PlayerPortalEvent e){
        // No-op
    }
}
