
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
import java.util.List;

public class UpgradeUI {

    public void open(Player player, IslandData data) {
        String title = Bukkit.getPluginManager().getPlugin("SamSkyBridge").getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.GREEN + title);

        // config
        Main plugin = (Main) Bukkit.getPluginManager().getPlugin("SamSkyBridge");
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 11);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 15);
        int levelSlot = plugin.getConfig().getInt("upgrade.gui.slots.level", 13);

        // size card
        int baseR = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int stepR = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int maxR = plugin.getConfig().getInt("upgrade.size.max-radius", 250);
        int nextR = Math.min(maxR, data.sizeRadius + stepR);
        int stepIndexR = Math.max(0, (data.sizeRadius - baseR) / Math.max(1, stepR)) + 1;
        int reqLvR = plugin.getConfig().getInt("upgrade.size.require-level.base", 5) + plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndexR;
        double costR = plugin.getConfig().getDouble("upgrade.size.cost.base", 50000D) + plugin.getConfig().getDouble("upgrade.size.cost.per-step", 25000D) * stepIndexR;
        ItemStack size = card(Material.GRASS_BLOCK,
                color(plugin.getConfig().getString("gui.items.size.name", "&a섬 크기 확장")),
                Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.size.lore.0", "&7현재 크기: &f{current}").replace("{current}", String.valueOf(data.sizeRadius))),
                        color(plugin.getConfig().getString("gui.items.size.lore.1", "&7다음 크기: &f{next}").replace("{next}", String.valueOf(nextR))),
                        color(plugin.getConfig().getString("gui.items.size.lore.2", "&7요구 레벨: &f{reqLevel}").replace("{reqLevel}", String.valueOf(reqLvR))),
                        color(plugin.getConfig().getString("gui.items.size.lore.3", "&7가격: &f{cost}").replace("{cost}", String.format("%.0f", costR))),
                        color(plugin.getConfig().getString("gui.items.size.lore.4", "")),
                        color(plugin.getConfig().getString("gui.items.size.lore.5", "&e클릭하면 업그레이드"))
                ));
        inv.setItem(sizeSlot, size);

        // team card
        int baseT = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int stepT = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int maxT = plugin.getConfig().getInt("upgrade.team.max-members", 10);
        int nextT = Math.min(maxT, data.teamMax + stepT);
        int stepIndexT = Math.max(0, (data.teamMax - baseT) / Math.max(1, stepT)) + 1;
        int reqLvT = plugin.getConfig().getInt("upgrade.team.require-level.base", 3) + plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndexT;
        double costT = plugin.getConfig().getDouble("upgrade.team.cost.base", 100000D) + plugin.getConfig().getDouble("upgrade.team.cost.per-step", 50000D) * stepIndexT;
        ItemStack team = card(Material.PLAYER_HEAD,
                color(plugin.getConfig().getString("gui.items.team.name", "&b팀 최대 인원 확장")),
                Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.team.lore.0", "&7현재 인원: &f{current}").replace("{current}", String.valueOf(data.teamMax))),
                        color(plugin.getConfig().getString("gui.items.team.lore.1", "&7다음 인원: &f{next}").replace("{next}", String.valueOf(nextT))),
                        color(plugin.getConfig().getString("gui.items.team.lore.2", "&7요구 레벨: &f{reqLevel}").replace("{reqLevel}", String.valueOf(reqLvT))),
                        color(plugin.getConfig().getString("gui.items.team.lore.3", "&7가격: &f{cost}").replace("{cost}", String.format("%.0f", costT))),
                        color(plugin.getConfig().getString("gui.items.team.lore.4", "")),
                        color(plugin.getConfig().getString("gui.items.team.lore.5", "&e클릭하면 업그레이드"))
                ));
        inv.setItem(teamSlot, team);

        // level purchase card
        int gain = plugin.getConfig().getInt("upgrade.level.purchase.xp-gain", 100);
        double base = plugin.getConfig().getDouble("upgrade.level.purchase.cost.base", 10000D);
        double perLv = plugin.getConfig().getDouble("upgrade.level.purchase.cost.per-level", 2000D);
        double costLv = base + perLv * data.level;
        ItemStack level = card(Material.EXPERIENCE_BOTTLE,
                color(plugin.getConfig().getString("gui.items.level.name", "&d섬 레벨 경험치 구매")),
                Arrays.asList(
                        color(plugin.getConfig().getString("gui.items.level.lore.0", "&7현재 레벨: &f{level}").replace("{level}", String.valueOf(data.level))),
                        color(plugin.getConfig().getString("gui.items.level.lore.1", "&7추가 경험치: &f{xpGain}").replace("{xpGain}", String.valueOf(gain))),
                        color(plugin.getConfig().getString("gui.items.level.lore.2", "&7가격: &f{cost}").replace("{cost}", String.format("%.0f", costLv))),
                        color(plugin.getConfig().getString("gui.items.level.lore.3", "")),
                        color(plugin.getConfig().getString("gui.items.level.lore.4", "&e클릭하면 레벨 경험치 구매"))
                ));
        inv.setItem(levelSlot, level);

        player.openInventory(inv);
    }

    private ItemStack card(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
            it.setItemMeta(im);
        }
        return it;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
