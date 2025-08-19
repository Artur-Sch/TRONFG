package ru.schneider_dev.tronfg;

/**
 * Главный класс игры TRON FG
 * <p>
 * Функциональность музыки:
 * - В меню проигрывается new_menu.ogg
 * - Во время игры случайно выбирается одна из: new_music1.ogg, new_music2.ogg, new_music3.ogg
 * - При перезапуске уровня или переходе к следующему уровню автоматически выбирается новая случайная музыка
 * - В паузе можно вручную переключать между игровыми мелодиями
 */

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.I18NBundle;
import com.boontaran.games.StageGame;
import ru.schneider_dev.tronfg.levels.Level;
import ru.schneider_dev.tronfg.media.Media;
import ru.schneider_dev.tronfg.screens.IntroScreen;
import ru.schneider_dev.tronfg.screens.LevelList;
import ru.schneider_dev.tronfg.screens.GameCompletedScreen;
import ru.schneider_dev.tronfg.screens.ScoreScreen;
import ru.schneider_dev.tronfg.utils.Data;


import java.util.Locale;
import java.util.Random;

public class TRONgame extends Game {

    public static final int SHOW_BANNER = 1;
    public static final int HIDE_BANNER = 2;
    public static final int LOAD_INTERSTITIAL = 3;
    public static final int SHOW_INTERSTITIAL = 4;
    public static final int OPEN_MARKET = 5;
    public static final int SHRE = 6;

    private boolean loadingAssets = false;
    private AssetManager assetManager;

    public static TextureAtlas atlas;
    public static BitmapFont font40;
    public static BitmapFont tr2nFont;
    public static BitmapFont font24;

    public static com.badlogic.gdx.graphics.g2d.TextureRegion shareIcon;
    public static com.badlogic.gdx.graphics.g2d.TextureRegion starIcon;

    public static Data data;
    public static boolean isSoundMuted = false;


    public static I18NBundle bundle;
    private String path_to_atlas;

    private GameCallback gameCallback;

    public static Media media;
    private IntroScreen introScreen;
    private LevelList levelList;
    private Level level;
    private GameCompletedScreen gameCompletedScreen;
    private ScoreScreen scoreScreen;
    private int lastLevelId;

    // Массив с именами игровой музыки для случайного выбора
    private static final String[] GAME_MUSIC = {"new_music1.ogg", "new_music2.ogg", "new_music3.ogg"};
    private static final Random random = new Random();

    public TRONgame(GameCallback gameCallback) {
        this.gameCallback = gameCallback;
    }

    /**
     * Возвращает случайно выбранную игровую музыку
     * @return имя файла случайной игровой музыки
     */
    private String getRandomGameMusic() {
        return GAME_MUSIC[random.nextInt(GAME_MUSIC.length)];
    }

    /**
     * Создает уровень с случайно выбранной музыкой
     * @param id идентификатор уровня
     * @return созданный уровень
     */
    private Level createLevelWithRandomMusic(int id) {
        Level newLevel;
        switch (id) {
            case 1:
                newLevel = new Level("level1");
                break;
            case 2:
                newLevel = new Level("level2");
                break;
            default:
                newLevel = new Level("level" + id);
                break;
        }

        // Устанавливаем случайную игровую музыку
        newLevel.setMusic(getRandomGameMusic());
        return newLevel;
    }

    /**
     * Создает уровень с новой случайной музыкой (отличной от предыдущей)
     * @param id идентификатор уровня
     * @param previousMusic предыдущая музыка (если null, выбирается случайная)
     * @return созданный уровень
     */
    private Level createLevelWithNewRandomMusic(int id, String previousMusic) {
        Level newLevel = createLevelWithRandomMusic(id);

        // Если есть предыдущая музыка, выбираем новую отличную от неё
        if (previousMusic != null) {
            String newMusic;
            do {
                newMusic = getRandomGameMusic();
            } while (newMusic.equals(previousMusic) && GAME_MUSIC.length > 1);

            newLevel.setMusic(newMusic);
        }

        return newLevel;
    }

