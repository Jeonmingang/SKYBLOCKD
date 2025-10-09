package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Clean reimplementation: stable braces, no duplicate fragments.
 * - Builds Upgrade GUI items (size/team/xp) with placeholder support (both <...> and {한글} keys).
 * - Handles click actions and applies upgrades with Vault cost deduction (if Vault is present).
 * - Restricts to island leader.
 */
public class UpgradeService {

    private final Main plugin;
    private final LevelService level;

    public UpgradeService(Main plugin, LevelService level){
        this.plugin = plugin;
        this.level = level;
    }

    // ---------- GUI ----------
    public void open(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)){
            p.sendMessage(Text.color("&c섬장만 업그레이드를 사용할 수 있습니다."));
            return;
        }
        String title = plugin.getConfig().getString("upgrade.gui.title-upgrade",
                plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드"));
        int invSize = Math.max(9, Math.min(54, (plugin.getConfig().getInt("upgrade.gui.inventory-size", 27) / 9) * 9));
        Inventory inv = Bukkit.createInventory(p, invSize, Text.color(title));

        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp",   22);

        inv.setItem(sizeSlot, buildSizeItem(p));
        inv.setItem(teamSlot, buildTeamItem(p));
        inv.setItem(xpSlot,   buildXpItem(p));
        p.openInventory(inv);
    }

    public void click(Player p, int rawSlot, boolean shiftClick){
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int teamSlot = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp",   22);
        if (rawSlot == sizeSlot) tryUpgradeSize(p);
        else if (rawSlot == teamSlot) tryUpgradeTeam(p);
        else if (rawSlot == xpSlot) buyXp(p);
    }

    // ---------- Items ----------
    private ItemStack materialOf(String primaryPath, String... fallbacks){
        String matName = plugin.getConfig().getString(primaryPath, null);
        if (matName == null){
            for (String fb : fallbacks){
                matName = plugin.getConfig().getString(fb, null);
                if (matName != null) break;
            }
        }
        Material m = Material.matchMaterial(matName != null ? matName : "PAPER");
        if (m == null) m = Material.PAPER;
        return new ItemStack(m);
    }

    private List<String> apply(List<String> tpl, Map<String,String> ctx){
        List<String> out = new ArrayList<>();
        for (String line : tpl){
            String s = line;
            for (Map.Entry<String,String> e : ctx.entrySet()){
                s = s.replace(e.getKey(), e.getValue());
            }
            // english aliases
            s = s.replace("<current>", ctx.getOrDefault("{현재크기}", ctx.getOrDefault("{현재인원}", "")))
                 .replace("<next>", ctx.getOrDefault("{다음크기}", ctx.getOrDefault("{다음인원}", "")))
                 .replace("<level>", ctx.getOrDefault("{크기필요레벨}", ctx.getOrDefault("{인원필요레벨}", "")))
                 .replace("<cost>", ctx.getOrDefault("{크기비용}", ctx.getOrDefault("{인원비용}", "")));
            out.add(Text.color(s));
        }
        return out;
    }

    private ItemStack buildSizeItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = (is != null ? is.getSize() : plugin.getConfig().getInt("defaults.size", 120));

        // read next range/req/cost from either new (upgrade.size) or legacy (size.tiers)
        int next = current, needLevel = 0; double cost = 0.0;

        ConfigurationSection newer = plugin.getConfig().getConfigurationSection("upgrade.size");
        if (newer != null && newer.getKeys(false).size() > 0){
            for (String k : newer.getKeys(false)){
                ConfigurationSection s = newer.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){ next = to; needLevel = need; cost = c; break; }
            }
        } else {
            // legacy
            ConfigurationSection legacy = plugin.getConfig().getConfigurationSection("size.tiers");
            if (legacy != null){
                for (String k : legacy.getKeys(false)){
                    ConfigurationSection s = legacy.getConfigurationSection(k);
                    if (s == null) continue;
                    int rng = s.getInt("range", current);
                    int need = s.getInt("need", 0);
                    double c = s.getDouble("cost", 0.0);
                    if (rng > current){ next = rng; needLevel = need; cost = c; break; }
                }
            }
        }

        ItemStack it = materialOf("upgrade.gui.items.size.material", "size.material");
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.gui.items.size.name",
                plugin.getConfig().getString("size.name", "&a섬 크기 업그레이드"))));
        List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.size.lore");
        if (tpl == null || tpl.isEmpty()) tpl = plugin.getConfig().getStringList("size.lore");
        if (tpl == null || tpl.isEmpty()){
            tpl = Arrays.asList("&7현재: &f<current> &7→ 다음: &f<next>",
                    "&7필요 레벨: &a<level>",
                    "&7가격: &6<cost>");
        }
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{현재크기}", String.valueOf(current));
        ctx.put("{다음크기}", String.valueOf(next));
        ctx.put("{크기필요레벨}", String.valueOf(needLevel));
        ctx.put("{크기비용}", String.format("%,.0f", cost));
        meta.setLore(apply(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack buildTeamItem(Player p){
        IslandData is = level.getIslandOf(p);
        int current = (is != null ? is.getTeamMax() : plugin.getConfig().getInt("defaults.team-max", 4));

        int next = current, needLevel = 0; double cost = 0.0;

        ConfigurationSection newer = plugin.getConfig().getConfigurationSection("upgrade.team");
        if (newer != null && newer.getKeys(false).size() > 0){
            for (String k : newer.getKeys(false)){
                ConfigurationSection s = newer.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){ next = to; needLevel = need; cost = c; break; }
            }
        } else {
            ConfigurationSection legacy = plugin.getConfig().getConfigurationSection("members.tiers");
            if (legacy != null){
                for (String k : legacy.getKeys(false)){
                    ConfigurationSection s = legacy.getConfigurationSection(k);
                    if (s == null) continue;
                    int to = s.getInt("next", current);
                    int need = s.getInt("need", 0);
                    double c = s.getDouble("cost", 0.0);
                    if (to > current){ next = to; needLevel = need; cost = c; break; }
                }
            }
        }

        ItemStack it = materialOf("upgrade.gui.items.team.material", "members.material");
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.gui.items.team.name",
                plugin.getConfig().getString("members.name", "&a섬 인원 업그레이드"))));
        List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.team.lore");
        if (tpl == null || tpl.isEmpty()) tpl = plugin.getConfig().getStringList("members.lore");
        if (tpl == null || tpl.isEmpty()){
            tpl = Arrays.asList("&7현재: &f<current> &7→ 다음: &f<next>",
                    "&7필요 레벨: &a<level>",
                    "&7가격: &6<cost>");
        }
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{현재인원}", String.valueOf(current));
        ctx.put("{다음인원}", String.valueOf(next));
        ctx.put("{인원필요레벨}", String.valueOf(needLevel));
        ctx.put("{인원비용}", String.format("%,.0f", cost));
        meta.setLore(apply(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack buildXpItem(Player p){
        int amount = plugin.getConfig().getInt("upgrade.xp.amount",
                plugin.getConfig().getInt("xp.amount", 0));
        if (amount <= 0) amount = 50;
        ItemStack it = materialOf("upgrade.gui.items.xp.material", "xp-purchase.material");
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.gui.items.xp.name",
                plugin.getConfig().getString("xp-purchase.name", "&a경험치 구매"))));
        List<String> tpl = plugin.getConfig().getStringList("upgrade.gui.items.xp.lore");
        if (tpl == null || tpl.isEmpty()) tpl = Arrays.asList("&7경험치 보틀 한 개당: &f" + amount);
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{경험치구매량}", String.valueOf(amount));
        meta.setLore(apply(tpl, ctx));
        it.setItemMeta(meta);
        return it;
    }

    // ---------- Actions ----------
    private void tryUpgradeSize(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)){ p.sendMessage(Text.color("&c섬장만 가능합니다.")); return; }

        int current = is.getSize();
        int next = current, needLevel = 0; double cost = 0.0;

        ConfigurationSection newer = plugin.getConfig().getConfigurationSection("upgrade.size");
        if (newer != null && newer.getKeys(false).size() > 0){
            for (String k : newer.getKeys(false)){
                ConfigurationSection s = newer.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){ next = to; needLevel = need; cost = c; break; }
            }
        } else {
            ConfigurationSection legacy = plugin.getConfig().getConfigurationSection("size.tiers");
            if (legacy != null){
                for (String k : legacy.getKeys(false)){
                    ConfigurationSection s = legacy.getConfigurationSection(k);
                    if (s == null) continue;
                    int rng = s.getInt("range", current);
                    int need = s.getInt("need", 0);
                    double c = s.getDouble("cost", 0.0);
                    if (rng > current){ next = rng; needLevel = need; cost = c; break; }
                }
            }
        }

        if (next <= current){ p.sendMessage(Text.color("&c업그레이드할 단계가 없습니다.")); return; }
        if (is.getLevel() < needLevel){ p.sendMessage(Text.color("&c요구 레벨: &f"+needLevel)); return; }
        if (!charge(p, cost)) return;

        is.setSize(next);
        plugin.getDataStore().save(); // persist
        p.sendMessage(Text.color("&a섬 반경이 &f"+current+"&a → &f"+next+"&a로 확장되었습니다!"));
        open(p);
    }

    private void tryUpgradeTeam(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)){ p.sendMessage(Text.color("&c섬장만 가능합니다.")); return; }

        int current = is.getTeamMax();
        int next = current, needLevel = 0; double cost = 0.0;

        ConfigurationSection newer = plugin.getConfig().getConfigurationSection("upgrade.team");
        if (newer != null && newer.getKeys(false).size() > 0){
            for (String k : newer.getKeys(false)){
                ConfigurationSection s = newer.getConfigurationSection(k);
                if (s == null) continue;
                int from = s.getInt("from", 0);
                int to = s.getInt("to", from);
                int need = s.getInt("need-level", 0);
                double c = s.getDouble("cost", 0.0);
                if (current >= from && to > current){ next = to; needLevel = need; cost = c; break; }
            }
        } else {
            ConfigurationSection legacy = plugin.getConfig().getConfigurationSection("members.tiers");
            if (legacy != null){
                for (String k : legacy.getKeys(false)){
                    ConfigurationSection s = legacy.getConfigurationSection(k);
                    if (s == null) continue;
                    int to = s.getInt("next", current);
                    int need = s.getInt("need", 0);
                    double c = s.getDouble("cost", 0.0);
                    if (to > current){ next = to; needLevel = need; cost = c; break; }
                }
            }
        }

        if (next <= current){ p.sendMessage(Text.color("&c업그레이드할 단계가 없습니다.")); return; }
        if (is.getLevel() < needLevel){ p.sendMessage(Text.color("&c요구 레벨: &f"+needLevel)); return; }
        if (!charge(p, cost)) return;

        is.setTeamMax(next);
        plugin.getDataStore().save();
        p.sendMessage(Text.color("&a섬 인원이 &f"+current+"&a → &f"+next+"&a로 증가했습니다!"));
        open(p);
    }

    private void buyXp(Player p){
        if (!plugin.getConfig().getBoolean("xp-purchase.enabled", true)){
            p.sendMessage(Text.color("&c이 서버에서는 XP 구매가 비활성화되어 있습니다."));
            return;
        }
        int amount = plugin.getConfig().getInt("upgrade.xp.amount",
                plugin.getConfig().getInt("upgrade.xp.amount", 50));
        int needLevel = plugin.getConfig().getInt("upgrade.xp.need-level", 0);
        double cost = plugin.getConfig().getDouble("upgrade.xp.cost", 0.0);

        IslandData is = level.getIslandOf(p);
        if (is == null){ p.sendMessage(Text.color("&c섬을 찾지 못했습니다.")); return; }
        if (is.getLevel() < needLevel){ p.sendMessage(Text.color("&c요구 레벨: &f"+needLevel)); return; }
        if (!charge(p, cost)) return;

        level.addXp(p, amount);
        plugin.getDataStore().save();

        long cur = is.getXp();
        long need = level.getNextXpRequirement(is.getLevel() + 1);
        int percent = (need > 0) ? (int)Math.floor(Math.min(100.0, (cur * 100.0) / need)) : 0;
        p.sendMessage(Text.color("&a경험치 &f+"+amount+"&a 구매 완료 (&f"+cur+"&7/&f"+need+"&7, &f"+percent+"%&a)"));
        open(p);
    }

    private boolean charge(Player p, double cost){
        if (cost <= 0) return true;
        try {
            VaultHook vh = plugin.getVault();
            if (vh == null || vh.getEconomy() == null){
                // If no economy, allow free (backwards-compat)
                return true;
            }
            if (!vh.has(p.getName(), cost)){
                p.sendMessage(Text.color("&c잔액이 부족합니다. &f" + String.format("%,.0f", cost)));
                return false;
            }
            vh.withdraw(p.getName(), cost);
            return true;
        } catch (Throwable t){
            return true;
        }
    }

    private boolean isLeader(Player p, IslandData is){
        if (is == null) return false;
        UUID owner = is.getOwner();
        return owner != null && owner.equals(p.getUniqueId());
    }
}
