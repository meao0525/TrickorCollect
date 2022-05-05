package com.meao0525.trickandcollect;

import com.meao0525.trickandcollect.command.CommandTabCompleter;
import com.meao0525.trickandcollect.command.GameCommand;
import com.meao0525.trickandcollect.event.DefaultGameEvent;
import com.meao0525.trickandcollect.event.InteractVillagerEvent;
import com.meao0525.trickandcollect.event.PlayerRespawnEvent;
import com.meao0525.trickandcollect.event.PlayerStealItemEvent;
import com.meao0525.trickandcollect.item.GameItems;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public final class TrickandCollect extends JavaPlugin {
    // ゲームフラグ
    private boolean game = false;

    //プレイヤーリスト
    private ArrayList<Player> tcPlayers = new ArrayList<>();
    //人狼人数
    private int traitorNum = 1;
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

    //スコアボード
    private ScoreboardManager manager;
    private Scoreboard info; //設定用

    //チーム
    private Team collectorTeam;
    private Team traitorTeam;

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
        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();
        //タイマーバー作成
        timerBar = Bukkit.createBossBar("残り時間:", BarColor.GREEN, BarStyle.SOLID);
        //スコアボード設定
        manager = Bukkit.getScoreboardManager();
        info = manager.getNewScoreboard();
        reloadInfo();
        //チーム登録
        registerTeam(manager.getMainScoreboard());
    }

    @Override
    public void onDisable() {
        // 停止時
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new DefaultGameEvent(this), this);
        getServer().getPluginManager().registerEvents(new InteractVillagerEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStealItemEvent(this), this);
    }

    public void start() {
        // 始める
        if (game == true) {
            return;
        } else {
            game = true;
        }
        //プレイヤーリスト作成
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                tcPlayers.add(p);
            }
        }

        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();

        /***村人の作り方***/
        World world = Bukkit.getWorlds().get(0);
        //今回の取り立て屋
        collector = (Villager) world.spawnEntity(spawnPoint, EntityType.VILLAGER);
        collector.setAI(false);
        collector.setCustomName("取り立て屋");
        collector.setCustomNameVisible(true);
        //インベントリを与える
        collects = Bukkit.createInventory(collector, 18, "目標アイテム");
        //TODO: 発光させる

        //チーム振り分け
        Collections.shuffle(tcPlayers);
        int tcount = traitorNum;

        for (Player p : tcPlayers) {
            //初期地点を設定する
            p.setBedSpawnLocation(spawnPoint, true);
            //チーム振り分け
            if (tcount > 0) {
                //まだ裏切者を作れる
                traitorTeam.addEntry(p.getDisplayName());
                tcount--;
            } else {
                //残りは集める人
                collectorTeam.addEntry(p.getDisplayName());
            }
            //インベントリ
            setGameInventory(p);

            //全員を同じ場所に飛ばす
            p.teleport(spawnPoint);
            //スコアボード変更
            p.setScoreboard(manager.getMainScoreboard());
            //タイマー表示
            timerBar.addPlayer(p);
            //合図は大事
            p.sendTitle("", ChatColor.GOLD + "--- start! ---", 10, 70, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 3.0F, 3.0F);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "ゲームを開始します");
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
            //チーム解散
            if (collectorTeam.hasEntry(p.getDisplayName())) {
                collectorTeam.removeEntry(p.getDisplayName());
            } else {
                traitorTeam.removeEntry(p.getDisplayName());
            }
            //インベントリを殻にする
            p.getInventory().clear();
            //スコアボード変更
            p.setScoreboard(info);
            //タイマー非表示
            timerBar.removePlayer(p);
            //エフェクト
            p.sendTitle("", ChatColor.GOLD + "--- 終了---", 0, 60, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3F, 0.5F);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "ゲームが終わりました");
        //プレイヤーリストを空にする
        tcPlayers.clear();
    }

    //ゲーム開始時のインベントリ作るやつ
    void setGameInventory(Player player) {
        Inventory inv = player.getInventory();
        //まず空っぽ
        inv.clear();

        //目標アイテム表示
        for (int i = 0; i < GameItems.values().length; i++) {
            inv.setItem(i+9, GameItems.values()[i].toItemStack());
        }
        //残り枠をバリアブロックにする
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }

        //ツール渡す
        for (ItemStack item : createToolsSet()) {
            inv.addItem(item);
        }
    }

    HashSet<ItemStack> createToolsSet() {
        HashSet<ItemStack> tools = new HashSet<>();
        //ツールセット作成
        tools.add(new ItemStack(Material.IRON_SWORD));
        tools.add(new ItemStack(Material.IRON_PICKAXE));
        tools.add(new ItemStack(Material.IRON_AXE));
        tools.add(new ItemStack(Material.IRON_SHOVEL));
        //食い物やるよ
        tools.add(new ItemStack(Material.COOKED_COD, 64));

        return tools;
    }

    public void reloadInfo() {
        //基本情報
        Objective obj = info.getObjective("info");
        //一度消す
        if (obj != null) {
            obj.unregister();
        }
        //再登録
        obj = info.registerNewObjective("info", "dummy", ChatColor.GOLD + "=====[Trick and Collect]=====");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int i = 0;
        //Traitorの数
        Score score = obj.getScore("Traitorの数: " + ChatColor.AQUA + traitorNum);
        score.setScore(i--);

//        //全員に表示
//        for (Player p : Bukkit.getOnlinePlayers()) {
//            p.setScoreboard(info);
//        }
    }

    public void registerTeam(Scoreboard board) {
        //チーム設定
        collectorTeam = board.getTeam("collectorteam");
        traitorTeam = board.getTeam("traitorteam");
        //なかったら作る
        if (collectorTeam == null) {
            collectorTeam = board.registerNewTeam("collectorteam");
            collectorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            collectorTeam.setAllowFriendlyFire(true);
            collectorTeam.setColor(ChatColor.BLUE);
        }
        if (traitorTeam == null) {
            traitorTeam = board.registerNewTeam("traitorteam");
            traitorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            traitorTeam.setAllowFriendlyFire(true);
            traitorTeam.setColor(ChatColor.RED);
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

    public int getTraitorNum() {
        return traitorNum;
    }

    public void setTraitorNum(int traitorNum) {
        this.traitorNum = traitorNum;
    }

    public Scoreboard getInfo() {
        return info;
    }

    public ArrayList<Player> getTcPlayers() {
        return tcPlayers;
    }

    public Team getCollectorTeam() {
        return collectorTeam;
    }

    public Team getTraitorTeam() {
        return traitorTeam;
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
