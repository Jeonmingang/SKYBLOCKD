package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

public class UpgradeService {
    private final Main plugin;
    private final DataStore store;

    public UpgradeService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    private int stepByDelta(int current, int delta){
        if (delta <= 0) return current;
        return current / Math.max(1, delta);
    }

    private int costLevel(String keyBase, int step){
        int base = plugin.getConfig().getInt("upgrade."+keyBase+".level-cost-base", 5);
        int mul  = plugin.getConfig().getInt("upgrade."+keyBase+".level-cost-mul", 2);
        return base + step * mul;
    }

    private long costMoney(String keyBase, int step){
        double base = plugin.getConfig().getDouble("upgrade."+keyBase+".money-cost-base", 1000.0);
        double mul  = plugin.getConfig().getDouble("upgrade."+keyBase+".money-cost-mul", 1.2);
        return (long)Math.floor(base * Math.pow(mul, step));
    }

    private static String money(long v){
        return String.format("%,d", v);
    }

    private ItemStack item(Material m, String name, String[] lore){
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        if (im != null){
            im.setDisplayName(Text.color(name));
            java.util.List<String> _l = new java.util.ArrayList<String>();
            for (String line : lore) _l.add(Text.color(line));
            im.setLore(_l);
            it.setItemMeta(im);
        }
        return it;
    }

    public void open(Player p){
        IslandData is = store.findByMember(p.getUniqueId()).orElseGet(() -> store.getOrCreate(p.getUniqueId()));

        int sizeDelta = plugin.getConfig().getInt("upgrade.size.delta", 10);
        int teamDelta = plugin.getConfig().getInt("upgrade.team.delta", 1);

        int sizeStep = stepByDelta(is.getSizeLevel(), sizeDelta);
        int teamStep = stepByDelta(is.getTeamLevel(), teamDelta);

        int sizeCostLevel = costLevel("size", sizeStep);
        long sizeCostMoney = costMoney("size", sizeStep);
        int teamCostLevel = costLevel("team", teamStep);
        long teamCostMoney = costMoney("team", teamStep);

        Inventory inv = Bukkit.createInventory(p, 27, Text.color(plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드")));

        inv.setItem(11, item(Material.PLAYER_HEAD,
                plugin.getConfig().getString("gui.upgrade.size.name","&a섬 크기 업그레이드 &7(Lv.<step>)").replace("<step>", String.valueOf(sizeStep)),
                new String[]{
                        plugin.getConfig().getString("gui.upgrade.size.lore.0","&7현재: &f<current> &7→ 다음: &f+<delta> &7(= &f<next>)")
                                .replace("<current>", String.valueOf(is.getSizeLevel()))
                                .replace("<delta>", String.valueOf(sizeDelta))
                                .replace("<next>", String.valueOf(is.getSizeLevel()+sizeDelta)),
                        plugin.getConfig().getString("gui.upgrade.size.lore.1","&7요구 레벨: &f<lv>").replace("<lv>", String.valueOf(sizeCostLevel)),
                        plugin.getConfig().getString("gui.upgrade.size.lore.2","&7요금: &f<money>").replace("<money>", money(sizeCostMoney)),
                        plugin.getConfig().getString("gui.upgrade.size.lore.3","&e클릭하여 업그레이드")
                }
        ));

        inv.setItem(15, item(Material.PLAYER_HEAD,
                plugin.getConfig().getString("gui.upgrade.team.name","&a팀 슬롯 업그레이드 &7(Lv.<step>)").replace("<step>", String.valueOf(teamStep)),
                new String[]{
                        plugin.getConfig().getString("gui.upgrade.team.lore.0","&7현재: &f<current> &7→ 다음: &f+<delta> &7(= &f<next>)")
                                .replace("<current>", String.valueOf(is.getTeamLevel()))
                                .replace("<delta>", String.valueOf(teamDelta))
                                .replace("<next>", String.valueOf(is.getTeamLevel()+teamDelta)),
                        plugin.getConfig().getString("gui.upgrade.team.lore.1","&7요구 레벨: &f<lv>").replace("<lv>", String.valueOf(teamCostLevel)),
                        plugin.getConfig().getString("gui.upgrade.team.lore.2","&7요금: &f<money>").replace("<money>", money(teamCostMoney)),
                        plugin.getConfig().getString("gui.upgrade.team.lore.3","&e클릭하여 업그레이드")
                }
        ));

        p.openInventory(inv);
    }

    public void click(Player p, int slot){
        IslandData is = store.findByMember(p.getUniqueId()).orElse(null);
        if (is == null) return;

        if (slot == 11){
            int delta = plugin.getConfig().getInt("upgrade.size.delta", 10);
            int step = stepByDelta(is.getSizeLevel(), delta);
            int reqLevel = costLevel("size", step);
            long reqMoney = costMoney("size", step);
            if (!pay(p, is, reqLevel, reqMoney)) return;
            is.setSizeLevel(is.getSizeLevel() + delta);
            int before = is.getSizeLevel()-plugin.getConfig().getInt("upgrade.size.delta", 10);
            com.signition.samskybridge.util.Hud.upgraded(plugin, is, "크기", before, is.getSizeLevel(), step);
            store.saveAsync();
            open(p);
            return;
        }
        if (slot == 15){
            int delta = plugin.getConfig().getInt("upgrade.team.delta", 1);
            int step = stepByDelta(is.getTeamLevel(), delta);
            int reqLevel = costLevel("team", step);
            long reqMoney = costMoney("team", step);
            if (!pay(p, is, reqLevel, reqMoney)) return;
            is.setTeamLevel(is.getTeamLevel() + delta);
            int before = is.getTeamLevel()-plugin.getConfig().getInt("upgrade.team.delta", 1);
            com.signition.samskybridge.util.Hud.upgraded(plugin, is, "팀", before, is.getTeamLevel(), step);
            store.saveAsync();
            open(p);
        }
    }

    private boolean pay(Player p, IslandData is, int reqLevel, long reqMoney){
        if (reqLevel>0 && is.getLevel() < reqLevel){
            p.sendMessage(Text.color("&c레벨 부족 &7(요구: "+reqLevel+")"));
            return false;
        }
        if (reqMoney > 0){
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null || rsp.getProvider() == null){
                p.sendMessage(Text.color("&cVault(경제) 플러그인이 없습니다. 업그레이드 불가."));
                return false;
            }
            Economy econ = rsp.getProvider();
            if (!econ.has(p, reqMoney)){
                p.sendMessage(Text.color("&c돈 부족 &7(요구: "+money(reqMoney)+")"));
                return false;
            }
            econ.withdrawPlayer(p, reqMoney);
        }
        if (reqLevel>0) // level requirement only; not consumed
        return true;
    }
}
