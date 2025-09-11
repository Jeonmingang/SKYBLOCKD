
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
  private final Main plugin;
  public JoinListener(Main plugin){ this.plugin=plugin; }
  @EventHandler public void onJoin(PlayerJoinEvent e){
    plugin.getRankingService().applyTo(e.getPlayer());
  }
}
