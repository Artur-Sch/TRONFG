package ru.schneider_dev.tronfg.screens;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.boontaran.games.StageGame;

import ru.schneider_dev.tronfg.Setting;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.media.LevelIcon;


public class LevelList  extends StageGame {

    public static final int ON_BACK = 1;
    public static final int ON_LEVEL_SELECTED = 2;
    public static final int ON_OPEN_MARKET = 3;
    public static final int ON_SHARE = 4;

    private Group container;
    private int selectedLevelId = 0;

    public LevelList() {
        Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
        addBackground(bg, true, false);

        container = new Group();
        addChild(container);

        int row = 4, col = 4;
        float space = 20;

        float iconWidht = 0, iconHeight = 0;
        int id = 1;
        int x, y;

        int progress = TRONgame.data.getProgress();

        for (y = 0; y < row; y++) {
            for (x = 0; x < col; x++) {
                LevelIcon icon = new LevelIcon(id);
                container.addActor(icon);

                if (iconWidht == 0) {
                    iconWidht = icon.getWidth();
                    iconHeight = icon.getHeight();
                }

                icon.setX(x * (iconWidht + space));
                icon.setY(((row - 1) -y ) * (iconHeight + space));

                if (id <= progress) {
                    icon.setLock(false);
                }
                if (id == progress) {
                    icon.setHilite();
                }

                if (Setting.DEBUG_GAME) {
                    icon.setLock(false);
                }

                icon.addListener(iconlistener);
                id++;
            }
        }

        container.setWidth(col * iconWidht + (col - 1) * space);
        container.setHeight(row * iconHeight + (row - 1) * space);

        container.setX(30);
        container.setY(getHeight() - container.getHeight() - 30);

        container.setColor(1,1,1,0);
        container.addAction(Actions.alpha(1, 0.4f));

        ImageButton rateBtn = new ImageButton(new TextureRegionDrawable(TRONgame.atlas.findRegion("rate")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("rate_down")));

        addChild(rateBtn);
        rateBtn.setX(getWidth() - rateBtn.getWidth() - 20);
        rateBtn.setY(getHeight() - rateBtn.getHeight() - 20);

        rateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                call(ON_OPEN_MARKET);
            }
        });

        ImageButton shareBtn = new ImageButton(new TextureRegionDrawable(TRONgame.atlas.findRegion("share")),
                new TextureRegionDrawable(TRONgame.atlas.findRegion("share_down")));
        addChild(shareBtn);
        shareBtn.setX(getWidth() - shareBtn.getWidth() - 20);
        shareBtn.setY(20);
        shareBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                call(ON_SHARE);
            }
        });
    }
    public int getSelectedLevelId() {
        return selectedLevelId;
    }

    private ClickListener iconlistener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            selectedLevelId = ((LevelIcon)event.getTarget()).getId();
            TRONgame.media.playSound("click.ogg");
            call(ON_LEVEL_SELECTED);
        }
    };

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            TRONgame.media.playSound("click.ogg");
            call(ON_BACK);
            return true;
        }

        return super.keyUp(keycode);
    }
}


















