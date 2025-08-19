package ru.schneider_dev.tronfg.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class TextButton extends Group {
    
    private Label normalText, pressedText;
    private boolean isPressed;
    
    // Неоновые цвета
    public static final Color NEON_CYAN = new Color(0.0f, 1.0f, 1.0f, 1.0f);      // Циан
    public static final Color NEON_WHITE = new Color(1.0f, 1.0f, 1.0f, 1.0f);      // Белый
    public static final Color NEON_MAGENTA = new Color(1.0f, 0.0f, 1.0f, 1.0f);   // Маджента
    public static final Color NEON_GREEN = new Color(0.0f, 1.0f, 0.0f, 1.0f);     // Зеленый
    public static final Color NEON_BLUE = new Color(0.0f, 0.5f, 1.0f, 1.0f);      // Синий
    public static final Color NEON_YELLOW = new Color(1.0f, 0.8f, 0.1f, 1.0f);    // Неоновый Желтый
    public static final Color YELLOW = new Color(1, 1, 0, 1);    // Желтый
    public static final Color NEON_RED = new Color(1.0f, 0.0f, 0.0f, 1.0f);// Красный
    public static final Color NEON_DARK_RED =  new Color(0.8f, 0, 0, 1); // Темно-красный для нажатия
    public static final Color NEON_ORANGE = new Color(1, 0.5f, 0, 1);    // Оранжевый
    public static final Color NEON_DARK_ORANGE = new Color(0.8f, 0.4f, 0, 1); // Темно-оранжевый для нажатия

    public static final float BUTTON_SPACING = 6f;
    public static final float Y_START_OFFSET_BUTTON = 45f;// отступ первой кнопки от центра
    
    public TextButton(String text, BitmapFont font) {
        this(text, font, NEON_WHITE, NEON_MAGENTA);
    }
    
    public TextButton(String text, BitmapFont font, Color normalColor, Color pressedColor) {
        // Создаем стили для обычного и нажатого состояния
        Label.LabelStyle normalStyle = new Label.LabelStyle(font, normalColor);
        Label.LabelStyle pressedStyle = new Label.LabelStyle(font, pressedColor);
        
        normalText = new Label(text, normalStyle);
        pressedText = new Label(text, pressedStyle);
        
        // Добавляем оба текста в группу
        addActor(pressedText);
        addActor(normalText);
        
        // Устанавливаем размер группы по размеру текста
        float width = normalText.getPrefWidth();
        float height = normalText.getPrefHeight();
        
        // Если ширина равна 0, используем примерную ширину на основе длины текста
        if (width <= 0) {
            width = text.length() * font.getData().spaceWidth * 1.2f;
        }
        
        setSize(width, height);
        
        // Позиционируем тексты друг на друга
        normalText.setPosition(0, 0);
        pressedText.setPosition(0, 0);
        
        // Изначально показываем обычный текст
        normalText.setVisible(true);
        pressedText.setVisible(false);
        
        // Добавляем обработчик кликов
        addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                isPressed = true;
                // Плавно скрываем обычный текст и показываем нажатый
                normalText.addAction(Actions.fadeOut(0.1f));
                pressedText.setVisible(true);
                pressedText.setColor(pressedText.getColor().r, pressedText.getColor().b, pressedText.getColor().g, 0);
                pressedText.addAction(Actions.fadeIn(0.1f));
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isPressed = false;
                // Плавно возвращаем обычный текст
                pressedText.addAction(Actions.fadeOut(0.1f));
                normalText.setVisible(true);
                normalText.setColor(normalText.getColor().r, normalText.getColor().g, normalText.getColor().b, 0);
                normalText.addAction(Actions.fadeIn(0.1f));
            }
            
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Анимация при наведении - пульсация цвета
                if (!isPressed) {
                    normalText.addAction(Actions.sequence(
                        Actions.color(normalColor, 0.2f),
                        Actions.color(normalColor, 0.2f)
                    ));
                }
            }
            
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Возврат к нормальному цвету
                if (!isPressed) {
                    normalText.addAction(Actions.color(normalColor, 0.2f));
                }
            }
        });
    }
    
    public boolean isPressed() {
        return isPressed;
    }
    
    // Метод для запуска циклической анимации цвета (для привлечения внимания)
    public void startColorAnimation() {
        normalText.addAction(Actions.forever(Actions.sequence(
            Actions.color(NEON_WHITE, 1.0f),
            Actions.color(NEON_GREEN, 1.0f),
            Actions.color(NEON_BLUE, 1.0f),
            Actions.color(NEON_CYAN, 1.0f),
            Actions.color(NEON_YELLOW, 1.0f)
        )));
    }
    
    // Метод для остановки анимации
    public void stopColorAnimation() {
        normalText.clearActions();
        normalText.setColor(NEON_WHITE);
    }
    
    // Метод для изменения текста
    public void setText(String text) {
        normalText.setText(text);
        pressedText.setText(text);
        
        float width = normalText.getPrefWidth();
        if (width <= 0) {
            width = text.length() * normalText.getStyle().font.getData().spaceWidth * 1.2f;
        }
        setSize(width, normalText.getPrefHeight());
    }
    
    // Метод для изменения размера шрифта
    public void setFontScale(float scale) {
        normalText.setFontScale(scale);
        pressedText.setFontScale(scale);
        
        float width = normalText.getPrefWidth();
        if (width <= 0) {
            width = normalText.getText().length() * normalText.getStyle().font.getData().spaceWidth * 1.2f;
        }
        setSize(width, normalText.getPrefHeight());
    }
    
    // Метод для принудительного обновления размеров
    public void updateSize() {
        float width = normalText.getPrefWidth();
        if (width <= 0) {
            width = normalText.getText().length() * normalText.getStyle().font.getData().spaceWidth * 1.2f;
        }
        setSize(width, normalText.getPrefHeight());
    }
}
