package ru.schneider_dev.tronfg.screens;


import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.boontaran.MessageEvent;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.MusicTextButton;
import ru.schneider_dev.tronfg.controls.TextButton;

import static ru.schneider_dev.tronfg.controls.TextButton.*;


public class PausedScreen extends Group {

    public static final int ON_RESUME = 1;
    public static final int ON_QUIT = 2;
    public static final int ON_RESTART = 3;

    private Label title;
    private TextButton resume, quit, restart;
    private MusicTextButton soundIcon;

    private float w, h;

    public PausedScreen(float w, float h) {
        this.w = w;
        this.h = h;

        // Создаем текстовый заголовок PAUSE неоновым зеленым цветом
        Label.LabelStyle titleStyle = new Label.LabelStyle(TRONgame.tr2nFont, NEON_GREEN); // Неоновый зеленый
        title = new Label("PAUSE", titleStyle);
        title.setFontScale(1.8f);
        addActor(title);

        // Принудительно обновляем размеры и центрируем по X
        title.setX((w - title.getPrefWidth()) / 2);
        title.setY(h);

        // Создаем кнопку RESUME неоновым белым цветом
        resume = new TextButton("RESUME", TRONgame.tr2nFont, NEON_WHITE, NEON_YELLOW); // Светло-серый для нажатия
        addActor(resume);
        // Принудительно обновляем размеры перед позиционированием
        resume.updateSize();
        // Центрируем кнопки по вертикали и устанавливаем горизонтальное расположение
        resume.setY((h - resume.getHeight()) / 2 - Y_START_OFFSET_BUTTON); // Поднимаем выше
        resume.setX(w / 2 - resume.getWidth() / 2); // Центрируем по горизонтали
        resume.setColor(1, 1, 1, 0); // Начинаем с прозрачности 0
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RESUME));
            }
        });

        // Создаем кнопку RESTART неоновым оранжевым цветом
        restart = new TextButton("RESTART", TRONgame.tr2nFont,
                NEON_ORANGE, // Оранжевый цвет
                NEON_DARK_ORANGE); // Темно-оранжевый для нажатия

        addActor(restart);

        // Принудительно обновляем размеры перед позиционированием
        restart.updateSize();

        // Центрируем кнопки по вертикали и устанавливаем горизонтальное расположение
        restart.setY(resume.getY() - restart.getHeight() - BUTTON_SPACING); // Под RESUME с отступом
        restart.setX(w / 2 - restart.getWidth() / 2); // Центрируем по горизонтали
        restart.setColor(1, 0.5f, 0, 0); // Начинаем с прозрачности 0
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_RESTART));
            }
        });

        // Создаем кнопку EXIT неоновым красным цветом
        quit = new TextButton("EXIT", TRONgame.tr2nFont,
               NEON_RED, // Красный цвет
                NEON_DARK_RED); // Темно-красный для нажатия

        addActor(quit);

        // Принудительно обновляем размеры перед позиционированием
        quit.updateSize();

        // Центрируем кнопки по вертикали и устанавливаем горизонтальное расположение
        quit.setY(restart.getY() - quit.getHeight() - BUTTON_SPACING); // Под RESTART с отступом
        quit.setX(w / 2 - quit.getWidth() / 2); // Центрируем по горизонтали
        quit.setColor(1, 0, 0, 0); // Начинаем с прозрачности 0
        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire(new MessageEvent(ON_QUIT));
            }
        });

        // Создаем иконку звука
        createIcons();
    }

    private void createIcons() {
        // Создаем текстовый индикатор музыки
        soundIcon = new MusicTextButton();

        // Позиционируем в правом верхнем углу
        float margin = 30; // Отступ от краев
        float iconWidth = 150; // Ширина текста (обновлено)
        float iconHeight = 40; // Высота текста

        soundIcon.setPosition(w - iconWidth - margin, margin); // П
        soundIcon.setColor(1, 1, 1, 0); // Начинаем с прозрачности 0

        // Добавляем в сцену
        addActor(soundIcon);
    }

    public void start() {
        title.setY(h);
        resume.setColor(1, 1, 1, 0); // Белый с прозрачностью 0
        restart.setColor(1, 0.5f, 0, 0); // Оранжевый с прозрачностью 0
        quit.setColor(1, 0, 0, 0);   // Красный с прозрачностью 0

//        title.addAction(Actions.moveTo(title.getX(), h - title.getHeight() - 50, 0.5f, Interpolation.swingOut));
        title.addAction(Actions.moveTo(title.getX(), h - title.getHeight() - 100, 0.5f, Interpolation.swingOut));
        resume.addAction(Actions.alpha(1, 0.3f));
        restart.addAction(Actions.alpha(1, 0.3f));
        quit.addAction(Actions.alpha(1, 0.3f));

        // Анимация появления иконки звука
        soundIcon.addAction(Actions.alpha(1, 0.3f));

        // Показываем иконку звука и синхронизируем состояние
        showSoundIcon();
    }

    public void hide() {
        title.addAction(Actions.moveTo(title.getX(), h + title.getHeight(), 0.5f, Interpolation.swingIn));
        resume.addAction(Actions.alpha(0, 0.3f));
        restart.addAction(Actions.alpha(0, 0.3f));
        quit.addAction(Actions.alpha(0, 0.3f));

        // Анимация исчезновения иконки звука
        soundIcon.addAction(Actions.alpha(0, 0.3f));
    }

    public void hideSoundIcon() {
        // Скрываем иконку звука при возврате в игру
        soundIcon.setVisible(false);
    }

    public void showSoundIcon() {
        // Показываем иконку звука и синхронизируем с глобальным состоянием
        soundIcon.setVisible(true);
        soundIcon.setMuted(TRONgame.isSoundMuted);
    }

    public void hideAllIcons() {
        // Скрываем иконку звука при возврате в игру
        soundIcon.setVisible(false);
    }
}




























