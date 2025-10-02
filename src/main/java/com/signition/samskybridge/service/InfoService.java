
package com.signition.samskybridge.service;

import org.bukkit.entity.Player;

public class InfoService {
    public void show(Player p) {
        // Placeholder: replace with real island info
        p.sendMessage("§6[섬] §f섬 정보");
        p.sendMessage(" §7섬장: §f자신(예시)");
        p.sendMessage(" §7인원: §f1/1");
        p.sendMessage(" §7크기: §f100x100");
        p.sendMessage(" §7레벨: §eLv.1");
    }
}
