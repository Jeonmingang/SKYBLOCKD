package com.minkang.usp2.level;

import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelService {
    private final org.bukkit.plugin.Plugin plugin;
    private final NamespacedKey minedKey;
    private final Map<String, Long> blockXp = new HashMap<String, Long>();

    public LevelService(org.bukkit.plugin.Plugin plugin){
        this.plugin = plugin;
        this.minedKey = new NamespacedKey(plugin, "mined_item");
        loadBlocks();
    }

    public NamespacedKey getMinedKey(){ return minedKey; }

    private boolean xpDebug(){ return plugin.getConfig().getBoolean("xp.debug", false); }
    private void debug(Player p, String m){ if (xpDebug() && p!=null) p.sendMessage(Text.color("&8[XP 디버그] &7"+m)); }

    private void loadBlocks(){
        try{
            File f = new File(plugin.getDataFolder(), "blocks.yml");
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            if (!f.exists()){
                YamlConfiguration y=new YamlConfiguration();
                y.set("minecraft:diamond_block", 20);
                y.set("pixelmon:ruby_block", 12);
                y.save(f);
            }
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            for (String k : y.getKeys(false)){
                blockXp.put(k.toLowerCase(), y.getLong(k));
            }
        }catch(Exception e){
            plugin.getLogger().warning("blocks.yml 로드 실패: "+e.getMessage());
        }
    }

    public void onPlace(BlockPlaceEvent e){
        if (e.isCancelled() || (!e.canBuild())) return;
        final Player p = e.getPlayer();
        final String world = e.getBlockPlaced().getWorld().getName();
        final List<String> allowed = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (allowed==null || !allowed.contains(world)){ debug(p,"허용 월드 아님: "+world); return; }

        // 손 아이템 드랍태그 검사
        final boolean fromDropTag;
        {
            ItemStack inHand = e.getItemInHand();
            boolean tagged = false;
            if (inHand!=null && inHand.hasItemMeta()){
                ItemMeta im = inHand.getItemMeta();
                if (im!=null && im.getPersistentDataContainer().has(minedKey, PersistentDataType.BYTE)){
                    tagged = true;
                }
            }
            fromDropTag = tagged;
        }

        final Material placedType = e.getBlockPlaced().getType();
        final Material replacedType = e.getBlockReplacedState().getType();
        final Location loc = e.getBlockPlaced().getLocation();

        Bukkit.getScheduler().runTask(plugin, new Runnable(){
            @Override public void run(){
                Material now = loc.getBlock().getType();
                if (now != placedType || now == replacedType){
                    debug(p, "실제 배치되지 않음(우클릭 상호작용 등): XP 미지급");
                    return;
                }
                if (fromDropTag){
                    debug(p, "드랍 재설치 감지: XP 미지급");
                    return;
                }

                String id = now.name().toLowerCase();
                long add = 0L;
                if (blockXp.containsKey(id)) add = blockXp.get(id);
                else {
                    try{
                        String ns = loc.getBlock().getBlockData().getMaterial().getKey().toString().toLowerCase();
                        if (blockXp.containsKey(ns)) add = blockXp.get(ns);
                    }catch(Throwable ignored){}
                }
                if (add<=0){
                    add = (long) plugin.getConfig().getDouble("xp.default-per-block", 0.0);
                    if (add<=0){ debug(p, "블럭 XP 매핑/기본값=0: XP 미지급"); return; }
                }
                // 이 모듈에선 섬 데이터 모델이 다르므로, 플레이어 레벨/경험치를 직접 올리는 콜백이 따로 있다면 여기에 연결하세요.
                // 일단 메세지로 지급량을 알림:
                p.sendMessage(Text.color("&a블럭 경험치 +" + add));
            }
        });
    }
}
