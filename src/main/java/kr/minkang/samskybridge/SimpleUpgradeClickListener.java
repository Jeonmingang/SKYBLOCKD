
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
 * Handles clicks for the 업그레이드 GUI (title in config: gui.title-upgrade).
 * Slots:
 *  11 - Size (protection range)
 *  13 - Island XP purchase
 *  15 - Team max size
 */
public class SimpleUpgradeClickListener implements Listener {

    private final Main plugin;

    public SimpleUpgradeClickListener(Main plugin) {
        this.plugin = plugin;
    }

    // -------------------- Events --------------------
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top == null) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        String expect = ChatColor.stripColor(color(plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드")));
        if (!title.equalsIgnoreCase(expect)) return; // not our GUI

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d == null) { p.sendMessage(color("&c섬이 없습니다.")); return; }

        int slot = e.getRawSlot();
        if (slot == 11) { upgradeSize(p, d); return; }
        if (slot == 13) { buyXp(p, d); return; }
        if (slot == 15) { upgradeTeam(p, d); return; }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        String title = ChatColor.stripColor(e.getView().getTitle());
        String expect = ChatColor.stripColor(color(plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드")));
        if (title.equalsIgnoreCase(expect)) e.setCancelled(true);
    }

    // -------------------- Actions --------------------
    private void upgradeSize(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
        int step = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
        int max = plugin.getConfig().getInt("upgrade.size.max-radius", 250);

        int cur = Math.max(base, d.sizeRadius);
        int next = Math.min(max, cur + step);
        if (cur >= max) { p.sendMessage(color(prefix()+"&e최대 단계에 도달했습니다.")); return; }

        int stepIndex = Math.max(1, ((cur - base) / Math.max(1, step)) + 1);
        int reqLv = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 100000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix()+"&c필요 레벨: "+reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix()+"&c잔액 부족: 필요 " + format(cost))); return; }

        d.sizeRadius = next;
        plugin.storage.write(d); plugin.storage.save();
        BentoBridge.applyProtectionRange(plugin, p, next);
        p.sendMessage(color(prefix()+"&a보호 반경이 &b" + cur + " -> " + next + " &a로 확장되었습니다!"));
        // Refresh GUI (optional)
        new UpgradeUI().open(p, d);
    }

    private void upgradeTeam(Player p, IslandData d) {
        int base = plugin.getConfig().getInt("upgrade.team.base-members", 2);
        int step = plugin.getConfig().getInt("upgrade.team.step-members", 1);
        int max = plugin.getConfig().getInt("upgrade.team.max-members", 10);

        int cur = Math.max(base, d.teamMax);
        int next = Math.min(max, cur + step);
        if (cur >= max) { p.sendMessage(color(prefix()+"&e최대 단계에 도달했습니다.")); return; }

        int stepIndex = Math.max(1, ((cur - base) / Math.max(1, step)) + 1);
        int reqLv = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndex;
        double cost = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 100000D) * stepIndex;

        if (d.level < reqLv) { p.sendMessage(color(prefix()+"&c필요 레벨: "+reqLv)); return; }
        if (!withdraw(p, cost)) { p.sendMessage(color(prefix()+"&c잔액 부족: 필요 " + format(cost))); return; }

        d.teamMax = next;
        plugin.storage.write(d); plugin.storage.save();
        BentoBridge.applyTeamMax(plugin, p, next);
        p.sendMessage(color(prefix()+"&a팀 최대 인원이 &b" + cur + " -> " + next + " &a로 확장되었습니다!"));
        new UpgradeUI().open(p, d);
    }

    private void buyXp(Player p, IslandData d) {
        int gain = plugin.getConfig().getInt("upgrade.level.xp.per-purchase", 50);
        double cost = plugin.getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);

        if (!withdraw(p, cost)) { p.sendMessage(color(prefix()+"&c잔액 부족: 필요 " + format(cost))); return; }

        d.xp += gain;
        // Level up chain if enough
        boolean leveled = false;
        while (d.xp >= Leveling.requiredXpForLevel(plugin, d.level + 1)) {
            d.xp -= Leveling.requiredXpForLevel(plugin, d.level + 1);
            d.level++;
            leveled = true;
        }
        plugin.storage.write(d); plugin.storage.save();
        p.sendMessage(color(prefix()+"&a경험치 +" + gain + (leveled ? " &7(레벨업!)" : "")));
        new UpgradeUI().open(p, d);
    }

    // -------------------- util --------------------
    private String prefix(){ return "&a[섬] &r"; }

    private String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    private boolean withdraw(Player p, double amount) {
        Economy eco = economy();
        if (eco == null) return true; // silent success if vault not present
        EconomyResponse resp = eco.withdrawPlayer(p, amount);
        return resp != null && resp.transactionSuccess();
    }

    private Economy economy() {
        try {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            return rsp == null ? null : rsp.getProvider();
        } catch (Throwable t) {
            return null;
        }
    }

    private String format(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-6) return String.valueOf((long)Math.rint(v));
        return String.format(java.util.Locale.KOREA, "%,.0f", v);
    }
}
