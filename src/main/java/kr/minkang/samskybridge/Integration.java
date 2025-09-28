
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import java.util.UUID;

public class Integration {
    private final Main plugin;

    public Integration(Main plugin) {
        this.plugin = plugin;
    }

    /** Apply size range to external plugins via command template */
    public void syncRange(UUID owner, int radius) {
        executeTemplate("integration.sync.range", owner, radius, 0);
    }

    /** Apply team max to external plugins via command template */
    public void syncTeam(UUID owner, int teamMax) {
        executeTemplate("integration.sync.team", owner, 0, teamMax);
    }

    /** Apply both values via command template */
    public void syncAll(UUID owner, int radius, int teamMax) {
        executeTemplate("integration.sync.all", owner, radius, teamMax);
    }

    private void executeTemplate(String path, UUID owner, int radius, int teamMax) {
        try {
            String template = plugin.getConfig().getString(path, "").trim();
            if (template.isEmpty()) return;
            String name = Bukkit.getOfflinePlayer(owner).getName();
            if (name == null) return;
            String cmd = template.replace("<owner>", name)
                    .replace("<size>", String.valueOf(radius))
                    .replace("<team>", String.valueOf(teamMax));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } catch (Throwable ignored) {}
    }
}
