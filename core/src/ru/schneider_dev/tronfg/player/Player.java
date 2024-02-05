package ru.schneider_dev.tronfg.player;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.boontaran.douglasPeucker.DouglasPeucker;
import com.boontaran.games.ActorClip;
import com.boontaran.marchingSquare.MarchingSquare;

import java.util.ArrayList;

import ru.schneider_dev.tronfg.Setting;
import ru.schneider_dev.tronfg.TRONgame;
import ru.schneider_dev.tronfg.levels.Level;


public class Player extends ActorClip implements IBody {

    private Image carImg, frontWheelImage,rearWheelImg;
    private Group frontWheelCont, rearWheelCont;
    public Body car, frontWheel, rearWheel;
    private Joint frontWheelJoint, rearWheelJoint;
    private World world;
    private boolean hasDestroyed = false;
    private boolean destroyOnNextUpdate = false;
    private boolean isTouchGround = true;
    private float jumpImpulse = Setting.JUMP_IMPULSE;
    private float jumpWait = 0;
    private Level level;


    public Player(Level level) {
        this.level = level;

        carImg = new Image(TRONgame.atlas.findRegion("rover"));
        childs.addActor(carImg);
        carImg.setX(-carImg.getWidth()/2);
        carImg.setY(-15);
    }

    public void touchGround() {
        isTouchGround = true;
    }

    public boolean isTouchedGround() {
        if (jumpWait > 0) return false;
        return isTouchGround;
    }

    @Override
    public Body createBody(World world) {
        this.world = world;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;

        float[] vertices = traceOutline("rover_model");
        Vector2 centroid = Level.calculateCentroid(vertices);

        int i = 0;
        while (i < vertices.length) {
            vertices[i] -= centroid.x;
            vertices[i + 1] -= centroid.y;
            i += 2;
        }

        vertices = DouglasPeucker.simplify(vertices, 4);
        Level.scaleToWorld(vertices);
        Array<Polygon> triangles = Level.getTriangles(new Polygon(vertices));
        car = createBodyFromTriangles(world, triangles);
        car.setTransform(((getX()) / Level.WORLD_SCALE), (getY() / Level.WORLD_SCALE), 0);
//        car.setTransform((getX()), (getY()), 0);

        frontWheel = createWheel(world, 22 / Level.WORLD_SCALE);
        frontWheel.setTransform(car.getPosition().x + 62 / Level.WORLD_SCALE, car.getPosition().y + 18 / Level.WORLD_SCALE, 0);

        frontWheelCont = new Group();
        frontWheelImage = new Image(TRONgame.atlas.findRegion("rear_wheel"));

        frontWheelCont.addActor(frontWheelImage);
        frontWheelImage.setX(-frontWheelImage.getWidth()/2);
        frontWheelImage.setY(-frontWheelImage.getHeight()/2);

        getParent().addActor(frontWheelCont);

        UserData data = new UserData();
        data.actor = frontWheelCont;
        frontWheel.setUserData(data);

        RevoluteJointDef rDef = new RevoluteJointDef();
        rDef.initialize(car, frontWheel, new Vector2(frontWheel.getPosition()));
        frontWheelJoint = world.createJoint(rDef);


        rearWheel = createWheel(world, 22 / Level.WORLD_SCALE);
        rearWheel.setTransform(car.getPosition().x - 68 / Level.WORLD_SCALE, car.getPosition().y + 18  / Level.WORLD_SCALE, 0);
        rDef = new RevoluteJointDef();


        rearWheelCont = new Group();
        rearWheelImg = new Image(TRONgame.atlas.findRegion("front_wheel"));
        rearWheelCont.addActor(rearWheelImg);
        rearWheelImg.setX(-rearWheelImg.getWidth()/2);
        rearWheelImg.setY(-rearWheelImg.getHeight()/2);

        getParent().addActor(rearWheelCont);
        data = new UserData();
        data.actor = rearWheelCont;
        rearWheel.setUserData(data);

        rDef.initialize(car, rearWheel, new Vector2(rearWheel.getPosition()));
        rearWheelJoint = world.createJoint(rDef);

        return car;
    }

