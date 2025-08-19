package ru.schneider_dev.tronfg.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.boontaran.MessageEvent;
import com.badlogic.gdx.utils.Align;

import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.TextButton;
import ru.schneider_dev.tronfg.utils.Data;

import static ru.schneider_dev.tronfg.controls.TextButton.*;

/**
 * Экран завершения уровня
 * <p>
 * Кнопки:
 * - RESTART: перезапуск текущего уровня
 * - NEXT: переход к следующему уровню
 * - EXIT: возврат к выбору уровней (сохраняет время прохождения) - последняя снизу
 */
public class LevelCompletedScreen extends Group {

    public static final int ON_DONE = 1;
    public static final int ON_RESTART = 2;
    public static final int ON_NEXT = 3;

    private Label title;
    private Label currentTimeLabel;
    private Label bestTimeLabel;
    private TextButton exit;
    private TextButton restart;
    private TextButton next;
    private float w, h;
    private int levelId;
    private float completionTime;

    public LevelCompletedScreen(float w, float h, int levelId, float completionTime) {
        this.w = w;
        this.h = h;
        this.levelId = levelId;
        this.completionTime = completionTime;

        // Заголовок
        Label.LabelStyle titleStyle = new Label.LabelStyle(TRONgame.tr2nFont, YELLOW);
        title = new Label("LEVEL COMPLETED", titleStyle);
        title.setFontScale(1.6f);
        addActor(title);
        title.setX((w - title.getPrefWidth()) / 2);
        title.setY(h);

        // Стили для времени
        Label.LabelStyle timeStyle = new Label.LabelStyle(TRONgame.font24, NEON_WHITE);
        Label.LabelStyle bestStyle = new Label.LabelStyle(TRONgame.font24, NEON_YELLOW);

        // Текущее время
        currentTimeLabel = new Label("TIME: " + formatTime(completionTime), timeStyle);
        currentTimeLabel.setAlignment(Align.center);
        addActor(currentTimeLabel);

        // Лучшее время
        bestTimeLabel = new Label("BEST: " + getBestTimeText(), bestStyle);
        bestTimeLabel.setAlignment(Align.center);
        addActor(bestTimeLabel);


        // Кнопка RESTART (неоновый оранжевый) - третья снизу
        restart = new TextButton("RESTART", TRONgame.tr2nFont, NEON_ORANGE, NEON_DARK_ORANGE);
        addActor(restart);
        restart.updateSize();
        restart.setY((h - restart.getHeight()) / 2 - Y_START_OFFSET_BUTTON);
        restart.setX(w / 2 - restart.getWidth() / 2);
        restart.setColor(1, 1, 1, 0);
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RESTART));
                TRONgame.media.playSound("new_click.ogg");
            }
        });

        // Кнопка NEXT - вторая по центру
        next = new TextButton("NEXT", TRONgame.tr2nFont, NEON_WHITE, NEON_YELLOW);
        addActor(next);
        next.updateSize();
        next.setY(restart.getY() - next.getHeight() - BUTTON_SPACING);
        next.setX(w / 2 - next.getWidth() / 2);
        next.setColor(1, 1, 1, 0);
        next.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_NEXT));
                TRONgame.media.playSound("new_click.ogg");
            }
        });

        // Кнопка EXIT (красная) - последняя снизу
        exit = new TextButton("EXIT", TRONgame.tr2nFont, NEON_RED, NEON_DARK_RED);
        addActor(exit);
        exit.updateSize();
        exit.setY(next.getY() - exit.getHeight() - BUTTON_SPACING); // Под NEXT с отступом
        exit.setY(30);
        exit.setX(w / 2 - exit.getWidth() / 2);
        exit.setColor(1, 1, 1, 0);
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_DONE));
                TRONgame.media.playSound("new_click.ogg");
            }
        });
    }



    // Обновление данных (уровень + время) при завершении
    public void setData(int levelId, float completionTime) {
        this.levelId = levelId;
        this.completionTime = completionTime;
        if (currentTimeLabel != null) {
            currentTimeLabel.setText("TIME: " + formatTime(completionTime));
            // Перепозиционируем по центру, если ширина изменилась
            currentTimeLabel.setX((w - currentTimeLabel.getPrefWidth()) / 2);
        }
        if (bestTimeLabel != null) {
            bestTimeLabel.setText("BEST: " + getBestTimeText());
            bestTimeLabel.setX((w - bestTimeLabel.getPrefWidth()) / 2);
        }
    }

    private String getBestTimeText() {
        Data data = TRONgame.data;
        if (data != null) {
            float bestTime = data.getFloat("cache_best_" + levelId, 0f);
            if (bestTime > 0f) {
                return formatTime(bestTime);
            }
        }
        return "-";
    }

    private String formatTime(float timeInSeconds) {
        int minutes = (int) (timeInSeconds / 60);
        int seconds = (int) (timeInSeconds % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void start() {
        title.setY(h);
        // Поднимаем заголовок ближе к верхней границе
        title.addAction(Actions.moveTo(title.getX(), h - title.getPrefHeight() - 60, 0.5f, Interpolation.swingOut));

        // Позиционируем лейблы времени
        currentTimeLabel.setX((w - currentTimeLabel.getPrefWidth()) / 2);
        currentTimeLabel.setY(h - title.getPrefHeight() - 100);
        currentTimeLabel.addAction(Actions.moveTo(currentTimeLabel.getX(), h - title.getPrefHeight() - 120, 0.5f, Interpolation.swingOut));

        bestTimeLabel.setX((w - bestTimeLabel.getPrefWidth()) / 2);
        bestTimeLabel.setY(h - title.getPrefHeight() - 150);
        bestTimeLabel.addAction(Actions.moveTo(bestTimeLabel.getX(), h - title.getPrefHeight() - 160, 0.5f, Interpolation.swingOut));

        exit.addAction(Actions.alpha(1, 0.3f));
        restart.addAction(Actions.alpha(1, 0.3f));
        next.addAction(Actions.alpha(1, 0.3f));
    }
}





















