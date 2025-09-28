
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;

import java.util.UUID;

public class Integration {
    private final Main plugin;

    public Integration(Main plugin) {
        this.plugin = plugin;
    }

    public void syncRangeToBento(UUID owner, int radius) {
        try {
            String name = Bukkit.getOfflinePlayer(owner).getName();
            if (name == null) return;
            // 명령 기반 연동(컴파일 의존성 없음)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bsbadmin range set " + name + " " + radius);
        } catch (Throwable ignored) {}
    }
}
