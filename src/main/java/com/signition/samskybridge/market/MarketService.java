package com.signition.samskybridge.market;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MarketService {
    private final Plugin plugin;
    private final java.util.Map<java.util.UUID, Long> listings = new java.util.HashMap<java.util.UUID, Long>();

    public MarketService(Plugin plugin){ this.plugin = plugin; }

    public void list(Player owner, long price){ listings.put(owner.getUniqueId(), price); }
    public java.util.Map<java.util.UUID, Long> all(){ return java.util.Collections.unmodifiableMap(listings); }
}
