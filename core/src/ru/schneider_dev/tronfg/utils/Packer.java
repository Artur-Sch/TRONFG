package ru.schneider_dev.tronfg.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;


public class Packer {

    public static void main(String[] args) {
        TexturePacker.Settings set = new TexturePacker.Settings();
        set.filterMin = Texture.TextureFilter.MipMapLinearNearest;
        set.filterMag = Texture.TextureFilter.Linear;
        set.paddingX = 2;
        set.paddingY = 2;
        set.maxHeight = 2048;
        set.maxWidth = 2048;

        TexturePacker.process(set, "tools/raw_images_ru2", "android/assets/images_ru", "pack");
        TexturePacker.process(set, "tools/raw_images_ru2", "android/assets/images_en", "pack");
    }
}
