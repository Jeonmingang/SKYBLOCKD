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
        inv.setItem(11, named(new ItemStack(Material.WHITE_STAINED_GLASS), plugin.getConfig().getString("gui.size-item-name","섬 크기 업그레이드"),
                new String[]{
                    "현재 반지름: " + is.getSize(),
                    "가격(돈): " + (long)costSize(is.getSize()) + " | 요구 레벨: " + needLevelSize(is.getSize()),
                    "좌클릭=돈 구매, 쉬프트=레벨 구매"
                }));
        inv.setItem(15, named(new ItemStack(Material.PLAYER_HEAD), plugin.getConfig().getString("gui.team-item-name","팀원 수 업그레이드"),
                new String[]{
                    "현재 팀 최대: " + is.getTeamMax(),
                    "가격(돈): " + (long)costTeam(is.getTeamMax()) + " | 요구 레벨: " + needLevelTeam(is.getTeamMax()),
                    "좌클릭=돈 구매, 쉬프트=레벨 구매"
                }));
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
        if (slot == 11){
            int before = is.getSize();
            int next = before + 10;
            if (shift){
                int need = needLevelSize(before);
                if (is.getLevel() < need){
                    String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                    p.sendMessage(msg);
                    return;
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
            // [removed] Barrier call
            try { if (bento != null && bento.isEnabled()) bento.applyRangeInstant(p, next); } catch (Throwable t){ plugin.getLogger().warning("BentoSync range failed: "+t.getMessage()); }
        } else if (slot == 15){
            int before = is.getTeamMax();
            int next = before + 1;
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
        int base = plugin.getConfig().getInt("economy.costs.size.level-required-base", 5);
        int step = plugin.getConfig().getInt("economy.costs.size.level-required-step", 2);
        int steps = Math.max(0, (currentRadius - 50) / 10);
        return base + step * steps;
    }
    private int needLevelTeam(int currentTeam){
        int base = plugin.getConfig().getInt("economy.costs.team.level-required-base", 3);
        int step = plugin.getConfig().getInt("economy.costs.team.level-required-step", 1);
        int steps = Math.max(0, currentTeam - 1);
        return base + step * steps;
    }
}