
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {
  private final Main plugin; private final DataStore store;
  public ChatListener(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }
  @EventHandler public void onChat(AsyncPlayerChatEvent e){
    Player p = e.getPlayer();
    if (store.isChatOn(p.getUniqueId())){
      e.setCancelled(true);
      store.findByMember(p.getUniqueId()).ifPresent(is -> {
        for (Player t : p.getWorld().getPlayers()){
          if (is.hasMember(t.getUniqueId())) t.sendMessage(Text.color("&a[섬채팅] &f"+p.getName()+"&7: &r"+e.getMessage()));
        }
      });
    }
  }
}
