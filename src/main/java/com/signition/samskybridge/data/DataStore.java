
package com.signition.samskybridge.data;
import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File; import java.io.IOException;
import java.util.*; import java.util.stream.Collectors;
public class DataStore {
  private final Main plugin; private final File dataFile; private final YamlConfiguration data;
  private final Map<UUID, IslandData> islands = new HashMap<>();
  private final Set<UUID> chatOn = new HashSet<>();
  public DataStore(Main plugin){
    this.plugin = plugin; this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    if (!dataFile.getParentFile().exists()) dataFile.getParentFile().mkdirs();
    this.data = YamlConfiguration.loadConfiguration(dataFile); load();
  }
  public void save(){
    try{
      data.set("islands", null);
      for (Map.Entry<UUID, IslandData> e : islands.entrySet()){
        String b = "islands."+e.getKey();
        IslandData is = e.getValue();
        data.set(b+".name", is.getName());
        data.set(b+".owner", is.getOwner().toString());
        data.set(b+".coOwners", toStrings(is.getCoOwners()));
        data.set(b+".members", toStrings(is.getMembers()));
        data.set(b+".workers", toStrings(is.getWorkers()));
        data.set(b+".level", is.getLevel());
        data.set(b+".xp", is.getXp());
        data.set(b+".sizeLevel", is.getSizeLevel());
        data.set(b+".teamLevel", is.getTeamLevel());
        data.set(b+".forSale", is.isForSale());
        data.set(b+".price", is.getPrice());
        data.set(b+".xpOnce", new ArrayList<String>(is.getXpOnce()));
      }
      data.set("chatOn", new ArrayList<String>(toStrings(chatOn)));
      data.save(dataFile);
    }catch (IOException e){ plugin.getLogger().warning("Failed to save data.yml: "+e.getMessage()); }
  }
  private List<String> toStrings(Collection<UUID> c){
    List<String> out = new ArrayList<String>();
    for (UUID u : c) out.add(u.toString());
    return out;
  }
  private void load(){
    islands.clear();
    if (data.isConfigurationSection("islands")){
      for (String k : data.getConfigurationSection("islands").getKeys(false)){
        try{
          UUID id = UUID.fromString(k);
          IslandData is = new IslandData(id);
          is.setName(data.getString("islands."+k+".name", k));
          try { is.setOwner(UUID.fromString(data.getString("islands."+k+".owner", k))); } catch (Exception ignored){}
          for (String s : data.getStringList("islands."+k+".coOwners")) try{ is.getCoOwners().add(UUID.fromString(s)); }catch (Exception ignored){}
          for (String s : data.getStringList("islands."+k+".members")) try{ is.getMembers().add(UUID.fromString(s)); }catch (Exception ignored){}
          for (String s : data.getStringList("islands."+k+".workers")) try{ is.getWorkers().add(UUID.fromString(s)); }catch (Exception ignored){}
          is.setLevel(data.getInt("islands."+k+".level", 0));
          is.setXp(data.getLong("islands."+k+".xp", 0L));
          is.setSizeLevel(data.getInt("islands."+k+".sizeLevel", 0));
          is.setTeamLevel(data.getInt("islands."+k+".teamLevel", 0));
          is.setForSale(data.getBoolean("islands."+k+".forSale", false));
          is.setPrice(data.getDouble("islands."+k+".price", 0.0));
          for (String s : data.getStringList("islands."+k+".xpOnce")) is.getXpOnce().add(s);
          islands.put(id, is);
        }catch (IllegalArgumentException ignored){}
      }
    }
    chatOn.clear(); for (String s : data.getStringList("chatOn")) try{ chatOn.add(UUID.fromString(s)); }catch(Exception ignored){}
  }
  public IslandData getOrCreate(UUID owner){ return islands.computeIfAbsent(owner, IslandData::new); }
  public Optional<IslandData> findByMember(UUID u){
    IslandData o = islands.get(u); if (o!=null) return Optional.of(o);
    for (IslandData is : islands.values()){
      if (is.getCoOwners().contains(u) || is.getMembers().contains(u)) return Optional.of(is);
    }
    return Optional.empty();
  }
  public boolean hasIsland(UUID u){
    if (islands.containsKey(u)) return true;
    for (IslandData is : islands.values()) if (is.getCoOwners().contains(u) || is.getMembers().contains(u)) return true;
    return false;
  }
  public List<IslandData> getAll(){ return new ArrayList<IslandData>(islands.values()); }
  public boolean isChatOn(UUID u){ return chatOn.contains(u); }
  public void toggleChat(UUID u){ if (!chatOn.add(u)) chatOn.remove(u); }
}
