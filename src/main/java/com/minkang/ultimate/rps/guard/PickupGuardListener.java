
package com.minkang.ultimate.rps.guard;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.persistence.PersistentDataType;

public class PickupGuardListener implements Listener {

    private final NamespacedKey KEY = new NamespacedKey(UltimateRpsPlugin.get(), "ultirps-owner");

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Item item = e.getItem();
        try {
            String ownerStr = item.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
            if (ownerStr == null) return; // 보호 기간 아님
            java.util.UUID owner = java.util.UUID.fromString(ownerStr);
            if (!owner.equals(((Player)e.getEntity()).getUniqueId())) {
                e.setCancelled(true);
            }
        } catch (Throwable ignored) {}
    }
}
