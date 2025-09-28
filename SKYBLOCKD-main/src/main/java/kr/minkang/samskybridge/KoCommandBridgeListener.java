
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * /섬 단독: 한글 도움말 출력 후 이벤트 취소
 * /섬 업그레이드|레벨|랭킹|채팅: 패스스루(취소하지 않음) → Main.onCommand가 처리
 * /섬 <한국어/영어 서브명령...>: 기본은 /is <원문 그대로>로 포워딩
 *   - 한국어 별칭은 아래 매핑 규칙으로 영어 /is 명령으로 변환
 */
public class KoCommandBridgeListener implements Listener {

    private static final Set<String> PASSTHROUGH = new HashSet<String>(Arrays.asList("업그레이드","레벨","랭킹","채팅"));

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPre(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null || !msg.startsWith("/섬")) return;

        Player p = e.getPlayer();
        String[] parts = msg.trim().split("\\s+");
        if (parts.length == 1) {
            // /섬 -> 한국어 도움말만 출력, 다른 안내 메시지 뜨지 않게 cancel
            sendHelp(p);
            e.setCancelled(true);
            return;
        }

        String sub = parts[1];

        // 우리 플러그인이 처리해야 하는 4개 명령은 패스스루
        if (PASSTHROUGH.contains(sub)) return;

        // /섬 도움말 [페이지] 도 허용
        if ("도움말".equalsIgnoreCase(sub) || "help".equalsIgnoreCase(sub)) {
            sendHelp(p);
            e.setCancelled(true);
            return;
        }

        // === 한국어 → /is 영어 매핑 규칙 ===
        String mapped = null;
        // 팀 관련
        if ("초대".equals(sub)) {
            if (parts.length >= 3 && "수락".equals(parts[2])) {
                mapped = "team accept";
            } else if (parts.length >= 3 && "거절".equals(parts[2])) {
                mapped = "team reject";
            } else if (parts.length >= 3) {
                mapped = "team invite " + joinFrom(parts, 2);
            }
        } else if ("추방".equals(sub)) {
            mapped = "team kick " + joinFrom(parts, 2);
        } else if ("탈퇴".equals(sub)) {
            mapped = "team leave";
        } else if ("알바".equals(sub)) {
            mapped = "team coop " + joinFrom(parts, 2);
        } else if ("알바해제".equals(sub)) {
            mapped = "team uncoop " + joinFrom(parts, 2);
        }
        // 섬 기본
        else if ("리셋".equals(sub)) {
            mapped = "reset " + joinFrom(parts, 2);
        } else if ("정보".equals(sub)) {
            mapped = "info " + joinFrom(parts, 2);
        } else if ("설정".equals(sub)) {
            mapped = "settings " + joinFrom(parts, 2);
        } else if ("이름설정".equals(sub)) {
            mapped = "setname " + joinFrom(parts, 2);
        } else if ("이름초기화".equals(sub)) {
            mapped = "resetname";
        } else if ("언어".equals(sub)) {
            mapped = "language " + joinFrom(parts, 2);
        } else if ("차단".equals(sub)) {
            mapped = "ban " + joinFrom(parts, 2);
        } else if ("언밴".equals(sub) || "해제".equals(sub)) {
            mapped = "unban " + joinFrom(parts, 2);
        } else if ("추방하기".equals(sub) || "퇴출".equals(sub)) {
            mapped = "expel " + joinFrom(parts, 2);
        } else if ("근처".equals(sub)) {
            mapped = "near";
        }
        // 홈
        else if ("집목록".equals(sub)) {
            mapped = "homes";
        } else if ("집".equals(sub)) {
            mapped = "home " + joinFrom(parts, 2);
        } else if ("집설정".equals(sub)) {
            mapped = "sethome " + joinFrom(parts, 2);
        } else if ("집삭제".equals(sub)) {
            mapped = "deletehome " + joinFrom(parts, 2);
        } else if ("집이름변경".equals(sub)) {
            mapped = "renamehome " + joinFrom(parts, 2);
        }

        // 최종 포워딩 명령 구성
        String forward;
        if (mapped != null) {
            forward = "is " + mapped.trim();
        } else {
            // 한국어 매핑에 없으면 입력 그대로 /is 로 전달
            String rest = msg.substring(2).trim(); // remove '/섬'
            if (rest.startsWith("섬")) rest = rest.substring(1).trim();
            forward = "is " + rest;
        }

        e.setCancelled(true);
        final String exec = forward;
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("SamSkyBridge"), () -> p.performCommand(exec));
    }

    private void sendHelp(Player p) {
        p.sendMessage(color("&7====== &b섬 도움말 &7(1/1) ======"));
        p.sendMessage(color("&b/섬 업그레이드 &7- 섬 크기/팀 인원/레벨 경험치 GUI"));
        p.sendMessage(color("&b/섬 레벨 &7- 내 섬 레벨/XP 요약"));
        p.sendMessage(color("&b/섬 랭킹 &7- 상위 섬 목록"));
        p.sendMessage(color("&b/섬 채팅 &7- 섬 전용 채팅 토글"));
        p.sendMessage(color("&b/섬 초대 <닉> &7- 팀 초대 (&e/섬 초대 수락&7, &e/섬 초대 거절&7)"));
        p.sendMessage(color("&b/섬 알바 <닉> &7- 협력 권한 부여 (&e/섬 알바해제 <닉>&7)"));
        p.sendMessage(color("&b/섬 리셋 <설계도> &7- 섬 초기화"));
        p.sendMessage(color("&7그 외: &f/is <원하는 명령> &7과 동일하게 사용"));
    }

    private String joinFrom(String[] arr, int idx) {
        if (idx >= arr.length) return "";
        return Arrays.stream(Arrays.copyOfRange(arr, idx, arr.length)).collect(Collectors.joining(" "));
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
