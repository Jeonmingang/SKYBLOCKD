package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 섬 채팅 라우팅 서비스.
 * - /섬 채팅 토글과 연동 (IslandChatCommandHook가 toggle 호출)
 * - 채팅은 한 번만 전송되도록 중복 방지
 * - 기본 채팅은 취소하고, 같은 섬에게만 전달
 * - 콘솔(버킷)에도 동일한 로그 출력
 *
 * Spigot/CatServer 1.16.5, Java 8 호환.
 */
public class IslandChatService implements Listener {

    private final Main plugin;

    // /is, /island, /섬, /sky 등 루트 명령어를 허용
    private final Set<String> aliases = new HashSet<String>(Arrays.asList("is","island","섬","sky","스카이"));

    // 섬채팅 토글 상태
    private final Set<UUID> toggled = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    // 동일 메시지 2중 발송 방지용 (아주 짧은 시간창)
    private final Map<UUID, Last> lastSent = new ConcurrentHashMap<UUID, Last>();
    private static final long DUP_WINDOW_MS = 300L;

    public IslandChatService(Main plugin){
        this.plugin = plugin;
    }

    /** 외부에서 호출하는 재로딩 훅 (config에 별도 alias 목록이 있으면 반영) */
    public void reload(){
        try {
            List<String> list = plugin.getConfig().getStringList("island-chat.aliases");
            if (list == null || list.isEmpty()) list = plugin.getConfig().getStringList("islandChat.aliases");
            if (list == null || list.isEmpty()) list = plugin.getConfig().getStringList("chat.island.aliases");
            if (list != null && !list.isEmpty()) {
                Set<String> newSet = new HashSet<String>();
                for (String s : list) {
                    if (s != null) {
                        String t = s.trim();
                        if (!t.isEmpty()) newSet.add(t.toLowerCase(Locale.ROOT));
                    }
                }
                if (!newSet.isEmpty()) {
                    aliases.clear();
                    aliases.addAll(newSet);
                }
            }
        } catch (Throwable ignored) {
            // 설정이 없어도 빌드/런타임 문제 없게 no-op
        }
    }

    public boolean isAlias(String root){
        return aliases.contains(root.toLowerCase(Locale.ROOT));
    }

    public boolean isToggled(UUID uuid){
        return toggled.contains(uuid);
    }

    /** true면 on, false면 off */
    public boolean toggle(UUID uuid){
        if (toggled.contains(uuid)){
            toggled.remove(uuid);
            return false;
        }else{
            toggled.add(uuid);
            return true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAsyncChat(AsyncPlayerChatEvent e){
        final Player sender = e.getPlayer();
        if (!isToggled(sender.getUniqueId())) return;

        // 일반 전체채팅은 막고, 섬 채팅으로만 보냄
        e.setCancelled(true);

        final String msg = e.getMessage();

        // 중복 방지
        if (isDuplicate(sender.getUniqueId(), msg)) return;

        // 대상자 계산 (같은 섬 소속)
        IslandData island = plugin.getLevelService().getIslandOf(sender);
        String line = Text.color("&a[섬채팅] &f" + sender.getName() + "&7: &f" + msg);

        if (island == null){
            // 섬이 없으면 자기 자신에게만 알림 + 콘솔 로그
            sender.sendMessage(line);
            Bukkit.getConsoleSender().sendMessage(line);
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()){
            IslandData other = plugin.getLevelService().getIslandOf(online);
            if (other != null && other.equals(island)){
                online.sendMessage(line);
            }
        }
        // 콘솔에도 출력
        Bukkit.getConsoleSender().sendMessage(line);
    }

    private boolean isDuplicate(UUID uuid, String msg){
        long now = System.currentTimeMillis();
        Last prev = lastSent.get(uuid);
        if (prev != null && prev.msg.equals(msg) && (now - prev.time) < DUP_WINDOW_MS){
            return true;
        }
        lastSent.put(uuid, new Last(now, msg));
        return false;
    }

    private static final class Last {
        final long time;
        final String msg;
        Last(long time, String msg){
            this.time = time;
            this.msg = msg;
        }
    }
}
