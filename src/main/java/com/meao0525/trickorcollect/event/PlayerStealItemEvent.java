package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
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
            //それぞれ取得
            Player player = e.getPlayer();
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
                //クリックされたのだ～れ
                HumanEntity target;
                int invsize;
                if (e.getRightClicked() instanceof Player) {
                    Player ptarget = (Player)e.getRightClicked();
                    //ゲーム参加者か？
                    if (!pluin.getTcPlayers().contains(ptarget)) { return; }
                    target = ptarget;
                    invsize = 9;
                } else if(e.getRightClicked() instanceof Villager) {
                    //取り立て屋さん？
                    String entityName = e.getRightClicked().getCustomName();
                    if (entityName == null || !entityName.equalsIgnoreCase("取り立て屋"))  { return; }
                    target = (HumanEntity) e.getRightClicked();
                    invsize = 27;
                } else {
                    //それ以外は何もしない
                    return;
                }
                stealItem(player, target, invsize);
                //クールダウン
                player.setCooldown(itemInMain.getType(), 300);
            }
        }
    }

    public void stealItem(Player player, HumanEntity target, int invsize) {
        //targetの持ち物取得
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack item;
        for (int i=0; i<invsize; i++) {
            item = target.getInventory().getItem(i);
            if (item != null) {
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            sendActionBarMessage(player, ChatColor.GRAY + target.getName() + "はアイテムを持っていないようだ...");
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
        player.sendMessage(ChatColor.AQUA + target.getName() + ChatColor.RESET + "からアイテムを盗みました");
        //targetの餅数一つ減らす
        target.getInventory().getItem(index).setAmount(item.getAmount() - amount);
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
