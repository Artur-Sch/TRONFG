package ru.schneider_dev.tronfg.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Простой класс для отображения PNG иконок из assets
 */
public class PngIcon extends Actor {
    
    private Drawable iconDrawable;
    private ClickListener clickListener;
    private Color iconColor;
    
    public PngIcon(TextureRegion textureRegion, int size) {
        this(textureRegion, size, Color.WHITE); // По умолчанию белый цвет
    }
    
    public PngIcon(TextureRegion textureRegion, int size, Color color) {
        // Создаем белые версии иконок
        TextureRegion whiteIcon = createWhiteIcon(textureRegion, color);
        iconDrawable = new TextureRegionDrawable(whiteIcon);
        this.iconColor = color;
        
        // Устанавливаем размер актора
        setSize(size, size);
        
        // Добавляем базовый click listener
        addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
    }
    
    /**
     * Создает иконку заданного цвета из оригинальной
     */
    private TextureRegion createWhiteIcon(TextureRegion original, Color targetColor) {
        // Получаем Pixmap из оригинальной текстуры
        if (!original.getTexture().getTextureData().isPrepared()) {
            original.getTexture().getTextureData().prepare();
        }
        
        Pixmap originalPixmap = original.getTexture().getTextureData().consumePixmap();
        Pixmap whitePixmap = new Pixmap(originalPixmap.getWidth(), originalPixmap.getHeight(), Pixmap.Format.RGBA8888);
        
        // Копируем оригинальную иконку и применяем цвет
        for (int x = 0; x < originalPixmap.getWidth(); x++) {
            for (int y = 0; y < originalPixmap.getHeight(); y++) {
                int pixel = originalPixmap.getPixel(x, y);
                
                // Получаем альфа-канал (прозрачность)
                int alpha = (pixel >> 24) & 0xff;
                
                if (alpha > 0) {
                    // Если пиксель не прозрачный, применяем целевой цвет
                    int newPixel = ((int)(targetColor.a * 255) << 24) | 
                                  ((int)(targetColor.r * 255) << 16) | 
                                  ((int)(targetColor.g * 255) << 8) | 
                                  (int)(targetColor.b * 255);
                    whitePixmap.drawPixel(x, y, newPixel);
                } else {
                    // Прозрачный пиксель оставляем как есть
                    whitePixmap.drawPixel(x, y, pixel);
                }
            }
        }
        
        // Создаем новую текстуру
        Texture whiteTexture = new Texture(whitePixmap);
        whitePixmap.dispose();
        
        // Создаем TextureRegion с правильными координатами
        TextureRegion whiteRegion = new TextureRegion(whiteTexture, 
            original.getRegionX(), original.getRegionY(), 
            original.getRegionWidth(), original.getRegionHeight());
        
        return whiteRegion;
    }
    
    /**
     * Устанавливает click listener
     */
    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
        addListener(listener);
    }
    
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        
        // Применяем прозрачность через batch
        Color originalColor = batch.getColor();
        batch.setColor(originalColor.r, originalColor.g, originalColor.b, parentAlpha);
        
        // Рисуем иконку (цвет уже применен к текстуре)
        iconDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());
        
        // Восстанавливаем оригинальный цвет batch
        batch.setColor(originalColor);
    }
}
