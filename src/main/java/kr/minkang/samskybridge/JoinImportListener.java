
package kr.minkang.samskybridge;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class JoinImportListener implements Listener {
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
