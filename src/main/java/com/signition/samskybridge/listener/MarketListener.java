
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;
import java.util.UUID;

public class MarketListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    public MarketListener(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (e.getView()==null || e.getView().getTitle()==null) return;
        String title = e.getView().getTitle().replace("§","&");
        if (!title.contains("섬 매물")) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack it = e.getCurrentItem();
        if (it==null || it.getType()!= Material.PLAYER_HEAD) return;
        if (!(it.getItemMeta() instanceof SkullMeta)) return;

        SkullMeta sm = (SkullMeta) it.getItemMeta();
        OfflinePlayer op = sm.getOwningPlayer();
        if (op==null) { p.sendMessage(Text.color("&c데이터가 없습니다.")); return; }
        UUID seller = op.getUniqueId();

        // 찾기
        IslandData target = null;
        for (IslandData is : store.getAll()){
            if (seller.equals(is.getOwner())) { target=is; break; }
        }
        if (target==null || !target.isForSale()){
            p.sendMessage(Text.color("&c해당 섬은 더 이상 판매중이 아닙니다.")); return;
        }

        switch (e.getClick()){
            case LEFT: // 구매
            case SHIFT_LEFT:{
                // 구매 제한: 이미 소속된 섬이 있으면 불가
                Optional<IslandData> mine = store.findByMember(p.getUniqueId());
                if (mine.isPresent()){
                    p.sendMessage(Text.color("&c이미 섬을 보유했거나 소속되어 있습니다. 탈퇴 후 구매하세요."));
                    return;
                }
                // Vault 결제
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp==null){
                    p.sendMessage(Text.color("&c경제 플러그인(Vault)이 없어 구매할 수 없습니다."));
                    return;
                }
                Economy econ = rsp.getProvider();
                double price = target.getPrice();
                if (!econ.has(p, price)){
                    p.sendMessage(Text.color("&c잔액이 부족합니다. 필요: &f"+(long)price));
                    return;
                }
                econ.withdrawPlayer(p, price);

                // 소유권 이전
                target.setOwner(p.getUniqueId());
                target.setForSale(false);
                store.save();
                p.sendMessage(Text.color("&a구매 완료! 이제 이 섬의 섬장이 되었습니다."));
                p.closeInventory();
                break;
            }
            default:{ // 우클릭: 구경 이동 (BentoBox 연동 필요)
                // 여기서는 안내만. 서버에 BentoBox 워프 연동이 있으면 거기로 이동하도록 별도 확장 가능.
                p.sendMessage(Text.color("&7구경 이동은 BentoBox가 설치되고 연동되었을 때 동작합니다."));
                break;
            }
        }
    }
}
