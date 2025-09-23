package com.signition.samskybridge.feature;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FeatureService {
    private final Main plugin;
    private final DataStore store;
    private final File file;
    private final FileConfiguration data;
    private final Random rng = new Random();

    public FeatureService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        this.file = new File(plugin.getDataFolder(), "features.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){ try { data.save(file); } catch (IOException ignored) {} }

    /* ====== Helpers ====== */
    private IslandData ownedIsland(Player p){ return store.findByOwnerName(p.getName()); }

    private boolean canOperateHere(Player p){
        // only in island worlds
        List<String> worlds = plugin.getConfig().getStringList("features.require.island-worlds");
        if (worlds != null && !worlds.isEmpty()){
            String wn = p.getWorld()!=null ? p.getWorld().getName() : "";
            if (!worlds.contains(wn)){
                p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-on-island-world","&c섬 월드에서만 사용할 수 있습니다.")));
                return false;
            }
        }
        // owner only
        if (plugin.getConfig().getBoolean("features.require.owner-only", true) && ownedIsland(p)==null){
            p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.owner-only","&c섬장만 사용할 수 있습니다.")));
            return false;
        }
        return true;
    }

    private String base(UUID id){ return "islands."+id.toString(); }
    private void setLoc(UUID id, String key, Location loc){
        if (loc==null || loc.getWorld()==null) return;
        String b = base(id)+"."+key+".loc";
        data.set(b+".world", loc.getWorld().getName());
        data.set(b+".x", loc.getBlockX());
        data.set(b+".y", loc.getBlockY());
        data.set(b+".z", loc.getBlockZ());
        save();
    }
    private Location getLoc(UUID id, String key){
        String b = base(id)+"."+key+".loc";
        String w = data.getString(b+".world", null);
        if (w == null) return null;
        World world = plugin.getServer().getWorld(w);
        if (world == null) return null;
        int x = data.getInt(b+".x", 0);
        int y = data.getInt(b+".y", 0);
        int z = data.getInt(b+".z", 0);
        return new Location(world, x, y, z);
    }

    /* ====== Region checks ====== */
    public boolean hasMine(UUID id){ return getLoc(id,"mine") != null; }
    public boolean hasFarm(UUID id){ return getLoc(id,"farm") != null; }

    public boolean isInMineRegion(Player p, Location loc){
        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "mine");
        if (origin==null) return false;
        if (!Objects.equals(origin.getWorld(), loc.getWorld())) return false;
        String axis = data.getString(base(is.getId())+".mine.axis","X");
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        // tight 1x1 tube along axis at y of origin
        int y = origin.getBlockY();
        if (loc.getBlockY() != y) return false;
        if ("Z".equalsIgnoreCase(axis)){
            int min = Math.min(origin.getBlockZ(), origin.getBlockZ() + (len-1));
            int max = Math.max(origin.getBlockZ(), origin.getBlockZ() + (len-1));
            return loc.getBlockX()==origin.getBlockX() && loc.getBlockZ()>=min && loc.getBlockZ()<=max;
        } else {
            int min = Math.min(origin.getBlockX(), origin.getBlockX() + (len-1));
            int max = Math.max(origin.getBlockX(), origin.getBlockX() + (len-1));
            return loc.getBlockZ()==origin.getBlockZ() && loc.getBlockX()>=min && loc.getBlockX()<=max;
        }
    }

    public boolean isInFarmRegion(Player p, Location loc){
        IslandData is = ownedIsland(p);
        if (is == null) return false;
        Location origin = getLoc(is.getId(), "farm");
        if (origin==null) return false;
        if (!Objects.equals(origin.getWorld(), loc.getWorld())) return false;
        // 3x3 square centered on origin at y+1 for crops, y for farmland
        int half = 1; // 3x3
        return abs(loc.getBlockX()-origin.getBlockX())<=half && abs(loc.getBlockZ()-origin.getBlockZ())<=half;
    }
    private int abs(int v){ return v<0?-v:v; }

    /* ====== Weighted tables ====== */
    private List<Material> parseWeighted(List<String> list){
        List<Material> bag = new ArrayList<>();
        if (list==null) return bag;
        for (String s : list){
            try {
                String[] sp = s.trim().split(":");
                Material m = Material.valueOf(sp[0].trim());
                int w = (sp.length>1? Integer.parseInt(sp[1].trim()) : 1);
                for (int i=0;i<Math.max(1,w);i++) bag.add(m);
            } catch(Throwable ignored) { }
        }
        return bag;
    }
    public Material pickOreFor(Player p){
        IslandData is = ownedIsland(p);
        if (is==null) return Material.COAL_ORE;
        int lv = getMineLevel(is.getId());
        List<Material> bag = parseWeighted(plugin.getConfig().getStringList("features.mine.ores."+lv));
        return bag.isEmpty()? Material.COAL_ORE : bag.get(rng.nextInt(bag.size()));
    }
    public Material pickCropFor(Player p, Material fallback){
        IslandData is = ownedIsland(p);
        if (is==null) return fallback;
        int lv = getFarmLevel(is.getId());
        List<Material> bag = parseWeighted(plugin.getConfig().getStringList("features.farm.crops."+lv));
        return bag.isEmpty()? fallback : bag.get(rng.nextInt(bag.size()));
    }

    /* ====== Install (one per island) ====== */
    public void installMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasMine(is.getId())){ p.sendMessage(Text.color("&c이미 설치된 광산이 있습니다. 먼저 제거하세요.")); return; }

        Location baseLoc = p.getLocation().getBlock().getLocation();
        // BentoBox/BSkyBlock: ensure placement inside own island
        if (!plugin.getBento().isInOwnIsland(p, baseLoc)){
            p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-own-island","&c자신의 섬 내부에서만 설치할 수 있습니다.")));
            return;
        }

        // place starting one block in front, aligned to look axis at player's feet Y
        String axis = Math.abs(p.getLocation().getYaw())%180<45 || Math.abs(p.getLocation().getYaw())%180>135 ? "Z" : "X";
        // Better: snap to nearest cardinal
        float yaw = p.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;
        if (yaw>=45 && yaw<135) axis = "X";      // south -> +Z; use Z axis; but we store axis we iterate on
        else if (yaw>=135 && yaw<225) axis = "Z";
        else if (yaw>=225 && yaw<315) axis = "X";
        else axis = "Z";

        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        World w = baseLoc.getWorld();
        Location start = baseLoc.clone().add(p.getLocation().getDirection().setY(0).normalize());
        start.setY(baseLoc.getBlockY());

        // Lay a row of ore nodes exactly along axis, 1x1xlen
        Material preview = Material.STONE;
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0, 0, i);
            else t.add(i, 0, 0);
            Block b = w.getBlockAt(t);
            b.setType(preview, false);
        }
        // Persist origin (start) and axis
        setLoc(is.getId(),"mine", start);
        data.set(base(is.getId())+".mine.axis", axis);
        save();
        p.sendMessage(Text.color("&a광산이 설치되었습니다. &7방향: &f"+axis+" &7길이: &f"+len));
    }

    public void installFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        if (hasFarm(is.getId())){ p.sendMessage(Text.color("&c이미 설치된 농장이 있습니다. 먼저 제거하세요.")); return; }
        Location c = p.getLocation().getBlock().getLocation().add(p.getLocation().getDirection().setY(0).normalize());
        // BentoBox/BSkyBlock: ensure placement inside own island
        if (!plugin.getBento().isInOwnIsland(p, c)){
            p.sendMessage(Text.color(plugin.getConfig().getString("features.messages.only-own-island","&c자신의 섬 내부에서만 설치할 수 있습니다.")));
            return;
        }

        World w = c.getWorld();
        // 3x3 farmland, plant WHEAT as initial marker
        for (int dx=-1; dx<=1; dx++){
            for (int dz=-1; dz<=1; dz++){
                Location s = c.clone().add(dx,0,dz);
                w.getBlockAt(s).setType(Material.DIRT,false);
                w.getBlockAt(s).setType(Material.FARMLAND,false);
                w.getBlockAt(s.clone().add(0,1,0)).setType(Material.WHEAT,false);
            }
        }
        setLoc(is.getId(), "farm", c);
        save();
        p.sendMessage(Text.color("&a농장이 3x3으로 설치되었습니다."));
    }

    /* ====== Upgrade (unchanged public surface) ====== */
    public boolean upgradeMine(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getMineLevel(is.getId());
        int max = plugin.getConfig().getInt("features.mine.max-level", 5);
        if (now>=max){ p.sendMessage(Text.color("&c광산 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.mine.require.base", 3) + now*plugin.getConfig().getInt("features.mine.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setMineLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a광산 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    public boolean upgradeFarm(Player p){
        if (!canOperateHere(p)) return false;
        IslandData is = ownedIsland(p); if (is==null) return false;
        int now = getFarmLevel(is.getId());
        int max = plugin.getConfig().getInt("features.farm.max-level", 5);
        if (now>=max){ p.sendMessage(Text.color("&c농장 레벨이 최대입니다.")); return false; }
        int needLv = plugin.getConfig().getInt("features.farm.require.base", 2) + now*plugin.getConfig().getInt("features.farm.require.per-step", 1);
        if (is.getLevel() < needLv){ p.sendMessage(Text.color("&c요구 섬 레벨: &f"+needLv)); return false; }
        double cost = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), now);
        if (!plugin.getVault().withdraw(p.getName(), cost)){ p.sendMessage(Text.color("&c잔액 부족. 필요: &f"+(long)cost)); return false; }
        setFarmLevel(is.getId(), now+1);
        p.sendMessage(Text.color("&a농장 레벨 업! &7현재: &f"+(now+1)));
        return true;
    }

    /* ====== Remove ====== */
    public void removeMine(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        Location start = getLoc(is.getId(), "mine");
        if (start==null){ p.sendMessage(Text.color("&c설치된 광산이 없습니다.")); return; }
        int len = Math.max(1, plugin.getConfig().getInt("features.mine.install.length", 4));
        String axis = data.getString(base(is.getId())+".mine.axis","X");
        World w = start.getWorld();
        for (int i=0;i<len;i++) {
            Location t = start.clone();
            if ("Z".equals(axis)) t.add(0,0,i); else t.add(i,0,0);
            w.getBlockAt(t).setType(Material.AIR,false);
        }
        data.set(base(is.getId())+".mine", null);
        save();
        p.sendMessage(Text.color("&a광산이 제거되었습니다."));
    }

    public void removeFarm(Player p){
        if (!canOperateHere(p)) return;
        IslandData is = ownedIsland(p); if (is==null) return;
        Location c = getLoc(is.getId(), "farm");
        if (c==null){ p.sendMessage(Text.color("&c설치된 농장이 없습니다.")); return; }
        World w = c.getWorld();
        for (int dx=-1; dx<=1; dx++){ for (int dz=-1; dz<=1; dz++){ 
            Location s = c.clone().add(dx,0,dz);
            w.getBlockAt(s).setType(Material.AIR,false);
            w.getBlockAt(s.clone().add(0,1,0)).setType(Material.AIR,false);
        }}
        data.set(base(is.getId())+".farm", null);
        save();
        p.sendMessage(Text.color("&a농장이 제거되었습니다."));
    }

    /* ====== Levels (persist) ====== */
    public int getMineLevel(UUID id){ return data.getInt(base(id)+".mine.level", 0); }
    public int getFarmLevel(UUID id){ return data.getInt(base(id)+".farm.level", 0); }
    public void setMineLevel(UUID id, int v){ data.set(base(id)+".mine.level", v); save(); }
    public void setFarmLevel(UUID id, int v){ data.set(base(id)+".farm.level", v); save(); }
}
