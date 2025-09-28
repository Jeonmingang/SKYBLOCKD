
package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpgradeUI implements Listener {

    private final Main plugin;
    private final Storage storage;
    private final Integration integration;
    private final Economy econ;
    private final String title = ChatColor.DARK_AQUA + "섬 업그레이드";

    public UpgradeUI(Main plugin, Storage storage, Integration integration) {
        this.plugin = plugin;
        this.storage = storage;
        this.integration = integration;
        this.econ = setupEconomy();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private Economy setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return null;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    public void open(Player p, IslandData d) {
        Inventory inv = Bukkit.createInventory(p, 27, title);

        inv.setItem(11, buildItem(Material.PLAYER_HEAD, "§a팀 인원 업그레이드",
                loreFor("team", d.teamMax, d.level)));
        inv.setItem(15, buildItem(Material.GRASS_BLOCK, "§a섬 크기 업그레이드",
                loreFor("size", d.sizeRadius, d.level)));
        p.openInventory(inv);
    }

    private List<String> loreFor(String type, int current, int level) {
        double costBase = plugin.getConfig().getDouble("upgrade."+type+".cost-base", 10000D);
        double mult = plugin.getConfig().getDouble("upgrade."+type+".cost-multiplier", 1.25D);
        int reqBase = plugin.getConfig().getInt("upgrade."+type+".required-level-base", 2);
        int reqStep = plugin.getConfig().getInt("upgrade."+type+".required-level-step", 2);
        int nextReq = reqBase + (int)Math.floor((current - plugin.getConfig().getInt("upgrade."+type+(type.equals("team")?".base-members":".base-radius"), current))/
                (double) plugin.getConfig().getInt("upgrade."+type+".per-level", 1)) * reqStep;
        double cost = costBase * Math.pow(mult, Math.max(0, current));

        List<String> lore = new ArrayList<>();
        lore.add("§7현재: §f" + current);
        lore.add("§7요구 레벨: §f" + nextReq);
        lore.add("§7가격: §f" + String.format("%.0f", cost));
        lore.add("§e클릭하여 업그레이드");
        return lore;
    }

    private ItemStack buildItem(Material m, String name, List<String> lore) {
        ItemStack is = new ItemStack(m);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!title.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;
        Player p = (Player) e.getWhoClicked();
        IslandData d = storage.getIslandByOwner(p.getUniqueId());
        if (d == null) {
            p.closeInventory();
            plugin.msg(p, plugin.getConfig().getString("messages.not-owner"));
            return;
        }
        if (e.getSlot() == 11) {
            tryUpgrade(p, d, "team");
        } else if (e.getSlot() == 15) {
            tryUpgrade(p, d, "size");
        }
        storage.write(d);
        open(p, d);
    }

    private void tryUpgrade(Player p, IslandData d, String type) {
        int per = plugin.getConfig().getInt("upgrade."+type+".per-level", 1);
        int current = (type.equals("team") ? d.teamMax : d.sizeRadius);
        double costBase = plugin.getConfig().getDouble("upgrade."+type+".cost-base", 10000D);
        double mult = plugin.getConfig().getDouble("upgrade."+type+".cost-multiplier", 1.25D);
        double cost = costBase * Math.pow(mult, Math.max(0, current));

        int reqBase = plugin.getConfig().getInt("upgrade."+type+".required-level-base", 2);
        int reqStep = plugin.getConfig().getInt("upgrade."+type+".required-level-step", 2);
        int stepDenom = plugin.getConfig().getInt("upgrade."+type+".per-level", 1);
        int baseVal = plugin.getConfig().getInt("upgrade."+type+(type.equals("team")?".base-members":".base-radius"), current);
        int steps = Math.max(0, (current - baseVal) / Math.max(1, stepDenom));
        int req = reqBase + steps * reqStep;

        if (d.level < req) {
            plugin.msg(p, plugin.getConfig().getString("messages.lack-level").replace("<req>", String.valueOf(req)));
            return;
        }
        if (econ != null && econ.getBalance(p) < cost) {
            plugin.msg(p, plugin.getConfig().getString("messages.lack-money").replace("<cost>", String.format("%.0f", cost)));
            return;
        }
        if (econ != null) econ.withdrawPlayer(p, cost);
        if (type.equals("team")) d.teamMax += per;
        else d.sizeRadius += per;

        // Optional: sync to BentoBox range
        if (type.equals("size") && plugin.getConfig().getBoolean("upgrade.sync.bento.range", false)) {
            integration.syncRangeToBento(p.getUniqueId(), d.sizeRadius);
        }
        plugin.msg(p, plugin.getConfig().getString("messages.upgraded").replace("<type>", type));
    }
}
