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

import java.util.Arrays;

public class UpgradeService {
    // --- Added in v0.5.1: step-based upgrade support and progress helpers ---
    private java.util.List<Integer> sizeSteps(){
        java.util.List<Integer> steps = new java.util.ArrayList<>();
        try {
            java.util.List<?> raw = plugin.getConfig().getList("upgrade.size.steps");
            if (raw != null) {
                for (Object o : raw) { try { steps.add(Integer.parseInt(String.valueOf(o))); } catch (Exception ignored) {} }
                java.util.Collections.sort(steps);
            }
        } catch (Throwable ignored){}
        return steps;
    }
    private java.util.List<Integer> teamSteps(){
        java.util.List<Integer> steps = new java.util.ArrayList<>();
        try {
            java.util.List<?> raw = plugin.getConfig().getList("upgrade.team.steps");
            if (raw != null) {
                for (Object o : raw) { try { steps.add(Integer.parseInt(String.valueOf(o))); } catch (Exception ignored) {} }
                java.util.Collections.sort(steps);
            }
        } catch (Throwable ignored){}
        return steps;
    }
    private int baseRange(){ return plugin.getConfig().getInt("upgrade.size.base-range", 50); }
    private int perRange(){ return plugin.getConfig().getInt("upgrade.size.per-level", 10); }
    private int baseMembers(){ return plugin.getConfig().getInt("upgrade.team.base-members", 4); }
    private int perMembers(){ return plugin.getConfig().getInt("upgrade.team.per-level", 1); }

