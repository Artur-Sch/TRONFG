package ru.schneider_dev.tronfg.screens;


import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.boontaran.MessageEvent;

import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.TextButton;

import static ru.schneider_dev.tronfg.controls.TextButton.*;


public class LevelFailedScreen extends Group {

    public static final int ON_RETRY = 1;
    public static final int ON_QUIT = 2;

    private Label title;
    private TextButton retry, quit;
    private float w, h;

    public LevelFailedScreen(float w, float h) {
        this.w = w;
        this.h = h;

        // Создаем текстовый заголовок GAME OVER неоновым красным цветом
        Label.LabelStyle titleStyle = new Label.LabelStyle(TRONgame.tr2nFont, new com.badlogic.gdx.graphics.Color(1, 0, 0, 1)); // Неоновый красный
        title = new Label("GAME OVER", titleStyle);
        title.setFontScale(1.8f);
        addActor(title);

        // Принудительно обновляем размеры и центрируем по X
        title.setX((w - title.getPrefWidth()) / 2);
        title.setY(h);

        // Создаем кнопку RESTART неоновым белым цветом
        retry = new TextButton("RESTART", TRONgame.tr2nFont, NEON_WHITE, NEON_YELLOW);
        addActor(retry);
        // Принудительно обновляем размеры перед позиционированием
        retry.updateSize();
        retry.setY((h - retry.getHeight()) / 2 - Y_START_OFFSET_BUTTON); // Поднимаем выше
        retry.setX(w / 2 - retry.getWidth() / 2); // Центрируем по горизонтали
        retry.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RETRY));
                TRONgame.media.playSound("new_click.ogg");
            }
        });

        // Создаем кнопку EXIT неоновым красным цветом
        quit = new TextButton("EXIT", TRONgame.tr2nFont, NEON_RED, NEON_DARK_RED);
        addActor(quit);
        // Принудительно обновляем размеры перед позиционированием
        quit.updateSize();

        quit.setY(retry.getY() - quit.getHeight() - BUTTON_SPACING); // Опускаем ниже
        quit.setX(w / 2 - quit.getWidth() / 2); // Центрируем по горизонтали
        quit.setColor(1, 0, 0, 0); // Красный с прозрачностью 0
        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_QUIT));
                TRONgame.media.playSound("new_click.ogg");
            }
        });
    }

    public void start() {
        title.addAction(Actions.moveTo(title.getX(), h - title.getPrefHeight() - 100, 0.5f, Interpolation.swingOut));
        retry.addAction(Actions.alpha(1, 0.3f));
        quit.addAction(Actions.alpha(1, 0.3f));
    }
}













