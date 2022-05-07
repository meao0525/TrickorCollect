package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PlayerRespawnEvent implements Listener {

    private TrickorCollect plugin;
    public static HashMap<Player, RespawnThread> respawnPlayers = new HashMap<>();

    public PlayerRespawnEvent(TrickorCollect plugin) { this.plugin = plugin; }

    //TODO: なんかおかしかった

    @EventHandler
    public void PlayerSneakEventListener(PlayerToggleSneakEvent e) {
        if (!plugin.isGame()) {
            return;
        }

        Player player = e.getPlayer();

        if (!player.isSneaking()) {
            //スニークしたとき
            if (!respawnPlayers.containsKey(player)) {
                //アンパンマン、新しいスレッドよ！！！
                RespawnThread thread = new RespawnThread(player);
                //リストに登録
                respawnPlayers.put(player, thread);
            }

        } else {
            //スニークを解除したわね！？
            if (respawnPlayers.containsKey(player)) {
                respawnPlayers.remove(player);
            }
        }
    }

    @EventHandler
    public void PlayerMoveEventListener(PlayerMoveEvent e) {
        if (!plugin.isGame()) {
            return;
        }

        //座標の取得(ブロック単位でやらないとシビアすぎる)
        Location from = e.getFrom().getBlock().getLocation();
        Location to = e.getTo().getBlock().getLocation();
        //移動したブロック数0
        if (from.equals(to)) { return; }

        //動いたらrespawn中断
        if (respawnPlayers.containsKey(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.GRAY + "リスポーンを中断しました");
            respawnPlayers.remove(e.getPlayer());
        }
    }

    //Thread用の内部クラス
    private class RespawnThread extends BukkitRunnable {
        private Player player;
        private int count = 8;

        private RespawnThread(Player player) {
            this.player = player;
            runTaskTimer(plugin, 0, 20);
        }

        @Override
        public void run() {
            //respawn中の人リストにいない
            if (!respawnPlayers.containsKey(player)) { this.cancel(); }

            if (count > 5) { //最初の3秒
                count--;
            } else if (count > 0) { //カウント5秒
                //経過秒数増やすメッセージ
                player.sendMessage("リスポーンまで残り " + ChatColor.AQUA + count-- + ChatColor.RESET + " 秒");
                //効果音
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 5.0F, 5.0F);
            } else { //時は満ちた
                //帰らせる
                player.teleport(plugin.getSpawnPoint());
                //効果音
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5.0F, 5.0F);
                //リストから削除
                respawnPlayers.remove(player);
                //止める
                this.cancel();
            }
        }
    }
}