    private Body createWheel(World world, float rad) {

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0; // линейное затухание для уменьшения скорости
        def.angularDamping = 1f; // угловое затухание

        Body body = world.createBody(def);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(rad);

        fDef.shape = shape;// радиус
        fDef.restitution = 0.2f;// эластичность
//        fDef.friction = 0.7f;// коэф трения
        fDef.friction = 0.8f;// коэф трения
        fDef.density = 0.4f;// плотность

        body.createFixture(fDef);
        shape.dispose();


        return body;
    }

    private float[] traceOutline(String regionName) {

        Texture bodyOutLine = TRONgame.atlas.findRegion(regionName).getTexture();
        TextureAtlas.AtlasRegion reg = TRONgame.atlas.findRegion(regionName);
        int w = reg.getRegionWidth();
        int h = reg.getRegionHeight();
        int x = reg.getRegionX();
        int y = reg.getRegionY();

        bodyOutLine.getTextureData().prepare();
        Pixmap allPixmap = bodyOutLine.getTextureData().consumePixmap();

        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(allPixmap,0,0,x,y,w,h);

        allPixmap.dispose();

        int pixel;

        w = pixmap.getWidth();
        h = pixmap.getHeight();

        int [][] map;
        map = new int[w][h];
        for (x=0; x < w; x++) {
            for (y = 0; y < h; y++) {
                pixel = pixmap.getPixel(x, y);
                if ((pixel & 0x000000ff) == 0) {
                    map[x][y] = 0;
                } else {
                    map[x][y] = 1;
                }
            }
        }

        pixmap.dispose();

        MarchingSquare ms = new MarchingSquare(map);
        ms.invertY();
        ArrayList<float[]> traces = ms.traceMap();

        float[] polyVertices = traces.get(0);
        return polyVertices;
    }

    private Body createBodyFromTriangles(World world, Array<Polygon> triangles) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.linearDamping = 0;
        Body body = world.createBody(def);

        for (Polygon triangle : triangles) {
            FixtureDef fDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            shape.set(triangle.getTransformedVertices());

            fDef.shape = shape;
            fDef.restitution = 0.3f;
            fDef.density = 1;

            body.createFixture(fDef);
            shape.dispose();
        }
        return body;
    }

    public void onKey(boolean moveFrontKey, boolean moveBackKey) {
        float torque = Setting.WHEEL_TORQUE;
        float maxAV = 20;

        if (moveFrontKey) {
            if (-rearWheel.getAngularVelocity() < maxAV) {
                rearWheel.applyTorque(-torque, true);
            }
            if (-frontWheel.getAngularVelocity() < maxAV) {
                frontWheel.applyTorque(-torque, true);
            }
        }
        if (moveBackKey) {
            if (rearWheel.getAngularVelocity() < maxAV) {
                rearWheel.applyTorque(torque, true);
            }
            if (frontWheel.getAngularVelocity() < maxAV) {
                frontWheel.applyTorque(torque, true);
            }
        }
    }

    public void jumpBack(float value) {
        if (value < 0.2f) value = 0.2f;

        car.applyLinearImpulse(0, jumpImpulse * value,
                car.getWorldCenter().x + 5 / Level.WORLD_SCALE,
                car.getWorldCenter().y, true);
//        car.applyLinearImpulse(0, 8,
//                car.getWorldCenter().x+1,
//                car.getWorldCenter().y, true);
        isTouchGround = false;
        jumpWait = 0.3f;
    }

    public void jumpForward(float value){

        if (value < 0.2f) value = 0.2f;

        car.applyLinearImpulse(0, jumpImpulse * value,
                car.getWorldCenter().x - 4 / Level.WORLD_SCALE,
                car.getWorldCenter().y, true);
//        car.applyLinearImpulse(0, 8,
//                car.getWorldCenter().x -1,
//                car.getWorldCenter().y, true);
        isTouchGround = false;
        jumpWait = 0.3f;
    }

    @Override
    public void act(float delta) {
        if (jumpWait > 0) {
            jumpWait -= delta;
        }

        if (destroyOnNextUpdate) {
            destroyOnNextUpdate = false;
            world.destroyJoint(frontWheelJoint);
            world.destroyJoint(rearWheelJoint);
        }

        super.act(delta);
    }


    public void destroy() {
        if (hasDestroyed) return;
        hasDestroyed = true;

        destroyOnNextUpdate = true;
    }

    public boolean isHasDestroyed() {
        return hasDestroyed;
    }
}



































