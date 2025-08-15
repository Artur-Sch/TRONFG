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

import static ru.schneider_dev.tronfg.controls.TextButton.NEON_WHITE;
import static ru.schneider_dev.tronfg.controls.TextButton.NEON_YELLOW;

public class LevelCompletedScreen extends Group {

    public static final int ON_DONE = 1;

    private Label title;
    private TextButton done;
    private float w, h;

    public LevelCompletedScreen(float w, float h) {
        this.w = w;
        this.h = h;

        // Создаем текстовый заголовок LEVEL COMPLETED неоновым желтым цветом
        Label.LabelStyle titleStyle = new Label.LabelStyle(TRONgame.tr2nFont, new com.badlogic.gdx.graphics.Color(1, 1, 0, 1)); // Неоновый желтый
        title = new Label("LEVEL COMPLETED", titleStyle);
        title.setFontScale(1.6f);
        addActor(title);

        // Принудительно обновляем размеры и центрируем по X
        title.setX((w - title.getPrefWidth()) / 2);
        title.setY(h);

        // Создаем кнопку DONE неоновым белым цветом
        done = new TextButton("DONE", TRONgame.tr2nFont, NEON_WHITE, NEON_YELLOW); // Светло-серый для нажатия

        addActor(done);

        // Принудительно обновляем размеры перед позиционированием
        done.updateSize();

        done.setY((h - done.getHeight()) / 2 - 30);
        done.setX(w / 2 - done.getWidth() / 2);
        done.setColor(1, 1, 1, 0);

        done.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_DONE));
                TRONgame.media.playSound("new_click.ogg");
            }
        });
    }

    public void start() {
        title.setY(h);
        title.addAction(Actions.moveTo(title.getX(), h - title.getPrefHeight() - 100, 0.5f, Interpolation.swingOut));
        done.addAction(Actions.alpha(1, 0.3f));
    }
}





















