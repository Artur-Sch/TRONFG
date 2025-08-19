package ru.schneider_dev.tronfg.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.boontaran.games.StageGame;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.TextButton;

import static ru.schneider_dev.tronfg.controls.TextButton.*;

public class GameCompletedScreen extends StageGame {

    public static final int ON_DONE = 1;

    private Label title;
    private Label endOfLineText;
    private Label messageText;
    private TextButton doneButton;

    public GameCompletedScreen() {
        // Создаем фон
        Image bg = new Image(TRONgame.atlas.findRegion("intro_bg"));
        addBackground(bg, true, false);

        // Создаем заголовок "USER WIN" неоновым желтым цветом
        Label.LabelStyle titleStyle = new Label.LabelStyle(TRONgame.tr2nFont, NEON_YELLOW);
        title = new Label("USER WIN", titleStyle);
        title.setFontScale(2.0f);
        addChild(title);

        // Создаем текст прощания
        Label.LabelStyle endOfLineStyle = new Label.LabelStyle(TRONgame.font24, NEON_WHITE);
        endOfLineText = new Label("END OF LINE.", endOfLineStyle);
        addChild(endOfLineText);

        // Создаем текст сообщения
        Label.LabelStyle messageStyle = new Label.LabelStyle(TRONgame.font24, NEON_WHITE);
        messageText = new Label("But the Game never truly ends...", messageStyle);
        addChild(messageText);

        // Создаем кнопку "DONE" неоновым красным цветом
        doneButton = new TextButton("DONE", TRONgame.tr2nFont,
                NEON_RED, // Неоновый красный
               NEON_DARK_RED); // Темно-красный для нажатия
        addChild(doneButton);

        // Позиционируем элементы
        positionElements();

        // Добавляем обработчик для кнопки
        doneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TRONgame.media.playSound("new_click.ogg");
                // Останавливаем музыку завершения игры
                TRONgame.media.stopMusic("grid_reflection.ogg");
                call(ON_DONE);
            }
        });

        // Начинаем с прозрачности 0 для анимации
        setInitialTransparency();

        // Воспроизводим музыку завершения игры
        TRONgame.media.playMusic("grid_reflection.ogg", true);
    }

    private void positionElements() {
        float w = getWidth();
        float h = getHeight();
        
        // Заголовок "USER WIN" в центре верхней половины экрана
        title.setX((w - title.getPrefWidth()) / 2);
        title.setY(h / 2 + 100); // В центре верхней половины

        // Текст благодарности под заголовком
        endOfLineText.setX((w - endOfLineText.getPrefWidth()) / 2);
        endOfLineText.setY(title.getY() - title.getPrefHeight() - 50);

        // Сообщение под текстом благодарности
        messageText.setX((w - messageText.getPrefWidth()) / 2);
        messageText.setY(endOfLineText.getY() - endOfLineText.getPrefHeight() - 30);

        // Кнопка "DONE" под подписью автора
        doneButton.updateSize();
        doneButton.setX((w - doneButton.getWidth()) / 2);
        doneButton.setY(messageText.getY() - messageText.getPrefHeight() - 50);
    }

    private void setInitialTransparency() {
        title.setColor(title.getColor().r, title.getColor().g, title.getColor().b, 0);
        endOfLineText.setColor(endOfLineText.getColor().r, endOfLineText.getColor().g, endOfLineText.getColor().b, 0);
        messageText.setColor(messageText.getColor().r, messageText.getColor().g, messageText.getColor().b, 0);
        doneButton.setColor(doneButton.getColor().r, doneButton.getColor().g, doneButton.getColor().b, 0);
    }

    public void start() {
        // Анимация появления заголовка (без движения, только прозрачность)
        title.addAction(Actions.alpha(1, 0.6f));

        // Анимация появления текста благодарности
        endOfLineText.addAction(Actions.alpha(1, 0.6f));

        // Анимация появления сообщения
        messageText.addAction(Actions.alpha(1, 0.6f));

        // Анимация появления кнопки
        doneButton.addAction(Actions.alpha(1, 0.6f));
    }

    public void hide() {
        // Анимация исчезновения всех элементов
        title.addAction(Actions.alpha(0, 0.3f));
        endOfLineText.addAction(Actions.alpha(0, 0.3f));
        messageText.addAction(Actions.alpha(0, 0.3f));
        doneButton.addAction(Actions.alpha(0, 0.3f));
    }
}
