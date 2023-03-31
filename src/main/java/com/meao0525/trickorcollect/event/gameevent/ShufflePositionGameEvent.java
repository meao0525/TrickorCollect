package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;

public class ShufflePositionGameEvent extends GameEvent {

    public ShufflePositionGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "位置シャッフルイベント発生！！！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "全員の座標がシャッフルされました");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.1f);
        }

        //ランダムに座標を入れ替える
        ArrayList<Location> positions = new ArrayList<>();
        //全員の座標取得
        for (Player player : plugin.getTcPlayers()) {
            positions.add(player.getLocation());
        }
        //SHUFFLE
        Collections.shuffle(positions);
        //順番に飛ばしていく
        int i = 0;
        for (Player player : plugin.getTcPlayers()) {
            player.teleport(positions.get(i++));
        }
    }

    @Override
    public void cancel() {
        //キャンセル処理なし
    }
}