    private int currentSizeStep(int current){
        java.util.List<Integer> steps = sizeSteps();
        if (!steps.isEmpty()) {
            int idx = 0;
            for (int i=0;i<steps.size();i++){ if (steps.get(i) <= current) idx = i; }
            return idx;
        }
        int base = baseRange();
        int per = perRange();
        if (current < base) return 0;
        return Math.max(0, (current - base) / per);
    }
    private int currentTeamStep(int current){
        java.util.List<Integer> steps = teamSteps();
        if (!steps.isEmpty()) {
            int idx = 0;
            for (int i=0;i<steps.size();i++){ if (steps.get(i) <= current) idx = i; }
            return idx;
        }
        int base = baseMembers();
        if (current < base) return 0;
        return Math.max(0, current - base);
    }
    private int maxSizeStep(){
        java.util.List<Integer> steps = sizeSteps();
        if (!steps.isEmpty()) return Math.max(0, steps.size()-1);
        return 1000; // virtually unlimited when formula-based
    }
    private int maxTeamStep(){
        java.util.List<Integer> steps = teamSteps();
        if (!steps.isEmpty()) return Math.max(0, steps.size()-1);
        return 1000;
    }
    private int nextSizeValue(int current){
        java.util.List<Integer> steps = sizeSteps();
        if (!steps.isEmpty()){
            for (int i=0;i<steps.size();i++){
                if (steps.get(i) > current) return steps.get(i);
            }
            return current; // already max
        }
        return current + perRange();
    }
    private int nextTeamValue(int current){
        java.util.List<Integer> steps = teamSteps();
        if (!steps.isEmpty()){
            for (int i=0;i<steps.size();i++){
                if (steps.get(i) > current) return steps.get(i);
            }
            return current;
        }
        return current + perMembers();
    }
    
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final VaultHook vault;
    private final BentoSync bento;

    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.vault = vault;
        this.bento = plugin.getBento();
    }

    public void openGui(Player p){
        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드"));
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        int slotSize = plugin.getConfig().getInt("upgrade.gui.slots.size", 14);
        int slotTeam = plugin.getConfig().getInt("upgrade.gui.slots.team", 12);

        // compute steps & next values
        int sizeNow = is.getSize();
        int sizeNext = nextSizeValue(sizeNow);
        int sizeNeedLv = needLevelSize(sizeNow);
        long sizeCost = Math.round(costSize(sizeNow));
        int sizeStepNow = currentSizeStep(sizeNow);
        int sizeStepMax = maxSizeStep();
        String sizeTitle = plugin.getConfig().getString("upgrade.size.title","&e섬 크기 업그레이드");
        java.util.List<String> sizeLore = plugin.getConfig().getStringList("upgrade.size.lore");
        if (sizeLore == null || sizeLore.isEmpty()){
            sizeLore = java.util.Arrays.asList(
                "&7현재 보호반경: &f{range} 블럭",
                "&7다음 단계: &a{next} 블럭",
                "&7요구 레벨: &bLv.{reqLevel}",
                "&7필요 금액: &a{cost}",
                "&7업그레이드 레벨: &d{stepNow}/{stepMax}",
                "&8클릭: 업그레이드"
            );
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
        inv.setItem(slotSize, named(new ItemStack(Material.WHITE_STAINED_GLASS),
                plugin.getConfig().getString("upgrade.size.title","&e섬 크기 업그레이드"),
                sizeLoreArr));

        int teamNow = is.getTeamMax();
        int teamNext = nextTeamValue(teamNow);
        int teamNeedLv = needLevelTeam(teamNow);
        long teamCost = Math.round(costTeam(teamNow));
        int teamStepNow = currentTeamStep(teamNow);
        int teamStepMax = maxTeamStep();
        java.util.List<String> teamLore = plugin.getConfig().getStringList("upgrade.team.lore");
        if (teamLore == null || teamLore.isEmpty()){
            teamLore = java.util.Arrays.asList(
                "&7현재 한도: &f{current}명",
                "&7다음 단계: &a{next}명",
                "&7요구 레벨: &bLv.{reqLevel}",
                "&7필요 금액: &a{cost}",
                "&7업그레이드 레벨: &d{stepNow}/{stepMax}",
                "&8클릭: 업그레이드"
            );
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
        p.openInventory(inv);
    }

    private ItemStack named(ItemStack it, String name, String[] lores){
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
        im.setLore(Arrays.asList(lores));
        it.setItemMeta(im);
        return it;
    }

    public void click(Player p, int slot, boolean shift){
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        if (slot == plugin.getConfig().getInt("upgrade.gui.slots.size", 14)){
            int before = is.getSize();
            int next = nextSizeValue(before);
            int need = needLevelSize(before);
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
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.size-success","섬 크기 업그레이드").replace("<radius>", String.valueOf(next)));
            // optional Bento range sync
            if (plugin.getConfig().getBoolean("upgrade.sync.bento.range", false)){
                try { org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "is admin range " + p.getName() + " " + next); } catch (Throwable ignored){}
            }
        } else if (slot == plugin.getConfig().getInt("upgrade.gui.slots.team", 12)){
            int before = is.getTeamMax();
            int next = nextTeamValue(before);
            int need = needLevelTeam(before);
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
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.team-success","섬 인원 업그레이드").replace("<max>", String.valueOf(next)));
            try { if (bento != null && bento.isEnabled()) bento.applyTeamSize(p, next); } catch (Throwable t){ plugin.getLogger().warning("BentoSync team-size failed: "+t.getMessage()); }
        }
    }
            } else {
                double cost = costSize(before);
                if (!vault.withdraw(p.getName(), cost)){
                    String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                    p.sendMessage(msg);
                    return;
                }
            }
            is.setSize(next);
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.size-up-success","섬 크기 업그레이드").replace("<radius>", String.valueOf(next)));
            // Barrier blocked (removed)
            try { if (bento != null && bento.isEnabled()) bento.applyRangeInstant(p, next); } catch (Throwable t){ plugin.getLogger().warning("BentoSync range failed: "+t.getMessage()); }
        } else if (slot == plugin.getConfig().getInt("upgrade.gui.slots.team", 12)){
            int before = is.getTeamMax();
            int next = nextTeamValue(before);
            if (shift){
                int need = needLevelTeam(before);
                if (is.getLevel() < need){
                    String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                    p.sendMessage(msg);
                    return;
                }
            } else {
                double cost = costTeam(before);
                if (!vault.withdraw(p.getName(), cost)){
                    String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                    p.sendMessage(msg);
                    return;
                }
            }
            is.setTeamMax(next);
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.team-up-success","팀원 업그레이드").replace("<count>", String.valueOf(next)));
            try { if (bento != null && bento.isEnabled()) bento.applyTeamMax(p, next); } catch (Throwable t){ plugin.getLogger().warning("BentoSync team failed: "+t.getMessage()); }
        }
    }

    private double costSize(int currentRadius){
        double base = plugin.getConfig().getDouble("economy.costs.size.base", 10000.0);
        double mul = plugin.getConfig().getDouble("economy.costs.size.multiplier", 1.25);
        int steps = Math.max(0, (currentRadius - 50) / 10);
        return Math.round(base * Math.pow(mul, steps));
    }
    private double costTeam(int currentTeam){
        double base = plugin.getConfig().getDouble("economy.costs.team.base", 5000.0);
        double mul = plugin.getConfig().getDouble("economy.costs.team.multiplier", 1.5);
        int steps = Math.max(0, currentTeam - 1);
        return Math.round(base * Math.pow(mul, steps));
    }
    private int needLevelSize(int currentRadius){
        int base = plugin.getConfig().getInt("upgrade.size.required-level-base", plugin.getConfig().getInt("economy.costs.size.level-required-base", 5));
        int step = plugin.getConfig().getInt("upgrade.size.required-level-step", plugin.getConfig().getInt("economy.costs.size.level-required-step", 2));
        int steps = Math.max(0, (currentRadius - 50) / 10);
        return base + step * steps;
    }
    private int needLevelTeam(int currentTeam){
        int base = plugin.getConfig().getInt("upgrade.team.required-level-base", plugin.getConfig().getInt("economy.costs.team.level-required-base", 3));
        int step = plugin.getConfig().getInt("upgrade.team.required-level-step", plugin.getConfig().getInt("economy.costs.team.level-required-step", 1));
        int steps = Math.max(0, currentTeam - 1);
        return base + step * steps;
    }
}