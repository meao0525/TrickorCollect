package com.meao0525.trickorcollect.gameevent;

import java.util.Random;

public enum GameEventID {
    SHUFFLE_POSITION;

    //ランダムなIDを返す
    public static GameEventID getRandomGameEvent() {
        //全GameEventID取得
        GameEventID[] gameEventIDList = GameEventID.values();
        //乱数生成
        Random random = new Random();
        int randomNum = random.nextInt(gameEventIDList.length);

        return gameEventIDList[randomNum];
    }
}
