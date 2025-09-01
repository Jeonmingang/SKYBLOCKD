
package com.signition.samskybridge.util;

import com.signition.samskybridge.data.IslandData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Hud {

    public static void actionBar(Player p, String legacy) {
        if (p == null) return;
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Text.color(legacy)));
    }

    public static void title(Player p, String title, String subtitle, int in, int stay, int out){
        if (p == null) return;
        p.sendTitle(Text.color(title), Text.color(subtitle), in, stay, out);
    }

    public static void broadcastToIsland(IslandData is, String msg){
        for (Player t : Bukkit.getOnlinePlayers()){
            java.util.UUID u = t.getUniqueId();
            if (u.equals(is.getOwner()) || is.getCoOwners().contains(u) || is.getMembers().contains(u)){
                t.sendMessage(Text.color(msg));
            }
        }
    }

    public static void levelUp(org.bukkit.plugin.Plugin plugin, IslandData is, int newLevel, long needXp){
        String title = "&6섬 레벨 UP &fLv."+newLevel;
        String sub = "&7다음 요구치: &f"+String.format("%,d", needXp);
        for (Player t : Bukkit.getOnlinePlayers()){
            java.util.UUID u = t.getUniqueId();
            if (u.equals(is.getOwner()) || is.getCoOwners().contains(u) || is.getMembers().contains(u)){
                title(t, title, sub, 5, 40, 10);
                actionBar(t, "&a레벨업! &7현재 레벨 &f"+newLevel);
            }
        }
        final BossBar bar = Bukkit.createBossBar(Text.color("&e섬 레벨: &f"+newLevel), BarColor.YELLOW, BarStyle.SEGMENTED_10);
        for (Player t : Bukkit.getOnlinePlayers()){
            java.util.UUID u = t.getUniqueId();
            if (u.equals(is.getOwner()) || is.getCoOwners().contains(u) || is.getMembers().contains(u)){
                bar.addPlayer(t);
            }
        }
        bar.setProgress(0.0);
        new BukkitRunnable(){
            double p = 0.0;
            @Override public void run(){
                p += 0.2;
                if (p >= 1.0){ bar.setProgress(1.0); bar.removeAll(); cancel(); return; }
                bar.setProgress(Math.min(1.0, p));
            }
        }.runTaskTimer(plugin, 0L, 6L);
    }

    public static void upgraded(org.bukkit.plugin.Plugin plugin, IslandData is, String type, int before, int after, int step){
        String title = "&b섬 업그레이드 &7("+type+")";
        String sub = "&7"+before+" &f→ &a"+after+" &7(단계: &f"+step+"&7)";
        for (Player t : Bukkit.getOnlinePlayers()){
            java.util.UUID u = t.getUniqueId();
            if (u.equals(is.getOwner()) || is.getCoOwners().contains(u) || is.getMembers().contains(u)){
                title(t, title, sub, 5, 40, 10);
                actionBar(t, "&b업그레이드 완료: &f"+type+" &7단계 &f"+step);
                t.sendMessage(Text.color("&b[섬] 업그레이드 완료: &f"+type+" &7→ 현재값 &f"+after+" &7(단계 &f"+step+"&7)"));
            }
        }
    }
}
