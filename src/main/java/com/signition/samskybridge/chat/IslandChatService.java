package com.signition.samskybridge.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandChatService implements Listener {

    private final Plugin plugin;
    private final Set<UUID> toggled = new HashSet<UUID>();

    public IslandChatService(Plugin plugin){
        this.plugin = plugin;
    }

    /** 섬 채팅 토글 (Player) : true=활성화, false=해제 */
    public boolean toggle(Player player){
        return toggle(player.getUniqueId());
    }

    /** 섬 채팅 토글 (UUID) : true=활성화, false=해제 */
    public boolean toggle(UUID id){
        if (id == null) return false;
        if (toggled.contains(id)) { toggled.remove(id); return false; }
        else { toggled.add(id); return true; }
    }

    /** 컨피그에서 별칭 확인 */
    public boolean isAlias(String s){
        List<String> list = plugin.getConfig().getStringList("chat.aliases");
        if (list == null || list.isEmpty()) list = java.util.Arrays.asList("섬채팅","islandchat");
        if (s == null) return false;
        for (String a : list){ if (s.equalsIgnoreCase(a)) return true; }
        return false;
    }

    /** 채팅 훅: 토글된 플레이어의 일반 채팅을 섬 채팅으로 전환 */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if (!toggled.contains(player.getUniqueId())) return;
        String msg = e.getMessage();
        // 같은 월드의 온라인 사용자에게 브로드캐스트(심플 폴백)
        for (Player online : Bukkit.getOnlinePlayers()){
            if (online.getWorld().equals(player.getWorld())){
                online.sendMessage("§a[섬채팅] §f" + player.getName() + ": §7" + msg);
            }
        }
        e.setCancelled(true);
    }

    /** 소유자 UUID 대상 전송(명령 훅에서 사용) */
    public void sendToIsland(UUID islandOwnerUUID, Player sender, String msg){
        try {
            Player owner = Bukkit.getPlayer(islandOwnerUUID);
            if (owner != null){
                owner.sendMessage(msg);
            }
        } catch (Throwable ignored){}
    }
}
