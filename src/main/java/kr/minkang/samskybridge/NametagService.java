
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagService implements Listener {

    private final Main plugin;

    public NametagService(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        refreshFor(e.getPlayer());
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            refreshFor(p);
        }
    }

    public void refreshFor(Player p) {
        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);

        int level = d != null ? d.level : 0;
        int team = d != null ? d.teamMax : -1;
        int size = d != null ? d.sizeRadius : -1;

        int realSize = BentoBridge.getProtectionRange(p);
        if (realSize > 0) size = realSize;
        int realTeam = BentoBridge.getTeamMax(p);
        if (realTeam > 0) team = realTeam;

        boolean isOwner = d != null && d.owner != null && d.owner.equals(p.getUniqueId());

        String rank = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
        try {
            java.lang.reflect.Method getRank = plugin.storage.getClass().getMethod("getRank", java.util.UUID.class);
            Object r = getRank.invoke(plugin.storage, p.getUniqueId());
            if (r != null) rank = String.valueOf(r);
        } catch (Throwable ignored) {}

        String ownerFmt = plugin.getConfig().getString("tab_prefix.owner",
                "&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7| 크기 <size> &7| 인원 <team> &7] &r");
        String memberFmt = plugin.getConfig().getString("tab_prefix.member",
                "&7[ &a섬원 섬장의섬랭크 &f<rank>위 &7| 섬장의 &blv.<level> &7| 크기 <size> &7| 인원 <team> &7] &r");

        String prefix = (isOwner ? ownerFmt : memberFmt)
                .replace("<rank>", rank)
                .replace("<level>", String.valueOf(level))
                .replace("<size>", size > 0 ? String.valueOf(size) : "-")
                .replace("<team>", team > 0 ? String.valueOf(team) : "-");

        String un = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
        prefix = prefix.replace(un + "위", un);

        p.setPlayerListName(color(prefix + p.getName()));

        String ownerTag = plugin.getConfig().getString("nametag.owner-format", "&7[섬장 lv.<level>|#<rank>] ");
        String memberTag = plugin.getConfig().getString("nametag.member-format", "&7[섬원 lv.<level>|#<rank>] ");
        String nameTag = (isOwner ? ownerTag : memberTag)
                .replace("<rank>", rank).replace("<level>", String.valueOf(level));
        nameTag = color(nameTag);
        nameTag = nameTag.replace(un + "위", un);
        applyScoreboardPrefix(p, nameTag);
    }

    private void applyScoreboardPrefix(Player p, String pref) {
        String prefix = ChatColor.translateAlternateColorCodes('&', pref);
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = ("samsky_" + p.getUniqueId().toString()).substring(0, 16);
        Team t = sb.getTeam(teamName);
        if (t == null) t = sb.registerNewTeam(teamName);
        t.setPrefix(prefix);
        if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
