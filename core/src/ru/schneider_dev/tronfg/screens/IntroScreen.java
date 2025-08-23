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
import ru.schneider_dev.tronfg.controls.MenuMusicTextButton;
import ru.schneider_dev.tronfg.controls.TextButton;

import static ru.schneider_dev.tronfg.controls.TextButton.NEON_YELLOW;

public class IntroScreen extends StageGame {

    public static final int ON_PLAY = 1;
    public static final int ON_BACK = 2;
    public static final int ON_SCORE = 3;

    // Константы для анимации и позиционирования
    private static final float TITLE_FONT_SCALE = 2.2f;
    private static final float TITLE_ANIMATION_DELAY = 0.2f;
    private static final float TITLE_ANIMATION_DURATION = 0.4f;
    private static final float TITLE_MOVE_MULTIPLIER = 1.5f; // Уменьшил с 2.0f до 1.5f - title поднимется выше
    private static final float Y_OFFSET = 120f; // Увеличил с 60f до 80f - кнопка PLAY опустится ниже
    private static final float PLAY_BUTTON_FADE_DELAY = 0.8f;
    private static final float PLAY_BUTTON_FADE_DURATION = 0.8f;
    private static final float PLAY_BUTTON_FADE_OUT_DURATION = 0.3f;
    private static final float TITLE_EXIT_DURATION = 0.5f;
    private static final float EXIT_DELAY = 0.7f;
    private static final String CLICK_SOUND = "new_click.ogg";
    private static final String DELAY_CODE_1 = "delay1";
    private static final String MENU_MUSIC = "new_menu.ogg";
    private MenuMusicTextButton soundIcon;

    private TextButton titleLabel;
    private TextButton playBtn;
    private TextButton scoreBtn;

    public IntroScreen() {
        setupBackground();
        setupTitle();
        setupPlayButton();
        setupScoreButton();
        // Запускаем проигрывание музыки меню
        if (!TRONgame.isSoundMuted) {
            TRONgame.media.playMusic(MENU_MUSIC, true);
        }
        createMusicButton();
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
        playBtn.setY(getHeight() / 2 - Y_OFFSET);

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
                TRONgame.playSoundSafe(CLICK_SOUND);
                call(ON_PLAY);
            }
        });
    }

    private void setupScoreButton() {
        scoreBtn = new TextButton("SCORE", TRONgame.tr2nFont, Color.WHITE, Color.GREEN);
        addChild(scoreBtn);

        centerHorizontally(scoreBtn);
        scoreBtn.setY(30);

        setupScoreButtonAnimation();
        setupScoreButtonListener();
    }

    private void setupScoreButtonAnimation() {
        scoreBtn.setColor(1, 1, 1, 0);
        scoreBtn.addAction(Actions.delay(PLAY_BUTTON_FADE_DELAY + 0.2f,
                Actions.fadeIn(PLAY_BUTTON_FADE_DURATION)));
    }

    private void setupScoreButtonListener() {
        scoreBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TRONgame.playSoundSafe(CLICK_SOUND);
                call(ON_SCORE);
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

        // Добавляем анимацию исчезновения для кнопки SCORE
        scoreBtn.setTouchable(Touchable.disabled);
        scoreBtn.addAction(Actions.fadeOut(PLAY_BUTTON_FADE_OUT_DURATION));

        titleLabel.addAction(Actions.moveTo(
                titleLabel.getX(),
                getHeight() + titleLabel.getHeight(),
                TITLE_EXIT_DURATION,
                Interpolation.swingIn));

        delayCall(DELAY_CODE_1, EXIT_DELAY);
    }

    private void onClickScore() {
        scoreBtn.setTouchable(Touchable.disabled);
        scoreBtn.addAction(Actions.fadeOut(PLAY_BUTTON_FADE_OUT_DURATION));
        
        // Добавляем анимацию исчезновения для кнопки PLAY
        playBtn.setTouchable(Touchable.disabled);
        playBtn.addAction(Actions.fadeOut(PLAY_BUTTON_FADE_OUT_DURATION));
        
        call(ON_SCORE);
    }

    private void createMusicButton() {
        // Создаем текстовый индикатор музыки
        soundIcon = new MenuMusicTextButton();

        // Позиционируем в правом нижнем углу
        float margin = 30; // Отступ от краев
        float iconWidth = 150; // Ширина текста
        float iconHeight = 40; // Высота текста

        soundIcon.setPosition(getWidth() - iconWidth - margin, margin); // Правый нижний угол
        soundIcon.setColor(1, 1, 1, 0); // Начинаем с прозрачности 0

        // Добавляем в сцену
        addChild(soundIcon);
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
    public void start() {
        // Анимация появления иконки звука
        if (soundIcon != null) {
            soundIcon.addAction(Actions.alpha(1, 0.3f));
            showSoundIcon();
        }
    }

    public void hide() {
        // Анимация исчезновения иконки звука
        if (soundIcon != null) {
            soundIcon.addAction(Actions.alpha(0, 0.3f));
        }
    }

    public void showSoundIcon() {
        // Показываем иконку звука и синхронизируем с глобальным состоянием
        if (soundIcon != null) {
            soundIcon.setVisible(true);
            soundIcon.setMuted(TRONgame.isSoundMuted);
        }
    }

    public void hideSoundIcon() {
        // Скрываем иконку звука
        if (soundIcon != null) {
            soundIcon.setVisible(false);
        }
    }
}


















