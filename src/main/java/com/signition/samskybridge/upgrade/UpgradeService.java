package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UpgradeService {
    private final Main plugin;
    private final LevelService level;

    public UpgradeService(Main plugin, LevelService level){
        this.plugin = plugin;
        this.level = level;
    }
    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault){
        this(plugin, level);
    }

    private java.util.List<String> getList(String... paths){
        for (String p : paths){
            java.util.List<String> v = plugin.getConfig().getStringList(p);
            if (v != null && !v.isEmpty()) return v;
        }
        return new java.util.ArrayList<>();
    }
    private String getString(String def, String... paths){
        for (String p : paths){
            if (plugin.getConfig().isString(p)){
                String s = plugin.getConfig().getString(p);
                if (s != null) return s;
            }
        }
        return def;
    }
    private java.util.List<String> applyPlaceholders(java.util.List<String> tpl, java.util.Map<String,String> ctx){
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String line : tpl){
            for (java.util.Map.Entry<String,String> e : ctx.entrySet()){
                line = line.replace(e.getKey(), e.getValue());
            }
            if (ctx.containsKey("{현재크기}")) {
                line = line.replace("<current>", ctx.get("{현재크기}"))
                           .replace("<next>", ctx.get("{다음크기}"))
                           .replace("<level>", ctx.get("{크기필요레벨}"))
                           .replace("<cost>", ctx.get("{크기비용}"));
            }
            if (ctx.containsKey("{현재인원}")) {
                line = line.replace("<current>", ctx.get("{현재인원}"))
                           .replace("<next>", ctx.get("{다음인원}"))
                           .replace("<level>", ctx.get("{인원필요레벨}"))
                           .replace("<cost>", ctx.get("{인원비용}"));
            }
            out.add(com.signition.samskybridge.util.Text.color(line));
        }
        return out;
    }

    public void open(Player p){
        IslandData __is = level.getIslandOf(p);
        if (!isLeader(p, __is)) {
            p.sendMessage(Text.color("&c섬장만 업그레이드를 사용할 수 있습니다."));
            return;
        }
        String raw = plugin.getConfig().getString("upgrade.gui.title-upgrade", null);
        if (raw == null) raw = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        String title = Text.color(raw);
        int invSize = Math.max(9, Math.min(54, (plugin.getConfig().getInt("upgrade.gui.inventory-size", 27) / 9) * 9));
        Inventory inv = Bukkit.createInventory(p, invSize, title);

        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp",   22);

        inv.setItem(sizeSlot, buildSizeItem(p));
        inv.setItem(teamSlot, buildTeamItem(p));
        inv.setItem(xpSlot,   buildXpItem(p));
        p.openInventory(inv);
    }

    public void openUpgradeGui(Player p){ open(p); }

    public void click(Player p, int slot, boolean shift){
        IslandData __is = level.getIslandOf(p);
        if (!isLeader(p, __is)) {
            p.sendMessage(Text.color("&c섬장만 업그레이드를 사용할 수 있습니다."));
            p.closeInventory();
            return;
        }
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp",   22);
        if (slot == sizeSlot) { tryUpgradeSize(p); return; }
        if (slot == teamSlot) { tryUpgradeMemberCap(p); return; }
        if (slot == xpSlot)   { tryPurchaseXp(p); return; }
    }

    private ItemStack buildSizeItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = is != null ? is.getSize() : 0;

        int nextRange = current;
        int reqLevel = 0;
        double cost = 0.0;

        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.size");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                org.bukkit.configuration.ConfigurationSection s = sec.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to   = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){
                    nextRange = to; reqLevel = need; cost = c; break;
                }
            }
        }

        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.matchMaterial(getString("MAP", "upgrade.gui.items.size.material", "size.material")));
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(com.signition.samskybridge.util.Text.color(getString("&a섬 크기 업그레이드", "upgrade.gui.items.size.name", "size.name")));
        java.util.List<String> tpl = getList("upgrade.gui.items.size.lore", "size.lore");
        java.util.Map<String,String> ctx = new java.util.HashMap<>();
        ctx.put("{현재크기}", String.valueOf(current));
        ctx.put("{다음크기}", String.valueOf(nextRange));
        ctx.put("{크기업그레이드레벨}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{크기필요레벨}", String.valueOf(reqLevel));
        ctx.put("{크기비용}", String.format("%,.0f", cost));
        meta.setLore(applyPlaceholders(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }
            }
        }

        ItemStack it = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("upgrade.gui.items.size.material","MAP")));
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.gui.items.size.name","&a섬 크기 업그레이드")));
        List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.size.lore");
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{현재크기}", String.valueOf(current));
        ctx.put("{다음크기}", String.valueOf(nextRange));
        ctx.put("{크기업그레이드레벨}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{크기필요레벨}", String.valueOf(reqLevel));
        ctx.put("{크기비용}", String.format("%,.0f", cost));
        ctx.put("{current}", String.valueOf(current));
        ctx.put("{next}", String.valueOf(nextRange));
        ctx.put("{level}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{need}", String.valueOf(reqLevel));
        ctx.put("{cost}", String.format("%,.0f", cost));
        meta.setLore(applyTemplate(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack buildTeamItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = is != null ? is.getTeamMax() : 0;

        int nextCap = current;
        int reqLevel = 0;
        double cost = 0.0;

        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.team");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                org.bukkit.configuration.ConfigurationSection s = sec.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to   = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){
                    nextCap = to; reqLevel = need; cost = c; break;
                }
            }
        }

        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.matchMaterial(getString("PLAYER_HEAD", "upgrade.gui.items.team.material", "members.material")));
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(com.signition.samskybridge.util.Text.color(getString("&a팀 인원 업그레이드", "upgrade.gui.items.team.name", "members.name")));
        java.util.List<String> tpl = getList("upgrade.gui.items.team.lore", "members.lore");
        java.util.Map<String,String> ctx = new java.util.HashMap<>();
        ctx.put("{현재인원}", String.valueOf(current));
        ctx.put("{다음인원}", String.valueOf(nextCap));
        ctx.put("{인원업그레이드레벨}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{인원필요레벨}", String.valueOf(reqLevel));
        ctx.put("{인원비용}", String.format("%,.0f", cost));
        meta.setLore(applyPlaceholders(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }
            }
        }

        ItemStack it = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("upgrade.gui.items.team.material","PLAYER_HEAD")));
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.gui.items.team.name","&a팀 인원 업그레이드")));
        List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.team.lore");
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{현재인원}", String.valueOf(current));
        ctx.put("{다음인원}", String.valueOf(nextCap));
        ctx.put("{인원업그레이드레벨}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{인원필요레벨}", String.valueOf(reqLevel));
        ctx.put("{인원비용}", String.format("%,.0f", cost));
        ctx.put("{current}", String.valueOf(current));
        ctx.put("{next}", String.valueOf(nextCap));
        ctx.put("{level}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{need}", String.valueOf(reqLevel));
        ctx.put("{cost}", String.format("%,.0f", cost));
        meta.setLore(applyTemplate(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack buildXpItem(Player p){
        long amount = plugin.getConfig().getLong("upgrade.xp.amount", 1000L);
        double cost = plugin.getConfig().getDouble("upgrade.xp.cost", 10000.0);
        int reqLevel = plugin.getConfig().getInt("upgrade.xp.need-level", 0);

        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.matchMaterial(getString("EXPERIENCE_BOTTLE", "upgrade.gui.items.xp.material", "xp-purchase.material")));
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(com.signition.samskybridge.util.Text.color(getString("&a경험치 구매", "upgrade.gui.items.xp.name", "xp-purchase.name")));
        java.util.List<String> tpl = getList("upgrade.gui.items.xp.lore", "xp-purchase.lore");
        if (tpl.isEmpty()){
            tpl = java.util.Arrays.asList("&7획득 경험치: &f{경험치구매량}", "&7요구 레벨: &f{경험치필요레벨}", "&7비용: &f{경험치비용}");
        }
        java.util.Map<String,String> ctx = new java.util.HashMap<>();
        ctx.put("{경험치구매량}", String.valueOf(amount));
        ctx.put("{경험치필요레벨}", String.valueOf(reqLevel));
        ctx.put("{경험치비용}", String.format("%,.0f", cost));
        meta.setLore(applyPlaceholders(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    private List<String> applyTemplate(List<String> tpl, Map<String,String> ctx){
        List<String> out = new ArrayList<>();
        if (tpl == null) return out;
        for (String line : tpl){
            for (Map.Entry<String,String> e : ctx.entrySet()){
                line = line.replace(e.getKey(), e.getValue());
            }
            out.add(Text.color(line));
        }
        return out;
    }

    private void tryUpgradeSize(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null){ p.sendMessage(Text.color("&c섬 데이터를 찾지 못했습니다.")); return; }

        int current = is.getSize();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.size");
        int next = current, needLevel = 0; double cost = 0.0;
        if (sec != null){
            for (String k : sec.getKeys(false)){
                ConfigurationSection s = sec.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to   = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){
                    next = to; needLevel = need; cost = c; break;
                }
            }
        }
        if (next <= current){ p.sendMessage(Text.color("&c더 이상 업그레이드할 단계가 없습니다.")); return; }
        if (is.getLevel() < needLevel){ p.sendMessage(Text.color("&c요구 레벨 " + needLevel + "이(가) 필요합니다.")); return; }

        try {
            net.milkbowl.vault.economy.Economy econ = plugin.getVault()!=null ? plugin.getVault().getEconomy() : null;
            if (econ != null && cost > 0){
                if (!econ.has(p, cost)){ p.sendMessage(Text.color("&c잔액이 부족합니다. &f" + String.format("%,.0f", cost))); return; }
                econ.withdrawPlayer(p, cost);
            }
        } catch (Throwable ignored){}

        is.setSize(next);
        plugin.getDataStore().put(is);

        try { plugin.getBento().applyRange(is.getOwner(), is.getSize()); } catch (Throwable ignored){}
        try { plugin.getRankingService().applyTab(p); } catch (Throwable ignored){}

        p.sendMessage(Text.color("&a섬 크기가 &f" + current + " &7→ &f" + next + " &a로 업그레이드 되었습니다."));
        open(p);
    }

    private void tryUpgradeMemberCap(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null){ p.sendMessage(Text.color("&c섬 데이터를 찾지 못했습니다.")); return; }

        int current = is.getTeamMax();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.team");
        int next = current, needLevel = 0; double cost = 0.0;
        if (sec != null){
            for (String k : sec.getKeys(false)){
                ConfigurationSection s = sec.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to   = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){
                    next = to; needLevel = need; cost = c; break;
                }
            }
        }
        if (next <= current){ p.sendMessage(Text.color("&c더 이상 업그레이드할 단계가 없습니다.")); return; }
        if (is.getLevel() < needLevel){ p.sendMessage(Text.color("&c요구 레벨 " + needLevel + "이(가) 필요합니다.")); return; }

        try {
            net.milkbowl.vault.economy.Economy econ = plugin.getVault()!=null ? plugin.getVault().getEconomy() : null;
            if (econ != null && cost > 0){
                if (!econ.has(p, cost)){ p.sendMessage(Text.color("&c잔액이 부족합니다. &f" + String.format("%,.0f", cost))); return; }
                econ.withdrawPlayer(p, cost);
            }
        } catch (Throwable ignored){}

        is.setTeamMax(next);
        plugin.getDataStore().put(is);

        try { plugin.getBento().applyTeamCap(is.getOwner(), is.getTeamMax()); } catch (Throwable ignored){}
        try { plugin.getRankingService().applyTab(p); } catch (Throwable ignored){}

        p.sendMessage(Text.color("&a섬 인원이 &f" + current + " &7→ &f" + next + " &a로 업그레이드 되었습니다."));
        open(p);
    }

    private void tryPurchaseXp(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null){ p.sendMessage(Text.color("&c섬 데이터를 찾지 못했습니다.")); return; }

        long amount = plugin.getConfig().getLong("upgrade.xp.amount", 1000L);
        double cost = plugin.getConfig().getDouble("upgrade.xp.cost", 10000.0);
        int reqLv   = plugin.getConfig().getInt("upgrade.xp.need-level", 0);

        if (is.getLevel() < reqLv){
            p.sendMessage(Text.color("&c요구 레벨 " + reqLv + "이(가) 필요합니다."));
            return;
        }

        try {
            net.milkbowl.vault.economy.Economy econ = plugin.getVault()!=null ? plugin.getVault().getEconomy() : null;
            if (econ != null && cost > 0){
                if (!econ.has(p, cost)){
                    p.sendMessage(Text.color("&c잔액이 부족합니다. &f" + String.format("%,.0f", cost)));
                    return;
                }
                econ.withdrawPlayer(p, cost);
            }
        } catch (Throwable ignored){}

        // add xp and handle level up internally
        level.addXp(p, amount);

        // feedback with gauge after purchase
        int lv = is.getLevel();
        long cur = is.getXp();
        long need = level.getNextXpRequirement(lv + 1);
        int percent = (need > 0 ? (int)Math.floor(Math.min(100.0, (cur * 100.0) / need)) : 0);
        p.sendMessage(Text.color("&a경험치 &f+" + amount + " &a구매 완료 (&f" + cur + "&7/&f" + need + "&a, &f" + percent + "%&a)"));

        try { plugin.getRankingService().applyTab(p); } catch (Throwable ignored){}
        open(p);
    }

    private boolean isLeader(org.bukkit.entity.Player p, IslandData is){
        if (is == null) return false;
        java.util.UUID owner = is.getOwner();
        return owner != null && owner.equals(p.getUniqueId());
    }
}