
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
    public static void actionBar(Player p, String legacy){
        if (p == null) return;
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Text.color(legacy)));
    }

    public static void title(Player p, String title, String subtitle, int in, int stay, int out){
        if (p == null) return;
        p.sendTitle(Text.color(title), Text.color(subtitle), in, stay, out);
    }

    public static void levelUp(org.bukkit.plugin.Plugin plugin, IslandData is, int newLevel, long nextNeed){
        String title = "&6섬 레벨 UP &fLv."+newLevel;
        String sub = "&7다음 요구치: &f"+String.format("%,d", nextNeed);
        for (Player t : Bukkit.getOnlinePlayers()){
            if (is.hasMember(t.getUniqueId())){
                title(t, title, sub, 5, 40, 10);
                actionBar(t, "&a레벨업! &7현재 레벨 &f"+newLevel);
            }
        }
        final BossBar bar = Bukkit.createBossBar(Text.color("&e섬 레벨: &f"+newLevel), BarColor.YELLOW, BarStyle.SEGMENTED_10);
        int duration = plugin.getConfig().getInt("hud.bossbar.duration-ticks", 40);
        int stepTicks = plugin.getConfig().getInt("hud.bossbar.step-ticks", 10);
        for (Player t : Bukkit.getOnlinePlayers()) if (is.hasMember(t.getUniqueId())) bar.addPlayer(t);
        bar.setProgress(0.0);
        new BukkitRunnable(){
            double p = 0.0, inc = stepTicks<=0? 0.2 : (stepTicks*1.0/Math.max(1, duration));
            @Override public void run(){
                p += inc;
                if (p >= 1.0){ bar.setProgress(1.0); bar.removeAll(); cancel(); return; }
                bar.setProgress(Math.min(1.0, p));
            }
        }.runTaskTimer(plugin, 0L, Math.max(1, stepTicks));
    }

    public static void upgraded(org.bukkit.plugin.Plugin plugin, IslandData is, String what, int before, int after, int step){
        for (Player t : Bukkit.getOnlinePlayers()){
            if (is.hasMember(t.getUniqueId())){
                title(t, "&6섬 "+what+" 업그레이드", "&7단계 &f"+step+" &8| &7"+before+" ➜ &a"+after, 5, 40, 10);
                actionBar(t, "&e"+what+" 업그레이드 성공!");
            }
        }
    }
}
