
package kr.minkang.samskybridge;

import java.util.Arrays;
import java.util.List;

/** Korean help message provider */
public class MessagesKo {
    public static List<String> helpPage(int page) {
        if (page <= 1) {
            return Arrays.asList(
                "&b/is reset &7<설계도> &f섬을 초기화 합니다! (&e섬이 제거되고 새롭게 만들어집니다&f)",
                "&b/is info &7<플레이어> &f현재있는섬이나 소유중 섬의 정보를 표시합니다",
                "&b/is settings &f섬 설정을 봅니다",
                "&b/is setname &f섬의 이름을 설정합니다",
                "&b/is resetname &f섬 이름을 초기화 합니다",
                "&b/is language &7<언어> &f언어 선택",
                "&b/is ban &7<플레이어> &f섬 데이터에서 차단(&7밴 처리&f)합니다",
                "&b/is unban &7<플레이어> &f섬 밴을 언밴합니다(&7출입 가능&f)",
                "&b/is expel &7<플레이어> &f플레이어를 섬에서 쫓아냅니다",
                "&b/is near &f주변 섬을 보여줍니다"
            );
        } else {
            return Arrays.asList(
                "&b/is homes &f내 집 목록을 보여줍니다",
                "&b/is home &7[이름] &f집으로 이동",
                "&b/is sethome &7[번호] &f집으로 설정",
                "&b/is deletehome &7[이름] &f집을 삭제",
                "&b/is renamehome &7[이름] &f집 이름을 변경",
                "&b/is team invite &7<닉> &f팀 초대",
                "&b/is team accept &f팀 초대 수락",
                "&b/is team reject &f팀 초대 거절",
                "&b/is team coop &7<닉> &f알바(협력) 권한 부여",
                "&b/is team uncoop &7<닉> &f알바 해제"
            );
        }
    }
}
