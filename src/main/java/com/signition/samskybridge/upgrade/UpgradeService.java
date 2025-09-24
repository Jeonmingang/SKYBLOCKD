package com.signition.samskybridge.upgrade;
import com.signition.samskybridge.util.Text;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeService {
// === Helpers for UI summaries ===
    private java.util.Map<String, Double> parseDist(java.util.List<String> defs){
        java.util.Map<String, Double> out = new java.util.LinkedHashMap<>();
        if (defs == null) return out;
        for (String s : defs){
            try{
                String[] sp = s.trim().split(":");
                String name = sp[0].trim();
                double v = (sp.length>1)? Double.parseDouble(sp[1].trim()) : 1.0;
                out.put(name, out.getOrDefault(name, 0.0)+v);
            }catch(Throwable ignore){}
        }
        double sum = 0.0; for (double v: out.values()) sum += v;
        if (sum <= 0) return out;
        for (java.util.Map.Entry<String,Double> e: out.entrySet()){
            out.put(e.getKey(), (e.getValue()/sum)*100.0);
        }
        return out;
    }
    private String top3Delta(java.util.Map<String, Double> now, java.util.Map<String, Double> next){
        java.util.Set<String> keys = new java.util.HashSet<>();
        keys.addAll(now.keySet()); keys.addAll(next.keySet());
        java.util.List<String> items = new java.util.ArrayList<>(keys);
        items.sort((a,b)-> Double.compare(next.getOrDefault(b,0.0), next.getOrDefault(a,0.0)));
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (String k : items){
            if (cnt >= 3) break;
            double a = Math.round(now.getOrDefault(k,0.0));
            double b = Math.round(next.getOrDefault(k,0.0));
            double d = b - a;
            if (cnt > 0) sb.append(" ");
            sb.append(k).append(d>=0? " +" : " ").append((int)d).append("%");
            cnt++;
        }
        return sb.toString();
    }

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final VaultHook vault;
    private final BentoSync bento;

    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault, BentoSync bento){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.vault = vault;
        this.bento = bento;
    }
    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault){
        this(plugin, store, level, vault, plugin.getBento());
    }


    /* ===================== GUI ===================== */
    public void openGui(Player p){
        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드"));
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());

        int slotSize = plugin.getConfig().getInt("upgrade.gui.slots.size", 14);
        int slotTeam = plugin.getConfig().getInt("upgrade.gui.slots.team", 12);

        // Size pane
        int sizeNow = is.getSize();
        int sizeNext = nextSizeValue(sizeNow);
        int sizeNeedLv = needLevelSize(sizeNow);
        long sizeCost = Math.round(costSize(sizeNow));
        int sizeStepNow = currentSizeStep(sizeNow);
        int sizeStepMax = maxSizeStep();

        List<String> sizeLore = plugin.getConfig().getStringList("upgrade.size.lore");
        if (sizeLore == null || sizeLore.isEmpty()){
            sizeLore = new ArrayList<>();
            sizeLore.add("&7현재 보호반경: &f{range} 블럭");
            sizeLore.add("&7다음 단계: &a{next} 블럭");
            sizeLore.add("&7요구 레벨: &bLv.{reqLevel}");
            sizeLore.add("&7필요 금액: &a{cost}");
            sizeLore.add("&7업그레이드 레벨: &d{stepNow}/{stepMax}");
            sizeLore.add("&8클릭: 업그레이드");
        }
        String[] sizeLoreArr = new String[sizeLore.size()];
        for (int i=0;i<sizeLore.size();i++){
            sizeLoreArr[i] = sizeLore.get(i)
                    .replace("{range}", String.valueOf(sizeNow))
                    .replace("{next}", String.valueOf(sizeNext))
                    .replace("{reqLevel}", String.valueOf(sizeNeedLv))
                    .replace("{cost}", String.valueOf(sizeCost))
                    .replace("{stepNow}", String.valueOf(sizeStepNow))
                    .replace("{stepMax}", sizeStepMax==1000? "∞" : String.valueOf(sizeStepMax));
        }
        inv.setItem(slotSize, named(new ItemStack(Material.MAP),
                plugin.getConfig().getString("upgrade.size.title","&e섬 크기 업그레이드"),
                sizeLoreArr));

        // Team pane
        int teamNow = is.getTeamMax();
        int teamNext = nextTeamValue(teamNow);
        int teamNeedLv = needLevelTeam(teamNow);
        long teamCost = Math.round(costTeam(teamNow));
        int teamStepNow = currentTeamStep(teamNow);
        int teamStepMax = maxTeamStep();

        List<String> teamLore = plugin.getConfig().getStringList("upgrade.team.lore");
        if (teamLore == null || teamLore.isEmpty()){
            teamLore = new ArrayList<>();
            teamLore.add("&7현재 한도: &f{current}명");
            teamLore.add("&7다음 단계: &a{next}명");
            teamLore.add("&7요구 레벨: &bLv.{reqLevel}");
            teamLore.add("&7필요 금액: &a{cost}");
            teamLore.add("&7업그레이드 레벨: &d{stepNow}/{stepMax}");
            teamLore.add("&8클릭: 업그레이드");
        }
        String[] teamLoreArr = new String[teamLore.size()];
        for (int i=0;i<teamLore.size();i++){
            teamLoreArr[i] = teamLore.get(i)
                    .replace("{current}", String.valueOf(teamNow))
                    .replace("{next}", String.valueOf(teamNext))
                    .replace("{reqLevel}", String.valueOf(teamNeedLv))
                    .replace("{cost}", String.valueOf(teamCost))
                    .replace("{stepNow}", String.valueOf(teamStepNow))
                    .replace("{stepMax}", teamStepMax==1000? "∞" : String.valueOf(teamStepMax));
        }
        
        // Mine upgrade pane
        int slotMine = plugin.getConfig().getInt("upgrade.gui.slots.mine", 24);
        int mineLv = plugin.getFeatures().getMineLevel(is.getId());
        int mineMax = plugin.getConfig().getInt("features.mine.max-level", 5);
        java.util.Map<String,Double> distNow = parseDist(plugin.getConfig().getStringList("features.mine.ores."+mineLv));
        java.util.Map<String,Double> distNext = parseDist(plugin.getConfig().getStringList("features.mine.ores."+(mineLv+1)));
        String mineChanges = top3Delta(distNow, distNext);
        java.util.List<String> mineLore = plugin.getConfig().getStringList("features.mine.lore");
        if (mineLore == null || mineLore.isEmpty()){
            mineLore = new java.util.ArrayList<>();
            mineLore.add("&7현재 레벨: &f{now}/{max}");
            mineLore.add("&7요구 레벨: &bLv.{need}");
            mineLore.add("&7가격: &a{cost}");
            mineLore.add("&7다음 변화: &f{changes}");
            mineLore.add("&8클릭: 업그레이드");
        }
        int needMine = plugin.getConfig().getInt("features.mine.require.base", 3) + mineLv * plugin.getConfig().getInt("features.mine.require.per-step", 1);
        double costMine = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), mineLv);
        String[] mineLoreArr = new String[mineLore.size()];
        for (int i=0;i<mineLore.size();i++){
            mineLoreArr[i] = mineLore.get(i)
                .replace("{now}", String.valueOf(mineLv)).replace("{changes}", (mineChanges==null?"":mineChanges))
                .replace("{max}", String.valueOf(mineMax))
                .replace("{need}", String.valueOf(needMine))
                .replace("{cost}", String.valueOf((long)costMine));
        }
        inv.setItem(slotMine, named(new ItemStack(Material.IRON_PICKAXE),
                plugin.getConfig().getString("features.mine.title","&6광산 업그레이드"),
                mineLoreArr));

        // Farm upgrade pane
        int slotFarm = plugin.getConfig().getInt("upgrade.gui.slots.farm", 22);
        int farmLv = plugin.getFeatures().getFarmLevel(is.getId());
        int farmMax = plugin.getConfig().getInt("features.farm.max-level", 5);
        java.util.Map<String,Double> fNow = parseDist(plugin.getConfig().getStringList("features.farm.crops."+farmLv));
        java.util.Map<String,Double> fNext = parseDist(plugin.getConfig().getStringList("features.farm.crops."+(farmLv+1)));
        String farmChanges = top3Delta(fNow, fNext);
        int radiusNow = plugin.getFeatures().getFarmInstallRadius(farmLv);
        int radiusNext = plugin.getFeatures().getFarmInstallRadius(farmLv+1);
        int farmLv = plugin.getFeatures().getFarmLevel(is.getId());
        int farmMax = plugin.getConfig().getInt("features.farm.max-level", 5);
        java.util.List<String> farmLore = plugin.getConfig().getStringList("features.farm.lore");
        if (farmLore == null || farmLore.isEmpty()){
            farmLore = new java.util.ArrayList<>();
            farmLore.add("&7현재 레벨: &f{now}/{max}");
            farmLore.add("&7요구 레벨: &bLv.{need}");
            farmLore.add("&7가격: &a{cost}");
            farmLore.add("&7크기: &f{sizeNow}&7→&a{sizeNext}");
            farmLore.add("&7다음 변화: &f{changes}");
            farmLore.add("&8클릭: 업그레이드");
            farmLore.add("&7현재 레벨: &f{now}/{max}");
            farmLore.add("&7요구 레벨: &bLv.{need}");
            farmLore.add("&7가격: &a{cost}");
            farmLore.add("&8클릭: 업그레이드");
        }
        int needFarm = plugin.getConfig().getInt("features.farm.require.base", 2) + farmLv * plugin.getConfig().getInt("features.farm.require.per-step", 1);
        double costFarm = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), farmLv);
        String[] farmLoreArr = new String[farmLore.size()];
        for (int i=0;i<farmLore.size();i++){
            farmLoreArr[i] = farmLore.get(i)
                .replace("{now}", String.valueOf(farmLv)).replace("{changes}", (farmChanges==null?"":farmChanges)).replace("{sizeNow}", String.valueOf((radiusNow*2+1)+"x"+(radiusNow*2+1))).replace("{sizeNext}", String.valueOf((radiusNext*2+1)+"x"+(radiusNext*2+1)))
                .replace("{max}", String.valueOf(farmMax))
                .replace("{need}", String.valueOf(needFarm))
                .replace("{cost}", String.valueOf((long)costFarm));
        }
        inv.setItem(slotFarm, named(new ItemStack(Material.WHEAT),
                plugin.getConfig().getString("features.farm.title","&6농장 업그레이드"),
                farmLoreArr));
    inv.setItem(slotTeam, named(new ItemStack(Material.PLAYER_HEAD),
                plugin.getConfig().getString("upgrade.team.title","&b섬 인원 업그레이드"),
                teamLoreArr));

        // XP Purchase Item injection
        int slotXp = plugin.getConfig().getInt("upgrade.xp.slots.item", 22);
        String xpTitle = plugin.getConfig().getString("upgrade.xp.title","&d경험치 구매");
        java.util.List<String> xpLore = plugin.getConfig().getStringList("upgrade.xp.lore");
        if (xpLore == null || xpLore.isEmpty()){
            xpLore = new java.util.ArrayList<>();
            xpLore.add("&7구매량: &f{amount} XP");
            xpLore.add("&7가격: &a{price}");
            xpLore.add("&8클릭: 구매");
        }
        String[] xpLoreArr = new String[xpLore.size()];
        long amount = plugin.getConfig().getLong("upgrade.xp.amount", 100);
        long price  = plugin.getConfig().getLong("upgrade.xp.price", 1000);
        for (int i=0;i<xpLore.size();i++){
            xpLoreArr[i] = xpLore.get(i).replace("{amount}", String.valueOf(amount)).replace("{price}", String.valueOf(price));
        }
        inv.setItem(slotXp, named(new org.bukkit.inventory.ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE), xpTitle, xpLoreArr));
        p.openInventory(inv);
    }

    public void click(Player p, int slot, boolean shift){
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        if (slot == plugin.getConfig().getInt("upgrade.gui.slots.size", 14)){
            int before = is.getSize();
            int next   = nextSizeValue(before);
            int need   = needLevelSize(before);
            if (is.getLevel() < need){
                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                p.sendMessage(msg);
                return;
            }
            double cost = costSize(before);
            if (!vault.withdraw(p.getName(), cost)){
                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                p.sendMessage(msg);
                return;
            }
            is.setSize(next);
            store.save();
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.size-success","섬 크기 업그레이드").replace("<radius>", String.valueOf(next)));
            try { if (bento != null && bento.isEnabled()) { bento.applyRangeInstant(p, next); } } catch (Throwable t){ plugin.getLogger().warning("BentoSync range failed: " + t.getMessage()); }
            if (plugin.getConfig().getBoolean("upgrade.sync.bento.range", false)){
                try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "is admin range " + p.getName() + " " + next); } catch (Throwable ignored){}
            }
        } else if (slot == plugin.getConfig().getInt("upgrade.gui.slots.team", 12)){
            int before = is.getTeamMax();
            int next   = nextTeamValue(before);
            int need   = needLevelTeam(before);
            if (is.getLevel() < need){
                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                p.sendMessage(msg);
                return;
            }
            double cost = costTeam(before);
            if (!vault.withdraw(p.getName(), cost)){
                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                p.sendMessage(msg);
                return;
            }
            is.setTeamMax(next);
            store.save();
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.team-success","섬 인원 업그레이드").replace("<max>", String.valueOf(next)));
            try {
                if (bento != null && bento.isEnabled()){
                    bento.applyTeamMax(p, next);
                }
            } catch (Throwable t){
                plugin.getLogger().warning("BentoSync team-size failed: " + t.getMessage());
            }
        }
    
                if (slot == plugin.getConfig().getInt("upgrade.xp.slots.item", 22)){
                    long buyAmount = plugin.getConfig().getLong("upgrade.xp.amount", 50);
                    double buyCost = plugin.getConfig().getDouble("upgrade.xp.cost", 1000.0);
                    if (!vault.withdraw(p.getName(), buyCost)){
                        p.sendMessage(Text.color(plugin.getConfig().getString("messages.not-enough-money","&c잔액이 부족합니다. 필요: &f<cost>").replace("<cost>", String.valueOf((long)buyCost))));
                        return;
                    }
                    level.applyXpPurchase(is, buyAmount);
                    store.save();
                    long needXp = level.requiredXpForLevel(is.getLevel());
                    long remain = Math.max(0, needXp - is.getXp());
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.xp.bought","&a경험치 &f<amount> &a를 구매했습니다. 남은 필요치: &e<remain>")
                        .replace("<amount>", String.valueOf(buyAmount))
                        .replace("<remain>", String.valueOf(Math.max(0, remain)))));
                    return;
                }
            
                if (slot == plugin.getConfig().getInt("upgrade.gui.slots.mine", 24)){
                    plugin.getFeatures().upgradeMine(p);
                    return;
                }
                if (slot == plugin.getConfig().getInt("upgrade.gui.slots.farm", 25)){
                    plugin.getFeatures().upgradeFarm(p);
                    return;
                }
}

    
    /* ===================== Helpers & math ===================== */

    private ItemStack named(ItemStack base, String name, String[] lore){
        ItemMeta im = base.getItemMeta();
        im.setDisplayName(com.signition.samskybridge.util.Text.color(name));
        List<String> lc = new ArrayList<>();
        for (String s : lore) lc.add(com.signition.samskybridge.util.Text.color(s));
        im.setLore(lc);
        base.setItemMeta(im);
        return base;
    }

    // SIZE
    private int baseRange(){ return plugin.getConfig().getInt("upgrade.size.base-range", 50); }
    private int perRange(){ return plugin.getConfig().getInt("upgrade.size.per-level", 10); }
    private double costSize(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){
            idx = Math.max(0, steps.indexOf(current));
        } else {
            idx = Math.max(0, (current - baseRange()) / Math.max(1, perRange()));
        }
        double base = plugin.getConfig().getDouble("upgrade.size.cost-base", 20000.0);
        double mul  = plugin.getConfig().getDouble("upgrade.size.cost-multiplier", 1.25);
        return base * Math.pow(mul, idx);
    }
    private int needLevelSize(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){
            idx = Math.max(0, steps.indexOf(current));
        } else {
            idx = Math.max(0, (current - baseRange()) / Math.max(1, perRange()));
        }
        int base = plugin.getConfig().getInt("upgrade.size.required-level-base", 2);
        int step = plugin.getConfig().getInt("upgrade.size.required-level-step", 2);
        return base + idx * step;
    }
    private int nextSizeValue(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(current);
            if (idx >= 0 && idx+1 < steps.size()) return steps.get(idx+1);
            return current;
        }
        return current + perRange();
    }
    private int currentSizeStep(int value){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(value);
            return (idx>=0? idx+1 : 1);
        }
        if (value < baseRange()) return 1;
        return 1 + (value - baseRange()) / Math.max(1, perRange());
    }
    private int maxSizeStep(){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()) return steps.size();
        return 1000;
    }

    // TEAM
    private int baseMembers(){ return plugin.getConfig().getInt("upgrade.team.base-members", 2); }
    private int perMembers(){ return plugin.getConfig().getInt("upgrade.team.per-level", 1); }
    private double costTeam(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){
            idx = Math.max(0, steps.indexOf(current));
        } else {
            idx = Math.max(0, (current - baseMembers()) / Math.max(1, perMembers()));
        }
        double base = plugin.getConfig().getDouble("upgrade.team.cost-base", 15000.0);
        double mul  = plugin.getConfig().getDouble("upgrade.team.cost-multiplier", 1.35);
        return base * Math.pow(mul, idx);
    }
    private int needLevelTeam(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){
            idx = Math.max(0, steps.indexOf(current));
        } else {
            idx = Math.max(0, (current - baseMembers()) / Math.max(1, perMembers()));
        }
        int base = plugin.getConfig().getInt("upgrade.team.required-level-base", 2);
        int step = plugin.getConfig().getInt("upgrade.team.required-level-step", 2);
        return base + idx * step;
    }
    private int nextTeamValue(int current){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(current);
            if (idx >= 0 && idx+1 < steps.size()) return steps.get(idx+1);
            return current;
        }
        return current + perMembers();
    }
    private int currentTeamStep(int value){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(value);
            return (idx>=0? idx+1 : 1);
        }
        if (value < baseMembers()) return 1;
        return 1 + (value - baseMembers()) / Math.max(1, perMembers());
    }
    private int maxTeamStep(){
        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()) return steps.size();
        return 1000;
    }


