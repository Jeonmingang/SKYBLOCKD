package com.signition.samskybridge.listener;

import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final org.bukkit.plugin.Plugin plugin;
    private final BentoSync bento;
    private final int teamMaxDefault;

    public JoinListener(org.bukkit.plugin.Plugin plugin, BentoSync bento){
        this.plugin = plugin;
        this.bento = bento;
        this.teamMaxDefault = plugin.getConfig().getInt("upgrades.team.levels.1.team", 4);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (!plugin.getConfig().getBoolean("join.reapply.teammax", true)) return;
        Player p = e.getPlayer();
        // naive: use highest configured team value as reapply (safe)
        int max = teamMaxDefault;
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades.team.levels");
        if (sec != null) for (String k : sec.getKeys(false)){
            max = Math.max(max, plugin.getConfig().getInt("upgrades.team.levels."+k+".team", max));
        }
        bento.reapplyOnJoin(p, max);
    }
}
