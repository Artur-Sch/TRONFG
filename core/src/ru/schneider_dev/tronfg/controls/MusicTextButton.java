package ru.schneider_dev.tronfg.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.schneider_dev.tronfg.TRONgame;

import java.util.Random;

/**
 * Текстовый индикатор состояния музыки
 * Отображает "MUSIC: ON" (зеленый) или "MUSIC: OFF" (красный)
 * При клике переключает состояние музыки
 * При повторном включении автоматически выбирает новую случайную мелодию
 */
public class MusicTextButton extends Actor {
    
    private boolean isMuted = false;
    private BitmapFont font;
    private GlyphLayout layout;
    private ClickListener clickListener;
    
    // Цвета для текста
    private static final Color MUSIC_COLOR = Color.WHITE;
    private static final Color ON_COLOR = Color.GREEN;
    private static final Color OFF_COLOR = Color.RED;
    
    // Массив с именами игровой музыки для случайного выбора
    private static final String[] GAME_MUSIC = {"new_music1.ogg", "new_music2.ogg", "new_music3.ogg"};
    private static final Random random = new Random();
    
    // Флаг для отслеживания, была ли музыка выключена
    private boolean wasMusicMuted = false;
    
    // Запоминаем последнюю играющую музыку для предотвращения повторов
    private String lastPlayedMusic = null;
    
    public MusicTextButton() {
        // Используем шрифт font24 из TRONgame
        this.font = TRONgame.font24;
        this.layout = new GlyphLayout();
        
        // Устанавливаем размер актора
        setSize(150, 40); // Увеличиваем ширину с 120 до 150 для лучшего размещения текста
        
        // Синхронизируем состояние с глобальным
        isMuted = TRONgame.isSoundMuted;
        wasMusicMuted = isMuted;
        
        // Инициализируем lastPlayedMusic случайной музыкой
        lastPlayedMusic = GAME_MUSIC[random.nextInt(GAME_MUSIC.length)];
        
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
        // Запоминаем предыдущее состояние
        boolean previousState = isMuted;
        
        // Переключаем состояние
        isMuted = !isMuted;
        
        // Если музыка была выключена и теперь включается, выбираем новую мелодию
        if (previousState && !isMuted) {
            // Музыка была выключена, теперь включается - выбираем новую мелодию
            selectNewRandomMusic();
        }
        
        updateMusicState();
    }
    
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        updateMusicState();
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Выбирает новую случайную мелодию для игры
     */
    private void selectNewRandomMusic() {
        // Останавливаем текущую музыку
        stopAllGameMusic();
        
        // Выбираем случайную мелодию, отличную от предыдущей
        String newMusic;
        do {
            newMusic = GAME_MUSIC[random.nextInt(GAME_MUSIC.length)];
        } while (newMusic.equals(lastPlayedMusic) && GAME_MUSIC.length > 1);
        
        // Запоминаем новую музыку
        lastPlayedMusic = newMusic;
        
        // Устанавливаем новую музыку в глобальном состоянии
        // Это позволит уровню использовать новую музыку при возобновлении
        TRONgame.media.addMusic(newMusic);
        
        // Запускаем новую музыку
        try {
            TRONgame.media.playMusic(newMusic, true);
        } catch (Exception e) {
            System.out.println("Cannot play new music: " + newMusic);
        }
    }
    
    /**
     * Останавливает всю игровую музыку
     */
    private void stopAllGameMusic() {
        for (String music : GAME_MUSIC) {
            safeStopMusic(music);
        }
    }
    
    private void updateMusicState() {
        // Обновляем глобальное состояние
        TRONgame.isSoundMuted = isMuted;
        
        if (isMuted) {
            // Выключаем музыку в игре
            safeStopMusic("new_menu.ogg");
            safeStopMusic("new_music1.ogg");
            safeStopMusic("new_music2.ogg");
            safeStopMusic("new_music3.ogg");
        } else {
            // Включаем музыку в игре - НЕ запускаем музыку из меню паузы
            // Просто разрешаем воспроизведение музыки в игре
            // Музыка будет воспроизводиться автоматически из уровня
        }
    }
    
    private void safeStopMusic(String musicName) {
        try {
            // Просто пытаемся остановить музыку, если файл не загружен, будет исключение
            TRONgame.media.stopMusic(musicName);
        } catch (Exception e) {
            // Игнорируем ошибки - файл может быть не загружен
            System.out.println("Music file " + musicName + " is not loaded or cannot be stopped");
        }
    }
    
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        
        // Получаем текст для отображения
        String musicText = "MUSIC: ";
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
