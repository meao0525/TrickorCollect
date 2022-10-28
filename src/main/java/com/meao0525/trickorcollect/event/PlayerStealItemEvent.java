package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
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
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 0.5f);
                return;
            }

            //鉄権を持ってる人狼ならアイテムを盗める
            if (itemInMain.getType().equals(Material.IRON_SWORD)) {
                //クリックされたのだ～れ
                Inventory targetInv;
                int invsize;
                int cooldownTicks;
                if (e.getRightClicked() instanceof Player) {
                    Player ptarget = (Player)e.getRightClicked();
                    //ゲーム参加者か？
                    if (!pluin.getTcPlayers().contains(ptarget)) { return; }
                    //ターゲットから投票されている
                    if (isVoted(player, ptarget)) {
                        sendActionBarMessage(player, ChatColor.RED + "投票されているため盗めません");
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 0.5f);
                        return;
                    }
                    targetInv = ptarget.getInventory();
                    invsize = 9;
                    //クールダウン
                    cooldownTicks = 600;
                } else if(e.getRightClicked() instanceof Villager) {
                    //取り立て屋さん？
                    Villager vtarget = (Villager) e.getRightClicked();
                    if (vtarget.getCustomName() == null || !vtarget.getCustomName().equalsIgnoreCase("取り立て屋"))  { return; }
                    targetInv = pluin.getCollects();
                    invsize = 27;
                    //クールダウン
                    cooldownTicks = 2400;
                } else {
                    //それ以外は何もしない
                    return;
                }
                //追放されているか
                if (pluin.getExiled().contains(player)) {
                    sendActionBarMessage(player, ChatColor.RED + "追放されているため盗めません");
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 0.5f);
                    return;
                }
                //盗む処理
                stealItem(player, targetInv, invsize);
                //クールダウン
                player.setCooldown(itemInMain.getType(), cooldownTicks);
            }
        }
    }

    public boolean isVoted(Player player, Player target) {
        HashMap<Player, ArrayList<Player>> voteMap = pluin.getVoteMap();
        //ターゲットから投票されている
        if (voteMap.containsKey(target) && voteMap.get(target).contains(player)) {
            return true;
        } else {
            return false;
        }
    }

    public void stealItem(Player player, Inventory targetInv, int invsize) {
        //targetの持ち物取得
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack item;
        for (int i=0; i<invsize; i++) {
            item = targetInv.getItem(i);
            if (item != null) {
                items.add(item);
            }
        }
        //オフハンド
        if (invsize == 9) {
            item = targetInv.getItem(40);
            if (item != null) {
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            sendActionBarMessage(player, ChatColor.GRAY + "チッ...文無しか...");
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 0.5f);
            return;
        }

        //シャッフル
        Collections.shuffle(items);

        //盗む処理
        item = items.get(0);
        int index = targetInv.first(item);
        int amount = item.getAmount();

        //盗んだ方に渡す
        HashMap<Integer, ItemStack> overs = player.getInventory().addItem(new ItemStack(item.getType(), amount));
        //持てなかった分を吐き出す
        for (ItemStack over : overs.values()) {
            player.getWorld().dropItem(player.getLocation(), over);
        }
        player.sendMessage(ChatColor.AQUA + "アイテムを盗みました！");
        player.playSound(player, Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.5f);
        //targetの餅数減らす
        targetInv.getItem(index).setAmount(item.getAmount() - amount);
    }

    public void sendActionBarMessage(Player player, String msg) {
        BaseComponent[] component = TextComponent.fromLegacyText(msg);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }
}
