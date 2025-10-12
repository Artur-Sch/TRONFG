package ru.schneider_dev.tronfg.utils;

import com.badlogic.gdx.Gdx;

/**
 * Утилитный класс для адаптивного позиционирования UI элементов
 * Все элементы имеют фиксированные размеры для одинакового вида на всех устройствах
 */
public class ScreenScaler {

    // Фиксированные размеры для всех устройств (в пикселях)
    public static final float BUTTON_SIZE = 48f;
    public static final float JOYSTICK_SIZE = 80f;
    public static final float FONT_SIZE = 24f;
    public static final float TITLE_FONT_SIZE = 48f;
    public static final float MARGIN = 24f;
    public static final float ICON_SIZE = 32f;

    // Текущие размеры экрана
    private static float screenWidth;
    private static float screenHeight;

    /**
     * Инициализирует систему позиционирования
     */
    public static void init() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Логируем информацию о экране для отладки
        Gdx.app.log("ScreenScaler", "Screen initialized: " + screenWidth + "x" + screenHeight +
                " (Tablet: " + isTablet() + ", Large: " + isLargeScreen() + ")");
    }

    /**
     * Обновляет размеры экрана при изменении ориентации или размера окна
     */
    public static void updateScreenSize() {
        float newWidth = Gdx.graphics.getWidth();
        float newHeight = Gdx.graphics.getHeight();

        if (newWidth != screenWidth || newHeight != screenHeight) {
            screenWidth = newWidth;
            screenHeight = newHeight;

            Gdx.app.log("ScreenScaler", "Screen size updated: " + screenWidth + "x" + screenHeight);
        }
    }

    /**
     * Позиционирует элемент по правому краю с отступом
     *
     * @param elementWidth ширина элемента
     * @param margin       отступ от края
     * @return X координата для позиционирования
     */
    public static float getRightPosition(float elementWidth, float margin) {
        return screenWidth - elementWidth - margin;
    }

    /**
     * Позиционирует элемент по левому краю с отступом
     *
     * @param margin отступ от края
     * @return X координата для позиционирования
     */
    public static float getLeftPosition(float margin) {
        return margin;
    }

    /**
     * Позиционирует элемент по центру экрана
     *
     * @param elementWidth ширина элемента
     * @return X координата для центрирования
     */
    public static float getCenterPosition(float elementWidth) {
        return (screenWidth - elementWidth) / 2;
    }

    /**
     * Позиционирует элемент по центру экрана по вертикали
     *
     * @param elementHeight высота элемента
     * @return Y координата для центрирования
     */
    public static float getVerticalCenterPosition(float elementHeight) {
        return (screenHeight - elementHeight) / 2;
    }

    /**
     * Позиционирует элемент в верхней части экрана
     *
     * @param margin отступ от верха
     * @return Y координата для позиционирования
     */
    public static float getTopPosition(float margin) {
        return screenHeight - margin;
    }

    /**
     * Позиционирует элемент в нижней части экрана
     *
     * @param margin отступ от низа
     * @return Y координата для позиционирования
     */
    public static float getBottomPosition(float margin) {
        return margin;
    }

    /**
     * Получает размер кнопки (фиксированный)
     *
     * @return размер кнопки в пикселях
     */
    public static float getButtonSize() {
        return BUTTON_SIZE;
    }

    /**
     * Получает размер джойстика (фиксированный)
     *
     * @return размер джойстика в пикселях
     */
    public static float getJoystickSize() {
        return JOYSTICK_SIZE;
    }

    /**
     * Получает размер шрифта (фиксированный)
     *
     * @return размер шрифта в пикселях
     */
    public static float getFontSize() {
        return FONT_SIZE;
    }

    /**
     * Получает размер заголовка (фиксированный)
     *
     * @return размер заголовка в пикселях
     */
    public static float getTitleFontSize() {
        return TITLE_FONT_SIZE;
    }

    /**
     * Получает отступ (фиксированный)
     *
     * @return отступ в пикселях
     */
    public static float getMargin() {
        return MARGIN;
    }

    /**
     * Получает размер иконки (фиксированный)
     *
     * @return размер иконки в пикселях
     */
    public static float getIconSize() {
        return ICON_SIZE;
    }

    /**
     * Получает ширину экрана
     *
     * @return ширина экрана в пикселях
     */
    public static float getScreenWidth() {
        return screenWidth;
    }

    /**
     * Получает высоту экрана
     *
     * @return высота экрана в пикселях
     */
    public static float getScreenHeight() {
        return screenHeight;
    }

    /**
     * Проверяет, является ли экран планшетом
     *
     * @return true если это планшет
     */
    public static boolean isTablet() {
        return screenWidth >= 600f || screenHeight >= 600f;
    }

    /**
     * Проверяет, является ли экран большим
     *
     * @return true если это большой экран
     */
    public static boolean isLargeScreen() {
        return screenWidth >= 1200f || screenHeight >= 1200f;
    }

    /**
     * Получает соотношение сторон экрана
     *
     * @return соотношение сторон (ширина/высота)
     */
    public static float getAspectRatio() {
        return screenWidth / screenHeight;
    }

    /**
     * Проверяет, является ли экран сверхшироким (например, складные устройства в развернутом виде)
     *
     * @return true если соотношение сторон больше 2.1
     */
    public static boolean isUltraWide() {
        return getAspectRatio() > 2.1f || (1.0f / getAspectRatio()) > 2.1f;
    }

    /**
     * Получает масштаб для правильного отображения на разных экранах
     *
     * @return коэффициент масштабирования
     */
    public static float getScale() {
        // Базовые размеры приложения (всегда альбомная ориентация)
        float baseWidth = 800f;
        float baseHeight = 480f;

        // Для альбомной ориентации используем ширину как основу
        return Math.min(screenWidth / baseWidth, screenHeight / baseHeight);
    }

    /**
     * Получает масштабированный размер кнопки
     *
     * @return масштабированный размер кнопки
     */
    public static float getScaledButtonSize() {
        return BUTTON_SIZE * getScale();
    }

    /**
     * Получает масштабированный размер джойстика
     *
     * @return масштабированный размер джойстика
     */
    public static float getScaledJoystickSize() {
        return JOYSTICK_SIZE * getScale();
    }

    /**
     * Получает масштабированный размер шрифта
     *
     * @return масштабированный размер шрифта
     */
    public static float getScaledFontSize() {
        return FONT_SIZE * getScale();
    }

    /**
     * Получает масштабированный размер заголовка
     *
     * @return масштабированный размер заголовка
     */
    public static float getScaledTitleFontSize() {
        return TITLE_FONT_SIZE * getScale();
    }

    /**
     * Получает информацию о системе для отладки
     *
     * @return строка с информацией
     */
    public static String getDebugInfo() {
        return String.format("Screen: %.0fx%.0f (%.2f:1), Scale: %.2f, Tablet: %s, Large: %s, UltraWide: %s, Fixed: Landscape",
                screenWidth, screenHeight, getAspectRatio(), getScale(),
                isTablet() ? "Yes" : "No",
                isLargeScreen() ? "Yes" : "No",
                isUltraWide() ? "Yes" : "No");
    }
}
