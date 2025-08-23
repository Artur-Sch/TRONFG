package ru.schneider_dev.tronfg.levels;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.boontaran.MessageListener;
import com.boontaran.games.StageGame;
import com.boontaran.games.tiled.TileLayer;
import ru.schneider_dev.tronfg.Setting;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.controls.CButton;
import ru.schneider_dev.tronfg.controls.JoyStick;
import ru.schneider_dev.tronfg.player.IBody;
import ru.schneider_dev.tronfg.player.Player;
import ru.schneider_dev.tronfg.player.UserData;
import ru.schneider_dev.tronfg.screens.LevelCompletedScreen;
import ru.schneider_dev.tronfg.screens.LevelFailedScreen;
import ru.schneider_dev.tronfg.screens.PausedScreen;
import com.badlogic.gdx.Gdx;

import static ru.schneider_dev.tronfg.TRONgame.GAME_MUSIC;


public class Level extends StageGame {
    private String directory;
    private int levelId;

    public static final float WORLD_SCALE = 40;
    public static final int ON_RESTART = 1;
    public static final int ON_QUIT = 2;
    public static final int ON_COMPLETED = 3;
    public static final int ON_FAILED = 4;
    public static final int ON_PAUSED = 5;
    public static final int ON_RESUME = 6;

    private static final int PLAY = 1;
    private static final int LEVEL_FAILED = 2;
    private static final int LEVEL_COMPLETED = 3;
    private static final int PAUSED = 4;

    private int state = 1;

    private int mapWidth, mapHeight, tilePixelWidth, tilePixelHeight, levelWidth, levelHeight;

    private Player player;
    private Body finish;

    private boolean moveFrontKey, moveBackKey;
    private Label pleaseWait;

    private JoyStick joyStick;
    private CButton jumpBackBtn, jumpForwardBtn;
    private CButton restartBtn; // Кнопка рестарта уровня
    private CButton pauseBtn; // Кнопка паузы

    private String musicName;
    private boolean musicHasLoaded;

    private String customBackground = null;

    private static final float LAND_RESTITUTION = 0.5f;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Array<Body> bodies = new Array<Body>();

    private boolean hasBeenBuilt = false;

    private TiledMap map;

    private LevelCompletedScreen levelCompletedScreen;
    private LevelFailedScreen levelFailedScreen;
    private PausedScreen pausedScreen;

    // Переменные для таймера переворота
    private boolean isTimerRunning = false;
    private float upsideDownTimer = 0f;
    private static final float UPSIDE_DOWN_TIMEOUT = 2.0f; // 2 секунды

    // Переменные для отображения текста таймера
    private com.badlogic.gdx.scenes.scene2d.ui.Label timerLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle timerStyle;

    // Переменные для таймера прохождения уровня
    private com.badlogic.gdx.scenes.scene2d.ui.Label levelTimerLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle levelTimerStyle;
    private float levelTimer = 0f;
    private boolean levelTimerRunning = false;

    // Коэффициент масштабирования камеры (больше значение = больше отдаление)
    private static final float CAMERA_ZOOM_FACTOR = 1.0f; // 1.0 = нормальный масштаб, 1.3 = отдаление на 30%

    public Level(String directory) {
        this.directory = directory;
        this.levelId = extractLevelId(directory);

        // Создаем Label с текстом "LOADING..." вместо изображения
        Label.LabelStyle loadingStyle = new Label.LabelStyle(TRONgame.tr2nFont, new com.badlogic.gdx.graphics.Color(0.0f, 0.8f, 1.0f, 1.0f)); // Синий неоновый цвет
        pleaseWait = new Label("LOADING...", loadingStyle);

        // Убеждаемся, что Label видим и правильно позиционирован
        pleaseWait.setVisible(true);
        pleaseWait.setColor(1, 1, 1, 1); // Устанавливаем полную непрозрачность

        // Сначала добавляем Label в сцену
        addOverlayChild(pleaseWait);

        // Принудительно обновляем размер Label с учетом текста
        pleaseWait.pack();

        // Теперь центрируем с правильным размером
        centerActorXY(pleaseWait);

        // Инициализируем стиль текста таймера
        initTimerStyle();

        delayCall("build_level", 0.2f);
    }

