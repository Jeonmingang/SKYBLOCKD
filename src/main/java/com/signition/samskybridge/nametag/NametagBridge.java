package com.signition.samskybridge.nametag;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Minimal, safe nametag updater that reads config:
 *   nametag.enabled (boolean)
 *   nametag.owner-format/member-format/visitor-format (String)
 *   nametag.teams.owner/member/visitor (String team ids)
 *
 * It uses the main scoreboard and periodically re-applies prefixes
 * according to BentoSync's island membership/owner checks.
 *
 * No core logic change, pure wiring.
 */
public class NametagBridge implements Listener {

    private final Main plugin;
    private final BentoSync bento;
    private Scoreboard board;
    private Team teamOwner, teamMember, teamVisitor;
    private String fmtOwner, fmtMember, fmtVisitor;
    private String idOwner, idMember, idVisitor;
    private BukkitTask task;
    private final Set<UUID> known = new HashSet<>();

    public NametagBridge(Main plugin, BentoSync bento) {
        this.plugin = plugin;
        this.bento = bento;
        setupFromConfig();
    }

    private void setupFromConfig() {
        this.board = Bukkit.getScoreboardManager().getMainScoreboard();

        this.idOwner = plugin.getConfig().getString("nametag.teams.owner", "samsky_owner");
        this.idMember = plugin.getConfig().getString("nametag.teams.member", "samsky_member");
        this.idVisitor = plugin.getConfig().getString("nametag.teams.visitor", "samsky_visitor");

        this.fmtOwner = Text.color(plugin.getConfig().getString("nametag.owner-format", "&6[섬장] &f"));
        this.fmtMember = Text.color(plugin.getConfig().getString("nametag.member-format", "&a[섬원] &f"));
        this.fmtVisitor = Text.color(plugin.getConfig().getString("nametag.visitor-format", "&7"));

        this.teamOwner = getOrCreateTeam(idOwner);
        this.teamMember = getOrCreateTeam(idMember);
        this.teamVisitor = getOrCreateTeam(idVisitor);

        // Apply prefixes
        applyPrefix(teamOwner, fmtOwner);
        applyPrefix(teamMember, fmtMember);
        applyPrefix(teamVisitor, fmtVisitor);
    }

    private Team getOrCreateTeam(String id) {
        Team t = board.getTeam(id);
        if (t == null) {
            t = board.registerNewTeam(id);
        }
        // Ensure friendly fire options remain untouched; we only set prefix
        return t;
    }

    private void applyPrefix(Team t, String prefix) {
        try {
            t.setPrefix(prefix);
        } catch (Throwable ignored) {}
    }

    private void assign(Player p) {
        try {
            // decide group
            Team target = teamVisitor;
            if (bento != null) {
                if (bento.isOwner(p)) {
                    target = teamOwner;
                } else if (bento.isMember(p)) {
                    target = teamMember;
                } else {
                    target = teamVisitor;
                }
            }
            // remove from others first
            if (teamOwner != null) teamOwner.removeEntry(p.getName());
            if (teamMember != null) teamMember.removeEntry(p.getName());
            if (teamVisitor != null) teamVisitor.removeEntry(p.getName());
            // add to target
            if (target != null) target.addEntry(p.getName());
            known.add(p.getUniqueId());
        } catch (Throwable ignored) {}
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("nametag.enabled", true)) return;
        // Initial assign for online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            assign(p);
        }
        // periodic refresh (every 2 seconds)
        long period = 40L;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                assign(p);
            }
        }, period, period);
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
        known.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(plugin, () -> assign(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        known.remove(e.getPlayer().getUniqueId());
    }
}
