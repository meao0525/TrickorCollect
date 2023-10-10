package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ZombiePartyGameEvent extends GameEvent implements Listener {

    //スポーン範囲
    private final int range = 20;
    //スポーン数
    private int zombieNum;
    //ゾンビ消すよう
    private ArrayList<Zombie> zombies = new ArrayList<>();

    public ZombiePartyGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Treat!]" + ChatColor.RESET + "仮装パーティーの始まりだ...！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "リスポーン地点の周りにゾンビの仮装集団が現れた！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "どうやらあいつら "+ ChatColor.AQUA + "お菓子" + " をあげないとイタズラしてくるらしい...");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_ZOMBIE_HURT, 1.0f, 0.5f);
        }

        //ゾンビスポーン数はプレイヤー数*5
        zombieNum = plugin.getTcPlayers().size() * 5;
        //とはいえ上限
        if (zombieNum > 25) {
            zombieNum = 25;
        }
        //初期地点取得
        Location spawnPoint = plugin.getSpawnPoint();
        World world = spawnPoint.getWorld();
        //夜にする
        world.setTime(18000);
        //rangeの範囲にzombieNum体ランダムにスポーンさせる
        for(int i = 0; i < zombieNum; i++) {
            //ゾンビを召喚
            Location location = getSummonLocation(spawnPoint, world);
            Zombie zombie = (Zombie)world.spawnEntity(location, EntityType.ZOMBIE, false);
            //ゾンビの体力を増やす
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            zombie.setHealth(zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            //名前変える
            zombie.setCustomName("仮装した人");
            zombie.setCustomNameVisible(true);
            //燃えない
            zombie.setFireTicks(0);
            //削除用リストに追加
            zombies.add(zombie);
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

    @EventHandler
    public void giveCookieEvent(PlayerInteractEntityEvent e) {
        //お菓子をあげたら仮装した人はいなくなる
        if (e.getRightClicked() instanceof Zombie) {
            //クリックしたのが仮装した人
            Zombie zombie = (Zombie) e.getRightClicked();
            String customName = zombie.getCustomName();
            if (customName != null && customName.equals("仮装した人")) {
                ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
                //クッキーを手に持っている
                if (itemInMainHand.getType().equals(Material.COOKIE) && itemInMainHand.getAmount() > 8) {
                    itemInMainHand.setAmount(itemInMainHand.getAmount() - 8);
                    zombie.remove();
                }
            }
        }
    }

    @Override
    public void cancel() {
        //ゾンビを全員消す
        for (Zombie zombie : zombies) {
            zombie.remove();
        }
    }
}