    @Override
    protected void onDelayCall(String code) {
        if (code.equals("build_level")) {
            build();

            removeOverlayChild(pleaseWait);

        } else if (code.equals("resumeLevel2")) {
            resumeLevel2();

        }
    }

    private void setBackGround(String region) {
        clearBackground();
        Image bg = new Image(TRONgame.atlas.findRegion(region));
        addBackground(bg, true, false);
    }


    private void build() {
        hasBeenBuilt = true;

        world = new World(new Vector2(0, -Setting.GRAVITY), true);
        world.setContactListener(contactListener);
        debugRenderer = new Box2DDebugRenderer();

        loadMap("tiled/" + directory + "/level.tmx");

        if (player == null) {
            throw new Error("player not defined");
        }
        if (finish == null) {
            throw new Error("finish not defined");
        }

        addRectangleLand(new Rectangle(-10, 0, 10, levelHeight));
        addRectangleLand(new Rectangle(levelWidth + 10, 0, 10, levelHeight));

        int count = 60;
        while (count-- > 0) {
            world.step(1f / 60, 10, 10);
        }
        joyStick = new JoyStick(mmToPx(10));
        addOverlayChild(joyStick);
        joyStick.setPosition(15, 15);

        Image rotateRightNormal = new Image(TRONgame.atlas.findRegion("jump_down"));
        Image rotateRightPressed = new Image(TRONgame.atlas.findRegion("jump_down_down"));

        jumpForwardBtn = new CButton(rotateRightNormal, rotateRightPressed, mmToPx(10));
        addOverlayChild(jumpForwardBtn);

        Image rotateLeftNormal = new Image(TRONgame.atlas.findRegion("jump_up"));
        Image rotateLeftPressed = new Image(TRONgame.atlas.findRegion("jump_up_up"));

        jumpBackBtn = new CButton(rotateLeftNormal, rotateLeftPressed, mmToPx(10));
        addOverlayChild(jumpBackBtn);

        addOverlayChild(jumpForwardBtn);

        float rightMargin = 50f;
        float buttonSpacing = 30f;

        jumpForwardBtn.setPosition(getWidth() - jumpForwardBtn.getWidth() - rightMargin, 15);
        jumpBackBtn.setPosition(jumpForwardBtn.getX() - jumpBackBtn.getWidth() - buttonSpacing, 15);

        jumpBackBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    player.jumpBack(3.0f);
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        jumpForwardBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    player.jumpForward(3.0f);
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        // Создаем кнопку паузы уровня
        Image pauseNormal = new Image(TRONgame.atlas.findRegion("pause"));
        Image pausePressed = new Image(TRONgame.atlas.findRegion("pause")); // Используем ту же текстуру для нажатого состояния
        
        pauseBtn = new CButton(pauseNormal, pausePressed, mmToPx(8)); // Размер 8мм
        addOverlayChild(pauseBtn);
        
        // Позиционируем кнопку паузы в верхнем левом углу экрана (на месте кнопки рестарта)
        pauseBtn.setPosition(20, getHeight() - 70); // 70 пикселей от верха, в левом углу
        
        pauseBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    TRONgame.playSoundSafe("new_click.ogg");
                    pauseLevel();
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        // Создаем кнопку рестарта уровня
        Image restartNormal = new Image(TRONgame.atlas.findRegion("restart_btn"));
        Image restartPressed = new Image(TRONgame.atlas.findRegion("restart_btn_dwn")); // Используем ту же текстуру для нажатого состояния
        
        restartBtn = new CButton(restartNormal, restartPressed, mmToPx(8)); // Размер 8мм
        addOverlayChild(restartBtn);
        
        // Позиционируем кнопку рестарта ниже кнопки паузы
        restartBtn.setPosition(20, getHeight() - 140); // 130 пикселей от верха, в левом углу
        
        restartBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state == PLAY) {
                    TRONgame.playSoundSafe("new_click.ogg");
                    restartLevel();
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        levelFailedScreen = new LevelFailedScreen(getWidth(), getHeight());
        levelFailedScreen.addListener(new MessageListener() {
            @Override
            protected void receivedMessage(int message, Actor actor) {
                if (message == LevelFailedScreen.ON_RETRY) {
                    call(ON_RESTART);
                } else if (message == LevelFailedScreen.ON_QUIT) {
                    quitLevel();
                }
            }
        });

        levelCompletedScreen = new LevelCompletedScreen(getWidth(), getHeight(), levelId, 0f);
        levelCompletedScreen.addListener(new MessageListener() {
            @Override
            protected void receivedMessage(int message, Actor actor) {
                if (message == LevelCompletedScreen.ON_DONE) {
                    // Уровень пройден - сохраняем время и возвращаемся к выбору уровней
                    call(ON_QUIT);
                } else if (message == LevelCompletedScreen.ON_RESTART) {
                    restartLevel();
                } else if (message == LevelCompletedScreen.ON_NEXT) {
                    // Переходим к следующему уровню
                    goToNextLevel();
                }
            }
        });

        pausedScreen = new PausedScreen(getWidth(), getHeight());
        pausedScreen.addListener(new MessageListener() {
            @Override
            protected void receivedMessage(int message, Actor actor) {
                if (message == PausedScreen.ON_RESUME) {
                    TRONgame.playSoundSafe("new_click.ogg");
                    resumelevel();
                } else if (message == PausedScreen.ON_QUIT) {
                    TRONgame.playSoundSafe("new_click.ogg");
                    quitLevel();
                } else if (message == PausedScreen.ON_RESTART) {
                    TRONgame.playSoundSafe("new_click.ogg");
                    restartLevel();
                }
            }
        });


        setBackGround("level_bg");

        world.getBodies(bodies);

        updateCamera();

        // Запускаем таймер уровня
        startLevelTimer();

        // Обновляем позицию таймера уровня
        updateLevelTimerPosition();

    }

