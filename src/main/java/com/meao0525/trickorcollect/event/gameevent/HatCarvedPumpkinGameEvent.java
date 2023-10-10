package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HatCarvedPumpkinGameEvent extends GameEvent {

    //テクスチャ保存用
    private ArrayList<PlayerTextures> textures = new ArrayList<>();
    //スティーブスキンURL
    private String url = "http://textures.minecraft.net/texture/1a4af718455d4aab528e7a61f86fa25e6a369d1768dcb13f7df319a713eb810b";
    //タイマー
    RemovePumpkinHatTimer pumpkinTimer;

    public HatCarvedPumpkinGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Treat!]" + ChatColor.RESET + "さあ...収穫の季節だ...！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "全員 かぼちゃ になっちゃった！");

        //全員にかぼちゃをかぶせる
        ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN, 1);
        ItemMeta pumpkinMeta = pumpkin.getItemMeta();
        //名前つける
        pumpkinMeta.setDisplayName("Happy Halloween!");
        pumpkin.setItemMeta(pumpkinMeta);
        //プレイヤー処理
        PlayerProfile profile;
        for (Player player : plugin.getTcPlayers()) {
            //透明化
            player.setInvisible(true);
            player.setCustomNameVisible(false);
            //かぼちゃをかぶせる
            player.getInventory().setHelmet(pumpkin);
            //効果音
            player.playSound(player, Sound.BLOCK_GRASS_BREAK, 1.0f, 0.1f);
        }
        //5分タイマー始動
        pumpkinTimer = new RemovePumpkinHatTimer(this);
        pumpkinTimer.runTaskLater(plugin, 5*60*20);
    }

    @EventHandler
    public void clickHelmetEvent(InventoryClickEvent e) {
        //かぼちゃは脱げない
        if (!plugin.isGame()) { return; }

        //クリックされたアイテムの取得
        ItemStack item = e.getCurrentItem();
        if (item == null) { return; }
        //Materialがかぼちゃで名前が「Happy Halloween!」
        if (item.getType().equals(Material.CARVED_PUMPKIN)) {
            ItemMeta pumpkinMeta = item.getItemMeta();
            if (pumpkinMeta != null && pumpkinMeta.getDisplayName().equals("Happy Halloween!")) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void cancel() {
        PlayerProfile profile;
        int i = 0;
        for (Player player : plugin.getTcPlayers()) {
            //かぼちゃを消す
            player.getInventory().setHelmet(new ItemStack(Material.AIR));
            //透明化解除
            player.setInvisible(false);
            player.setCustomNameVisible(true);
            i++;
        }
        pumpkinTimer.cancel();
    }

    private class RemovePumpkinHatTimer extends BukkitRunnable {

        GameEvent gameEvent;

        public RemovePumpkinHatTimer(GameEvent gameEvent) {
            this.gameEvent = gameEvent;
        }

        @Override
        public void run() {
            //一定時間経過後にキャンセル
            gameEvent.cancel();
        }
    }
}