    @Override
    public void create() {
        StageGame.setAppSize(800, 480);
        Gdx.input.setCatchBackKey(true);

        Locale locale = Locale.getDefault();
        bundle = I18NBundle.createBundle(Gdx.files.internal("MyBundle"), locale);
        path_to_atlas = bundle.get("path");


        loadingAssets = true;
        assetManager = new AssetManager();

        assetManager.load(path_to_atlas, TextureAtlas.class);

        assetManager.load("musics/new_menu.ogg", Music.class);
        assetManager.load("musics/new_music1.ogg", Music.class);
        assetManager.load("musics/new_music2.ogg", Music.class);
        assetManager.load("musics/new_music3.ogg", Music.class);
        assetManager.load("musics/grid_reflection.ogg", Music.class);
        assetManager.load("sounds/new_click.ogg", Sound.class);
        assetManager.load("sounds/crash.ogg", Sound.class);
        assetManager.load("sounds/level_win_new.ogg", Sound.class);

        // Загружаем PNG иконки
        assetManager.load("png/share.png", com.badlogic.gdx.graphics.Texture.class);
        assetManager.load("png/star.png", com.badlogic.gdx.graphics.Texture.class);

        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter sizeParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        sizeParams.fontFileName = "fonts/GROBOLD.ttf";
        sizeParams.fontParameters.size = 40;
        assetManager.load("font40.ttf", BitmapFont.class, sizeParams);

        // Загружаем шрифт GROBOLD меньшего размера для цифр на уровнях
        FreetypeFontLoader.FreeTypeFontLoaderParameter sizeParams24 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        sizeParams24.fontFileName = "fonts/GROBOLD.ttf";
        sizeParams24.fontParameters.size = 24;
        assetManager.load("font24.ttf", BitmapFont.class, sizeParams24);

        FreetypeFontLoader.FreeTypeFontLoaderParameter tr2nParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        tr2nParams.fontFileName = "fonts/Tr2n.ttf";
        tr2nParams.fontParameters.size = 48;
        assetManager.load("tr2nFont.ttf", BitmapFont.class, tr2nParams);

        media = new Media(assetManager);
        data = new Data();

    }

    @Override
    public void render() {
        if (loadingAssets) {
            if (assetManager.update()) {
                loadingAssets = false;
                onAssetsLoaded();
            }
        }
        super.render();

    }

    @Override
    public void dispose() {
        assetManager.dispose();
        super.dispose();
    }

    private void onAssetsLoaded() {
        atlas = assetManager.get(path_to_atlas, TextureAtlas.class);

        font40 = assetManager.get("font40.ttf", BitmapFont.class);
        font24 = assetManager.get("font24.ttf", BitmapFont.class);
        tr2nFont = assetManager.get("tr2nFont.ttf", BitmapFont.class);

        // Получаем PNG иконки
        shareIcon = new com.badlogic.gdx.graphics.g2d.TextureRegion(
                assetManager.get("png/share.png", com.badlogic.gdx.graphics.Texture.class));
        starIcon = new com.badlogic.gdx.graphics.g2d.TextureRegion(
                assetManager.get("png/star.png", com.badlogic.gdx.graphics.Texture.class));

        showIntro();
    }

    private void exitApp() {
        // Выход из приложения
        Gdx.app.exit();
    }

    private void showIntro() {
        introScreen = new IntroScreen();
        setScreen(introScreen);

        introScreen.start();
        introScreen.setCallback(new StageGame.Callback() {
            @Override
            public void call(int code) {
                if (code == IntroScreen.ON_PLAY) {
                    hideIntro();
                    showLevelList();
                } else if (code == IntroScreen.ON_SCORE) {
                    hideIntro();
                    showScoreScreen(1); // 1 = Intro
                } else if (code == IntroScreen.ON_BACK) {
                    exitApp();
                }
            }
        });
    }

    private void hideIntro() {
        introScreen = null;
    }

    private void showLevelList() {
        levelList = new LevelList();
        setScreen(levelList);

        // Инициализируем кнопку музыки
        levelList.start();

        levelList.setCallback(new StageGame.Callback() {
            @Override
            public void call(int code) {
                if (code == LevelList.ON_BACK) {
                    showIntro();
                    hideLevelList();
                    // Убираем выключение музыки - при возврате в Intro музыка должна продолжать играть
                } else if (code == LevelList.ON_LEVEL_SELECTED) {
                    showLevel(levelList.getSelectedLevelId());
                    hideLevelList();
                } else if (code == LevelList.ON_SCORE) {
                    hideLevelList();
                    showScoreScreen(2); // 2 = LevelList
                } else if (code == LevelList.ON_OPEN_MARKET) {
                    gameCallback.sendMessage(OPEN_MARKET);
                } else if (code == LevelList.ON_SHARE) {
                    gameCallback.sendMessage(SHRE);
                }

            }
        });
        gameCallback.sendMessage(SHOW_BANNER);
    }

