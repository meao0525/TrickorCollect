package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class AprilBlockLieGameEvent extends GameEvent {

    HashMap<Material, Material> blockMap = new HashMap<>();

    //コンストラクタ
    public AprilBlockLieGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "今日はエイプリルフール！！！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "あたりのブロックたちがウソをつき始めた...");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.1f);
        }
        //シャッフルするブロックの対応表作成
        createBlockMap();
    }

    private void createBlockMap() {
        List<Material> blocks = Arrays.asList(
                Material.OAK_LOG,
                Material.BIRCH_LOG,
                Material.OAK_LEAVES,
                Material.STONE,
                Material.DEEPSLATE,
                Material.GRANITE,
                Material.DIORITE,
                Material.ANDESITE,
                Material.CLAY,
                Material.DIRT,
                Material.SAND,
                Material.GRAVEL,
                Material.COAL_ORE,
                Material.COPPER_ORE);
        //シャッフル
        List<Material> shuffles = new ArrayList<>(blocks);
        Collections.shuffle(shuffles);
        //Map作成
        for (int i = 0; i < blocks.size(); i++) {
            blockMap.put(blocks.get(i), shuffles.get(i));
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        //ゲーム中か
        if (!plugin.isGame()) { return; }
        //ブロック取得
        Block block = event.getBlock();
        Material material = block.getType();
        //関連ブロック
        if (material.equals(Material.GRASS_BLOCK)
            || material.equals(Material.PODZOL)
            || material.equals(Material.MYCELIUM)
            || material.equals(Material.DIRT_PATH)
            || material.equals(Material.FARMLAND)) {
            //土系
            material = Material.DIRT;

        } else if (material.equals(Material.DEEPSLATE_COAL_ORE)) {
            //石炭
            material = Material.COAL_ORE;
        } else if (material.equals(Material.DEEPSLATE_COPPER_ORE)) {
            //石炭
            material = Material.COPPER_ORE;
        } else if (material.equals(Material.BIRCH_LEAVES)) {
            //石炭
            material = Material.OAK_LEAVES;
        }

        if (blockMap.containsKey(material)) {
            //デフォルトのドロップを無くす
            event.setDropItems(false);
            //変更後のドロップ取得
            Material shuffle = blockMap.get(material);
            block.setType(shuffle);
            Collection<ItemStack> drops = block.getDrops();
            //ドロップ生成
            for (ItemStack drop : drops) {
                block.getWorld().dropItem(block.getLocation(), drop);
            }
        }
    }

    @Override
    public void cancel() {

    }
}
