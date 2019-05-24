package ru.schneider_dev.tronfg.screens;


import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.boontaran.MessageEvent;

import ru.schneider_dev.tronfg.TRONgame;


public class LevelFailedScreen extends Group {

    public static final int ON_RETRY = 1;
    public static final int ON_QUIT = 2;

    private Image title;
    private ImageButton retry, quit;
    private float w, h;

    public LevelFailedScreen(float w, float h) {
        this.w = w;
        this.h = h;

        title = new Image(TRONgame.atlas.findRegion("game_over"));
        title.setX((w-title.getWidth())/2);
        title.setY(h);
        addActor(title);

        retry = new ImageButton(
                new TextureRegionDrawable(TRONgame.atlas.findRegion("retry_btn")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("retry_btn_down"))    );

        addActor(retry);
        retry.setY((h-retry.getHeight())/2-60);
        retry.setX(w/2-retry.getWidth()-30);
        retry.setColor(1,1,1,0);

        retry.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RETRY));
                TRONgame.media.playSound("click.ogg");
            }
        });

        quit = new ImageButton(
                new TextureRegionDrawable(TRONgame.atlas.findRegion("quit_btn")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("quit_btn_down"))
        );

        addActor(quit);
        quit.setY((h-quit.getHeight())/2-60);
        quit.setX(w/2 + 30);
        quit.setColor(1,1,1,0);
        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_QUIT));
                TRONgame.media.playSound("click.ogg");
            }
        });
    }
    public void start() {
        title.addAction(Actions.moveTo(title.getX(), h-title.getHeight()-50, 0.5f, Interpolation.swingOut));
        retry.addAction(Actions.alpha(1, 0.3f));
        quit.addAction(Actions.alpha(1, 0.3f));
    }
}