    private void hideLevelList() {
        levelList = null;
        gameCallback.sendMessage(HIDE_BANNER);

    }

    private void showLevel(int id) {
        showLevel(id, null);
    }

    private void showLevel(int id, String previousMusic) {
        lastLevelId = id;

        // Останавливаем музыку меню при переходе к уровню
        media.stopMusic("new_menu.ogg");

        // Если есть предыдущая музыка, создаем уровень с новой случайной музыкой
        if (previousMusic != null) {
            level = createLevelWithNewRandomMusic(id, previousMusic);
        } else {
            level = createLevelWithRandomMusic(id);
        }

        setScreen(level);

        level.setCallback(new StageGame.Callback() {
            @Override
            public void call(int code) {
                if (code == Level.ON_RESTART) {
                    gameCallback.sendMessage(HIDE_BANNER);
                    gameCallback.sendMessage(SHOW_INTERSTITIAL);
                    hideLevel();
                    // При перезапуске создаем новый уровень с новой случайной музыкой
                    showLevel(lastLevelId);
                } else if (code == Level.ON_QUIT) {
                    gameCallback.sendMessage(SHOW_INTERSTITIAL);
                    hideLevel();
                    showLevelList();
                } else if (code == Level.ON_COMPLETED) {
                    // Сохраняем время прохождения уровня
                    if (level != null) {
                        float levelTime = level.getLevelTimer();
                        data.saveLevelTime(lastLevelId, levelTime);
                    }
                    
                    // Сохраняем текущую музыку для следующего уровня
                    String currentMusic = level.getMusicName();
                    updateProgress();
                    gameCallback.sendMessage(SHOW_INTERSTITIAL);
                    gameCallback.sendMessage(SHOW_BANNER);
                    hideLevel();
                    
                    // Проверяем, завершена ли игра (после 16-го уровня)
                    if (lastLevelId >= 16) {
                        // Показываем финальный экран
                        showGameCompleted();
                    } else {
                        // При переходе к следующему уровню используем новую случайную музыку
                        showLevel(lastLevelId + 1, currentMusic);
                    }
                } else if (code == Level.ON_PAUSED) {
                    gameCallback.sendMessage(SHOW_BANNER);

                } else if (code == Level.ON_RESUME) {
                    gameCallback.sendMessage(HIDE_BANNER);

                } else if (code == Level.ON_FAILED) {
                    gameCallback.sendMessage(SHOW_BANNER);
                }
            }
        });

        gameCallback.sendMessage(LOAD_INTERSTITIAL);
    }

    private void hideLevel() {
        level.dispose();
        level = null;
    }

    private void showGameCompleted() {
        gameCompletedScreen = new GameCompletedScreen();
        setScreen(gameCompletedScreen);
        // Инициализируем финальный экран
        gameCompletedScreen.start();

        gameCompletedScreen.setCallback(new StageGame.Callback() {
            @Override
            public void call(int code) {
                if (code == GameCompletedScreen.ON_DONE) {
                    // Возвращаемся на главный экран
                    hideGameCompleted();
                    showIntro();
                }
            }
        });
    }

    private void hideGameCompleted() {
        if (gameCompletedScreen != null) {
            gameCompletedScreen.hide();
            gameCompletedScreen = null;
        }
    }

    private void showScoreScreen(int sourceScreen) {
        scoreScreen = new ScoreScreen(sourceScreen);
        setScreen(scoreScreen);

        scoreScreen.setCallback(new StageGame.Callback() {
            @Override
            public void call(int code) {
                if (code == ScoreScreen.ON_BACK_TO_INTRO) {
                    hideScoreScreen();
                    showIntro();
                } else if (code == ScoreScreen.ON_BACK_TO_LEVEL_LIST) {
                    hideScoreScreen();
                    showLevelList();
                } else if (code == ScoreScreen.ON_BACK) {
                    // Fallback - возвращаемся в Intro
                    hideScoreScreen();
                    showIntro();
                }
            }
        });
    }

    private void hideScoreScreen() {
        if (scoreScreen != null) {
            scoreScreen.dispose();
            scoreScreen = null;
        }
    }

    protected void updateProgress() {
        int newProgress = lastLevelId + 1;
        if (newProgress > data.getProgress()) {
            data.setProgress(newProgress);
        }
    }

}
