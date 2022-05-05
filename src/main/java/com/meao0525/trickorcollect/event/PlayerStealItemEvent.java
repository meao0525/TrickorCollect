package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerStealItemEvent implements Listener {

    private TrickorCollect pluin;

    public PlayerStealItemEvent(TrickorCollect pluin) {
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
                //クールダウン中
                if (player.getCooldown(itemInMain.getType()) > 0) {
                    sendActionBarMessage(player, ChatColor.GRAY + "クールダウン中です");
                    return;
                }

                //鉄権を持ってる人狼ならアイテムを盗める
                if (itemInMain.getType().equals(Material.IRON_SWORD)) {
                    //targetの持ち物取得
                    ArrayList<ItemStack> items = new ArrayList<>();
                    ItemStack item;
                    for (int i=0; i<9; i++) {
                        item = target.getInventory().getItem(i);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    //オフハンドも
                    item = target.getInventory().getItem(40);
                    if (item != null) {
                        items.add(item);
                    }

                    if (items.isEmpty()) {
                        sendActionBarMessage(player, ChatColor.GRAY + target.getDisplayName() + "はアイテムを持っていないようだ...");
                        return;
                    }

                    //シャッフル
                    Collections.shuffle(items);

                    //盗む処理
                    item = items.get(0);
                    int index = target.getInventory().first(item);
                    int amount = item.getAmount() / 10 + 1;

                    //盗んだ方に一つ渡す
                    player.getInventory().addItem(new ItemStack(item.getType(), amount));
                    player.sendMessage(ChatColor.AQUA + target.getDisplayName() + ChatColor.RESET + "からアイテムを盗みました");
                    //クールダウン
                    player.setCooldown(itemInMain.getType(), 300);
                    //targetの餅数一つ減らす
                    target.getInventory().getItem(index).setAmount(item.getAmount() - amount);
                }
            }
        }
    }

    public void sendActionBarMessage(Player player, String msg) {
        BaseComponent[] component = TextComponent.fromLegacyText(msg);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
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
