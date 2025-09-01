
package com.signition.samskybridge.integration;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.entity.Player;
import java.util.*;

public class IslandSync {
  public static void ensureSyncedFromBento(Main plugin, DataStore store, Player p){
    try{
      if (!BentoBridge.available()) return;
      if (!BentoBridge.hasIsland(p)) return;
      if (store.findByMember(p.getUniqueId()).isPresent()) return;

      java.util.UUID owner = BentoBridge.getIslandOwner(p);
      if (owner == null) owner = p.getUniqueId();
      IslandData is = store.getOrCreate(owner);
      is.getMembers().add(owner);
      for (java.util.UUID u : BentoBridge.getMembers(p)) is.getMembers().add(u);
      store.saveAsync();
    }catch(Exception ignored){}
  }
}
