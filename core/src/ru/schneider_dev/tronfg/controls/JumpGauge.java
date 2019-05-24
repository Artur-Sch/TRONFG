package ru.schneider_dev.tronfg.controls;


import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

import ru.schneider_dev.tronfg.TRONgame;


public class JumpGauge  extends Group {

    private Array<Image> offs, ons;
    private final float TIME = 0.5f;
    private float time = 0;
    private boolean counting = false;

    public JumpGauge() {
        setTouchable(Touchable.disabled);
        Image box;

        offs = new Array<Image>();
        ons = new Array<Image>();
        NinePatch patch = new NinePatch(TRONgame.atlas.findRegion("jump_gauge_off"), 3,3,3,3);

        int i;
        int w;
        for (i = 0; i < 5; i++) {
            w = (i+3)*4;
            box = new Image(patch);
            box.setWidth(w);
            box.setHeight(8);

            box.setX(-box.getWidth()/2);
            box.setY(14*i);

            addActor(box);
            offs.add(box);


        }

        patch = new NinePatch(TRONgame.atlas.findRegion("jump_gauge_on"), 3, 3, 3, 3);

        for (i = 0; i < 5; i++) {
            w = (i+3)*4;

            box = new Image(patch);
            box.setWidth(w);
            box.setHeight(8);

            box.setX(-box.getWidth()/2);
            box.setY(14*i);

            addActor(box);
            ons.add(box);
            box.setVisible(false);
        }

        setVisible(false);
    }

    @Override
    public void act(float delta) {
        if (counting && time < TIME) {
            time += delta;

            float value = time/TIME;

            if (value < 1f/5) {
                ons.get(0).setVisible(true);
            } else if (value < 2f/5) {
                ons.get(1).setVisible(true);
            } else if (value < 3f/5) {
                ons.get(2).setVisible(true);
            } else if (value < 4f/5) {
                ons.get(3).setVisible(true);
            } else if (value < 5f/5) {
                ons.get(4).setVisible(true);
            }
        }

        super.act(delta);
    }

    public void start() {
        if (counting) return;
        counting = true;
        setVisible(true);
        time = 0f;
    }

    public float getValue() {
        setVisible(false);
        counting = false;

        for (Image box : ons) {
            box.setVisible(false);
        }
        float value = time/TIME;
        time = 0;

        return value;
    }
}





















