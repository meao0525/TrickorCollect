package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class RaidBattleGameEvent extends GameEvent {

    //スポーン範囲
    private final int range = 30;
    //襲撃者消す用
    private ArrayList<Entity> entities = new ArrayList<>();

    public RaidBattleGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "襲撃イベント発生！！！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "リスポーン地点の周りに襲撃者が現れた！");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.1f);
        }

        /* スポーン数はプレイヤー*4体(上限20体)
         * ただし、5人につき1体ラヴェジャーを1体召喚
         * 例: プレイヤー5人の場合
         *   ピリジャー 16体　ラヴェジャー 1体
         */
        //プレイヤー人数
        int playerNum = plugin.getTcPlayers().size();
        //ラヴェジャーの数
        int ravaNum = playerNum / 5;
        //プレイヤーを5人ごとに1人減らす
        playerNum -= playerNum / 5;
        //ピリジャーの数
        int spawnNum = playerNum * 4;
        //とはいえ上限
        if (spawnNum > 25) {
            spawnNum = 25;
        }

        //初期地点取得
        Location spawnPoint = plugin.getSpawnPoint();
        World world = spawnPoint.getWorld();
        //rangeの範囲にspawnNum体ピリジャーをランダムにスポーンさせる
        for(int i = 0; i < spawnNum; i++) {
            //ピリジャーを召喚
            Location location = getSummonLocation(spawnPoint, world);
            Entity pillager = world.spawnEntity(location, EntityType.PILLAGER);
            //削除用リストに追加
            entities.add(pillager);
        }

        //rangeの範囲にravaNum体ラヴェジャーをランダムにスポーンさせる
        for(int i = 0; i < ravaNum; i++) {
            //ラヴェジャーを召喚
            Location location = getSummonLocation(spawnPoint, world);
            Entity ravager = world.spawnEntity(location, EntityType.RAVAGER);
            //削除用リストに追加
            entities.add(ravager);
        }
    }

    public Location getSummonLocation(Location spawnPoint, World world) {
        //ゾンビ沸く位置取得
        Random random = new Random();
        int x = spawnPoint.getBlockX() + random.nextInt(-1*range, range);
        int y = spawnPoint.getBlockY();
        int z = spawnPoint.getBlockZ() + random.nextInt(-1*range, range);
        //足元にブロックが無ければ1マスずつ下にずらす
        while(world.getBlockAt(x, y-1, z).getType().equals(Material.AIR)) {
            //下限-64
            if (y < -64) { break; }
            y--;
        }
        //頭の位置(1マス上)がブロックだったら1マスずつ上にずらす
        while (!world.getBlockAt(x, y+1, z).getType().equals(Material.AIR)) {
            //上限128
            if (y > 128) { break; }
            y++;
        }
        //スポーン地点返す
        return new Location(world, x, y, z);
    }

    @Override
    public void cancel() {
        //召喚した襲撃者を全員消す
        for (Entity entity : entities) {
            entity.remove();
        }
    }
}