    /**
     * Сохраняет завершение уровня
     */
    private void saveLevelCompletion() {
        // Сохраняем время прохождения уровня
        if (TRONgame.data != null) {
            float levelTime = getLevelTimer();
            TRONgame.data.saveLevelTime(levelId, levelTime);

            // Обновляем прогресс игры чтобы следующий уровень стал доступным
            int newProgress = levelId + 1;
            if (newProgress > TRONgame.data.getProgress()) {
                TRONgame.data.setProgress(newProgress);
            }
        }
    }

    /**
     * Переходит к следующему уровню
     */
    private void goToNextLevel() {
		// Переходим к следующему уровню
		call(ON_COMPLETED);
	}

    private void resumelevel() {
        state = PLAY;

        pausedScreen.hide();
        pausedScreen.hideAllIcons(); // Скрываем все иконки
        delayCall("resumelevel2", 0.6f);
        showButtons();
        call(ON_RESUME);

        // Возобновляем таймер уровня
        levelTimerRunning = true;

        // Синхронизируем с новой музыкой, выбранной в паузе
        syncMusicWithPauseScreen();

        // Музыка уровня теперь проверяет настройки звука!
        if (!TRONgame.isSoundMuted) {
            playMusic();
        } else {
            Gdx.app.log("Level", "🔇 Level music not resumed (SOUND: OFF)");
        }
    }

    /**
     * Синхронизирует музыку уровня с текущей выбранной в паузе
     * Теперь проверяет, какая музыка играет в данный момент
     */
    private void syncMusicWithPauseScreen() {
        try {
            // Проверяем, какая музыка сейчас играет
            String currentPlayingMusic = getCurrentlyPlayingMusic();
            if (currentPlayingMusic != null && !currentPlayingMusic.equals(musicName)) {
                // Если музыка изменилась, обновляем уровень
                if (musicName != null && musicHasLoaded) {
                    TRONgame.media.stopMusic(musicName);
                }
                musicName = currentPlayingMusic;
                musicHasLoaded = true;
            }
        } catch (Exception e) {
            // Игнорируем ошибки
            System.out.println("Cannot sync music with pause screen: " + e.getMessage());
        }
    }

