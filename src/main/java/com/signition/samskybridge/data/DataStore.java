package com.signition.samskybridge.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration yaml;

    // players -> IslandData
    private final Map<UUID, IslandData> players = new HashMap<>();

    // simple chat toggle
    private final Set<UUID> chatOn = new HashSet<>();

    // simple market in-memory
    private final List<IslandSale> market = new ArrayList<>();

    public DataStore(JavaPlugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    private void load(){
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) file.createNewFile();
            yaml = YamlConfiguration.loadConfiguration(file);
            // minimal load: xp/level per player if present
            if (yaml.isConfigurationSection("players")){
                for (String key : yaml.getConfigurationSection("players").getKeys(false)){
                    try {
                        UUID id = UUID.fromString(key);
                        IslandData d = new IslandData();
                        d.setXp(yaml.getInt("players."+key+".xp", 0));
                        d.setLevel(yaml.getInt("players."+key+".level", 0));
                        players.put(id, d);
                    } catch (IllegalArgumentException ignore){}
                }
            }
        } catch (IOException e){
            plugin.getLogger().warning("Failed to load data.yml: " + e.getMessage());
        }
    }

    public void saveAsync(){
        try {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
                @Override public void run(){
                    try {
                        YamlConfiguration y = new YamlConfiguration();
                        for (Map.Entry<UUID, IslandData> en : players.entrySet()){
                            String key = "players."+ en.getKey().toString();
                            IslandData d = en.getValue();
                            y.set(key+".xp", d.getXp());
                            y.set(key+".level", d.getLevel());
                            y.set(key+".sizeLevel", d.getSizeLevel());
                            y.set(key+".teamLevel", d.getTeamLevel());
                        }
                        y.save(file);
                    } catch (Throwable t){
                        plugin.getLogger().warning("saveAsync error: " + t.getMessage());
                    }
                }
            });
        } catch (Throwable ignore){}
    }

    public IslandData getOrCreate(UUID uid){
        IslandData d = players.get(uid);
        if (d == null){ d = new IslandData(); d.setOwner(uid); players.put(uid, d); }
        return d;
    }

    public Optional<IslandData> findByMember(UUID uid){
        IslandData self = players.get(uid);
        if (self != null) return Optional.of(self);
        for (IslandData d : players.values()){
            if (d.hasMember(uid)) return Optional.of(d);
        }
        return Optional.empty();
    }

    public boolean isChatOn(UUID uid){ return chatOn.contains(uid); }
    public void setChatOn(UUID uid, boolean on){
        if (on) chatOn.add(uid); else chatOn.remove(uid);
    }

    // --- Market API ---
    public Collection<IslandSale> getMarket(){ return Collections.unmodifiableList(market); }
    public void addSale(IslandSale s){ if (s != null) market.add(s); }
    public void removeSale(IslandSale s){ market.remove(s); }
    public Set<UUID> getAllOwners(){
        Set<UUID> set = new HashSet<>();
        for (IslandSale s: market) if (s.owner != null) set.add(s.owner);
        return set;
    }

    // --- DTO ---
    public static final class IslandSale {
        public final UUID owner;        // seller/owner uuid
        public final String ownerName;  // cached name (optional)
        public final long price;
        public final long listedAt;

        public IslandSale(UUID owner, String ownerName, long price, long listedAt){
            this.owner = owner; this.ownerName = ownerName; this.price = price; this.listedAt = listedAt;
        }
        // overload used by old call-sites: (owner, price)
        public IslandSale(UUID owner, double price){
            this(owner, null, (long)price, System.currentTimeMillis());
        }
    }
}
