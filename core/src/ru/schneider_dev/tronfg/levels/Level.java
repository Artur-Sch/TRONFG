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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
import ru.schneider_dev.tronfg.controls.JumpGauge;
import ru.schneider_dev.tronfg.player.IBody;
import ru.schneider_dev.tronfg.player.Player;
import ru.schneider_dev.tronfg.player.UserData;
import ru.schneider_dev.tronfg.screens.LevelCompletedScreen;
import ru.schneider_dev.tronfg.screens.LevelFailedScreen;
import ru.schneider_dev.tronfg.screens.PausedScreen;


public class Level extends StageGame {
    private String directory;

    public static final  float WORLD_SCALE = 40;

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

    private JumpGauge jumpGauge;

    private int mapWidth, mapHeight, tilePixelWidth, tilePixelHeight, levelWidth, levelHeight;

    private Player player;
    private Body finish;

    private boolean moveFrontKey, moveBackKey;
    private Image pleaseWait;

    private JoyStick joyStick;
    private CButton jumpBackBtn, jumpForwardBtn;

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

    public Level(String directory) {
        this.directory = directory;

        pleaseWait = new Image(TRONgame.atlas.findRegion("please_wait"));
        addOverlayChild(pleaseWait);
        centerActorXY(pleaseWait);

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

            jumpGauge = new JumpGauge();
            addOverlayChild(jumpGauge);

            joyStick = new JoyStick(mmToPx(10));
            addOverlayChild(joyStick);
            joyStick.setPosition(15, 15);

            jumpBackBtn = new CButton(
                    new Image(TRONgame.atlas.findRegion("jump1")),
                    new Image(TRONgame.atlas.findRegion("jump1_down")),
                    mmToPx(10)
            );

            addOverlayChild(jumpBackBtn);

            jumpForwardBtn = new CButton(
                    new Image(TRONgame.atlas.findRegion("jump2")),
                    new Image(TRONgame.atlas.findRegion("jump2_down")),
                    mmToPx(10)
            );

            addOverlayChild(jumpForwardBtn);

            jumpForwardBtn.setPosition(getWidth() - jumpForwardBtn.getWidth() - 15, 15);
            jumpBackBtn.setPosition(jumpForwardBtn.getX() - jumpBackBtn.getWidth() - 15, 15);

            jumpBackBtn.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (state == PLAY) {
                        if (player.isTouchedGround()) {
//                            player.jumpBack(8);
                            jumpGauge.start();
                            return true;
                        }
                    }

                    return super.touchDown(event, x, y, pointer, button);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    float jumpValue = jumpGauge.getValue();
                    player.jumpBack(jumpValue);

                }
            });

            jumpForwardBtn.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (state == PLAY) {
                        if (player.isTouchedGround()) {
//                            player.jumpForward(8);
//
                            jumpGauge.start();
                            return true;
                        }
                    }

                    return super.touchDown(event, x, y, pointer, button);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    float jumpValue = jumpGauge.getValue();
                    player.jumpForward(jumpValue);

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

        levelCompletedScreen = new LevelCompletedScreen(getWidth(), getHeight());
        levelCompletedScreen.addListener(new MessageListener() {
            @Override
            protected void receivedMessage(int message, Actor actor) {
                if (message == LevelCompletedScreen.ON_DONE) {
                    call(ON_COMPLETED);
                }
            }
        });

        pausedScreen = new PausedScreen(getWidth(), getHeight());
        pausedScreen.addListener(new MessageListener() {
            @Override
            protected void receivedMessage(int message, Actor actor) {
                if (message == PausedScreen.ON_RESUME) {
                    TRONgame.media.playSound("click.ogg");
                    resumelevel();
                } else if (message == PausedScreen.ON_QUIT) {
                    TRONgame.media.playSound("click.ogg");
                    quitLevel();
                }
            }
        });


        setBackGround("level_bg");

        world.getBodies(bodies);

        updateCamera();

    }

    private void resumelevel() {
        state = PLAY;

        pausedScreen.hide();
        delayCall("resumelevel2", 0.6f);
        showButtons();
        call(ON_RESUME);

        playMusic();
    }

    protected void quitLevel() {
        call(ON_QUIT);
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
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.car) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyA == player.frontWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.frontWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyA == player.rearWheel) {
                UserData data = (UserData) bodyB.getUserData();
                if (data!= null) {
                    if (data.name.equals("land")) {
                        player.touchGround();
                        return;
                    }
                }
            }if (bodyB == player.rearWheel) {
                UserData data = (UserData) bodyA.getUserData();
                if (data!= null) {
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
            }
            else if (name.equals("items")) {
                createItems(layer.getObjects());
            }
            else {
                TileLayer tLayer = new TileLayer(camera, map, name,stage.getBatch());
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
        shape.setAsBox(rectangle.width/2, rectangle.height/2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;
        fdef.isSensor = true;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2, 0);
        shape.dispose();

        return body;
    }

    private void playMusic() {
        if (musicName != null && musicHasLoaded) {
            TRONgame.media.playMusic(musicName, true);
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
    }
    private void showButtons() {
        joyStick.setVisible(true);
        jumpBackBtn.setVisible(true);
        jumpForwardBtn.setVisible(true);
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
                rectangle = ((RectangleMapObject)object).getRectangle();
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
        shape.setAsBox(rectangle.width/2, rectangle.height/2);

        fdef.shape = shape;
        fdef.restitution = LAND_RESTITUTION;
        fdef.density = 1;

        Body body = world.createBody(def);
        body.createFixture(fdef);
        body.setTransform(rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2, 0);
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

        int num = triangleIds.size/3;
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
        int num = vertices.length/2;
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

        if (camera.position.x - camera.viewportWidth/2 < 0) {
            camera.position.x = camera.viewportWidth/2;
        }
        if (camera.position.x + camera.viewportWidth/2 > levelWidth) {
            camera.position.x = levelWidth - camera.viewportWidth/2;
        }
        if (camera.position.y - camera.viewportHeight/2 < 0) {
            camera.position.y = camera.viewportHeight/2;
        }
        if (camera.position.y + camera.viewportHeight/2 > levelHeight) {
            camera.position.y = levelHeight - camera.viewportHeight/2;
        }

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
                if (player.getRotation() < -90 || player.getRotation() > 90) {
                    player.destroy();
                    TRONgame.media.playSound("crash.ogg");
                    levelFailed();
                } else {
                    player.touchGround();
                }
            }
        } else {
            if (body == finish) {
                levelCompleted();
            }
        }
    }

    private void levelCompleted() {
        if (state == LEVEL_COMPLETED) return;
        state = LEVEL_COMPLETED;

        stopMusic();

        hideButtons();

        addOverlayChild(levelCompletedScreen);
        levelCompletedScreen.start();

        TRONgame.media.playSound("level_completed.ogg");
        TRONgame.media.playMusic("level_win.ogg", false);
    }

    private void levelFailed() {
        if (state == LEVEL_FAILED) return;
        state = LEVEL_FAILED;
        stopMusic();

        addOverlayChild(levelFailedScreen);
        levelFailedScreen.start();

        jumpGauge.setVisible(false);
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
                    actor.setRotation(body.getAngle() * 180/3.14f);
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
                playMusic();
            }
        }
        if (!hasBeenBuilt) {
            return;
        }

        boolean lFront = joyStick.isRight();
        boolean lBack = joyStick.isLeft();

        if (state == PLAY) {

            player.onKey(lFront, lBack);

            jumpGauge.setX(getStageToOverlayX(player.getX()));
            jumpGauge.setY(getStageToOverlayY(player.getY() + 20));

            updateCamera();

            if (player.getY() < -100) {
                levelFailed();
            }

        }

        if (state != PAUSED) {
            float delta2 = 0.033f;
            if (delta < delta2)
                delta2 = delta;

            updateWorld(delta2);
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

        x = x/pointCount;
        y = y/pointCount - 33;

        return new Vector2(x, y);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            TRONgame.media.playSound("click.ogg");
            pauseLevel();
        }

        return super.keyUp(keycode);
    }
}
















