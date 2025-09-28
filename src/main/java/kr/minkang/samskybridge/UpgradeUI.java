package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;

public class UpgradeUI {

    private Economy econ;

    public void open(Player p, IslandData data) {
        Inventory inv = Bukkit.createInventory(p, 27, color("&b섬 업그레이드"));

        if (econ == null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) econ = rsp.getProvider();
        }

        Main plugin = (Main) Bukkit.getPluginManager().getPlugin("SamSkyBridge");

        int baseS = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int stepS = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int maxS = plugin.getConfig().getInt("upgrade.size.max-radius", 250);
        int nextS = Math.min(maxS, data.sizeRadius + stepS);
        int stepIndexS = Math.max(0, (data.sizeRadius - baseS) / Math.max(1, stepS)) + 1;
        int reqLvS = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndexS;
        double costS = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 100000D) * stepIndexS;

        ItemStack size = card(Material.MAP,
                color(plugin.getConfig().getString("gui.items.size.name", "&a보호 반경 확장")),
                java.util.Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.size.lore.0", "&7현재 반경: &f{current}").replace("{current}", String.valueOf(data.sizeRadius))),
                        color(plugin.getConfig().getString("gui.items.size.lore.1", "&7다음 반경: &f{next}").replace("{next}", String.valueOf(nextS))),
                        color(plugin.getConfig().getString("gui.items.size.lore.2", "&7필요 레벨: &f{lv}").replace("{lv}", String.valueOf(reqLvS))),
                        color(plugin.getConfig().getString("gui.items.size.lore.3", "&7가격: &f{cost}").replace("{cost}", format(costS)))
                )
        );

        int baseT = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int stepT = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int maxT = plugin.getConfig().getInt("upgrade.team.max-members", 10);
        int nextT = Math.min(maxT, data.teamMax + stepT);
        int stepIndexT = Math.max(0, (data.teamMax - baseT) / Math.max(1, stepT)) + 1;
        int reqLvT = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndexT;
        double costT = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 50000D) * stepIndexT;

        ItemStack team = card(Material.PLAYER_HEAD,
                color(plugin.getConfig().getString("gui.items.team.name", "&b팀 최대 인원 확장")),
                java.util.Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.team.lore.0", "&7현재 인원: &f{current}").replace("{current}", String.valueOf(data.teamMax))),
                        color(plugin.getConfig().getString("gui.items.team.lore.1", "&7다음 인원: &f{next}").replace("{next}", String.valueOf(nextT))),
                        color(plugin.getConfig().getString("gui.items.team.lore.2", "&7필요 레벨: &f{lv}").replace("{lv}", String.valueOf(reqLvT))),
                        color(plugin.getConfig().getString("gui.items.team.lore.3", "&7가격: &f{cost}").replace("{cost}", format(costT)))
                )
        );

        int xpGain = plugin.getConfig().getInt("upgrade.level.xp.per-purchase", 50);
        double xpCost = plugin.getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);
        ItemStack level = card(Material.EXPERIENCE_BOTTLE,
                color(plugin.getConfig().getString("gui.items.level.name", "&d섬 경험치 구매")),
                java.util.Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.level.lore.0", "&7획득 경험치: &f{xp}").replace("{xp}", String.valueOf(xpGain))),
                        color(plugin.getConfig().getString("gui.items.level.lore.1", "&7가격: &f{cost}").replace("{cost}", format(xpCost)))
                )
        );

        inv.setItem(11, size);
        inv.setItem(15, team);
        inv.setItem(13, level);

        p.openInventory(inv);
    }

    private org.bukkit.inventory.ItemStack card(Material mat, String name, java.util.List<String> lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
            is.setItemMeta(im);
        }
        return is;
    }

    private String format(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        return String.format("%,.0f", d);
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
