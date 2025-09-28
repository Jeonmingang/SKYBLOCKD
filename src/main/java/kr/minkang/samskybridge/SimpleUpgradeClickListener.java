package kr.minkang.samskybridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class SimpleUpgradeClickListener implements Listener {

    private final Main plugin;
    private Economy econ;

    public SimpleUpgradeClickListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("섬 업그레이드")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d == null) { p.sendMessage(color(prefix() + "&c섬이 없습니다.")); return; }

        if (slot == 11) upgradeSize(p, d);
        else if (slot == 15) upgradeTeam(p, d);
        else if (slot == 13) buyLevelXp(p, d);
    }

    private void upgradeSize(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int step = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int max = plugin.getConfig().getInt("upgrade.size.max-radius", 250);

        int cur = d.sizeRadius;
        int next = cur + step;
        if (next > max) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(0, (cur - base) / Math.max(1, step)) + 1;
        int reqLv = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 100000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix() + "&c필요 레벨: " + reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(cost))); return; }

        d.sizeRadius = next; plugin.storage.write(d); plugin.storage.save();
        applySizeToBento(p, d, next);
        // Reflect to BentoBox
        BentoBridge.applyProtectionRange(plugin, p, next);
        p.sendMessage(color(prefix() + "&a보호 반경이 " + next + "으로 확장되었습니다."));
    }

    private void upgradeTeam(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int step = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int max = plugin.getConfig().getInt("upgrade.team.max-members", 10);

        int cur = d.teamMax;
        int next = Math.min(max, cur + step);
        if (next == cur) { p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.max-reached", "&e최대 단계에 도달했습니다."))); return; }

        int stepIndex = Math.max(0, (cur - base) / Math.max(1, step)) + 1;
        int reqLv = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 50000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix() + "&c필요 레벨: " + reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(cost))); return; }

        d.teamMax = next; plugin.storage.write(d); plugin.storage.save();
        applyTeamToBento(p, d, next);
        // Reflect to BentoBox
        BentoBridge.applyTeamMax(plugin, p, next);
        p.sendMessage(color(prefix() + "&a팀 최대 인원이 " + next + "명으로 확장되었습니다."));
    }

    private void buyLevelXp(Player p, IslandData d) {
        int xpGain = plugin.getConfig().getInt("upgrade.level.xp.per-purchase", 50);
        double xpCost = plugin.getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);

        if (!withdraw(p, xpCost)) { p.sendMessage(color(prefix() + "&c잔액 부족. 필요: " + format(xpCost))); return; }

        d.xp += xpGain; levelUpIfNeeded(d); plugin.storage.write(d); plugin.storage.save();
        p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.level-purchased", "&a경험치 &f<xp>&a 를 획득했습니다!").replace("<xp>", String.valueOf(xpGain))));
    
}

private void levelUpIfNeeded(IslandData d) {
    Main plugin = this.plugin;
    int guard = 0;
    while (guard++ < 1000) {
        int need = Leveling.requiredXpForLevel(plugin, d.level + 1);
        if (d.xp >= need) { d.xp -= need; d.level++; }
        else break;
    }
}

private void applySizeToBento(Player p, IslandData d, int newSize) {
    String apply = plugin.getConfig().getString("integration.size.apply", "command").toLowerCase();
    if (apply.equals("api")) {
        BentoBridge.applyProtectionRange(plugin, p, newSize);
        return;
    }
    String tmpl = plugin.getConfig().getString("integration.size.command", "bsbadmin range set <owner> <size>");
    String owner = p.getName();
    if (d.owner != null) {
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(d.owner);
        if (op != null && op.getName() != null) owner = op.getName();
    }
    String cmd = tmpl.replace("<owner>", owner).replace("<size>", String.valueOf(newSize));
    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd);
}

private void applyTeamToBento(Player p, IslandData d, int newTeamMax) {
    String method = plugin.getConfig().getString("integration.team.apply", "permission").toLowerCase();
    if (method.equals("api")) {
        BentoBridge.applyTeamMax(plugin, p, newTeamMax);
        return;
    }
    java.util.UUID ownerId = d.owner != null ? d.owner : p.getUniqueId();
    plugin.applyOwnerTeamPerm(ownerId, newTeamMax);
}

private boolean withdraw(Player p, double amount) {
        if (amount <= 0) return true;
        if (econ == null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) econ = rsp.getProvider();
        }
        if (econ == null) return false;
        return econ.withdrawPlayer(p, amount).transactionSuccess();
    }

    private String prefix() { return plugin.getConfig().getString("messages.prefix", "&a[섬]&r "); }
    private String color(String s) { return org.bukkit.ChatColor.translateAlternateColorCodes('&', s); }
    private String format(double d) { return (d == (long) d) ? String.format("%d", (long)d) : String.format("%,.0f", d); }
}
