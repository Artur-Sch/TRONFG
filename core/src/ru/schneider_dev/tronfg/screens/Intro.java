package ru.schneider_dev.tronfg.screens;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.boontaran.games.StageGame;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.TextButton;

import static ru.schneider_dev.tronfg.controls.TextButton.NEON_YELLOW;

public class Intro extends StageGame {

    public static final int ON_PLAY = 1;
    public static final int ON_BACK = 2;

    // Константы для анимации и позиционирования
    private static final float TITLE_FONT_SCALE = 2.0f;
    private static final float TITLE_ANIMATION_DELAY = 0.2f;
    private static final float TITLE_ANIMATION_DURATION = 0.4f;
    private static final float TITLE_MOVE_MULTIPLIER = 1.5f; // Уменьшил с 2.0f до 1.5f - title поднимется выше
    private static final float PLAY_BUTTON_Y_OFFSET = 100f; // Увеличил с 60f до 80f - кнопка PLAY опустится ниже
    private static final float PLAY_BUTTON_FADE_DELAY = 0.8f;
    private static final float PLAY_BUTTON_FADE_DURATION = 0.8f;
    private static final float PLAY_BUTTON_FADE_OUT_DURATION = 0.3f;
    private static final float TITLE_EXIT_DURATION = 0.5f;
    private static final float EXIT_DELAY = 0.7f;
    private static final String CLICK_SOUND = "new_click.ogg";
    private static final String DELAY_CODE_1 = "delay1";
    private static final String MENU_MUSIC = "new_menu.ogg";

    private TextButton titleLabel;
    private TextButton playBtn;

    public Intro() {
        setupBackground();
        setupTitle();
        setupPlayButton();
        // Запускаем проигрывание музыки меню
        if (!TRONgame.isSoundMuted) {
            TRONgame.media.playMusic(MENU_MUSIC, true);
        }
    }

    private void setupBackground() {
        Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
        addBackground(bg, true, false);
    }

    private void setupTitle() {
        titleLabel = new TextButton("TRON FG", TRONgame.tr2nFont, Color.WHITE, Color.WHITE);
        titleLabel.setFontScale(TITLE_FONT_SCALE);
        addChild(titleLabel);

        centerHorizontally(titleLabel);
        titleLabel.setY(getHeight());

        setupTitleAnimation();
    }

    private void setupTitleAnimation() {
        // Анимация движения заголовка
        MoveByAction moveAction = new MoveByAction();
        moveAction.setAmount(0, -titleLabel.getHeight() * TITLE_MOVE_MULTIPLIER);
        moveAction.setDuration(TITLE_ANIMATION_DURATION);
        moveAction.setInterpolation(Interpolation.swingOut);
        moveAction.setActor(titleLabel);

        titleLabel.addAction(Actions.delay(TITLE_ANIMATION_DELAY, moveAction));

        // Анимация цвета заголовка
        titleLabel.addAction(Actions.delay(TITLE_ANIMATION_DELAY,
                Actions.run(titleLabel::startColorAnimation)));
    }

    private void setupPlayButton() {
        playBtn = new TextButton("PLAY", TRONgame.tr2nFont, Color.WHITE, NEON_YELLOW);
        addChild(playBtn);

        centerHorizontally(playBtn);
        playBtn.setY(getHeight() / 2 - PLAY_BUTTON_Y_OFFSET);

        setupPlayButtonAnimation();
        setupPlayButtonListener();
    }

    private void setupPlayButtonAnimation() {
        playBtn.setColor(1, 1, 1, 0);
        playBtn.addAction(Actions.delay(PLAY_BUTTON_FADE_DELAY,
                Actions.fadeIn(PLAY_BUTTON_FADE_DURATION)));
    }

    private void setupPlayButtonListener() {
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClickPlay();
                TRONgame.media.playSound(CLICK_SOUND);
            }
        });
    }

    private void centerHorizontally(TextButton button) {
        button.updateSize();
        button.setX((getWidth() - button.getWidth()) / 2);
    }

    private void onClickPlay() {
        playBtn.setTouchable(Touchable.disabled);
        playBtn.addAction(Actions.fadeOut(PLAY_BUTTON_FADE_OUT_DURATION));

        titleLabel.addAction(Actions.moveTo(
                titleLabel.getX(),
                getHeight() + titleLabel.getHeight(),
                TITLE_EXIT_DURATION,
                Interpolation.swingIn));

        delayCall(DELAY_CODE_1, EXIT_DELAY);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            call(ON_BACK);
            return true;
        }
        return super.keyUp(keycode);
    }

    @Override
    protected void onDelayCall(String code) {
        if (DELAY_CODE_1.equals(code)) {
            call(ON_PLAY);
        }
    }

    @Override
    public void dispose() {
        // Останавливаем музыку меню при закрытии экрана
        TRONgame.media.stopMusic(MENU_MUSIC);
        // Очистка ресурсов если необходимо
        super.dispose();
    }
}


















