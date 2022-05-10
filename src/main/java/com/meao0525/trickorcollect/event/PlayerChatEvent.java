package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatEvent implements Listener {

    private TrickorCollect plugin;

    public PlayerChatEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerChatEventListener(AsyncPlayerChatEvent e) {
        //近くの人にしかチャットが見えないイベんちょ
        if (!plugin.isGame()) {
            return;
        }

        //一旦キャンセル
        e.setCancelled(true);
        //なんて送った？
        String msg = e.getMessage();
        //受信者全員との距離を測る
        Player sender = e.getPlayer();
        for (Player p : e.getRecipients()) {
            double dis = p.getLocation().distance(sender.getLocation());
            //10メートル以内の人にだけ送る
            if (dis <= 10.0) {
                p.sendMessage(msg);
            }
        }
    }
}
