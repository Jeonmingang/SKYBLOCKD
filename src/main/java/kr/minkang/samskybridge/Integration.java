
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;

import java.util.UUID;

public class Integration {
    private final Main plugin;

    public Integration(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Sync size/team to external plugins by executing configurable server commands.
     * Commands support placeholders:
     *  <owner> - owner name
     *  <size>  - radius/size value
     *  <team>  - team max members
     *
     * Example config commands:
     *  upgrade.sync.commands.size: "bsbadmin range set <owner> <size>"
     *  upgrade.sync.commands.team: "bsbadmin setmaxmembers <owner> <team>"
     */
    public void syncRangeToBento(UUID owner, int radius) {
        executeTemplate("upgrade.sync.commands.size", owner, radius, -1);
    }

    public void syncTeamToExternal(UUID owner, int teamMax) {
        executeTemplate("upgrade.sync.commands.team", owner, -1, teamMax);
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
