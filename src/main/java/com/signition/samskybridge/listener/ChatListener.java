
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;
import java.util.UUID;

public class ChatListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    public ChatListener(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if (!store.isChatOn(p.getUniqueId())) return;

        Optional<IslandData> opt = store.findByMember(p.getUniqueId());
        if (!opt.isPresent()) return;
        IslandData is = opt.get();

        // role label from config
        String role;
        if (p.getUniqueId().equals(is.getOwner())){
            role = plugin.getConfig().getString("messages.island-chat.role.owner","섬장");
        } else if (is.getCoOwners().contains(p.getUniqueId())){
            role = plugin.getConfig().getString("messages.island-chat.role.coowner","부섬장");
        } else {
            role = plugin.getConfig().getString("messages.island-chat.role.member","섬원");
        }

        String islandName = is.getName();
        // UUID명은 닉네임으로 보정
        try{
            UUID.fromString(islandName);
            String ownerName = Bukkit.getOfflinePlayer(is.getOwner()).getName();
            islandName = ownerName == null ? "섬" : ownerName;
        }catch(IllegalArgumentException ignore){}

        // format
        String fmt = plugin.getConfig().getString("messages.island-chat.format", "&a[섬채팅] &7[<island>] &f<role> &r<player>&7: &r<message>");
        String rendered = fmt
                .replace("<island>", islandName)
                .replace("<role>", role)
                .replace("<player>", p.getDisplayName())
                .replace("<message>", e.getMessage());
        rendered = Text.color(rendered);

        // cancel global and send only to island members
        e.setCancelled(true);
        for (Player target : plugin.getServer().getOnlinePlayers()){
            UUID tu = target.getUniqueId();
            if (tu.equals(is.getOwner()) || is.getCoOwners().contains(tu) || is.getMembers().contains(tu)){
                target.sendMessage(rendered);
            }
        }
    }
}
