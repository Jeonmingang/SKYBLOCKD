
package com.signition.samskybridge.data;

import com.signition.samskybridge.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
  private final Main plugin;
  private final File file;
  private final YamlConfiguration yml;
  private final Map<UUID, IslandData> islands = new HashMap<>();
  private final Set<UUID> chatOn = new HashSet<>();

  // market
  public static class IslandSale {
    public UUID owner;
    public double price;
    public long listedAt;
    public IslandSale(UUID o, double p){ owner=o; price=p; listedAt=System.currentTimeMillis(); }
  }
  private final List<IslandSale> market = new ArrayList<>();

  private final Object ioLock = new Object();
  private org.bukkit.scheduler.BukkitTask pendingSaveTask;
  private int debounceTicks = 20;

  public DataStore(Main plugin){
    this.plugin = plugin;
    plugin.getDataFolder().mkdirs();
    this.file = new File(plugin.getDataFolder(), "islands.yml");
    this.yml = YamlConfiguration.loadConfiguration(file);
    try{ this.debounceTicks = Math.max(1, plugin.getConfig().getInt("storage.debounce-ticks", 20)); }catch (Exception ignored){}
    load();
  }

  public void load(){
    islands.clear();
    if (yml.contains("islands")){
      for (String k : yml.getConfigurationSection("islands").getKeys(false)){
        try{
          UUID id = UUID.fromString(k);
          IslandData is = new IslandData(id);
          is.setName(yml.getString("islands."+k+".name", k));
          try{ is.setOwner(UUID.fromString(yml.getString("islands."+k+".owner", k))); }catch (Exception ignored){}
          for (String s : yml.getStringList("islands."+k+".coOwners")) try{ is.getCoOwners().add(UUID.fromString(s)); }catch(Exception ignored){}
          for (String s : yml.getStringList("islands."+k+".members")) try{ is.getMembers().add(UUID.fromString(s)); }catch(Exception ignored){}
          is.setLevel(yml.getInt("islands."+k+".level", 0));
          is.setXp(yml.getLong("islands."+k+".xp", 0L));
          is.setSizeLevel(yml.getInt("islands."+k+".sizeLevel", 0));
          is.setTeamLevel(yml.getInt("islands."+k+".teamLevel", 0));
          islands.put(id, is);
        }catch(Exception ignored){}
      }
    }
    chatOn.clear();
    for (String s : yml.getStringList("chatOn")){
      try{ chatOn.add(UUID.fromString(s)); }catch(Exception ignored){}
    }
    market.clear();
    java.util.List<?> list = yml.getList("market");
    if (list != null){
      for (Object o : list){
        if (o instanceof java.util.Map){
          java.util.Map m = (java.util.Map)o;
          try{
            UUID oid = UUID.fromString(String.valueOf(m.get("owner")));
            double price = Double.parseDouble(String.valueOf(m.get("price")));
            long ts = Long.parseLong(String.valueOf(m.getOrDefault("listedAt", System.currentTimeMillis())));
            IslandSale s = new IslandSale(oid, price); s.listedAt = ts; market.add(s);
          }catch(Exception ignore){}
        }
      }
    }
  }

  private void saveMarketToYml(){
    java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
    for (IslandSale s : market){
      java.util.Map<String,Object> m = new java.util.HashMap<>();
      m.put("owner", s.owner.toString());
      m.put("price", s.price);
      m.put("listedAt", s.listedAt);
      list.add(m);
    }
    yml.set("market", list);
  }

  public void save(){
    synchronized (ioLock){
      for (Map.Entry<UUID, IslandData> en : islands.entrySet()){
        String k = en.getKey().toString();
        IslandData is = en.getValue();
        yml.set("islands."+k+".name", is.getName());
        yml.set("islands."+k+".owner", is.getOwner().toString());
        List<String> cos = new ArrayList<>(); for (UUID u : is.getCoOwners()) cos.add(u.toString());
        List<String> mem = new ArrayList<>(); for (UUID u : is.getMembers()) mem.add(u.toString());
        yml.set("islands."+k+".coOwners", cos);
        yml.set("islands."+k+".members", mem);
        yml.set("islands."+k+".level", is.getLevel());
        yml.set("islands."+k+".xp", is.getXp());
        yml.set("islands."+k+".sizeLevel", is.getSizeLevel());
        yml.set("islands."+k+".teamLevel", is.getTeamLevel());
      }
      List<String> co = new ArrayList<>(); for (UUID u : chatOn) co.add(u.toString());
      yml.set("chatOn", co);
      saveMarketToYml();
      try{ yml.save(file); }catch(IOException e){ e.printStackTrace(); }
    }
  }

  public void saveAsync(){
    synchronized (ioLock){
      if (pendingSaveTask != null && !pendingSaveTask.isCancelled()) return;
      pendingSaveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable(){
        public void run(){ save(); pendingSaveTask = null; }
      }, debounceTicks);
    }
  }

  public IslandData getOrCreate(UUID owner){
    IslandData is = islands.get(owner);
    if (is == null){ is = new IslandData(owner); islands.put(owner, is); saveAsync(); }
    return is;
  }

  public Optional<IslandData> findByMember(UUID u){
    for (IslandData is : islands.values()) if (is.hasMember(u)) return Optional.of(is);
    return Optional.empty();
  }

  public Set<UUID> getAllOwners(){ return new HashSet<>(islands.keySet()); }
  public boolean isChatOn(UUID u){ return chatOn.contains(u); }
  public void setChatOn(UUID u, boolean on){ if (on) chatOn.add(u); else chatOn.remove(u); saveAsync(); }

  public List<IslandSale> getMarket(){ return market; }
  public void addSale(IslandSale s){ market.add(s); saveAsync(); }
  public void removeSale(IslandSale s){ market.remove(s); saveAsync(); }
}
