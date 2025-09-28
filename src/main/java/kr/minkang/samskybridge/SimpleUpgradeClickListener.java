package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles clicks for the 업그레이드 GUI:
 * 11: size, 13: level xp buy, 15: team max
 * Cancels all moves (click/drag) inside the GUI.
 */
public class SimpleUpgradeClickListener implements Listener {

    private final Main plugin;
    private Economy econ;

    public SimpleUpgradeClickListener(Main plugin) {
        this.plugin = plugin;
        setupEconomy();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupEconomy() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                        .getServicesManager().getRegistration(Economy.class);
                if (rsp != null) econ = rsp.getProvider();
            }
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView() == null || e.getView().getTitle() == null) return;

        String confTitle = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        String viewTitle = ChatColor.stripColor(e.getView().getTitle());
        if (!viewTitle.equalsIgnoreCase(ChatColor.stripColor(confTitle))) return;

        // Only process clicks in the top inventory
        Inventory top = e.getView().getTopInventory();
        if (e.getClickedInventory() != top) { e.setCancelled(true); return; }
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        int rawSlot = e.getRawSlot();
        // Safety: out of top bounds
        if (rawSlot < 0 || rawSlot >= top.getSize()) return;

        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d == null) { p.sendMessage(color(prefix() + "&c섬이 없습니다.")); p.closeInventory(); return; }

        if (rawSlot == 11) {
            upgradeSize(p, d);
        } else if (rawSlot == 13) {
            buyLevelXp(p, d);
        } else if (rawSlot == 15) {
            upgradeTeam(p, d);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        String confTitle = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        String viewTitle = ChatColor.stripColor(e.getView().getTitle());
        if (viewTitle.equalsIgnoreCase(ChatColor.stripColor(confTitle))) {
            e.setCancelled(true);
        }
    }

    private void upgradeSize(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int step = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int max = plugin.getConfig().getInt("upgrade.size.max-radius", 250);

        int cur = d.sizeRadius <= 0 ? base : d.sizeRadius;
        int next = Math.min(max, cur + step);
        if (cur >= max) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(1, ((cur - base) / Math.max(1, step)) + 1);
        int reqLv = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 100000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix() + "&c필요 레벨: " + reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(cost))); return; }

        d.sizeRadius = next;
        plugin.storage.write(d);
        plugin.storage.save();
        BentoBridge.applyProtectionRange(plugin, p, next);
        p.sendMessage(color(prefix() + "&a보호 반경이 &f" + next + "&a로 확장되었습니다."));
    }

    private void upgradeTeam(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int step = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int max = plugin.getConfig().getInt("upgrade.team.max-members", 8);

        int cur = d.teamMax <= 0 ? base : d.teamMax;
        int next = Math.min(max, cur + step);
        if (cur >= max) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(1, ((cur - base) / Math.max(1, step)) + 1);
        int reqLv = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 100000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix() + "&c필요 레벨: " + reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(cost))); return; }

        d.teamMax = next;
        plugin.storage.write(d);
        plugin.storage.save();
        BentoBridge.applyTeamMax(plugin, p, next);
        p.sendMessage(color(prefix() + "&a팀 최대 인원이 &f" + next + "&a명으로 확장되었습니다."));
    }

    private void buyLevelXp(Player p, IslandData d) {
        int xpGain = plugin.getConfig().getInt("upgrade.level.xp.per-purchase", 50);
        double xpCost = plugin.getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);

        if (!withdraw(p, xpCost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(xpCost))); return; }

        d.xp += xpGain;
        levelUpIfNeeded(d);
        plugin.storage.write(d);
        plugin.storage.save();

        String msg = plugin.getConfig().getString("messages.gain-xp", "&a경험치 <xp> 를 획득했습니다!")
                .replace("<xp>", String.valueOf(xpGain));
        p.sendMessage(color(prefix() + msg));
    }

    private void levelUpIfNeeded(IslandData d) {
        int guard = 0;
        while (guard++ < 1000) {
            int need = Leveling.requiredXpForLevel(plugin, d.level + 1);
            if (d.xp >= need) { d.xp -= need; d.level++; }
            else break;
        }
    }

    private boolean withdraw(Player p, double amount) {
        if (amount <= 0) return true;
        if (econ == null) return false;
        try {
            EconomyResponse r = econ.withdrawPlayer(p, amount);
            return r != null && r.transactionSuccess();
        } catch (Throwable t) {
            return false;
        }
    }

    private String prefix() { return plugin.getConfig().getString("messages.prefix", "&a[섬]&r "); }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String format(double d) { return (d == (long) d) ? String.format("%d", (long)d) : String.format("%,.0f", d); }
}
