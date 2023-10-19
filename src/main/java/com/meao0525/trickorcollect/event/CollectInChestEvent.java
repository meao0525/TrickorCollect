package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CollectInChestEvent implements Listener {

    private TrickorCollect plugin;
    private static HashMap<Player, Chest> playerChestMap = new HashMap<>();
    //各プレイヤーのスコア
    private Objective obj;
    private final String SCORE_NAME = "eachScore";

    public CollectInChestEvent(TrickorCollect plugin) {
        this.plugin = plugin;
        //全プレイヤーのチェストマップ
        playerChestMap = new HashMap<>();
        //各プレイヤーのスコア表示
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        obj = manager.getMainScoreboard().getObjective(SCORE_NAME);
        if (obj == null) {
            obj = manager.getMainScoreboard().registerNewObjective(SCORE_NAME, "dummy", ChatColor.AQUA + "=====集めた数=====");
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player player : plugin.getTcPlayers()) {
            //Metaに登録されているChestに紐づけ
            List<MetadataValue> valueList = player.getMetadata("collectChest");
            if (valueList.size() > 0) {
                //Metaに登録されているチェスト取得
                Chest chest;
                try {
                    chest = (Chest) valueList.get(0).value();
                    //名前つける
                    chest.setCustomName(player.getDisplayName() + " の納品先チェスト");
                    //空にする
                    chest.getInventory().clear();
                } catch (Exception exception) {
                    Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.RED + " のチェストの登録に失敗しました...");
                    continue;
                }
                //マップに登録
                playerChestMap.put(player, chest);
                //スコア初期化
                obj.getScore(player.getDisplayName()).setScore(0);

            } else {
                Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.RED + " のチェストの登録に失敗しました...");
            }
        }
    }

    @EventHandler
    public void CollectInChestEventListener(PlayerInteractEvent e) {
        //クリックしたのがチェストか
        Block clicked = e.getClickedBlock();
        Chest chest;
        if (clicked != null && clicked.getState() instanceof Chest) {
            chest = (Chest) clicked.getState();
        } else {
            return;
        }

        //プレイヤー取得
        Player player = e.getPlayer();
        //自分の納品先チェストをクリック
        if (!playerChestMap.get(player).equals(chest)) {
            return;
        }

        ItemStack inMainHand = player.getInventory().getItemInMainHand();
        //ゲーム中か
        if (plugin.isGame()) {
            //ルール確認
            if (!plugin.getRule().equals("collectEach")) {
                player.sendMessage(ChatColor.GRAY + "[取り立て屋]に納品してください");
                return;
            }
            //目標アイテム取得
            ArrayList<ItemStack> collectItems = plugin.getCollectItems();
            //手に持ってるアイテムが目標アイテムか
            for (int i=0; i<collectItems.size(); i++) {
                if (inMainHand.getType().equals(collectItems.get(i).getType())) {
                    //集め終わってるなら飛ばす
                    ItemStack item = chest.getInventory().getItem(i);
                    if (item != null && item.getEnchantmentLevel(Enchantment.BINDING_CURSE) == 255) { continue; }
                    //デフォのイベントキャンセル
                    e.setCancelled(true);
                    //納品アイテム持ってたら回収
                    int dif = collectItem(i, collectItems.get(i).getAmount(), inMainHand, chest);
                    if (dif >= 0) {
                        //完了通知
                        String msg = ChatColor.GOLD + "[Trick or Collect]" +
                                ChatColor.RESET + player.getName() + "が " +
                                ChatColor.AQUA + inMainHand.toString() +
                                ChatColor.RESET + " を集め終わりました";
                        Bukkit.broadcastMessage(msg);
                        //スコア更新
                        int score = obj.getScore(player.getDisplayName()).getScore();
                        obj.getScore(player.getDisplayName()).setScore(++score);
                    } else {
                        dif = 0;
                    }
                    //回収しまーす
                    player.getInventory().getItemInMainHand().setAmount(dif);
                    //納品エフェクト
                    chest.getWorld().playSound(chest.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f, 0.5f);
                    //ゲーム終わったかな？？？？
                    check(player, chest);
                    return;
                }
            }

        }

    }

    public int collectItem(int index, int max, ItemStack item, Chest chest) {
        //アイテムを納品する処理
        Inventory collects = chest.getInventory();
        //現在の納品数、新しい納品数
        int currentAmount, newAmount;

        newAmount = item.getAmount();
        if (collects.contains(item.getType())) {
            //同じアイテムすでに納品済み
            currentAmount = collects.getItem(index).getAmount();
        } else {
            //そのアイテムは君が最初の納品者だ
            currentAmount = 0;
        }

        if (currentAmount + newAmount >= max) {
            //あふれちゃう
            ItemStack complete = new ItemStack(item.getType(), max);
            //完了エフェクト
            ItemMeta meta = complete.getItemMeta();
            meta.addEnchant(Enchantment.BINDING_CURSE, 255, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            complete.setItemMeta(meta);
            collects.setItem(index, complete);
        } else {
            //足した数をセット
            collects.setItem(index, new ItemStack(item.getType(), currentAmount+newAmount));
        }
        //差分を返す
        return currentAmount + newAmount - max;
    }

    public void check(Player player, Chest chest) {
        //ゲーム終わってたりしないよね？
        int checkcount = 0;
        //現在の納品状況
        Inventory collects = chest.getInventory();
        //目標アイテム取得
        ArrayList<ItemStack> collectItems = plugin.getCollectItems();
        //必要数たりてるかちぇえええええっく
        for (int i=0; i<collectItems.size(); i++) {
            ItemStack current = collects.getItem(i);
            if (current != null && collectItems.get(i).getAmount() == current.getAmount()) {
                checkcount++;
            }
        }
        //全種類集め終わった
        int need = plugin.getItemCount();
        if (checkcount == need) {
            //ログ
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" +
                    ChatColor.AQUA + player.getDisplayName() +
                    ChatColor.RESET + " ゲームクリア！");
            //スペクテイターモードになる
            player.setGameMode(GameMode.SPECTATOR);
            //全プレイヤー処理
            for (Player p : Bukkit.getOnlinePlayers()) {
                //効果音
                if (p.getGameMode().equals(GameMode.SPECTATOR)) {
                    p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f);
                } else {
                    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 4.0f);
                }
            }

        }
    }

    //全納品先チェスト取得
    public static Collection<Chest> getPlayerChests() {
        return playerChestMap.values();
    }
}
