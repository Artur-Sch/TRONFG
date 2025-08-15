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
import ru.schneider_dev.tronfg.controls.PngIcon;
import ru.schneider_dev.tronfg.controls.MenuMusicTextButton;


public class LevelList  extends StageGame {

    public static final int ON_BACK = 1;
    public static final int ON_LEVEL_SELECTED = 2;
    public static final int ON_OPEN_MARKET = 3;
    public static final int ON_SHARE = 4;

    private static final String MENU_MUSIC = "new_menu.ogg";

    private Group container;
    private int selectedLevelId = 0;
    private MenuMusicTextButton soundIcon;

    public LevelList() {
        Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
        addBackground(bg, true, false);

        // Запускаем проигрывание музыки меню
        if (!TRONgame.isSoundMuted) {
            TRONgame.media.playMusic(MENU_MUSIC, true);
        }

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

        // Создаем иконку рейтинга (звезда) - белая
        PngIcon rateBtn = new PngIcon(TRONgame.starIcon, 48, com.badlogic.gdx.graphics.Color.WHITE);
        addChild(rateBtn);
        rateBtn.setX(getWidth() - rateBtn.getWidth() - 20);
        rateBtn.setY(getHeight() - rateBtn.getWidth() - 20);

        rateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                call(ON_OPEN_MARKET);
            }
        });

        // Создаем иконку "Поделиться" - белая, слева от кнопки "Оценить"
        PngIcon shareBtn = new PngIcon(TRONgame.shareIcon, 48, com.badlogic.gdx.graphics.Color.WHITE);
        addChild(shareBtn);
        shareBtn.setX(rateBtn.getX() - shareBtn.getWidth() - 20); // Слева от кнопки "Оценить"
        shareBtn.setY(rateBtn.getY()); // На той же высоте что и кнопка "Оценить"
        shareBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                call(ON_SHARE);
            }
        });

        // Создаем кнопку музыки в правом нижнем углу
        createMusicButton();
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
    public int getSelectedLevelId() {
        return selectedLevelId;
    }

    private ClickListener iconlistener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            selectedLevelId = ((LevelIcon)event.getTarget()).getId();
            TRONgame.media.playSound("new_click.ogg");
            call(ON_LEVEL_SELECTED);
        }
    };

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            TRONgame.media.playSound("new_click.ogg");
            call(ON_BACK);
            return true;
        }

        return super.keyUp(keycode);
    }

    @Override
    public void dispose() {
        // Приостанавливаем музыку меню при закрытии экрана
        TRONgame.media.stopMusic(MENU_MUSIC);
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


















