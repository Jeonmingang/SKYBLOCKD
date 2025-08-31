
package com.signition.samskybridge.data;
import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File; import java.io.IOException; import java.util.*;
public class DataStore {
  private final Main plugin; private final File file; private final YamlConfiguration yml;
  private final Map<java.util.UUID, IslandData> islands = new HashMap<java.util.UUID, IslandData>();
  private final java.util.Set<java.util.UUID> chatOn = new java.util.HashSet<java.util.UUID>();
  public DataStore(Main plugin){ this.plugin=plugin; this.file=new File(plugin.getDataFolder(), "data.yml"); if (!file.getParentFile().exists()) file.getParentFile().mkdirs(); this.yml=YamlConfiguration.loadConfiguration(file); load(); }
  public void save(){
    try{
      yml.set("islands", null);
      for (Map.Entry<java.util.UUID, IslandData> e : islands.entrySet()){
        String b = "islands."+e.getKey();
        IslandData is = e.getValue();
        yml.set(b+".name", is.getName());
        yml.set(b+".owner", is.getOwner().toString());
        java.util.List<String> co=new java.util.ArrayList<String>(); for (java.util.UUID u:is.getCoOwners()) co.add(u.toString()); yml.set(b+".coOwners", co);
        java.util.List<String> me=new java.util.ArrayList<String>(); for (java.util.UUID u:is.getMembers()) me.add(u.toString()); yml.set(b+".members", me);
        yml.set(b+".level", is.getLevel()); yml.set(b+".xp", is.getXp()); yml.set(b+".sizeLevel", is.getSizeLevel()); yml.set(b+".teamLevel", is.getTeamLevel());
        yml.set(b+".forSale", is.isForSale()); yml.set(b+".price", is.getPrice());
        yml.set(b+".xpOnce", new java.util.ArrayList<String>(is.getXpOnce()));
      }
      java.util.List<String> chat=new java.util.ArrayList<String>(); for (java.util.UUID u:chatOn) chat.add(u.toString()); yml.set("chatOn", chat);
      yml.save(file);
    }catch(IOException ex){ plugin.getLogger().warning("save error: "+ex.getMessage()); }
  }
  private void load(){
    islands.clear();
    if (yml.isConfigurationSection("islands")){
      for (String k : yml.getConfigurationSection("islands").getKeys(false)){
        try{
          java.util.UUID id = java.util.UUID.fromString(k);
          IslandData is = new IslandData(id);
          is.setName(yml.getString("islands."+k+".name", k));
          try{ is.setOwner(java.util.UUID.fromString(yml.getString("islands."+k+".owner", k))); }catch(Exception ignored){}
          for (String s : yml.getStringList("islands."+k+".coOwners")) try{ is.getCoOwners().add(java.util.UUID.fromString(s)); }catch(Exception ignored){}
          for (String s : yml.getStringList("islands."+k+".members")) try{ is.getMembers().add(java.util.UUID.fromString(s)); }catch(Exception ignored){}
          is.setLevel(yml.getInt("islands."+k+".level",0)); is.setXp(yml.getLong("islands."+k+".xp",0)); is.setSizeLevel(yml.getInt("islands."+k+".sizeLevel",0)); is.setTeamLevel(yml.getInt("islands."+k+".teamLevel",0));
          is.setForSale(yml.getBoolean("islands."+k+".forSale",false)); is.setPrice(yml.getDouble("islands."+k+".price",0.0));
          for (String s : yml.getStringList("islands."+k+".xpOnce")) is.getXpOnce().add(s);
          islands.put(id, is);
        }catch(Exception ignored){}
      }
    }
    chatOn.clear(); for (String s : yml.getStringList("chatOn")) try{ chatOn.add(java.util.UUID.fromString(s)); }catch(Exception ignored){}
  }
  public IslandData getOrCreate(java.util.UUID owner){ IslandData is=islands.get(owner); if (is==null){ is=new IslandData(owner); islands.put(owner,is);} return is; }
  public java.util.Optional<IslandData> findByMember(java.util.UUID u){
    IslandData o=islands.get(u); if (o!=null) return java.util.Optional.of(o);
    for (IslandData is : islands.values()) if (is.getCoOwners().contains(u) || is.getMembers().contains(u)) return java.util.Optional.of(is);
    return java.util.Optional.empty();
  }
  public java.util.List<IslandData> getAll(){ return new java.util.ArrayList<IslandData>(islands.values()); }
  public boolean isChatOn(java.util.UUID u){ return chatOn.contains(u); }
  public void toggleChat(java.util.UUID u){ if (!chatOn.add(u)) chatOn.remove(u); }
}
