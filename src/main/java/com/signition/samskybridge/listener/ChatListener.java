
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
    public ChatListener(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }

    private boolean isUuid(String s){
        if (s == null) return false;
        try { UUID.fromString(s); return true; }
        catch (IllegalArgumentException e){ return false; }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        // 섬 채팅 모드가 아니면 패스
        if (!store.isChatOn(p.getUniqueId())) return;

        Optional<IslandData> opt = store.findByMember(p.getUniqueId());
        if (!opt.isPresent()) return;
        IslandData is = opt.get();

        // 역할 라벨
        String role;
        if (p.getUniqueId().equals(is.getOwner())) role = plugin.getConfig().getString("messages.island-chat.role.owner","섬장");
        else if (is.getCoOwners().contains(p.getUniqueId())) role = plugin.getConfig().getString("messages.island-chat.role.coowner","부섬장");
        else role = plugin.getConfig().getString("messages.island-chat.role.member","섬원");

        // 섬 이름: UUID처럼 저장된 경우 오너 닉네임으로 대체
        String islandName = is.getName();
        if (islandName == null || isUuid(islandName)){
            String ownerName = Bukkit.getOfflinePlayer(is.getOwner()).getName();
            islandName = ownerName == null ? "섬" : ownerName;
        }

        String rendered = plugin.getConfig().getString("messages.island-chat.format", "&a[섬:&f<island>&a] &e[<role>] &f<player>&7: &r<message>")
                .replace("<island>", islandName)
                .replace("<role>", role)
                .replace("<player>", p.getDisplayName())
                .replace("<message>", e.getMessage());
        rendered = Text.color(rendered);

        // 글로벌 차단, 섬 멤버에게만 전송
        e.setCancelled(true);
        for (Player target : plugin.getServer().getOnlinePlayers()){
            UUID tu = target.getUniqueId();
            if (tu.equals(is.getOwner()) || is.getCoOwners().contains(tu) || is.getMembers().contains(tu)){
                target.sendMessage(rendered);
            }
        }
    }
}
