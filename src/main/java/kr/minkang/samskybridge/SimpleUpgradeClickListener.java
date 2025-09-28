
package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class SimpleUpgradeClickListener implements Listener {

    private final Main plugin;
    private final Storage storage;
    private final Integration integration;

    public SimpleUpgradeClickListener(Main plugin, Storage storage, Integration integration) {
        this.plugin = plugin;
        this.storage = storage;
        this.integration = integration;
    }

    private Economy economy;

    private Economy econ() {
        if (economy != null) return economy;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) economy = rsp.getProvider();
        return economy;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getCurrentItem() == null) return;
        String title = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        if (!e.getView().getTitle().equals(ChatColor.GREEN + title) && !e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        IslandData d = storage.getIslandByOwner(p.getUniqueId());
        if (d == null) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.no-island", "&c섬이 없습니다."))); return; }
        if (!d.owner.equals(p.getUniqueId())) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.not-owner", "&c섬장만 사용할 수 있습니다."))); return; }

        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 11);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 15);
        int levelSlot = plugin.getConfig().getInt("upgrade.gui.slots.level", 13);

        int slot = e.getRawSlot();
        if (slot == sizeSlot) {
            upgradeSize(p, d);
        } else if (slot == teamSlot) {
            upgradeTeam(p, d);
        } else if (slot == levelSlot) {
            buyLevelXp(p, d);
        }
    }

    private void upgradeSize(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int step = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int max = plugin.getConfig().getInt("upgrade.size.max-radius", 250);

        int cur = d.sizeRadius;
        int next = cur + step;
        if (next > max) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(0, (cur - base) / Math.max(1, step)) + 1;
        int reqBase = plugin.getConfig().getInt("upgrade.size.require-level.base", 5);
        int reqPer = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1);
        int reqLevel = Math.max(0, reqBase + reqPer * stepIndex);

        if (d.level < reqLevel) {
            p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.lack-level", "&c요구 레벨 &f<req>&c이 필요합니다.").replace("<req>", String.valueOf(reqLevel))));
            return;
        }

        double costBase = plugin.getConfig().getDouble("upgrade.size.cost.base", 50000D);
        double costPer = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 25000D);
        double cost = Math.max(0D, costBase + costPer * stepIndex);

        if (!withdraw(p, cost)) return;

        d.sizeRadius = next;
        storage.save();
        if (integration != null) {
            try { integration.syncRange(d.owner, d.sizeRadius); } catch (Throwable ignored) {}
            try { integration.syncAll(d.owner, d.sizeRadius, d.teamMax); } catch (Throwable ignored) {}
        }
        p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.upgraded", "&a업그레이드 완료! &7(&f<type>&7)").replace("<type>", "섬 크기")));
        p.closeInventory();
    }

    private void upgradeTeam(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int step = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int max = plugin.getConfig().getInt("upgrade.team.max-members", 10);

        int cur = d.teamMax;
        int next = cur + step;
        if (next > max) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(0, (cur - base) / Math.max(1, step)) + 1;
        int reqBase = plugin.getConfig().getInt("upgrade.team.require-level.base", 3);
        int reqPer = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1);
        int reqLevel = Math.max(0, reqBase + reqPer * stepIndex);
        if (d.level < reqLevel) {
            p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.lack-level", "&c요구 레벨 &f<req>&c이 필요합니다.").replace("<req>", String.valueOf(reqLevel))));
            return;
        }

        double costBase = plugin.getConfig().getDouble("upgrade.team.cost.base", 100000D);
        double costPer = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 50000D);
        double cost = Math.max(0D, costBase + costPer * stepIndex);
        if (!withdraw(p, cost)) return;

        d.teamMax = next;
        storage.save();
        if (integration != null) {
            try { integration.syncTeam(d.owner, d.teamMax); } catch (Throwable ignored) {}
            try { integration.syncAll(d.owner, d.sizeRadius, d.teamMax); } catch (Throwable ignored) {}
        }
        p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.upgraded", "&a업그레이드 완료! &7(&f<type>&7)").replace("<type>", "팀 최대 인원")));
        p.closeInventory();
    }

    private void buyLevelXp(Player p, IslandData d) {
        int xpPerLevel = plugin.getConfig().getInt("level.xp-per-level", 1000);
        int gain = plugin.getConfig().getInt("upgrade.level.purchase.xp-gain", 100);
        double base = plugin.getConfig().getDouble("upgrade.level.purchase.cost.base", 10000D);
        double perLv = plugin.getConfig().getDouble("upgrade.level.purchase.cost.per-level", 2000D);
        double cost = Math.max(0D, base + perLv * d.level);
        if (!withdraw(p, cost)) return;

        d.xp += gain;
        // auto level up
        while (d.xp >= xpPerLevel) {
            d.xp -= xpPerLevel;
            d.level += 1;
        }
        storage.save();
        p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.level-purchased", "&a경험치 &f<xp>&a 를 획득했습니다!").replace("<xp>", String.valueOf(gain))));
        p.closeInventory();
    }

    private boolean withdraw(Player p, double amount) {
        Economy eco = econ();
        if (eco == null) {
            p.sendMessage(color(prefix() + "&cVault 경제 플러그인이 설치되어 있지 않습니다."));
            return false;
        }
        if (!eco.has(p, amount)) {
            p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.lack-money", "&c필요 금액 &f<cost>&c이 부족합니다.").replace("<cost>", String.format("%.0f", amount))));
            return false;
        }
        return eco.withdrawPlayer(p, amount).transactionSuccess();
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&a[섬]&r "));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
