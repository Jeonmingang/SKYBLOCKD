package com.signition.samskybridge.upgrade;

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
            sizeLore.add("&7현재 섬 크기: &f{range} 블럭");
            sizeLore.add("&7다음 단계: &a{next} 블럭");
            sizeLore.add("&7요구 레벨: &bLv.{reqLevel}");
            sizeLore.add("&7필요 금액: &a{cost}");
            sizeLore.add("&7업그레이드 레벨: &d{stepNow}/{stepMax}");
            sizeLore.add("&8클릭: 업그레이드");
        }
        String[] sizeLoreArr = new String[sizeLore.size()];
        for (int i=0;i<sizeLore.size();i++){
            sizeLoreArr[i] = sizeLore.get(i)
                    .replace("{range}", String.valueOf(plugin.getConfig().getBoolean("upgrade.size.display-as-blocks", true) ? sizeNow*2 : sizeNow))
                    .replace("{next}", String.valueOf(plugin.getConfig().getBoolean("upgrade.size.display-as-blocks", true) ? sizeNext*2 : sizeNext))
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

    private java.util.List<Integer> stepsRadius(){
        java.util.List<Integer> blocks = plugin.getConfig().getIntegerList("upgrade.size.steps-blocks");
        if (blocks != null && !blocks.isEmpty()){
            java.util.List<Integer> r = new java.util.ArrayList<>();
            for (Integer b : blocks){ r.add(Math.max(1, (int)Math.round(b / 2.0))); }
            return r;
        }
        return stepsRadius();
    }

    private int baseRange(){
    int blocks = plugin.getConfig().getInt("upgrade.size.base-blocks", -1);
    if (blocks > 0) return Math.max(1, (int)Math.round(blocks / 2.0));
    return plugin.getConfig().getInt("upgrade.size.base-range", plugin.getConfig().getInt("upgrade.size.base-radius", 25));
}
    private int perRange(){
    int blk = plugin.getConfig().getInt("upgrade.size.per-level-blocks", -1);
    if (blk > 0) return Math.max(1, (int)Math.round(blk / 2.0));
    return plugin.getConfig().getInt("upgrade.size.per-level", 10);
}
    private double costSize(int current){
        List<Integer> steps = stepsRadius();
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
        List<Integer> steps = stepsRadius();
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
        List<Integer> steps = stepsRadius();
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(current);
            if (idx >= 0 && idx+1 < steps.size()) return steps.get(idx+1);
            return current;
        }
        return current + perRange();
    }
    private int currentSizeStep(int value){
        List<Integer> steps = stepsRadius();
        if (steps != null && !steps.isEmpty()){
            int idx = steps.indexOf(value);
            return (idx>=0? idx+1 : 1);
        }
        if (value < baseRange()) return 1;
        return 1 + (value - baseRange()) / Math.max(1, perRange());
    }
    private int maxSizeStep(){
        List<Integer> steps = stepsRadius();
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
}
