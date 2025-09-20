package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    public void openGui(Player p){
        String title = plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드");
        Inventory inv = Bukkit.createInventory(null, 27, Text.color(title));
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());

        // Size
        int sizeNow = is.getSize();
        int sizeStep = plugin.getConfig().getInt("upgrade.size.step", 10);
        int sizeBase = plugin.getConfig().getInt("upgrade.size.base", 50);
        int sizeMaxSteps = plugin.getConfig().getInt("upgrade.size.max-steps", 10);
        int sizeStepNow = Math.max(0,(sizeNow - sizeBase) / Math.max(1, sizeStep));
        int sizeStepMax = sizeMaxSteps;
        int sizeNext = sizeBase + Math.min(sizeMaxSteps, sizeStepNow + 1) * sizeStep;
        int sizeNeedLv = (sizeStepNow + 1) * plugin.getConfig().getInt("upgrade.size.need-level-per-step", 1);
        long sizeCost = (long)( (sizeStepNow + 1) * plugin.getConfig().getLong("upgrade.size.cost-per-step", 10000) );

        List<String> sizeLore = plugin.getConfig().getStringList("upgrade.size.lore");
        if (sizeLore == null || sizeLore.isEmpty()){
            sizeLore = new ArrayList<>();
            sizeLore.add("&7현재 보호반경: &f{range} 블럭");
            sizeLore.add("&7다음 단계: &a{next} 블럭");
            sizeLore.add("&7요구 레벨: &bLv.{reqLevel}");
            sizeLore.add("&7필요 금액: &a{cost}");
            sizeLore.add("&7업그레이드 단계: &d{stepNow}/{stepMax}");
            sizeLore.add("&8클릭: 업그레이드");
        }
        String[] sizeLoreArr = new String[sizeLore.size()];
        for (int i=0;i<sizeLore.size();i++){
            sizeLoreArr[i] = sizeLore.get(i)
                .replace("{range}", String.valueOf(sizeNow))
                .replace("{next}", String.valueOf(sizeNext))
                .replace("{reqLevel}", String.valueOf(sizeNeedLv))
                .replace("{cost}", String.valueOf(sizeCost))
                .replace("{stepNow}", String.valueOf(sizeStepNow))
                .replace("{stepMax}", String.valueOf(sizeStepMax));
        }
        inv.setItem(plugin.getConfig().getInt("upgrade.size.slots.item", 14),
                named(new ItemStack(Material.SHIELD), plugin.getConfig().getString("upgrade.size.title","&b섬 보호반경 확장"), sizeLoreArr));

        // Team
        int teamNow = is.getTeamMax();
        int teamStep = plugin.getConfig().getInt("upgrade.team.step", 1);
        int teamBase = plugin.getConfig().getInt("upgrade.team.base", 1);
        int teamMaxSteps = plugin.getConfig().getInt("upgrade.team.max-steps", 9);
        int teamStepNow = Math.max(0,(teamNow - teamBase) / Math.max(1, teamStep));
        int teamStepMax = teamMaxSteps;
        int teamNext = teamBase + Math.min(teamMaxSteps, teamStepNow + 1) * teamStep;
        int teamNeedLv = (teamStepNow + 1) * plugin.getConfig().getInt("upgrade.team.need-level-per-step", 2);
        long teamCost = (long)( (teamStepNow + 1) * plugin.getConfig().getLong("upgrade.team.cost-per-step", 20000) );

        List<String> teamLore = plugin.getConfig().getStringList("upgrade.team.lore");
        if (teamLore == null || teamLore.isEmpty()){
            teamLore = new ArrayList<>();
            teamLore.add("&7현재 한도: &f{current}명");
            teamLore.add("&7다음 단계: &a{next}명");
            teamLore.add("&7요구 레벨: &bLv.{reqLevel}");
            teamLore.add("&7필요 금액: &a{cost}");
            teamLore.add("&7업그레이드 단계: &d{stepNow}/{stepMax}");
            teamLore.add("&8클릭: 업그레이드");
        }
        String[] teamLoreArr = new String[teamLore.size()];
        for (int i=0;i<teamLore.size();i++){
            teamLoreArr[i] = teamLore.get(i)
                .replace("{current}", String.valueOf(teamNow))
                .replace("{next}", String.valueOf(teamNext))
                .replace("{reqLevel}", String.valueOf(teamNeedLv))
                .replace("{cost}", String.valueOf(teamCost))
                .replace("{stepNow}", String.valueOf(teamStepNow))
                .replace("{stepMax}", String.valueOf(teamStepMax));
        }
        inv.setItem(plugin.getConfig().getInt("upgrade.team.slots.item", 12),
                named(new ItemStack(Material.PLAYER_HEAD), plugin.getConfig().getString("upgrade.team.title","&b팀 인원 확장"), teamLoreArr));

        // XP purchase
        int slotXp = plugin.getConfig().getInt("upgrade.xp.slots.item", 22);
        String xpTitle = plugin.getConfig().getString("upgrade.xp.title","&d경험치 구매");
        List<String> xpLore = plugin.getConfig().getStringList("upgrade.xp.lore");
        if (xpLore == null || xpLore.isEmpty()){
            xpLore = new ArrayList<>();
            xpLore.add("&7구매량: &f{amount} XP");
            xpLore.add("&7가격: &a{price}");
            xpLore.add("&8클릭: 구매");
        }
        String[] xpLoreArr = new String[xpLore.size()];
        long amount = plugin.getConfig().getLong("upgrade.xp.amount", 100);
        long price  = plugin.getConfig().getLong("upgrade.xp.price", 1000);
        for (int i=0;i<xpLore.size();i++){
            xpLoreArr[i] = xpLore.get(i).replace("{amount}", String.valueOf(amount)).replace("{price}", String.valueOf(price));
        }
        inv.setItem(slotXp, named(new ItemStack(Material.EXPERIENCE_BOTTLE), xpTitle, xpLoreArr));

        p.openInventory(inv);
    }

    public void click(Player p, int slot, boolean shift){
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        // Size upgrade
        if (slot == plugin.getConfig().getInt("upgrade.size.slots.item", 14)){
            int sizeNow = is.getSize();
            int sizeStep = plugin.getConfig().getInt("upgrade.size.step", 10);
            int sizeBase = plugin.getConfig().getInt("upgrade.size.base", 50);
            int sizeMaxSteps = plugin.getConfig().getInt("upgrade.size.max-steps", 10);
            int sizeStepNow = Math.max(0,(sizeNow - sizeBase) / Math.max(1, sizeStep));
            int need = (sizeStepNow + 1) * plugin.getConfig().getInt("upgrade.size.need-level-per-step", 1);
            long cost = (long)( (sizeStepNow + 1) * plugin.getConfig().getLong("upgrade.size.cost-per-step", 10000) );
            if (is.getLevel() < need){
                p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.not-enough-level","&c요구 레벨 Lv.<need> 부족").replace("<need>", String.valueOf(need))));
                return;
            }
            if (cost > 0){
                if (!vault.has(p.getName(), cost) || !vault.withdraw(p.getName(), cost)){
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.not-enough-money","&c잔액 부족: <cost>").replace("<cost>", String.valueOf(cost))));
                    return;
                }
            }
            int next = Math.min(sizeBase + (sizeStepNow + 1) * sizeStep, sizeBase + sizeMaxSteps * sizeStep);
            is.setSize(next);
            p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.size-success","&a보호반경 확장! 현재 반경: <range>").replace("<range>", String.valueOf(next))));
            return;
        }

        // Team upgrade
        if (slot == plugin.getConfig().getInt("upgrade.team.slots.item", 12)){
            int teamNow = is.getTeamMax();
            int teamStep = plugin.getConfig().getInt("upgrade.team.step", 1);
            int teamBase = plugin.getConfig().getInt("upgrade.team.base", 1);
            int teamMaxSteps = plugin.getConfig().getInt("upgrade.team.max-steps", 9);
            int teamStepNow = Math.max(0,(teamNow - teamBase) / Math.max(1, teamStep));
            int need = (teamStepNow + 1) * plugin.getConfig().getInt("upgrade.team.need-level-per-step", 2);
            long cost = (long)( (teamStepNow + 1) * plugin.getConfig().getLong("upgrade.team.cost-per-step", 20000) );
            if (is.getLevel() < need){
                p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.not-enough-level","&c요구 레벨 Lv.<need> 부족").replace("<need>", String.valueOf(need))));
                return;
            }
            if (cost > 0){
                if (!vault.has(p.getName(), cost) || !vault.withdraw(p.getName(), cost)){
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.not-enough-money","&c잔액 부족: <cost>").replace("<cost>", String.valueOf(cost))));
                    return;
                }
            }
            int next = Math.min(teamBase + (teamStepNow + 1) * teamStep, teamBase + teamMaxSteps * teamStep);
            is.setTeamMax(next);
            p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.team-success","&a팀 인원 확장! 현재 한도: <team>").replace("<team>", String.valueOf(next))));
            return;
        }

        // XP purchase
        if (slot == plugin.getConfig().getInt("upgrade.xp.slots.item", 22)){
            long amount = plugin.getConfig().getLong("upgrade.xp.amount", 100);
            long price  = plugin.getConfig().getLong("upgrade.xp.price", 1000);
            if (price > 0){
                if (!vault.has(p.getName(), price) || !vault.withdraw(p.getName(), price)){
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.upgrade.not-enough-money","&c잔액 부족: <cost>").replace("<cost>", String.valueOf(price))));
                    return;
                }
            }
            is.addXp(amount);
            level.tryLevelUp(is);
            String m = plugin.getConfig().getString("messages.upgrade.xp-purchase-success","&a경험치 <amount> 구매! 현재 XP: <xp> (Lv.<level>)");
            m = m.replace("<amount>", String.valueOf(amount)).replace("<xp>", String.valueOf(is.getXp())).replace("<level>", String.valueOf(is.getLevel()));
            p.sendMessage(Text.color(m));
            return;
        }
    }

    private ItemStack named(ItemStack is, String title, String... lore){
        ItemMeta im = is.getItemMeta();
        if (im != null){
            im.setDisplayName(Text.color(title));
            List<String> l = new ArrayList<>();
            for (String s : lore) l.add(Text.color(s));
            im.setLore(l);
            is.setItemMeta(im);
        }
        return is;
    }
}
