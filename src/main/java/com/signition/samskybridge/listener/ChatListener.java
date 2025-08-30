
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent; import org.bukkit.entity.Player;
import java.util.Optional;
public class ChatListener implements Listener {
  private final Main plugin; private final DataStore store;
  public ChatListener(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  @EventHandler public void onChat(AsyncPlayerChatEvent e){
    Player p = e.getPlayer(); if (!store.isChatOn(p.getUniqueId())) return;
    Optional<IslandData> is = store.findByMember(p.getUniqueId()); if (!is.isPresent()) return;
    String prefix = plugin.getConfig().getString("messages.island-chat-prefix","&a[섬] &f");
    String msg = Text.color(prefix + p.getName() + ": " + e.getMessage());
    for (Player target : plugin.getServer().getOnlinePlayers()){
      if (is.get().isMember(target.getUniqueId())) target.sendMessage(msg);
    }
  }
}
