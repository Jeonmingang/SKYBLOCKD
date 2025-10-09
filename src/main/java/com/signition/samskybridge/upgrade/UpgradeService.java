package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.CommonPlaceholders;
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
 * Rewritten, defensive implementation for the 섬 업그레이드 GUI.
 * - Clean config schema (upgrade.size / upgrade.members / upgrade.xp)
 * - Gracefully shows "다음 단계 없음" when at max
 * - XP purchase is fully configurable (per-bottle XP and price)
 * - Keeps all previous commands/features intact (drop-in replacement)
 */
public class UpgradeService {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final VaultHook vault;

    public UpgradeService(Main plugin, LevelService level){
        this.plugin = plugin;
        this.level = level;
        this.store = plugin.getDataStore();
        this.vault = plugin.getVault();
    }

    // ------------------------------------------------------------------
    // GUI
    // ------------------------------------------------------------------
    public void open(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)){
            p.sendMessage(Text.color("&c섬장만 업그레이드를 사용할 수 있습니다."));
            return;
        }
        String title = plugin.getConfig().getString("upgrade.gui.title", plugin.getConfig().getString("upgrade.gui.title-upgrade", "섬 업그레이드"));
        int invSize = Math.max(9, Math.min(54, (plugin.getConfig().getInt("upgrade.gui.inventory-size", 27) / 9) * 9));
        Inventory inv = Bukkit.createInventory(p, invSize, Text.color(title));

        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int memSlot  = plugin.getConfig().getInt("upgrade.gui.slots.members", plugin.getConfig().getInt("upgrade.gui.slots.team", 14));
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);

        inv.setItem(sizeSlot, buildSizeItem(p));
        inv.setItem(memSlot,  buildMembersItem(p));
        if (plugin.getConfig().getBoolean("upgrade.xp.enabled", true)){
            inv.setItem(xpSlot,   buildXpItem(p));
        }
        p.openInventory(inv);
    }

    public void click(Player p, int rawSlot, boolean shiftClick){
        int sizeSlot = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
        int memSlot  = plugin.getConfig().getInt("upgrade.gui.slots.members", plugin.getConfig().getInt("upgrade.gui.slots.team", 14));
        int xpSlot   = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);
        if (rawSlot == sizeSlot) tryUpgradeSize(p);
        else if (rawSlot == memSlot) tryUpgradeMembers(p);
        else if (rawSlot == xpSlot) buyXp(p, shiftClick ? plugin.getConfig().getInt("upgrade.xp.shift-multiplier", 16) : 1);
    }

    private boolean isLeader(Player p, IslandData is){
        return (is != null && is.getOwner() != null && is.getOwner().equals(p.getUniqueId()));
    }

    // ------------------------------------------------------------------
    // Build items
    // ------------------------------------------------------------------
    private ItemStack item(String path, Material defMat, String defName, List<String> defLore, Map<String,String> ctx){
        String matName = plugin.getConfig().getString(path + ".material", defMat.name());
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = defMat;
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Text.color(plugin.getConfig().getString(path + ".name", defName)));
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        if (lore == null || lore.isEmpty()) lore = defLore;
        List<String> out = new ArrayList<>();
        for (String line : lore){
            String s = line;
            for (Map.Entry<String,String> e : ctx.entrySet()) s = s.replace(e.getKey(), e.getValue());
            // English placeholders
            s = s.replace("<current>", ctx.getOrDefault("{current}", ""))
                 .replace("<next>",    ctx.getOrDefault("{next}", ""))
                 .replace("<level>",   ctx.getOrDefault("{need}", ""))
                 .replace("<cost>",    ctx.getOrDefault("{cost}", ""))
                 .replace("<per>",     ctx.getOrDefault("{per}", ""));
            out.add(Text.color(s));
        }
        meta.setLore(out);
        it.setItemMeta(meta);
        return it;
    }

    private ItemStack buildSizeItem(Player p){
        ItemStack _TMP_IT_ = null;

        IslandData is = level.getIslandOf(p);
        int current = (is != null ? is.getSize() : plugin.getConfig().getInt("defaults.size", 120));
        NextTier tier = nextSizeTier(current);

        Map<String,String> ctx = new HashMap<>();
        ctx.put("{current}", String.valueOf(current));
        if (tier != null){
            ctx.put("{next}", String.valueOf(tier.to));
            ctx.put("{need}", String.valueOf(tier.needLevel));
            ctx.put("{cost}", String.valueOf(tier.cost));
        } else {
            ctx.put("{next}", "MAX");
            ctx.put("{need}", "-");
            ctx.put("{cost}", "-");
        }

        _TMP_IT_ = item("upgrade.gui.items.size",
                Material.GRASS_BLOCK,
                "&a섬 크기 업그레이드",
                Arrays.asList("&7섬 영역을 확장합니다.",
                              "&7현재: &f<current> &7→ 다음: &f<next>",
                              "&7필요 레벨: &a<level>",
                              "&7가격: &6<cost>"),
                ctx);

// Apply CommonPlaceholders to the lore (xp/need/percent/gauge/level)

int _lv_ = 1; long _xp_ = 0L;
try {
    IslandData _is_ = level.getIslandOf(p);
    if (_is_ != null){ _lv_ = Math.max(1, _is_.getLevel()); _xp_ = Math.max(0L, _is_.getXp()); }
} catch (Throwable ignore) {}

try {
    org.bukkit.inventory.meta.ItemMeta _meta_ = _TMP_IT_.getItemMeta();
    java.util.List<String> _lore_ = (_meta_ != null ? _meta_.getLore() : null);
    if (_lore_ != null && !_lore_.isEmpty()) {
        _lore_ = CommonPlaceholders.apply(_lore_, _lv_, _xp_, plugin);
        _meta_.setLore(_lore_);
        _TMP_IT_.setItemMeta(_meta_);
    }
} catch (Throwable ignore) {}

        return _TMP_IT_;
    }

    private ItemStack buildMembersItem(Player p){
        ItemStack _TMP_IT_ = null;

        IslandData is = level.getIslandOf(p);
        int current = (is != null ? is.getTeamMax() : plugin.getConfig().getInt("defaults.team-max", 4));
        NextTier tier = nextMembersTier(current);

        Map<String,String> ctx = new HashMap<>();
        ctx.put("{current}", String.valueOf(current));
        if (tier != null){
            ctx.put("{next}", String.valueOf(tier.to));
            ctx.put("{need}", String.valueOf(tier.needLevel));
            ctx.put("{cost}", String.valueOf(tier.cost));
        } else {
            ctx.put("{next}", "MAX");
            ctx.put("{need}", "-");
            ctx.put("{cost}", "-");
        }

        _TMP_IT_ = item("upgrade.gui.items.members",
                Material.DARK_OAK_PLANKS,
                "&a섬 인원 업그레이드",
                Arrays.asList("&7섬 허용 인원을 늘립니다.",
                              "&7현재: &f<current> &7→ 다음: &f<next>",
                              "&7필요 레벨: &a<level>",
                              "&7가격: &6<cost>"),
                ctx);

// Apply CommonPlaceholders to the lore (xp/need/percent/gauge/level)

int _lv_ = 1; long _xp_ = 0L;
try {
    IslandData _is_ = level.getIslandOf(p);
    if (_is_ != null){ _lv_ = Math.max(1, _is_.getLevel()); _xp_ = Math.max(0L, _is_.getXp()); }
} catch (Throwable ignore) {}

try {
    org.bukkit.inventory.meta.ItemMeta _meta_ = _TMP_IT_.getItemMeta();
    java.util.List<String> _lore_ = (_meta_ != null ? _meta_.getLore() : null);
    if (_lore_ != null && !_lore_.isEmpty()) {
        _lore_ = CommonPlaceholders.apply(_lore_, _lv_, _xp_, plugin);
        _meta_.setLore(_lore_);
        _TMP_IT_.setItemMeta(_meta_);
    }
} catch (Throwable ignore) {}

        return _TMP_IT_;
    }

    private ItemStack buildXpItem(Player p){
        ItemStack _TMP_IT_ = null;

        int per = plugin.getConfig().getInt("upgrade.xp.per-bottle-xp", 50);
        double price = plugin.getConfig().getDouble("upgrade.xp.price-per-bottle", 1000.0);
        Map<String,String> ctx = new HashMap<>();
        ctx.put("{per}", String.valueOf(per));
        ctx.put("{cost}", String.valueOf(price));

        _TMP_IT_ = item("upgrade.gui.items.xp",
                Material.EXPERIENCE_BOTTLE,
                "&a경험치 구매",
                Arrays.asList("&7섬 경험치를 구매합니다.",
                              "&7경험치 보틀 한 개당: &f<per>",
                              "&7가격: &6<cost>"),
                ctx);

// Apply CommonPlaceholders to the lore (xp/need/percent/gauge/level)

int _lv_ = 1; long _xp_ = 0L;
try {
    IslandData _is_ = level.getIslandOf(p);
    if (_is_ != null){ _lv_ = Math.max(1, _is_.getLevel()); _xp_ = Math.max(0L, _is_.getXp()); }
} catch (Throwable ignore) {}

try {
    org.bukkit.inventory.meta.ItemMeta _meta_ = _TMP_IT_.getItemMeta();
    java.util.List<String> _lore_ = (_meta_ != null ? _meta_.getLore() : null);
    if (_lore_ != null && !_lore_.isEmpty()) {
        _lore_ = CommonPlaceholders.apply(_lore_, _lv_, _xp_, plugin);
        _meta_.setLore(_lore_);
        _TMP_IT_.setItemMeta(_meta_);
    }
} catch (Throwable ignore) {}

        return _TMP_IT_;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    public void tryUpgradeSize(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)) return;
        int current = is.getSize();
        NextTier tier = nextSizeTier(current);
        if (tier == null){
            p.sendMessage(Text.color("&c더 이상 업그레이드할 단계가 없습니다."));
            return;
        }
        if (is.getLevel() < tier.needLevel){
            p.sendMessage(Text.color("&c요구 레벨: &f" + tier.needLevel));
            return;
        }
        if (vault == null || !vault.withdraw(p.getName(), tier.cost)){
            p.sendMessage(Text.color("&c잔액이 부족합니다. 필요: &6" + tier.cost));
            return;
        }
        is.setSize(tier.to);
        store.put(is);
        p.sendMessage(Text.color("&a섬 반경이 &f" + current + "&a → &f" + tier.to + "&a 으로 확장되었습니다!"));
        open(p);
    }

    public void tryUpgradeMembers(Player p){
        IslandData is = level.getIslandOf(p);
        if (!isLeader(p, is)) return;
        int current = is.getTeamMax();
        NextTier tier = nextMembersTier(current);
        if (tier == null){
            p.sendMessage(Text.color("&c더 이상 업그레이드할 단계가 없습니다."));
            return;
        }
        if (is.getLevel() < tier.needLevel){
            p.sendMessage(Text.color("&c요구 레벨: &f" + tier.needLevel));
            return;
        }
        if (vault == null || !vault.withdraw(p.getName(), tier.cost)){
            p.sendMessage(Text.color("&c잔액이 부족합니다. 필요: &6" + tier.cost));
            return;
        }
        is.setTeamMax(tier.to);
        store.put(is);
        p.sendMessage(Text.color("&a섬 인원이 &f" + current + "&a → &f" + tier.to + "&a 으로 증가했습니다!"));
        open(p);
    }

    public void buyXp(Player p, int count){
        if (!plugin.getConfig().getBoolean("upgrade.xp.enabled", true)) return;
        count = Math.max(1, Math.min(count, plugin.getConfig().getInt("upgrade.xp.max-per-click", 64)));

        int per = plugin.getConfig().getInt("upgrade.xp.per-bottle-xp", 50);
        double price = plugin.getConfig().getDouble("upgrade.xp.price-per-bottle", 1000.0);
        double total = price * count;

        if (vault == null || !vault.withdraw(p.getName(), total)){
            p.sendMessage(Text.color("&c잔액이 부족합니다. 필요: &6" + total));
            return;
        }
        long add = (long) per * count;
        level.addXp(p, add);
        p.sendMessage(Text.color("&a섬 경험치 &f" + add + "&a 획득 (&fx" + count + "&a)"));
        open(p);
    }

    // ------------------------------------------------------------------
    // Tiers
    // ------------------------------------------------------------------
    private static class NextTier{
        final int to;
        final int needLevel;
        final double cost;
        NextTier(int to, int needLevel, double cost){
            this.to = to; this.needLevel = needLevel; this.cost = cost;
        }
    }

    private NextTier nextSizeTier(int current){
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
        if (sec == null) return null;
        List<String> keys = new ArrayList<>(sec.getKeys(false));
        Collections.sort(keys); // numeric keys okay
        NextTier best = null;
        for (String k : keys){
            ConfigurationSection t = sec.getConfigurationSection(k);
            if (t == null) continue;
            int to = t.getInt("to", t.getInt("range", current));
            int need = t.getInt("need-level", t.getInt("need", 0));
            double cost = t.getDouble("cost", 0.0);
            if (to > current && (best == null || to < best.to)){
                best = new NextTier(to, need, cost);
            }
        }
        return best;
    }

    private NextTier nextMembersTier(int current){
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.members.tiers");
        if (sec == null) return null;
        List<String> keys = new ArrayList<>(sec.getKeys(false));
        Collections.sort(keys);
        NextTier best = null;
        for (String k : keys){
            ConfigurationSection t = sec.getConfigurationSection(k);
            if (t == null) continue;
            int to = t.getInt("to", t.getInt("next", current));
            int need = t.getInt("need-level", t.getInt("need", 0));
            double cost = t.getDouble("cost", 0.0);
            if (to > current && (best == null || to < best.to)){
                best = new NextTier(to, need, cost);
            }
        }
        return best;
    }
}
