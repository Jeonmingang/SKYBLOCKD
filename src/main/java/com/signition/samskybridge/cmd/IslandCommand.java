// --- Add/replace in IslandCommand subcommand: /섬 레벨 ---
// Assumes: LevelService level; UpgradeService upgrade;
UUID owner = player.getUniqueId();
int lv = level.getLevel(owner);
long cur = level.getCurrentXp(owner);
long need = level.requiredXpForLevel(lv + 1);
double pct = need <= 0 ? 1.0 : Math.min(1.0, (double)cur / (double)need);

// 아주 간단한 진행바
int bars = 20;
int filled = (int)Math.round(pct * bars);
StringBuilder bar = new StringBuilder();
for (int i = 0; i < bars; i++) bar.append(i < filled ? "§a▮" : "§7▮");

IslandData is = level.getIslandOf(player);
int size = upgrade.getProtectedSize(is);
int cap  = upgrade.getMemberCap(is);

player.sendMessage(Text.color("&b[섬 레벨] &fLv.&e" + lv));
player.sendMessage(Text.color("&7경험치: &f" + String.format("%,d", cur) + "&7 / &a" + String.format("%,d", need) + " &8(" + (int)(pct*100) + "%)"));
player.sendMessage(bar.toString());
player.sendMessage(Text.color("&7섬 크기: &f" + size + " 블럭  &8|  &7인원수: &f" + cap + " 명"));
