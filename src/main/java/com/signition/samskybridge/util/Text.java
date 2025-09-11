
package com.signition.samskybridge.util;

import org.bukkit.ChatColor;

public class Text {
    public static String col(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    

    /** Backward-compat alias */
    public static String color(String s){
        return col(s);
    }
}