    /**
     * Определяет, какая музыка сейчас играет
     */
    private String getCurrentlyPlayingMusic() {
        for (String music : GAME_MUSIC) {
            try {
                // Пытаемся получить музыку из Media
                com.badlogic.gdx.audio.Music musicObj = TRONgame.media.getMusic(music);
                if (musicObj != null && musicObj.isPlaying()) {
                    return music;
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
        }

        // Если ничего не играет, возвращаем текущую музыку уровня
        return musicName;
    }

    protected void quitLevel() {
        call(ON_QUIT);
    }

    /**
     * Перезапускает уровень с новой случайной музыкой
     */
    protected void restartLevel() {
        // Останавливаем текущую музыку
        if (musicName != null && musicHasLoaded) {
            TRONgame.media.stopMusic(musicName);
        }

        // Вызываем событие перезапуска
        call(ON_RESTART);
    }

    public void setMusic(String name) {
        musicName = name;
        TRONgame.media.addMusic(name);
    }

    public String getMusicName() {
        return musicName;
    }

    @Override
    public void dispose() {
        if (musicName != null && musicHasLoaded) {
            TRONgame.media.stopMusic(musicName);
            TRONgame.media.removeMusic(musicName);
        }
        if (world != null) world.dispose();
        map.dispose();

        super.dispose();
    }

    private ContactListener contactListener = new ContactListener() {
        @Override
        public void beginContact(Contact contact) {
            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();

            // Проверяем финиш для обоих тел
            if (isFinishBody(bodyA) || isFinishBody(bodyB)) {
                Body playerBody = (bodyA == player.car || bodyA == player.frontWheel || bodyA == player.rearWheel) ? bodyA : bodyB;
                if (playerBody != null) {
                    Gdx.app.log("Level", "🎯 FINISH CONTACT DETECTED! Player body: " + getPlayerBodyName(playerBody));
                    levelCompleted();
                    return;
                }
            }

            if (bodyA == player.car) {
                playerTouch(bodyB);
                return;
            }

            if (bodyB == player.car) {
                playerTouch(bodyA);
                return;
            }

            if (bodyA == player.car) {
                UserData data = (UserData) bodyB.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
            if (bodyB == player.car) {
                UserData data = (UserData) bodyA.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
            if (bodyA == player.frontWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
            if (bodyB == player.frontWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
            if (bodyA == player.rearWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
            if (bodyB == player.rearWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data != null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }
        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    };

    // Вспомогательные методы для проверки финиша
    private boolean isFinishBody(Body body) {
        if (body == finish) return true;
        UserData data = (UserData) body.getUserData();
        return data != null && "finish".equals(data.name);
    }

    private String getPlayerBodyName(Body body) {
        if (body == player.car) return "car";
        if (body == player.frontWheel) return "frontWheel";
        if (body == player.rearWheel) return "rearWheel";
        return "unknown";
    }

    private void loadMap(String tmxFile) {

        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.generateMipMaps = true;
        params.textureMinFilter = TextureFilter.MipMapLinearNearest;
        params.textureMagFilter = TextureFilter.Linear;

        map = new TmxMapLoader().load(tmxFile, params);

        MapProperties prop = map.getProperties();
        mapWidth = prop.get("width", Integer.class);
        mapHeight = prop.get("height", Integer.class);
        tilePixelWidth = prop.get("tilewidth", Integer.class);
        tilePixelHeight = prop.get("tileheight", Integer.class);
        levelWidth = mapWidth * tilePixelWidth;
        levelHeight = mapHeight * tilePixelHeight;

        for (MapLayer layer : map.getLayers()) {
            String name = layer.getName();

            if (name.equals("land")) {
                createLands(layer.getObjects());
            } else if (name.equals("items")) {
                createItems(layer.getObjects());
            } else {
                TileLayer tLayer = new TileLayer(camera, map, name, stage.getBatch());
                addChild(tLayer);
            }

        }


    }

    private void createItems(MapObjects objects) {
        Rectangle rect;

        for (MapObject object : objects) {
            rect = ((RectangleMapObject) object).getRectangle();

            if (object.getName().equals("player")) {
                player = new Player(this);
                player.setPosition(rect.x, rect.y);
                addChild(player);
                addBody(player);

                stage.addActor(player);
            } else if (object.getName().equals("finish")) {
                finish = addFinish(rect);
            }
        }
    }

    private Body addFinish(Rectangle rectangle) {
        rectangle.x /= WORLD_SCALE;
        rectangle.y /= WORLD_SCALE;
        rectangle.width /= WORLD_SCALE;
        rectangle.height /= WORLD_SCALE;

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.linearDamping = 0;

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rectangle.width / 2, rectangle.height / 2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;
        fdef.isSensor = true;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, 0);
        
        // Добавляем UserData для финиша
        UserData finishData = new UserData();
        finishData.name = "finish";
        body.setUserData(finishData);
        
        shape.dispose();

        return body;
    }

    private void playMusic() {
        // Музыка уровня теперь проверяет настройки звука!
        // Если SOUND OFF - музыка не играет
        if (musicName != null && musicHasLoaded && !TRONgame.isSoundMuted) {
            TRONgame.media.playMusic(musicName, true);
            Gdx.app.log("Level", "🎵 Level music started: " + musicName + " (SOUND: ON)");
        } else if (TRONgame.isSoundMuted) {
            Gdx.app.log("Level", "🔇 Level music not started: " + musicName + " (SOUND: OFF)");
        }
    }

    private void stopMusic() {
        if (musicName != null && musicHasLoaded) {
            TRONgame.media.stopMusic(musicName);
        }
    }

    private void pauseMusic() {
        if (musicName != null && musicHasLoaded) {
            TRONgame.media.pauseMusic(musicName);
        }
    }

    private void hideButtons() {
        joyStick.setVisible(false);
        jumpBackBtn.setVisible(false);
        jumpForwardBtn.setVisible(false);
        restartBtn.setVisible(false);
        pauseBtn.setVisible(false);
    }

    private void showButtons() {
        joyStick.setVisible(true);
        jumpBackBtn.setVisible(true);
        jumpForwardBtn.setVisible(true);
        restartBtn.setVisible(true);
        pauseBtn.setVisible(true);
    }


    private void addBody(IBody item) {
        Body body = item.createBody(world);
        UserData data = new UserData();
        data.actor = (Actor) item;
        body.setUserData(data);
    }

    private void createLands(MapObjects objects) {
        Polygon polygon;
        Rectangle rectangle;

        Array<Polygon> childs;

        for (MapObject object : objects) {
            if (object instanceof PolygonMapObject) {
                polygon = ((PolygonMapObject) object).getPolygon();
                scaleToWorld(polygon);
                childs = getTriangles(polygon);
                addPolygonLand(childs);
            } else if (object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject) object).getRectangle();
                addRectangleLand(rectangle);
            }
        }
    }

    private void addRectangleLand(Rectangle rectangle) {
        rectangle.x /= WORLD_SCALE;
        rectangle.y /= WORLD_SCALE;
        rectangle.width /= WORLD_SCALE;
        rectangle.height /= WORLD_SCALE;

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.linearDamping = 0;

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rectangle.width / 2, rectangle.height / 2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, 0);
        body.setUserData(new UserData(null, "land"));
        shape.dispose();
    }

    private void addPolygonLand(Array<Polygon> triangles) {
        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.linearDamping = 0;

        for (Polygon poly : triangles) {
            FixtureDef fDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.set(poly.getTransformedVertices());

            fDef.shape = shape;
            fDef.restitution = LAND_RESTITUTION;
            fDef.friction = 1;
            fDef.density = 1;

            Body body = world.createBody(def);
            body.createFixture(fDef);
            body.setUserData(new UserData(null, "land"));
            shape.dispose();
        }
    }

    public static Array<Polygon> getTriangles(Polygon polygon) {
        Array<Polygon> trianglesPoly = new Array<Polygon>();

        EarClippingTriangulator ear = new EarClippingTriangulator();
        float vertices[] = polygon.getTransformedVertices();
        ShortArray triangleIds = ear.computeTriangles(vertices);
        Vector2 list[] = fromArray(vertices);

        Polygon triangle;

        int num = triangleIds.size / 3;
        Vector2 triPoints[];
        int i, j;

        for (i = 0; i < num; i++) {
            triPoints = new Vector2[3];
            for (j = 0; j < 3; j++) {
                triPoints[j] = list[triangleIds.get(i * 3 + j)];
            }
            triangle = new Polygon(toArray(triPoints));

            if (Math.abs(triangle.area()) > 0.001f) {
                trianglesPoly.add(triangle);
            }
        }
        return trianglesPoly;

    }

    public static Vector2[] fromArray(float vertices[]) {
        int num = vertices.length / 2;
        int i;
        Vector2 result[] = new Vector2[num];

        for (i = 0; i < num; i++) {
            result[i] = new Vector2(vertices[2 * i], vertices[2 * i + 1]);
        }
        return result;
    }

    public static float[] toArray(Vector2[] points) {
        float vertices[] = new float[points.length * 2];
        int i;

        for (i = 0; i < points.length; i++) {
            vertices[i * 2] = points[i].x;
            vertices[i * 2 + 1] = points[i].y;

        }
        return vertices;
    }

    public static void scaleToWorld(Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();
        scaleToWorld(vertices);
    }

    public static void scaleToWorld(float[] vertices) {
        int i;

        for (i = 0; i < vertices.length; i++) {
            vertices[i] /= WORLD_SCALE;
        }
    }


    private void resumeLevel2() {
        removeOverlayChild(pausedScreen);
    }

    private void updateCamera() {
        camera.position.x = player.getX();
        camera.position.y = player.getY();

        // Применяем масштабирование для отдаления
        camera.viewportWidth = getWidth() * CAMERA_ZOOM_FACTOR;
        camera.viewportHeight = getHeight() * CAMERA_ZOOM_FACTOR;
        camera.update();

        if (camera.position.x - camera.viewportWidth / 2 < 0) {
            camera.position.x = camera.viewportWidth / 2;
        }
        if (camera.position.x + camera.viewportWidth / 2 > levelWidth) {
            camera.position.x = levelWidth - camera.viewportWidth / 2;
        }
        if (camera.position.y - camera.viewportHeight / 2 < 0) {
            camera.position.y = camera.viewportHeight / 2;
        }
        if (camera.position.y + camera.viewportHeight / 2 > levelHeight) {
            camera.position.y = levelHeight - camera.viewportHeight / 2;
        }

        // Обновляем позицию таймера уровня при изменении камеры
        updateLevelTimerPosition();
    }

    public void addChild(Actor actor) {
        this.stage.addActor(actor);
    }

    public void addChild(Actor actor, float x, float y) {
        this.addChild(actor);
        actor.setX(x);
        actor.setY(y);
    }

    protected void playerTouch(Body body) {
        UserData data = (UserData) body.getUserData();
        if (data != null) {
            if (data.name.equals("land") && !player.isHasDestroyed()) {
                // Проверяем, что машина перевернута
                // Нормализуем угол в диапазон 0-360°
                float normalizedRotation = player.getRotation() % 360;
                if (normalizedRotation < 0) normalizedRotation += 360;

                // Проверяем, что машина действительно перевернута (угол 120-240° - более щадящие)
                boolean isUpsideDown = (normalizedRotation > 120 && normalizedRotation < 240);

                if (isUpsideDown) {
                    // Машина перевернута - запускаем таймер
                    if (!isTimerRunning) {
                        startUpsideDownTimer();
                    }
                    player.touchGround();
                } else {
                    // Машина выровнялась - останавливаем таймер
                    if (isTimerRunning) {
                        stopUpsideDownTimer();
                    }
                    player.touchGround();
                }
            } else if (data.name.equals("finish")) {
                // Финиш достигнут!
                Gdx.app.log("Level", "🎯 FINISH REACHED! Player touched finish line");
                levelCompleted();
            }
        } else {
            // Fallback проверка для финиша (если UserData не установлен)
            if (body == finish) {
                Gdx.app.log("Level", "🎯 FINISH REACHED! (fallback check)");
                levelCompleted();
            }
        }
    }

    // Инициализация стиля текста таймера
    private void initTimerStyle() {
        timerStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        // Используем GROBOLD.ttf из папки fonts
        try {
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator =
                    new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(
                            com.badlogic.gdx.Gdx.files.internal("fonts/GROBOLD.ttf")
                    );
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 30;
            timerStyle.font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            timerStyle.font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        }
        timerStyle.fontColor = com.badlogic.gdx.graphics.Color.GOLD;

        timerLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("", timerStyle);
        timerLabel.setVisible(false);
        addOverlayChild(timerLabel);
        timerLabel.setPosition((getWidth() - timerLabel.getWidth()) / 2, getHeight() - 50); // Центр верхней части экрана

        // Инициализация таймера уровня
        levelTimerStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        try {
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator =
                    new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(
                            com.badlogic.gdx.Gdx.files.internal("fonts/GROBOLD.ttf")
                    );
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            levelTimerStyle.font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            levelTimerStyle.font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        }
        levelTimerStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

        levelTimerLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("00:00", levelTimerStyle);
        levelTimerLabel.setVisible(true);
        addOverlayChild(levelTimerLabel);
        // Позиционируем в правом верхнем углу
        levelTimerLabel.setPosition(getWidth() - levelTimerLabel.getWidth() - 20, getHeight() - 40);
    }

    // Методы для управления таймером переворота
    private void startUpsideDownTimer() {
        isTimerRunning = true;
        upsideDownTimer = 0f;
        timerLabel.setVisible(true);
    }

    private void stopUpsideDownTimer() {
        isTimerRunning = false;
        upsideDownTimer = 0f;
        timerLabel.setVisible(false);
    }

    // Методы для управления таймером уровня
    private void startLevelTimer() {
        levelTimerRunning = true;
        levelTimer = 0f;
        if (levelTimerLabel != null) {
            levelTimerLabel.setVisible(true);
        }
    }

    private void stopLevelTimer() {
        levelTimerRunning = false;
        if (levelTimerLabel != null) {
            levelTimerLabel.setVisible(false);
        }
    }

    private float updateLevelTimer(float delta) {
        if (levelTimerRunning && levelTimerLabel != null) {
            levelTimer += delta;
            int minutes = (int) (levelTimer / 60);
            int seconds = (int) (levelTimer % 60);
            levelTimerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }
        return levelTimer;
    }

    public float getLevelTimer() {
        return levelTimer;
    }

    private void updateUpsideDownTimer(float delta) {
        // Не обновляем таймер если уровень завершен или проигран
        if (state == LEVEL_COMPLETED || state == LEVEL_FAILED) {
            if (isTimerRunning) {
                stopUpsideDownTimer();
            }
            return;
        }

        if (isTimerRunning) {
            // Дополнительная проверка - если машина уже не перевернута, останавливаем таймер
            float currentRotation = player.getRotation() % 360;
            if (currentRotation < 0) currentRotation += 360;
            boolean currentlyUpsideDown = (currentRotation > 120 && currentRotation < 240);

            if (!currentlyUpsideDown) {
                stopUpsideDownTimer();
                return;
            }

            upsideDownTimer += delta;
            float remainingTime = UPSIDE_DOWN_TIMEOUT - upsideDownTimer;

            // Обновляем текст на экране
            timerLabel.setText("UPSIDE DOWN! " + String.format("%.1f", remainingTime) + "s");

            if (upsideDownTimer >= UPSIDE_DOWN_TIMEOUT) {
                // Время истекло - проигрыш
                player.destroy();
                TRONgame.playSoundSafe("crash.ogg");
                levelFailed();
            }
        }
    }

    private void levelCompleted() {
        if (state == LEVEL_COMPLETED) return;
        state = LEVEL_COMPLETED;

        // Останавливаем все таймеры и процессы
        if (isTimerRunning) {
            stopUpsideDownTimer();
        }
        stopLevelTimer();
        stopMusic();
        hideButtons();

        addOverlayChild(levelCompletedScreen);
        // Перед показом экрана обновляем данные времени и уровня
        levelCompletedScreen.setData(levelId, getLevelTimer());
        levelCompletedScreen.start();
        saveLevelCompletion();
        TRONgame.playSoundSafe("level_win_new.ogg");
    }

    private void levelFailed() {
        if (state == LEVEL_FAILED) return;
        state = LEVEL_FAILED;
        stopLevelTimer();
        stopMusic();

        addOverlayChild(levelFailedScreen);
        levelFailedScreen.start();

        hideButtons();

        call(ON_FAILED);
    }

    private void pauseLevel() {
        pauseLevel(true);
    }

    private void pauseLevel(boolean withDialog) {
        if (state != PLAY) return;
        state = PAUSED;

        if (withDialog) {
            addOverlayChild(pausedScreen);
            pausedScreen.start();
            hideButtons();
        }

        call(ON_PAUSED);
        pauseMusic();
        // Останавливаем таймер уровня при паузе
        levelTimerRunning = false;
    }

    @Override
    public void pause() {

        if (hasBeenBuilt && state == PLAY) {
            pauseLevel();
        }

        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    private void updateWorld(float delta) {
        if (player.getRight() < levelWidth - 100) {
            world.step(delta, 10, 10);
        }

        int i;
        Body body;
        UserData data;
        for (i = 0; i < bodies.size; i++) {
            body = bodies.get(i);

            data = (UserData) body.getUserData();

            if (data != null) {
                Actor actor = data.actor;

                if (actor != null) {
                    actor.setPosition(body.getPosition().x * WORLD_SCALE, body.getPosition().y * WORLD_SCALE);
                    actor.setRotation(body.getAngle() * 180 / 3.14f);
                }
            }
        }
    }

    @Override
    protected void update(float delta) {
        super.update(delta);

        if (musicName != null && !musicHasLoaded) {
            if (TRONgame.media.update()) {
                musicHasLoaded = true;
                // Воспроизводим музыку только если она не выключена глобально
                if (!TRONgame.isSoundMuted) {
                    playMusic();
                }
            }
        }
        if (!hasBeenBuilt) {
            return;
        }

        // Обновляем физику даже после проигрыша для анимации разбрасывания колес
        if (state != PAUSED) {
            float delta2 = 0.033f;
            if (delta < delta2)
                delta2 = delta;

            updateWorld(delta2);
        }

        // Не обновляем игровую логику если уровень завершен или проигран
        if (state == LEVEL_COMPLETED || state == LEVEL_FAILED) {
            return;
        }

        boolean lFront = joyStick.isRight();
        boolean lBack = joyStick.isLeft();

        if (state == PLAY) {

            player.onKey(lFront, lBack);

            // Обновляем таймер переворота
            updateUpsideDownTimer(delta);

            // Обновляем таймер уровня
            updateLevelTimer(delta);

            updateCamera();

            if (player.getY() < -100) {
                levelFailed();
            }

        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (Setting.DEBUG_WORLD) {
            if (hasBeenBuilt) {
                debugRenderer.render(world, camera.combined.cpy().scl(WORLD_SCALE));
            }
        }
    }

    public static Vector2 calculateCentroid(float vertices[]) {
        Vector2[] points = fromArray(vertices);
        float x = 0;
        float y = 0;
        int pointCount = points.length;
        for (int i = 0; i < pointCount - 1; i++) {
            final Vector2 point = points[i];
            x += point.x;
            y += point.y;
        }

        x = x / pointCount;
        y = y / pointCount - 33;

        return new Vector2(x, y);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            if (state == PAUSED) {
                // Если игра в паузе, кнопка "Назад" возвращает к игре
                TRONgame.playSoundSafe("new_click.ogg");
                resumelevel();
                return true;
            } else if (state == PLAY) {
                // Если игра идет, кнопка "Назад" входит в паузу
                TRONgame.playSoundSafe("new_click.ogg");
                pauseLevel();
                return true;
            }
        }

        return super.keyUp(keycode);
    }

    private void updateLevelTimerPosition() {
        if (levelTimerLabel != null) {
            levelTimerLabel.pack(); // Обновляем размер label
            levelTimerLabel.setPosition(getWidth() - levelTimerLabel.getWidth() - 20, getHeight() - 40);
        }
        
        // Обновляем позицию таймера переворота в центре верхней части экрана
        if (timerLabel != null) {
            timerLabel.pack(); // Обновляем размер label
            timerLabel.setPosition((getWidth() - timerLabel.getWidth()) / 2, getHeight() - 50);
        }
        
        // Обновляем позицию кнопки паузы в верхнем левом углу
        if (pauseBtn != null) {
            pauseBtn.setPosition(20, getHeight() - 70);
        }
        
        // Обновляем позицию кнопки рестарта ниже кнопки паузы
        if (restartBtn != null) {
            restartBtn.setPosition(20, getHeight() - 140);
        }
    }

    private int extractLevelId(String directory) {
        if (directory.startsWith("level")) {
            try {
                return Integer.parseInt(directory.substring(5)); // "level" = 5 символов
            } catch (NumberFormatException e) {
                System.err.println("Could not parse level ID from directory: " + directory);
                return 1; // Default to level 1
            }
        }
        return 1; // Default to level 1
    }
}
















