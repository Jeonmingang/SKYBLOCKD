
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Korean command bridge for BentoBox/BSkyBlock.
 * - Intercepts "/섬 <명령어>" and forwards to "/is <mapped>" when mapping exists.
 * - Lets local features (업그레이드/레벨/랭킹/채팅) pass through to existing executor.
 * - Shows Korean help for "/섬" and "/섬 도움말 [페이지]".
 * Java 8 / 1.16.5 compatible. No compile-time dependency on BentoBox.
 */
public class KoCommandBridgeListener implements Listener {

    private final Plugin plugin;
    private final Set<String> passthrough = new HashSet<String>(Arrays.asList(
            "업그레이드", "레벨", "랭킹", "채팅"
    ));

    // Map first Korean token -> "is ..." subcommand start
    private final Map<String, String> map = new HashMap<String, String>();

    public KoCommandBridgeListener(Plugin plugin) {
        this.plugin = plugin;

        // Team management
        map.put("초대", "team invite");      // /섬 초대 <닉>
        map.put("수락", "team accept");      // /섬 수락
        map.put("거절", "team reject");      // /섬 거절
        map.put("추방", "team kick");        // /섬 추방 <닉>
        map.put("탈퇴", "team leave");       // /섬 탈퇴
        map.put("알바", "team coop");        // /섬 알바 <닉>
        map.put("알바해제", "team uncoop");  // /섬 알바해제 <닉>

        // Island basics
        map.put("리셋", "reset");            // /섬 리셋 <설계도>
        map.put("정보", "info");             // /섬 정보 [플레이어]
        map.put("설정", "settings");         // /섬 설정
        map.put("이름설정", "setname");      // /섬 이름설정 <이름>
        map.put("이름초기화", "resetname");   // /섬 이름초기화
        map.put("언어", "language");         // /섬 언어 <lang>
        map.put("차단", "ban");              // /섬 차단 <플레이어>
        map.put("언밴", "unban");            // /섬 언밴 <플레이어>
        map.put("추방하기", "expel");         // /섬 추방하기 <플레이어>
        map.put("근처", "near");             // /섬 근처

        // Homes
        map.put("집목록", "homes");          // /섬 집목록
        map.put("집설정", "sethome");        // /섬 집설정 [번호]
        map.put("집삭제", "deletehome");     // /섬 집삭제 <이름>
        map.put("집이름변경", "renamehome"); // /섬 집이름변경 <이름>
        map.put("집", "home");               // /섬 집 [이름]
    }

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (!msg.startsWith("/섬")) return;

        Player p = e.getPlayer();
        String[] parts = msg.trim().split("\s+");
        if (parts.length == 1) {
            sendHelp(p, 1);
            e.setCancelled(true);
            return;
        }

        String sub = parts[1];

        // Korean help
        if ("도움말".equalsIgnoreCase(sub) || "help".equalsIgnoreCase(sub)) {
            int page = 1;
            if (parts.length >= 3) {
                try { page = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
            }
            sendHelp(p, page);
            e.setCancelled(true);
            return;
        }

        // Let local features pass through to existing executor
        if (passthrough.contains(sub)) {
            return; // do NOT cancel; original /섬 executor handles it
        }

        // Forward if mapping exists
        String mapped = map.get(sub);
        if (mapped != null) {
            // Build the rest args
            StringBuilder rest = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                rest.append(' ').append(parts[i]);
            }
            final String cmd = "is " + mapped + rest.toString();
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override public void run() { p.performCommand(cmd); }
            });
            return;
        }

        // If no mapping, show help header and fall back to original executor
        // (do not cancel)
        p.sendMessage(ChatColor.GRAY + "알 수 없는 하위 명령어입니다. /섬 도움말 를 참고하세요.");
    }

    private void sendHelp(Player p, int page) {
        String header = ChatColor.GRAY + "====== " + ChatColor.AQUA + "섬 도움말 "
                + ChatColor.GRAY + "(" + page + "/2)" + " ======";
        p.sendMessage(header);
        List<String> lines = MessagesKo.helpPage(page);
        for (String l : lines) p.sendMessage(ChatColor.translateAlternateColorCodes('&', l));
        p.sendMessage(ChatColor.GRAY + "다음 페이지: " + ChatColor.YELLOW + "/섬 도움말 " + (page == 1 ? 2 : 1));
    }
}
