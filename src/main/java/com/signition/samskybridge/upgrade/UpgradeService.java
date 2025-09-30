// --- Add to UpgradeService ---
// 구매한 XP를 레벨 서비스에 정확히 반영
public void buyExperience(Player player, long xp) {
    if (xp <= 0) return;
    IslandData is = level.getIslandOf(player);
    level.applyXpPurchase(is, xp); // 자동 레벨업 루프 포함
    // 필요시 여기서 경제 차감/로그 저장/GUI 갱신
}

// UI 핸들러에서 호출하는 편의 메서드
public void tryUpgradeSize(Player p){
    IslandData is = level.getIslandOf(p);
    if (!canUpgradeSize(is, p)) return;
    withdraw(p, getPriceForSize(is));
    doUpgradeSize(is);
    p.sendMessage(Text.color("&a섬 크기를 업그레이드했습니다."));
}

public void tryUpgradeMemberCap(Player p){
    IslandData is = level.getIslandOf(p);
    if (!canUpgradeMemberCap(is, p)) return;
    withdraw(p, getPriceForMemberCap(is));
    doUpgradeMemberCap(is);
    p.sendMessage(Text.color("&a섬 인원수를 업그레이드했습니다."));
}
