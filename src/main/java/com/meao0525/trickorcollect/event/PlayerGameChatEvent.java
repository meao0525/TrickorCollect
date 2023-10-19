package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class PlayerGameChatEvent implements Listener {

    private TrickorCollect plugin;

    public PlayerGameChatEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerChatEventListener(AsyncPlayerChatEvent e) {
        //近くの人にしかチャットが見えないイベんちょ
        if (!plugin.isGame()) {
            return;
        }

        //受信者全員との距離を測る
        ArrayList<Player> cantSeeChat = new ArrayList<>();
        Player sender = e.getPlayer();
        for (Player p : e.getRecipients()) {
            double dis = p.getLocation().distance(sender.getLocation());
            //10メートル以上離れた人を取り出す(サバイバルモードの人だけ見えない)
            if (dis > 10.0 && p.getGameMode().equals(GameMode.SURVIVAL)) {
                cantSeeChat.add(p);
            }
        }
        //離れた人をRecipientsから外す
        cantSeeChat.forEach(e.getRecipients()::remove);

    }
}
