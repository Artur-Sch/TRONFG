package ru.schneider_dev.tronfg.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.schneider_dev.tronfg.TRONgame;
import com.badlogic.gdx.Gdx;

/**
 * Текстовый индикатор состояния музыки для меню
 * Отображает "MUSIC: ON" (зеленый) или "MUSIC: OFF" (красный)
 * При клике переключает состояние музыки
 * НЕ меняет треки автоматически - только включение/выключение
 */
public class MenuMusicTextButton extends Actor {
    
    private boolean isMuted = false;
    private BitmapFont font;
    private GlyphLayout layout;
    private ClickListener clickListener;
    
    // Цвета для текста
    private static final Color MUSIC_COLOR = Color.WHITE;
    private static final Color ON_COLOR = Color.GREEN;
    private static final Color OFF_COLOR = Color.RED;
    
    public MenuMusicTextButton() {
        // Используем шрифт font24 из TRONgame
        this.font = TRONgame.font24;
        this.layout = new GlyphLayout();
        
        // Устанавливаем размер актора
        setSize(150, 40);
        
        // Синхронизируем состояние с глобальным
        isMuted = TRONgame.isSoundMuted;
        
        // Добавляем обработчик кликов
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleMusic();
                if (clickListener != null) {
                    clickListener.clicked(event, x, y);
                }
            }
        });
    }
    
    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }
    
    public void toggleMusic() {
        // Переключаем состояние ВСЕХ звуков (музыка + звуковые эффекты)
        isMuted = !isMuted;
        updateMusicState();
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        updateMusicState();
    }

    public boolean isMuted() {
        return isMuted;
    }

    private void updateMusicState() {
        // Обновляем глобальное состояние ВСЕХ звуков
        TRONgame.isSoundMuted = isMuted;
        
        // Сохраняем настройки в Data для восстановления после перезапуска
        if (TRONgame.data != null) {
            TRONgame.data.saveSoundMuted(isMuted);
            Gdx.app.log("MenuMusicTextButton", "💾 Sound setting saved: " + (isMuted ? "MUTED" : "UNMUTED"));
        }
        
        if (isMuted) {
            // ВСЕ звуки выключены - останавливаем музыку меню
            safePauseMusic("new_menu.ogg");
            Gdx.app.log("MenuMusicTextButton", "🔇 ALL SOUNDS muted (music + sound effects)");
        } else {
            // ВСЕ звуки включены - включаем музыку меню
            if (!TRONgame.media.isMusicPlaying("new_menu.ogg")) {
                TRONgame.media.playMusic("new_menu.ogg", true);
            }
            Gdx.app.log("MenuMusicTextButton", "🔊 ALL SOUNDS unmuted (music + sound effects)");
        }
    }
    
    private void safeStopMusic(String musicName) {
        try {
            TRONgame.media.stopMusic(musicName);
        } catch (Exception e) {
            // Игнорируем ошибки - файл может быть не загружен
            System.out.println("Music file " + musicName + " is not loaded or cannot be stopped");
        }
    }

    private void safePauseMusic(String musicName) {
        try {
            TRONgame.media.pauseMusic(musicName);
        } catch (Exception e) {
            // Игнорируем ошибки - файл может быть не загружен
            System.out.println("Music file " + musicName + " is not loaded or cannot be stopped");
        }
    }
    
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        
        // Получаем текст для отображения
        String musicText = "SOUND: ";
        String statusText = isMuted ? "OFF" : "ON";
        
        // Рисуем "MUSIC: " белым цветом
        font.setColor(MUSIC_COLOR.r, MUSIC_COLOR.g, MUSIC_COLOR.b, MUSIC_COLOR.a * parentAlpha);
        layout.setText(font, musicText);
        float musicX = getX();
        float musicY = getY() + (getHeight() + layout.height) / 2;
        float musicWidth = layout.width; // Сохраняем ширину "MUSIC: "
        font.draw(batch, layout, musicX, musicY);
        
        // Рисуем "ON" или "OFF" соответствующим цветом
        // Позиционируем после "MUSIC: " с небольшим отступом
        Color statusColor = isMuted ? OFF_COLOR : ON_COLOR;
        font.setColor(statusColor.r, statusColor.g, statusColor.b, statusColor.a * parentAlpha);
        layout.setText(font, statusText);
        float statusX = musicX + musicWidth + 8; // Позиция после "MUSIC: " с отступом 8 пикселей
        font.draw(batch, layout, statusX, musicY);
    }
}