// --- injected: upgrades-ui-lore-and-owner-check ---

private void appendNextLevelLore(org.bukkit.inventory.meta.ItemMeta meta, String typeKey, int nextLevel) {
    if (meta == null) return;
    if (!this.plugin.getConfig().getBoolean("upgrades-ui.show-next-level-lore", true)) return;
    org.bukkit.configuration.ConfigurationSection lvl = this.plugin.getConfig()
        .getConfigurationSection("upgrades." + typeKey + ".levels." + nextLevel);
    if (lvl == null) return;
    java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<String>();
    lore.add(Text.color(this.plugin.getConfig().getString("upgrades-ui.templates." + typeKey + ".next-header", "&e다음 레벨 효과:")));
    java.util.List<String> add = lvl.getStringList("lore");
    for (String line : add) lore.add(Text.color(line));
    meta.setLore(lore);
}
private void appendCurrentLevelLine(org.bukkit.inventory.meta.ItemMeta meta, String typeKey, int curLevel) {
    if (meta == null) return;
    String fmt = this.plugin.getConfig().getString("upgrades-ui.templates." + typeKey + ".current", "&7현재 레벨: &f{level}");
    String line = fmt.replace("{level}", String.valueOf(curLevel));
    java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<String>();
    lore.add(Text.color(line));
    meta.setLore(lore);
}
private boolean isIslandOwner(org.bukkit.entity.Player p) {
    try {
        return plugin.getFeatureService().islandOwner(p);
    } catch (Throwable t) {
        return p.hasPermission("samskybridge.island.owner");
    }
}

}
