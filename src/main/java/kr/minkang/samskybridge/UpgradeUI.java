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

public class UpgradeUI {

    private Economy econ;

    
public void open(Player p, IslandData data) {
    Inventory inv = Bukkit.createInventory(p, 27, color("&b섬 업그레이드"));

    if (econ == null) {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) econ = rsp.getProvider();
    }

    Main plugin = (Main) Bukkit.getPluginManager().getPlugin("SamSkyBridge");

    int baseS = plugin.getConfig().getInt("upgrade.size.base-radius", 50);
    int stepS = plugin.getConfig().getInt("upgrade.size.step-radius", 10);
    int maxS = plugin.getConfig().getInt("upgrade.size.max-radius", 250);
    int nextS = Math.min(maxS, data.sizeRadius + stepS);
    int stepIndexS = Math.max(0, (data.sizeRadius - baseS) / Math.max(1, stepS)) + 1;
    int reqLvS = plugin.getConfig().getInt("upgrade.size.require-level.per-step", 1) * stepIndexS;
    double costS = plugin.getConfig().getDouble("upgrade.size.cost.per-step", 100000D) * stepIndexS;

    int currentSLevel = Math.max(0, (data.sizeRadius - baseS) / Math.max(1, stepS)) + 1;
            int effectiveS = BentoBridge.getProtectionRange(p);
            ItemStack size = card(Material.MAP,
            color("&a보호 반경 확장"),
            java.util.Arrays.asList(
                    color("&7현재 반경: &f" + data.sizeRadius),
                    color("&7다음 반경: &f" + nextS),
                    color("&7요구 레벨: &f" + reqLvS),
                    color("&7가격: &f" + format(costS)),
                            color("&7현재 업그레이드 단계: &f" + currentSLevel),
                            color("&7실제 적용 반경: &f" + (effectiveS>0?effectiveS:data.sizeRadius))
            )
    );

    int baseT = plugin.getConfig().getInt("upgrade.team.base-members", 2);
    int stepT = plugin.getConfig().getInt("upgrade.team.step-members", 1);
    int maxT = plugin.getConfig().getInt("upgrade.team.max-members", 10);
    int nextT = Math.min(maxT, data.teamMax + stepT);
    int stepIndexT = Math.max(0, (data.teamMax - baseT) / Math.max(1, stepT)) + 1;
    int reqLvT = plugin.getConfig().getInt("upgrade.team.require-level.per-step", 1) * stepIndexT;
    double costT = plugin.getConfig().getDouble("upgrade.team.cost.per-step", 50000D) * stepIndexT;

    int currentTLevel = Math.max(0, (data.teamMax - baseT) / Math.max(1, stepT)) + 1;
            int effectiveT = BentoBridge.getTeamMax(p);
            ItemStack team = card(Material.PLAYER_HEAD,
            color("&b팀 최대 인원 확장"),
            java.util.Arrays.asList(
                    color("&7현재 인원: &f" + data.teamMax),
                    color("&7다음 인원: &f" + nextT),
                    color("&7요구 레벨: &f" + reqLvT),
                    color("&7가격: &f" + format(costT)),
                            color("&7현재 업그레이드 단계: &f" + currentTLevel),
                            color("&7실제 적용 인원: &f" + (effectiveT>0?effectiveT:data.teamMax))
            )
    );

    int xpGain = plugin.getConfig().getInt("upgrade.level.xp.per-purchase", 50);
    double xpCost = plugin.getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);
    int nextReq = Leveling.requiredXpForLevel(plugin, data.level + 1);
    int remaining = Math.max(0, nextReq - data.xp);

    ItemStack level = card(Material.EXPERIENCE_BOTTLE,
            color("&d섬 레벨 경험치 구매"),
            java.util.Arrays.asList(
                    color("&7현재 레벨: &f" + data.level),
                    color("&7현재 경험치: &f" + data.xp + " &7/ &f" + nextReq),
                    color("&7다음 레벨까지 남은: &f" + remaining),
                    color("&7추가 경험치: &f" + xpGain),
                    color("&7가격: &f" + format(xpCost))
            )
    );

    inv.setItem(11, size);
    inv.setItem(15, team);
    inv.setItem(13, level);

    p.openInventory(inv);
}
