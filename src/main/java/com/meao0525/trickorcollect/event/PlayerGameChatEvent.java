package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.ArrayList;

public class PlayerGameChatEvent implements Listener {

    private TrickorCollect plugin;

    public PlayerGameChatEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerChatEventListener(PlayerChatEvent e) {
        //近くの人にしかチャットが見えないイベんちょ
        if (!plugin.isGame()) {
            return;
        }
        
//        //受信者全員との距離を測る
        Player sender = e.getPlayer();
        for (Player p : e.getRecipients()) {
            double dis = p.getLocation().distance(sender.getLocation());
            //10メートル以内の人にだけ送る
            if (dis > 10.0) {
                e.getRecipients().remove(p);
            }
        }
    }
}
