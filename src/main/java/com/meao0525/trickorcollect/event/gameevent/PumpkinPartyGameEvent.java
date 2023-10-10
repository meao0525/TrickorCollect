package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PumpkinPartyGameEvent extends GameEvent implements Listener {

    //特製パンプキンパイ
    ItemStack pumpkinPie;
    //ランダムエフェクト
    ArrayList<PotionEffect> randomEffects = new ArrayList<PotionEffect>() {
        {
            add(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1));
            add(new PotionEffect(PotionEffectType.SPEED, 600, 2));
            add(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 3));
            add(new PotionEffect(PotionEffectType.JUMP, 600, 2));
            add(new PotionEffect(PotionEffectType.LUCK, 600, 5));
            add(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 2));
        }
    };

    public PumpkinPartyGameEvent(TrickorCollect plugin) {
        super(plugin);

        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Treat!]" + ChatColor.RESET + "今夜はパンプキンパーティだ！！！");
        Bukkit.broadcastMessage(ChatColor.AQUA + "ジャック・オ・ランタン" + ChatColor.GRAY + " を手に持って "
                + ChatColor.AQUA + "かまど" + ChatColor.GRAY + " を右クリックすると...？");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_ZOMBIE_HURT, 1.0f, 0.5f);
        }

        //特製パンプキンパイ
        pumpkinPie = new ItemStack(Material.PUMPKIN_PIE, 1);
        ItemMeta meta = pumpkinPie.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "特製パンプキンパイ");
        meta.addEnchant(Enchantment.BINDING_CURSE, 255, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pumpkinPie.setItemMeta(meta);
    }

    @EventHandler
    public void makePumpkinPieEvent(PlayerInteractEvent e) {
        //ジャック・オ・ランタンでかまどをクリックしたらパンプキンパイ作れる
        ItemStack itemInMainHand = e.getItem();
        //ジャック・オ・ランタンを持っているか
        if (itemInMainHand != null && itemInMainHand.getType().equals(Material.JACK_O_LANTERN)) {
            Block clicked = e.getClickedBlock();
            //かまどがクリックされたか
            if (clicked != null && clicked.getType().equals(Material.FURNACE)) {
                e.setCancelled(true);
                //1つ引き換える
                itemInMainHand.setAmount(itemInMainHand.getAmount()-1);
                HashMap<Integer, ItemStack> over = e.getPlayer().getInventory().addItem(pumpkinPie);
                //持てなかった分を吐き出す
                for (ItemStack item : over.values()) {
                    e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
                }
            }
        }
    }

    @EventHandler
    public void eatPumpkinPieEvent(FoodLevelChangeEvent e) {
        //食べたらランダムエフェクトを獲得
        ItemStack food = e.getItem();
        if (food == null) { return; }
        //食べたのは特製パンプキンパイか
        ItemMeta foodMeta = food.getItemMeta();
        ItemMeta pumpkinPieMeta = pumpkinPie.getItemMeta();
        if (foodMeta.getDisplayName().equals(pumpkinPieMeta.getDisplayName())) {
            //ランダムエフェクト付与（俊足、跳躍、採掘速度、幸運、透明、暗視）
            int randomIndex = new Random().nextInt(randomEffects.size());
            e.getEntity().addPotionEffect(randomEffects.get(randomIndex));
        }
    }

    @Override
    public void cancel() {
        //処理なし
    }
}