
package kr.minkang.samskybridge;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class JoinImportListener implements Listener {
    @org.bukkit.event.EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d != null && d.owner != null && d.owner.equals(p.getUniqueId())) {
            plugin.applyOwnerTeamPerm(d.owner, d.teamMax);
        }
    }

    private final Main plugin;
    public JoinImportListener(Main plugin) { this.plugin = plugin; }
    @EventHandler public void onJoin(PlayerJoinEvent e) {
        if (!BentoBridge.isAvailable()) return;
        Player p = e.getPlayer();
        if (plugin.storage.getIslandByPlayer(p.getUniqueId()) == null) {
            BentoBridge.resolveFromBento(plugin, p);
        }
    }
}
