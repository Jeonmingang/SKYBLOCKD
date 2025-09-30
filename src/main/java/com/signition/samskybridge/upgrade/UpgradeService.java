package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.vault = vault;
    }

    public void open(Player p){
        String title = plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드");
        Inventory inv = Bukkit.createInventory(p, 27, title);
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 13);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 15);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);

        inv.setItem(sizeSlot, buildSizeItem(p));
        inv.setItem(teamSlot, buildTeamItem(p));
        inv.setItem(xpSlot,   buildXpItem(p));

        p.openInventory(inv);
    }

    public void click(Player p, int slot, boolean shift){
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 13);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 15);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);

        if (slot == sizeSlot) { tryUpgradeSize(p); return; }
        if (slot == teamSlot) { tryUpgradeMemberCap(p); return; }
        if (slot == xpSlot)   { buyXp(p); return; }
    }

    private ItemStack buildSizeItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = is != null ? is.getSize() : 0;

        int nextRange = current;
        int reqLevel = 0;
        double cost = 0.0;

        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("size.tiers");
        if (tiers == null) tiers = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
        if (tiers != null) {
            for (String k : tiers.getKeys(false)){
                int range = tiers.getInt(k + ".range", current);
                int need  = tiers.getInt(k + ".need", 0);
                double c  = tiers.getDouble(k + ".cost", 0.0);
                if (range > current) { nextRange = range; reqLevel = need; cost = c; break; }
            }
        } else {
            nextRange = plugin.getConfig().getInt("size.base-range", plugin.getConfig().getInt("upgrade.size.base-range", current+10));
            reqLevel  = plugin.getConfig().getInt("size.required", 1);
            cost      = plugin.getConfig().getDouble("size.cost", 10000.0);
        }

        ItemStack it = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Text.color(plugin.getConfig().getString("size.title", plugin.getConfig().getString("upgrade.size.title","&e섬 크기 업그레이드"))));
        List<String> lore = new ArrayList<>();
        lore.add(Text.color(plugin.getConfig().getString("size.lore.0","&7현재 보호반경: &f{range} 블럭").replace("{range}", String.valueOf(current))));
        lore.add(Text.color(plugin.getConfig().getString("size.lore.1","&7다음 단계: &a{next} 블럭").replace("{next}", String.valueOf(nextRange))));
        lore.add(Text.color(plugin.getConfig().getString("size.lore.2","&7요구 레벨: &bLv.{reqLevel}").replace("{reqLevel}", String.valueOf(reqLevel))));
        lore.add(Text.color(plugin.getConfig().getString("size.lore.3","&7필요 금액: &a{cost}").replace("{cost}", String.format("%,.0f", cost))));
        im.setLore(lore);
        it.setItemMeta(im);
        return it;
    }

    private ItemStack buildTeamItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = is != null ? is.getTeamMax() : 0;

        int nextMax = current;
        int reqLevel = 0;
        double cost = 0.0;

        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("members.tiers");
        if (tiers == null) tiers = plugin.getConfig().getConfigurationSection("upgrade.team.tiers");
        if (tiers != null) {
            for (String k : tiers.getKeys(false)){
                int max = tiers.getInt(k + ".max", current);
                int need  = tiers.getInt(k + ".need", 0);
                double c  = tiers.getDouble(k + ".cost", 0.0);
                if (max > current) { nextMax = max; reqLevel = need; cost = c; break; }
            }
        } else {
            nextMax = current + 1;
            reqLevel = 1;
            cost = 15000.0;
        }

        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Text.color(plugin.getConfig().getString("members.name", "&a섬 인원 업그레이드")));
        List<String> lore = new ArrayList<>();
        lore.add(Text.color("&7현재 한도: &f" + current + "명"));
        lore.add(Text.color("&7다음 단계: &a" + nextMax + "명"));
        lore.add(Text.color("&7요구 레벨: &bLv." + reqLevel));
        lore.add(Text.color("&7필요 금액: &a" + String.format("%,.0f", cost)));
        im.setLore(lore);
        it.setItemMeta(im);
        return it;
    }

    private ItemStack buildXpItem(Player p){
        int amt = plugin.getConfig().getInt("upgrade.xp.amount", 50);
        double cost = plugin.getConfig().getDouble("upgrade.xp.cost", 1000.0);
        ItemStack it = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Text.color("&b경험치 구매 (&f" + amt + "&b)"));
        List<String> lore = new ArrayList<>();
        lore.add(Text.color("&7가격: &a" + String.format("%,.0f", cost)));
        lore.add(Text.color("&8클릭: 구매"));
        im.setLore(lore);
        it.setItemMeta(im);
        return it;
    }

    public void tryUpgradeSize(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null) return;
        int current = is.getSize();

        int nextRange = -1;
        int reqLevel = 0;
        double cost = 0.0;
        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("size.tiers");
        if (tiers == null) tiers = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
        if (tiers != null) {
            for (String k : tiers.getKeys(false)){
                int range = tiers.getInt(k + ".range", current);
                int need  = tiers.getInt(k + ".need", 0);
                double c  = tiers.getDouble(k + ".cost", 0.0);
                if (range > current) { nextRange = range; reqLevel = need; cost = c; break; }
            }
        }
        if (nextRange <= current) { p.sendMessage("§c업그레이드 가능한 다음 크기가 없습니다."); return; }
        if (level.getLevel(p.getUniqueId()) < reqLevel) { p.sendMessage("§c요구 레벨 Lv." + reqLevel + " 필요"); return; }
        if (!withdraw(p, cost)) { p.sendMessage("§c잔액 부족"); return; }

        is.setSize(nextRange);
        store.save();
        plugin.getBento().applyRangeInstant(p, nextRange);
        p.sendMessage(Text.color(plugin.getConfig().getString("size.size-success","&a섬 크기 업그레이드: 반지름 <radius>").replace("<radius>", String.valueOf(nextRange))));
        open(p);
    }

    public void tryUpgradeMemberCap(Player p){
        IslandData is = level.getIslandOf(p);
        if (is == null) return;
        int current = is.getTeamMax();

        int nextMax = -1;
        int reqLevel = 0;
        double cost = 0.0;
        ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("members.tiers");
        if (tiers == null) tiers = plugin.getConfig().getConfigurationSection("upgrade.team.tiers");
        if (tiers != null) {
            for (String k : tiers.getKeys(false)){
                int max = tiers.getInt(k + ".max", current);
                int need  = tiers.getInt(k + ".need", 0);
                double c  = tiers.getDouble(k + ".cost", 0.0);
                if (max > current) { nextMax = max; reqLevel = need; cost = c; break; }
            }
        }
        if (nextMax <= current) { p.sendMessage("§c업그레이드 가능한 다음 인원수가 없습니다."); return; }
        if (level.getLevel(p.getUniqueId()) < reqLevel) { p.sendMessage("§c요구 레벨 Lv." + reqLevel + " 필요"); return; }
        if (!withdraw(p, cost)) { p.sendMessage("§c잔액 부족"); return; }

        is.setTeamMax(nextMax);
        store.save();
        plugin.getBento().applyTeamMax(p, nextMax);
        p.sendMessage(Text.color("&a섬 인원수를 업그레이드했습니다. 현재 " + nextMax + "명"));
        open(p);
    }

    public void buyXp(Player p){
        int amt = plugin.getConfig().getInt("upgrade.xp.amount", 50);
        double cost = plugin.getConfig().getDouble("upgrade.xp.cost", 1000.0);
        if (!withdraw(p, cost)) { p.sendMessage("§c잔액 부족"); return; }
        IslandData is = level.getIslandOf(p);
        if (is == null) return;
        level.applyXpPurchase(is, amt); // 자동 레벨업 처리
        store.save();
        p.sendMessage(Text.color("&a경험치 &f" + amt + "&a 를 구매했습니다."));
        open(p);
    }

    private boolean withdraw(Player p, double amount){
        try {
            Economy econ = vault.getEconomy();
            if (econ == null) return false;
            if (!econ.has(p, amount)) return false;
            econ.withdrawPlayer(p, amount);
            return true;
        } catch (Throwable t){
            return false;
        }
    }

    // helpers for IslandCommand
    public int getProtectedSize(IslandData is){ return is.getSize(); }
    public int getMemberCap(IslandData is){ return is.getTeamMax(); }
}
