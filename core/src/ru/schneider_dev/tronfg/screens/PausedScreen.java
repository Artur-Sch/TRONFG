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


public class PausedScreen extends Group {

    public static final int ON_RESUME = 1;
    public static final int ON_QUIT = 2;

    private Image title;
    private ImageButton resume, quit;

    private float w, h;

    public PausedScreen(float w, float h) {
        this.w = w;
        this.h = h;

        title = new Image(TRONgame.atlas.findRegion("paused"));
        title.setX((w - title.getWidth()) / 2);
        title.setY(h);
        addActor(title);

        resume = new ImageButton(new TextureRegionDrawable(TRONgame.atlas.findRegion("play_btn")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("play_btn_down")
                ));

        addActor(resume);
        resume.setY((h - resume.getHeight()) / 2 - 60);
        resume.setX(w / 2 - resume.getWidth() - 30);
        resume.setColor(1, 1, 1, 0);
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RESUME));
            }
        });

        quit = new ImageButton(
                new TextureRegionDrawable(TRONgame.atlas.findRegion("quit_btn")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("quit_btn_down"))
        );

        addActor(quit);
        quit.setY((h - quit.getHeight()) / 2 - 60);
        quit.setX(w / 2 + 30);
        quit.setColor(1, 1, 1, 0);
        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                fire(new MessageEvent(ON_QUIT));
            }
        });
    }

    public void start() {
        title.setY(h);
        resume.setColor(1,1,1,0);
        quit.setColor(1,1,1,0);

        title.addAction(Actions.moveTo(title.getX(), h-title.getHeight()-50, 0.5f, Interpolation.swingOut));
        resume.addAction(Actions.alpha(1, 0.3f));
        quit.addAction(Actions.alpha(1, 0.3f));
    }

    public void hide() {
        title.addAction(Actions.moveTo(title.getX(), h, 0.5f, Interpolation.swingIn));
        resume.addAction(Actions.alpha(0, 0.3f));
        quit.addAction(Actions.alpha(0, 0.3f));
    }
}




























