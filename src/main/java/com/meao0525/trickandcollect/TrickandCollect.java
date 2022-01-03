package com.meao0525.trickandcollect;

import com.meao0525.trickandcollect.command.CommandTabCompleter;
import com.meao0525.trickandcollect.command.GameCommand;
import com.meao0525.trickandcollect.event.DefaultGameEvent;
import com.meao0525.trickandcollect.event.InteractVillagerEvent;
import com.meao0525.trickandcollect.item.GameItems;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public final class TrickandCollect extends JavaPlugin {
    // ゲームフラグ
    private boolean game = false;

    //プレイヤーリスト
    private ArrayList<Player> tcPlayers = new ArrayList<>();
    //取り立て屋
    Villager collector;

    // 初期地点
    private Location spawnPoint;
    //収集進捗格納用
    Inventory collects;

    //タイマー
    private int time = 20; //分
    GameTimer timer;
    private BossBar timerBar;

    //TODO: 初期地点に戻させるアイテム
    //TODO: 盗めるアイテム
    //TODO: ゲーム内イベントありかも
    /*
     *ウーパールーパー捕まえたら足早くなる
     *
     */

    @Override
    public void onEnable() {
        // 起動時
        getLogger().info("だまして集める");
        //コマンド設定
        getCommand("tc").setExecutor(new GameCommand(this));
        //タブ保管できるようにする
        getCommand("tc").setTabCompleter(new CommandTabCompleter());
        //タイマーバー作成
        timerBar = Bukkit.createBossBar("残り時間:", BarColor.GREEN, BarStyle.SOLID);
    }

    @Override
    public void onDisable() {
        // 停止時
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new DefaultGameEvent(this), this);
        getServer().getPluginManager().registerEvents(new InteractVillagerEvent(this), this);
    }

    public void start() {
        // 始める
        game = true;
        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();
        //プレイヤーリスト作成
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                tcPlayers.add(p);
            }
        }

        /***村人の作り方***/
        World world = Bukkit.getWorlds().get(0);
        //今回の取り立て屋
        collector = (Villager) world.spawnEntity(spawnPoint, EntityType.VILLAGER);
        collector.setAI(false);
        collector.setCustomName("取り立て屋");
        collector.setCustomNameVisible(true);
        //インベントリを与える
        collects = Bukkit.createInventory(collector, 18, "目標アイテム");

        for (Player p : tcPlayers) {
            //TODO: 初期地点を設定する
            //TODO: 目標アイテム設置する
            //TODO: チーム振り分け
            //インベントリ
            setGameInventory(p);

            //TODO: 全員を同じ場所に飛ばす
            //タイマー表示
            timerBar.addPlayer(p);
            //合図は大事
            p.sendTitle("", ChatColor.GOLD + "--- start! ---", 10, 70, 20);
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "ゲームを開始します");
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 3.0F, 3.0F);
        }

        //TODO: タイマースタート

        //イベント登録
        registerEvents();
        //タイマースタート
        timer = new GameTimer(time);
        timer.runTaskTimer(this, 0, 20);
    }

    public void stop() {
        // 終える
        game = false;
        //村人は用済み
        collector.damage(8000);
        //タイマー止める
        timer.cancel();
        //インベントリ空にする
        for (Player p : tcPlayers) {
            //インベントリを殻にする
            p.getInventory().clear();
            //タイマー非表示
            timerBar.removePlayer(p);
            //エフェクト
            p.sendTitle("", ChatColor.GOLD + "--- 終了---", 0, 60, 20);
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "ゲームが終わりました");
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3F, 0.5F);
        }
    }

    //ゲーム開始時のインベントリ作るやつ
    void setGameInventory(Player player) {
        Inventory inv = player.getInventory();
        //まず空っぽ
        inv.clear();
        //TODO: ツール渡す

        //目標アイテム表示
        for (int i = 0; i < GameItems.values().length; i++) {
            inv.setItem(i+9, GameItems.values()[i].toItemStack());
        }
        //残り枠をバリアブロックにする
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }
    }

    //げったんせったん

    public boolean isGame() {
        return game;
    }

    public void setGame(boolean game) {
        this.game = game;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Villager getCollector() {
        return collector;
    }

    public Inventory getCollects() {
        return collects;
    }

    //タイマー用内部クラス
    private class GameTimer extends BukkitRunnable {

        private int time;
        private double maxTime;


        GameTimer(int time) {
            this.maxTime = time * 60;
            this.time = time * 60;
        }

        @Override
        public void run() {
            if (time > 0) {
                timerBar.setTitle("残り時間:" + time/60 + "m " + time%60 + "s");
                timerBar.setProgress(time/maxTime);
                //タイマーバーの色を変えるよ
                if (time/maxTime < 0.5) {
                    timerBar.setColor(BarColor.YELLOW);
                } else if (time/maxTime < 0.2) {
                    timerBar.setColor(BarColor.RED);
                }
                //最後のカウントダウン
                if (time < 6) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.3F, 0.5F);
                    }
                }
            } else {
                //ゲーム終了
                stop();
            }
            //1秒減らす
            time--;
        }
    }
}
