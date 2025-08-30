
package com.signition.samskybridge.integration;
import org.bukkit.Bukkit; import org.bukkit.entity.Player;
public class BentoSync {
  public static boolean visitIsland(Player viewer, java.util.UUID owner){
    try{
      if (Bukkit.getPluginManager().getPlugin("BentoBox")==null) return false;
      String name = Bukkit.getOfflinePlayer(owner).getName();
      if (name==null) return false;
      viewer.performCommand("is go " + name);
      return true;
    }catch (Throwable t){ return false; }
  }
}
