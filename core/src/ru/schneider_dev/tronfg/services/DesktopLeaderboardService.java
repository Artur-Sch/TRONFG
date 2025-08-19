package ru.schneider_dev.tronfg.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class DesktopLeaderboardService implements LeaderboardService {
    
    private final Json json;
    
    public DesktopLeaderboardService() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
    }
    
    @Override
    public void submitResult(String userId, int level, float time, LeaderboardCallback callback) {
        // На десктопе просто показываем заглушку
        Gdx.app.log("DesktopLeaderboardService", "Submit result: " + userId + ", level: " + level + ", time: " + time);
        
        // Имитируем задержку сети
        new Thread(() -> {
            try {
                Thread.sleep(300);
                Gdx.app.postRunnable(() -> {
                    callback.onSuccess(1, 1, time);
                });
            } catch (InterruptedException e) {
                Gdx.app.postRunnable(() -> {
                    callback.onError("Interrupted");
                });
            }
        }).start();
    }

    @Override
    public void getRank(String userId, int level, LeaderboardCallback callback) {
        // Заглушка: возвращаем 1/1
        new Thread(() -> {
            try {
                Thread.sleep(150);
                Gdx.app.postRunnable(() -> callback.onSuccess(1, 1, 0f));
            } catch (InterruptedException e) {
                Gdx.app.postRunnable(() -> callback.onError("Interrupted"));
            }
        }).start();
    }
}
