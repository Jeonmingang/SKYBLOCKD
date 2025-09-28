
package kr.minkang.samskybridge;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinImportListener implements Listener {

    private final Main plugin;
    public JoinImportListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (BentoBridge.isAvailable()) {
            if (plugin.storage.getIslandByPlayer(p.getUniqueId()) == null) {
                BentoBridge.resolveFromBento(plugin, p);
            }
        }
        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d != null && d.owner != null && d.owner.equals(p.getUniqueId())) {
            plugin.applyOwnerTeamPerm(d.owner, d.teamMax);
        }
    }
}
