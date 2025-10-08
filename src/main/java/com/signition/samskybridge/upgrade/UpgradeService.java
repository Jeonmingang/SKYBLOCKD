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
        String title = com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드"));
        Inventory inv = Bukkit.createInventory(p, Math.max(9, Math.min(54, (plugin.getConfig().getInt("upgrade.gui.inventory-size", 27) / 9) * 9)), title);
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);

        inv.setItem(sizeSlot, buildSizeItem(p));
        inv.setItem(teamSlot, buildTeamItem(p));
        inv.setItem(xpSlot,   buildXpItem(p));

        p.openInventory(inv);
    }

    public void click(Player p, int slot, boolean shift){
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
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

        ItemStack it = new ItemStack(org.bukkit.Material.matchMaterial(plugin.getConfig().getString("upgrade.gui.items.size.material","MAP")));
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("upgrade.gui.items.size.name","&a섬 크기 업그레이드")));
        java.util.List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.size.lore");
        java.util.Map<String,String> ctx = new java.util.HashMap<>();
        ctx.put("{현재크기}", String.valueOf(current));
        ctx.put("{다음크기}", String.valueOf(nextRange));
        ctx.put("{크기업그레이드레벨}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{크기필요레벨}", String.valueOf(reqLevel));
        ctx.put("{크기비용}", String.format("%,.0f", cost));
        // Generic aliases
        ctx.put("{current}", String.valueOf(current));
        ctx.put("{next}", String.valueOf(nextRange));
        ctx.put("{level}", String.valueOf(is != null ? is.getLevel() : 0));
        ctx.put("{need}", String.valueOf(reqLevel));
        ctx.put("{cost}", String.format("%,.0f", cost));

        meta.setLore(fillTemplate(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }