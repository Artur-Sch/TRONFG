package ru.schneider_dev.tronfg.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class Packer {

    public static void main(String[] args) {
        try {
            System.out.println("Начинаем упаковку текстур...");
            
            TexturePacker.Settings set = new TexturePacker.Settings();
            set.filterMin = Texture.TextureFilter.MipMapLinearNearest;
            set.filterMag = Texture.TextureFilter.Linear;
            set.paddingX = 2;
            set.paddingY = 2;
            set.maxHeight = 2048;
            set.maxWidth = 2048;

            System.out.println("Упаковываем текстуры для русского языка...");
            TexturePacker.process(set, "tools/raw_images", "android/assets/images_ru", "pack");
            
            System.out.println("Упаковываем текстуры для английского языка...");
            TexturePacker.process(set, "tools/raw_images", "android/assets/images_en", "pack");
            
            System.out.println("Упаковка текстур завершена успешно!");
            
        } catch (Exception e) {
            System.err.println("Ошибка при упаковке текстур: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
