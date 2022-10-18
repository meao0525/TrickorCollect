package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;

public class ShufflePositionGameEvent extends GameEvent {

    public ShufflePositionGameEvent(TrickorCollect plugin) {
        super(plugin);

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
}
