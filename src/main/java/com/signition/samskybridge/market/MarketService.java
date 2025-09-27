package com.signition.samskybridge.market;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MarketService {
    private final Plugin plugin;
    private final java.util.Map<java.util.UUID, Long> listings = new java.util.HashMap<java.util.UUID, Long>();

    public MarketService(Plugin plugin){ this.plugin = plugin; }

    public void list(Player owner, long price){ listings.put(owner.getUniqueId(), price); }
    public java.util.Map<java.util.UUID, Long> all(){ return java.util.Collections.unmodifiableMap(listings); }
    public void click(org.bukkit.entity.Player p, org.bukkit.inventory.ItemStack item, boolean shift){
        // basic no-op; extend to buy/sell behavior using item meta and NBT tags
        p.sendMessage(com.signition.samskybridge.util.Text.color("&7거래 기능은 구성에 따라 동작합니다."));
    }

}
