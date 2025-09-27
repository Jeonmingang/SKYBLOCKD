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

    /** 섬 채팅 토글 */
    public boolean toggle(Player p){
        return toggle(p.getUniqueId());
    }

    public boolean toggle(java.util.UUID id){
        if (id == null) return false;
        if (toggled.contains(id)) { toggled.remove(id); return false; }
        else { toggled.add(id); return true; }
    }
{
        UUID id = p.getUniqueId();
        if (toggled.contains(id)) toggled.remove(id);
        else toggled.add(id);
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
        Player p = e.getPlayer();
        if (!toggled.contains(p.getUniqueId())) return;
        String msg = e.getMessage();
        // 간단히 같은 섬의 온라인 멤버에게만 전달 (소유자 기준 브로드캐스트는 Bento 연동에 위임)
        for (Player online : Bukkit.getOnlinePlayers()){
            // 동일 월드 기준 최소한의 필터 (정교한 멤버십 확인은 BentoSync가 담당)
            if (online.getWorld().equals(p.getWorld())){
                online.sendMessage("§a[섬채팅] §f" + p.getName() + ": §7" + msg);
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
