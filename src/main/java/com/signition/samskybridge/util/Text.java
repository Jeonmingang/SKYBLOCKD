
package com.signition.samskybridge.util;
import org.bukkit.ChatColor;
public class Text{
  public static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s==null? "" : s); }
}
