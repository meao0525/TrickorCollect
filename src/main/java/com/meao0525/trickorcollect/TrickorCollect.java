package com.meao0525.trickorcollect;

import com.meao0525.trickorcollect.command.CommandTabCompleter;
import com.meao0525.trickorcollect.command.GameCommand;
import com.meao0525.trickorcollect.event.*;
import com.meao0525.trickorcollect.item.GameItems;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public final class TrickorCollect extends JavaPlugin {
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
    //収集アイテム保存用
    private ArrayList<ItemStack> collectItems = new ArrayList<>();
    private int itemCount = 0;

    //タイマー
    private int time = 20; //分
    GameTimer timer;
    private BossBar timerBar;

    //スコアボード
    private ScoreboardManager manager;
    private Scoreboard info; //設定用
    private boolean infoFlag = false;

    //チーム
    private Team collectorTeam;
    private Team traitorTeam;

    //TODO: ゲーム内イベントありかも
    /*
     * ウーパールーパー捕まえたらエフェクトたくさん
     * 取り立て屋の周りに襲撃者
     * インベントリシャッフル
     * 座標シャッフル
     */

    //TODO: 投票されたら村人に触れない

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
        spawnPoint.setX(spawnPoint.getX() + 0.5);
        spawnPoint.setZ(spawnPoint.getZ() + 0.5);
        //タイマーバー作成
        timerBar = Bukkit.createBossBar("残り時間:", BarColor.GREEN, BarStyle.SOLID);
        //スコアボード設定
        manager = Bukkit.getScoreboardManager();
        info = manager.getNewScoreboard();
        reloadInfo();
        //チーム登録
        registerTeam(manager.getMainScoreboard());
        //イベント登録
        registerEvents();
    }

    @Override
    public void onDisable() {
        // 停止時
        if (collector != null) {
            collector.remove();
        }
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new DefaultGameEvent(this), this);
        getServer().getPluginManager().registerEvents(new InteractVillagerEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStealItemEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGameChatEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVoteEvent(this), this);

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
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) {
                tcPlayers.add(p);
            }
        }

        //収集アイテムの保存
        createCollectItemList();
        //ゲームプレイヤー設定
        makeTcPlayers();
        //取り立て屋のインベントリを空にする
        collects.clear();
        //光らせる
        collector.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time*60*20, 1, true, false));
        //ノーマルモードにする
        Bukkit.getWorlds().get(0).setDifficulty(Difficulty.NORMAL);

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "ゲームを開始します");
        //イベント登録
        registerEvents();
        //タイマースタート
        timer = new GameTimer(time);
        timer.runTaskTimer(this, 0, 20);
    }

    public void stop() {
        // 終える
        game = false;
        //取り立て屋のインベントリを戻す
        for (int i=0; i<27; i++) {
            ItemStack item = collectItems.get(i);
            if (!item.getType().equals(Material.BARRIER)) {
                collects.setItem(i, item);
            }
        }
        //保存用リストを空にする
        collectItems.clear();
        //光を消す
        collector.removePotionEffect(PotionEffectType.GLOWING);
        itemCount = 0;
        //タイマー止める
        timer.cancel();

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
        //ピースフルにする
        Bukkit.getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "ゲームが終わりました");
        //プレイヤーリストを空にする
        tcPlayers.clear();
    }

    //取り立て屋（村人）作る
    public void summonCollector() {
        if (collector == null) {
            /* 村人の作り方 */
            World world = spawnPoint.getWorld();
            if (world == null) {
                Bukkit.broadcastMessage("ワールドを読み込めませんでした");
                return;
            }
            //足元のブロック
            spawnPoint.add(0, -0.1, 0);
            spawnPoint.getBlock().setType(Material.DIAMOND_BLOCK);
            spawnPoint.add(0, 0.1, 0);
            //今回の取り立て屋
            collector = (Villager) world.spawnEntity(spawnPoint, EntityType.VILLAGER);
            collector.setAI(false);
            collector.setCustomName("取り立て屋");
            collector.setCustomNameVisible(true);
            //インベントリを与える
            collects = Bukkit.createInventory(collector, 27, "目標アイテム");
            //音
            world.playSound(spawnPoint, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
        } else {
            //toggleで消す
            collector.remove();
            collector = null;
        }
    }

    //収集アイテム保存するやつ
    public void createCollectItemList() {
        for (int i=0; i<27; i++) {
            ItemStack item = collects.getItem(i);
            if (item != null) {
                //同じの取得してもスタックしないためのNBT追加
                ItemMeta meta = item.getItemMeta();
                meta.setCustomModelData(1);
                item.setItemMeta(meta);
                collectItems.add(item);
                itemCount++;
            } else {
                collectItems.add(new ItemStack(Material.BARRIER));
            }
        }
    }

    //ゲーム開始時の各プレイヤー設定
    public void makeTcPlayers() {
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
                p.sendMessage(ChatColor.DARK_RED + "あなたはtraitorになりました...w");
                tcount--;
            } else {
                //残りは集める人
                collectorTeam.addEntry(p.getDisplayName());
                p.sendMessage(ChatColor.AQUA + "あなたはcollectorになりました");
            }
            //インベントリ
            setGameInventory(p);

            //全員を同じ場所に飛ばす
            p.teleport(spawnPoint);
            //スコアボード変更
            p.setScoreboard(manager.getMainScoreboard());
            //サバイバルモードに設定
            p.setGameMode(GameMode.SURVIVAL);
            //タイマー表示
            timerBar.addPlayer(p);
            //合図は大事
            p.sendTitle("", ChatColor.GOLD + "--- start! ---", 10, 70, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 3.0F, 3.0F);
        }
    }

    //ゲーム開始時のインベントリ作るやつ
    public void setGameInventory(Player player) {
        Inventory inv = player.getInventory();
        //まず空っぽ
        inv.clear();

        //インベントリに目標アイテムを表示
        for (int i = 0; i < 27; i++) {
            ItemStack item = collectItems.get(i);
            //目標アイテムをコピーしていく
            inv.setItem(i+9, item);
        }

        //ツール渡す
        for (ItemStack item : createToolsSet()) {
            inv.addItem(item);
        }
    }

    public HashSet<ItemStack> createToolsSet() {
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

    public void toggleInfo() {
        infoFlag = !infoFlag;
        //一応リロード
        reloadInfo();
        //スコアボード切り替え
        Scoreboard board;
        if (infoFlag) {
            board = info;
        } else {
            board = manager.getMainScoreboard();
        }
        //全員に表示
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void reloadInfo() {
        //基本情報
        Objective obj = info.getObjective("info");
        //一度消す
        if (obj != null) {
            obj.unregister();
        }
        //再登録
        obj = info.registerNewObjective("info", "dummy", ChatColor.GOLD + "[Trick or Collect]");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int i = 0;
        //Traitorの数
        Score score = obj.getScore("Traitor: " + ChatColor.AQUA + traitorNum);
        score.setScore(i--);
        score = obj.getScore("Spawn: " + ChatColor.AQUA + spawnPoint.getBlockX() + " " + spawnPoint.getBlockY() + " " + spawnPoint.getBlockZ());
        score.setScore(i--);

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
            collectorTeam.setColor(ChatColor.WHITE);
        }
        if (traitorTeam == null) {
            traitorTeam = board.registerNewTeam("traitorteam");
            traitorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            traitorTeam.setAllowFriendlyFire(true);
            traitorTeam.setColor(ChatColor.WHITE);
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
        this.spawnPoint.setX(spawnPoint.getBlockX() + 0.5);
        this.spawnPoint.setZ(spawnPoint.getBlockZ() + 0.5);
        if (collector != null) {
            //足元のブロック
            spawnPoint.add(0, -0.1, 0);
            spawnPoint.getBlock().setType(Material.DIAMOND_BLOCK);
            spawnPoint.add(0, 0.1, 0);
            collector.teleport(spawnPoint);
        }
        reloadInfo();
    }

    public Villager getCollector() {
        return collector;
    }

    public Inventory getCollects() {
        return collects;
    }

    public ArrayList<ItemStack> getCollectItems() {
        return collectItems;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getTraitorNum() {
        return traitorNum;
    }

    public void setTraitorNum(int traitorNum) {
        this.traitorNum = traitorNum;
        reloadInfo();
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
