package com.signition.samskybridge.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {
    public final NamespacedKey RECYCLED;
    public Keys(Plugin plugin){
        this.RECYCLED = new NamespacedKey(plugin, "recycled_from_placed");
    }
}
