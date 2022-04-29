package com.meao0525.trickandcollect.event;

import com.meao0525.trickandcollect.TrickandCollect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerStealItemEvent implements Listener {

    private TrickandCollect pluin;

    public PlayerStealItemEvent(TrickandCollect pluin) {
        this.pluin = pluin;
    }

    @EventHandler
    public void PlayerStealItemEventListener(PlayerInteractEntityEvent e) {
        //ゲームっちゅうっすか？
        if (pluin.isGame()) {
            //クリックしたのはプレイヤーか
            if (e.getRightClicked() instanceof Player) {
                //それぞれ取得
                Player player = e.getPlayer();
                Player target = (Player)e.getRightClicked();
                ItemStack itemInMain = player.getInventory().getItemInMainHand();

                //人狼チームか
                if (!pluin.getTraitorTeam().hasEntry(player.getDisplayName())) { return; }
                //鉄権を持ってる人狼ならアイテムを盗める
                if (itemInMain.getType().equals(Material.IRON_SWORD)) {
                    while (true) {
                        //0～9番（9->40）の乱数を作ってその番号のアイテムを盗む
                        Random random = new Random();
                        int index = random.nextInt(9);
                        if (index == 9) {
                            index = 40;
                        }
                        //ホットバーかオフハンドのアイテム取得
                        ItemStack item = target.getInventory().getItem(index);
                        //nullじゃなくて盗めるアイテムかな？
                        if (item != null && checkCanStealItem(item.getType())) {
                            //targetの餅数一つ減らす
                            target.getInventory().setItem(index, new ItemStack(item.getType(), item.getAmount()-1));
                            //盗んだ方に一つ渡す
                            player.getInventory().addItem(new ItemStack(item.getType(), 1));
                            player.sendMessage(item.getItemMeta().getDisplayName() + " を盗みました");
                            break;
                        }
                        //TODO: クールダウンを付ける
                        //TODO: 盗めるものがない時に無限ループなる
                    }
                }
            }
        }
    }

    public boolean checkCanStealItem(Material mat) {
        //取れないブロック → null、鉄のツール系、焼き魚
        if (mat.equals(Material.IRON_SWORD) ||
                mat.equals(Material.IRON_AXE) ||
                mat.equals(Material.IRON_PICKAXE) ||
                mat.equals(Material.IRON_SHOVEL) ||
                mat.equals(Material.COOKED_COD)) {
            //盗めないアイテムたちダヨーン
            return false;
        }
        //盗んでいいよーん
        return true;
    }
}
