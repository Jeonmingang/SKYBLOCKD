
package com.signition.samskybridge.integration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.lang.reflect.*;

public class BentoBridge {
  private static Class<?> clazzBento;
  private static Object bento;
  private static Method mGetIslands;
  private static Class<?> clazzIslandsManager;
  private static Method mGetIslandWorldUUID;
  private static Method mGetIslandWorldUser;
  private static Class<?> clazzIsland;
  private static Method mSetProtectionRange;
  private static Method mSetRange;
  private static Method mSetMaxMembersRank;
  private static Class<?> clazzRanksMgr;
  private static Integer MEMBER_RANK = 500; // fallback

  private static boolean init(){
    try{
      if (clazzBento != null) return true;
      clazzBento = Class.forName("world.bentobox.bentobox.BentoBox");
      Method getInstance = clazzBento.getMethod("getInstance");
      bento = getInstance.invoke(null);
      try{
        mGetIslands = clazzBento.getMethod("getIslandsManager");
      }catch(NoSuchMethodException ex){
        mGetIslands = clazzBento.getMethod("getIslands");
      }
      Object islands = mGetIslands.invoke(bento);
      clazzIslandsManager = islands.getClass();
      for (Method m : clazzIslandsManager.getMethods()){
        if (m.getName().equals("getIsland") && m.getParameterCount()==2){
          if (m.getParameterTypes()[0].getName().equals("org.bukkit.World") && m.getParameterTypes()[1].getName().contains("UUID")){
            mGetIslandWorldUUID = m; break;
          }
        }
      }
      clazzIsland = Class.forName("world.bentobox.bentobox.database.objects.Island");
      try{ mSetProtectionRange = clazzIsland.getMethod("setProtectionRange", int.class);}catch(Exception ignore){}
      try{ mSetRange = clazzIsland.getMethod("setRange", int.class);}catch(Exception ignore){}
      try{ mSetMaxMembersRank = clazzIsland.getMethod("setMaxMembers", int.class, Integer.class);}catch(Exception ignore){}
      try{
        clazzRanksMgr = Class.forName("world.bentobox.bentobox.managers.RanksManager");
        MEMBER_RANK = clazzRanksMgr.getField("MEMBER_RANK").getInt(null);
      }catch(Exception ignore){}
      return true;
    }catch(Exception e){
      return false;
    }
  }

  private static Object getIsland(Player p){
    try{
      if (!init()) return null;
      Object islands = mGetIslands.invoke(bento);
      java.util.UUID u = p.getUniqueId();
      World w = p.getWorld();
      return mGetIslandWorldUUID.invoke(islands, w, u);
    }catch(Exception e){
      return null;
    }
  }

  public static boolean available(){
    return init();
  }

  public static boolean setProtectionRange(Player p, int range){
    try{
      Object island = getIsland(p);
      if (island == null) return false;
      if (mSetProtectionRange != null) mSetProtectionRange.invoke(island, range);
      else if (mSetRange != null) mSetRange.invoke(island, range);
      else return false;
      // try to save all to persist
      Object islands = mGetIslands.invoke(bento);
      for (Method m : clazzIslandsManager.getMethods()){
        if (m.getName().equals("saveAll") && m.getParameterCount()==0){ m.invoke(islands); break; }
      }
      return true;
    }catch(Exception e){
      return false;
    }
  }

  public static boolean setMemberLimit(Player p, int limit){
    try{
      Object island = getIsland(p);
      if (island == null || mSetMaxMembersRank == null) return false;
      mSetMaxMembersRank.invoke(island, MEMBER_RANK, Integer.valueOf(limit));
      Object islands = mGetIslands.invoke(bento);
      for (Method m : clazzIslandsManager.getMethods()){
        if (m.getName().equals("saveAll") && m.getParameterCount()==0){ m.invoke(islands); break; }
      }
      return true;
    }catch(Exception e){
      return false;
    }
  }
}
