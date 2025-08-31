
package com.signition.samskybridge.listener;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Optional;
import java.util.UUID;

public class ChatListener implements Listener {
  private final Main plugin; private final DataStore store;
  public ChatListener(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onChat(AsyncPlayerChatEvent e){
    Player p = e.getPlayer();
    if (!store.isChatOn(p.getUniqueId())) return;

    Optional<IslandData> opt = store.findByMember(p.getUniqueId());
    if (!opt.isPresent()) return;
    IslandData is = opt.get();

    String fmt = plugin.getConfig().getString("messages.island-chat.format",
            "&a[섬:&f<island>&a] &e[<role>] &f<player>&7: &r<message>");
    String roleOwner = plugin.getConfig().getString("messages.island-chat.role.owner", "섬장");
    String roleCo = plugin.getConfig().getString("messages.island-chat.role.coowner", "부섬장");
    String roleMem = plugin.getConfig().getString("messages.island-chat.role.member", "섬원");

    String role;
    UUID u = p.getUniqueId();
    if (is.getOwner()!=null && is.getOwner().equals(u)) role = roleOwner;
    else if (is.getCoOwners().contains(u)) role = roleCo;
    else role = roleMem;

    String islandName = (is.getName()==null || is.getName().matches("^[0-9a-fA-F\-]{36}$")) ? java.util.Optional.ofNullable(org.bukkit.Bukkit.getOfflinePlayer(is.getOwner()).getName()).orElse("섬") : is.getName();
    String rendered = fmt.replace("<island>", islandName)
            .replace("<role>", role)
            .replace("<player>", p.getDisplayName())
            .replace("<message>", e.getMessage());
    rendered = Text.color(rendered);

    e.setCancelled(true); // prevent global broadcast & duplicate
    for (Player target : plugin.getServer().getOnlinePlayers()){
      UUID tu = target.getUniqueId();
      if (tu.equals(is.getOwner()) || is.getCoOwners().contains(tu) || is.getMembers().contains(tu)){
        target.sendMessage(rendered);
      }
    }
  }
}
