import java.awt.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import util.*;

import static java.lang.Math.floor;

/* Richard Brady
 * 16726839
 * */
public class Model {
    // TODO IMPLEMENT: set proper enemy intervals, do video

    private GameObject Player;
    private final CopyOnWriteArrayList<GameObject> EnemiesList = new CopyOnWriteArrayList<GameObject>();
    private final CopyOnWriteArrayList<GameObject> BulletList = new CopyOnWriteArrayList<GameObject>();
    private final CopyOnWriteArrayList<PowerUpObject> PowerUpList = new CopyOnWriteArrayList<PowerUpObject>();
    private final CopyOnWriteArrayList<PowerUpObject> PowerUpCollectedList = new CopyOnWriteArrayList<PowerUpObject>();
    private final CopyOnWriteArrayList<Integer> livesList = new CopyOnWriteArrayList<Integer>();
    private final ArrayList<String[]> enemyTextures = new ArrayList<>();
    private final Rectangle groundCollider = new Rectangle(0, 665, 1020, 39);
    private final Settings settings;

    private double yVelocity = 0; // increased when space pressed, decreased with gravity
    private double xVelocity = 0; // increased with a or d and decreased over time
    private int jump = 0; //

    private int enemySpeed;
    private int enemyCount = 0; // enemies change at enemyCount intervals

    private boolean isHit = false; // true if hit by enemy or fall to ground
    private boolean hasPower = false; // true if powerUp collected
    private boolean hasBulletPower = false;
    private Vector3f bulletVector = new Vector3f(0,0,0);
    private int bulletCounter = 5; // 5 bullets for each bullet power up collected
    private boolean hasStrengthPower = false;
    private int strengthCounter = 5; // can attack 5 enemies when strength power up collected
    private boolean left = false; // true when facing left
    private boolean right = false; // true when facing right
    private boolean gameOver = false;
    private boolean winner = false;

    private boolean rocketMode = false; // true when rocket enemies spawn
    private boolean spiderMode = false; // true when spider enemies spawn
    private boolean ufoMode = false; // true when ufo enemies spawn
    private boolean bossMode = false; // true when boss has spawned
    private boolean bossSpawned = false;
    private int bossLives = 1; // number of lives for boss different for each difficulty level

    private Timer enemySpawnTimer = new Timer(); // used to spawn enemies at set intervals
    private TimerTask enemySpawnTask = new EnemySpawnTask(); // logic for enemy spawn

    private final Timer powerUpSpawnTimer = new Timer(); // spawn power ups at set intervals
    private final TimerTask powerUpSpawnTask = new PowerUpSpawnTask(); // logic for power up spawn

    private int Score = 0;
    private int enemyIncrementer = 0; // incremented when enemyCount reaches set intervals. each increment spawns different enemy

    // enemy spawn logic
    class EnemySpawnTask extends TimerTask {
        // sets ufo flight direction when spawned, index chosen at random
        int[] ufoFlyMode = {-1, 0, 1};

        int[] bossFlyMode = {-1, 1};

        public void run() {
            // enemy type and difficulty changes when enemyCount reaches different intervals
            // default enemies are bees, next rockets, spiders, ufos, final boss
            if (!bossMode) {
                if ((enemyCount == 45)) {
                    enemyIncrementer = 1;
                    rocketMode = true;
                } else if (enemyCount == 80 && !spiderMode) {
                    enemyIncrementer = 2;
                    rocketMode = false;
                    spiderMode = true;
                    getEnemies().clear();
                } else if (enemyCount == 115 && !ufoMode) {
                    enemyIncrementer = 3;
                    spiderMode = false;
                    ufoMode = true;
                    getEnemies().clear();
                } else if (enemyCount == 150) {
                    getEnemies().clear();
                    enemyIncrementer = 4;
                    ufoMode = false;
                    bossMode = true;
                    bossSpawned = true;
                }
            }
            if (!bossMode) {

                if (spiderMode) { // spiders spawn from top of screen

                    float x = ((float) Math.random() * 1000);
                    EnemiesList.add(new GameObject(enemyTextures.get(enemyIncrementer), 80, 80, new Point3f(x, 0, 0),
                            new Rectangle((int) x, 0, 50, 50)));

                } else if (ufoMode) { // ufos spawn from the right but with random y axis direction

                    float y = ((float) Math.random() * 600);
                    int index = (int) (Math.random() * ((2) + 1));
                    EnemiesList.add(new GameObject(enemyTextures.get(enemyIncrementer), 80, 80, new Point3f(1000, y, 0),
                            new Rectangle(1000, (int) y, 50, 50), ufoFlyMode[index]));

                } else { // rockets and bees spawn from the right and move in straight line

                    float y = ((float) Math.random() * 600);
                    EnemiesList.add(new GameObject(enemyTextures.get(enemyIncrementer), 80, 80, new Point3f(1000, y, 0),
                            new Rectangle(1000, (int) y, 50, 50)));

                }
            }
            if (bossSpawned) { // boss spawns from right and moves similar to ufos.

                float y = ((float) Math.random() * 600);
                int index = (int) (Math.random() * ((1) + 1)); // random flight pattern

                EnemiesList.add(new GameObject(enemyTextures.get(enemyIncrementer), 80, 80, new Point3f(1000, y, 0),
                        new Rectangle(1000, (int) y, 50, 50), bossFlyMode[index]));
                bossSpawned = false;

                // cancel new enemy spawns
                enemySpawnTask.cancel();
                enemySpawnTimer.cancel();
            }
        }
    }
    // power up spawn logic
    class PowerUpSpawnTask extends TimerTask {
        // randomly selected power up images
        String[] powerTextures = {
                "res/bullet_game_assets/bullet4.png",
                "res/sweet_cake/sweet_cake_small.png",
                "res/poison/poison_bottle_small.png",
                "res/iron_fist/boxed-fist_small.png",
        };
        // iron fist power removed when boss mode is true
        int ironIndex;
        // set random int
        public void run() {

            if (bossMode) {
                ironIndex = 2;
            } else {
                ironIndex = 3;
            }

            float x = ((float) Math.random() * 1000); // spawn randomly from top of window
            int index = (int) (Math.random() * ((ironIndex) + 1)); // random image index chosen
            PowerUpList.add(new PowerUpObject(powerTextures[index], x, index));
        }
    }

    public Model(Settings settings) {
        // setup game world
        // Player
        this.settings = settings;
        // TODO implement character select in menu and figure out logic to implement change of direction
        String[] textures = {"res/green_fly/up_right_small.png", "res/green_fly/down_right_small.png"};
        Player = new GameObject(textures, 80, 80, new Point3f(300, 300, 0),
                new Rectangle(300, 300, 50, 50));
    }

    // reset all relevant values when game is restarted
    public void resetGameWorld() {
        Score = 0;
        enemyCount = 0;
        enemyIncrementer = 0;
        isHit = false;
        hasPower = false;
        hasBulletPower = false;
        hasStrengthPower = false;
        left = false;
        right = false;
        gameOver = false;
        rocketMode = false;
        spiderMode = false;
        ufoMode = false;

        bossSpawned = false;

        if (bossMode) {
            bossMode = false;
            enemySpawnTimer = new Timer();
            enemySpawnTask = new EnemySpawnTask();
            enemySpawnTimer.scheduleAtFixedRate(enemySpawnTask, 100, settings.getEnemySpawnRate());
        }

        EnemiesList.clear();
        PowerUpList.clear();
        PowerUpCollectedList.clear();
        BulletList.clear();
        livesList.clear();

        initSettings();
        String[] textures = {"res/green_fly/up_right_small.png", "res/green_fly/down_right_small.png"};
        Player = new GameObject(textures, 80, 80, new Point3f(300, 300, 0),
                new Rectangle(300, 300, 50, 50));
    }

    // initialize power up and enemy spawn timers. Called in main window
    public void initTimers() {
        enemyTextures.add(new String[]{"res/Grumpy_bee/1small.png", "res/Grumpy_bee/2small.png"});
        enemyTextures.add(new String[]{"res/rocket/left1_small.png"});
        enemyTextures.add(new String[]{"res/spider/orange-spider_small.png"});
        enemyTextures.add(new String[]{"res/ufo_alien/ufo_enemy_small.png"});
        enemyTextures.add(new String[]{"res/skull_ufo_boss/skull_left_small.png"});
        enemySpawnTimer.scheduleAtFixedRate(enemySpawnTask, 100, settings.getEnemySpawnRate());
        powerUpSpawnTimer.scheduleAtFixedRate(powerUpSpawnTask, 5000, settings.getPowerUpSpawnRate());
    }
    // initialize gameworld settings from settings object. different values for difficulty chosen
    public void initSettings() {

        for (int i = 0; i < settings.getNumberOfLives(); i++) {
            livesList.add(i);
        }

        enemySpeed = settings.getEnemySpeed();
        bossLives = settings.getNumberOfBossLives();
    }

    // This is the heart of the game , where the model takes in all the inputs ,decides the outcomes and then changes the model accordingly.
    public void gamelogic() {

        // Player Logic first
        playerLogic();
        // Enemy Logic next
        enemyLogic();
        // Bullets move next
        bulletLogic();
        // powerup logic next
        powerUpLogic();
        // interactions between objects
        gameLogic();
    }

    // when player collides with power up set relevant parameters for power up collected
    private void setPowerUpEffects(PowerUpObject powerUp){
        // iron fist power up allows player to crash into 5 enemies
        if (powerUp.getTextureLocation().contains("iron_fist")) {

            hasPower = true; // when true power up image and counter painted in left corner
            hasStrengthPower = true; // when true allows player to crash into and destroy enemies
            strengthCounter = 5; // when 0 power up gone
            getPowerUpCollectedList().add(powerUp); // used in viewer to paint collected power up

        } else if (powerUp.getTextureLocation().contains("sweet")) { // sweet cakes provides bonus points

            Score += 300;

        } else if (powerUp.getTextureLocation().contains("poison")) { // poison deducts points

            Score -= 500;

        } else if (powerUp.getTextureLocation().contains("bullet")) { // bullet power up allows player to shoot 5 bullets

            hasPower = true;
            hasBulletPower = true;
            bulletCounter = 5;
            getPowerUpCollectedList().add(powerUp);

        }
    }

    // logic for strength power up
    private void strengthPower(GameObject temp) {

        EnemiesList.remove(temp);
        Score += 100;

        if (strengthCounter >= 1) {

            strengthCounter--;

        } else if (strengthCounter == 0){

            if (!hasBulletPower) {
                getPowerUpCollectedList().clear();
                hasPower = false;
            }

            hasStrengthPower = false;
        }
    }

    private void gameLogic() {

        // power up collection logic
        for (PowerUpObject powerUp : PowerUpList) {
            if (powerUp.getCollider().intersects(Player.getCollider())) {
                setPowerUpEffects(powerUp);
                PowerUpList.remove(powerUp); // clears here
            }
        }

        // enemy interaction logic
        for (GameObject temp : EnemiesList) {

            if (temp.getCollider().intersects(Player.getCollider())) {

                if (hasStrengthPower && !bossMode) {
                    strengthPower(temp);

                } else {
                    // deduct points and life if hit by enemy
                    Score -= 250;
                    if (livesList.size() > 0) {
                        livesList.remove(0);
                    } else {
                        gameOver = true; // if no lives left gameover
                        break;
                    }
                    isHit = true;
                    break;
                }
            }

            // bullet logic
            for (GameObject bullet : BulletList) {
                if (bullet.getCollider().intersects(temp.getCollider())) {
                    // destroy enemy if not boss
                    if (!bossMode) {
                        EnemiesList.remove(temp);
                        BulletList.remove(bullet);

                    } else if (bossLives > 0) { // remove boss life if hit by bullet
                        bossLives--;
                        BulletList.remove(bullet);

                    } else if (bossLives == 0){ // if boss lives == 0 player wins
                        EnemiesList.clear();
                        winner = true;
                        break;
                    }
                    Score += 200; // increase points for each enemy hit by bullet
                }
            }
        }

        if (gameOver || winner) {
            return;
        }

        if (isHit) {
            getPlayer().setCentre(new Point3f(300, 300, 0)); // reset player location and remove all enemies if hit
            if (!bossMode) {
                getEnemies().clear();
            }
            isHit = false;
        }
    }

    private void playerLogic() {
        // smoother animation is possible if we make a target position  // done but may try to change things for students

        //check for movement and if you fired a bullet
        // temporary point to check for collision before players vector updated
        Point3f checkForPlayerCollision = new Point3f(Player.getCentre().getX(), Player.getCentre().getY(), 0);

        xVelocity = 2; // left and right velocity
        if (Controller.getInstance().isKeyAPressed()) {

            setLeft(true); // not sure if needed anymore, was used in early development. afraid to remove now.
            movementLogic(checkForPlayerCollision, (int) -xVelocity, true); // call movement method
        }

        if (Controller.getInstance().isKeyDPressed()) {

            setRight(true);
            movementLogic(checkForPlayerCollision, (int) xVelocity, true);
        }

        // mouse click to shoot bullets if power up is activated
        if (MouseController.isMouseClicked()) {
            if (hasBulletPower) {
                int x = MouseController.getMouseX();
                int y = MouseController.getMouseY();

                if (bulletCounter >= 1) {

                    bulletVector(x, y); // calculate correct vector based on click location and player location
                    CreateBullet(); // spawn bullet
                    bulletCounter--;

                } else {

                    if (!hasStrengthPower) {
                        getPowerUpCollectedList().clear();
                        hasPower = false;
                    }
                    hasBulletPower = false;
                }
            }
            MouseController.setMouseClicked(false);
        }
        // press space to fly
        if (Controller.getInstance().isKeySpacePressed()) {
            if (jump >= 0) {
                yVelocity = 10; // higher number will make the character fly higher
                jump = 8; // jump countdown
            }
            Controller.getInstance().setKeySpacePressed(false);
        }

        // change direction character image is facing based on mouse pointer location
        if (MouseMotionController.getMouseX() < getPlayer().getCentre().getX()) {
            getPlayer().setTextureLocations(new String[]{"res/green_fly/up_left_small.png", "res/green_fly/down_left_small.png"});
        } else {
            getPlayer().setTextureLocations(new String[]{"res/green_fly/up_right_small.png", "res/green_fly/down_right_small.png"});
        }
    }

    // calculate bullet vector
    private void bulletVector(int mouseClickX, int mouseClickY){
        double playerX = getPlayer().getCentre().getX();
        double playerY = getPlayer().getCentre().getY();

        // get difference of player location and mouse click
        double vx = (double)mouseClickX - playerX;
        double vy = (double)mouseClickY - playerY;

        // normalise to get distance
        double distance = Math.sqrt(vx * vx + vy * vy);

        // calculate direction
        double dirX = vx / distance;
        double dirY = vy / distance;
        dirY = dirY * -1;
        bulletVector = new Vector3f((float)dirX*3, (float)dirY*3, 0);
    }

    //
    private void bulletLogic() {
        // TODO Auto-generated method stub
        // move bullets

        for (GameObject temp : BulletList) {
            //check to move them
            temp.getCentre().ApplyVector(bulletVector);
            //set bullet collider
            temp.setCollider(new Rectangle((int)temp.getCentre().getX() + 5, (int) temp.getCentre().getY() + 5, 25, 25));

            // remove if hit boundary
            if (temp.getCentre().getY() == 0) {
                BulletList.remove(temp);
            } else if (floor(temp.getCentre().getY()) == 665) {
                BulletList.remove(temp);
            } else if (floor(temp.getCentre().getX()) == 0) {
                BulletList.remove(temp);
            } else if (floor(temp.getCentre().getX()) == 1020) {
                BulletList.remove(temp);
            }
        }
    }

    private void CreateBullet() {
        String[] texture = {"res/bullet_game_assets/bullet4.png"};
        BulletList.add(new GameObject(texture, 32, 64,
                new Point3f(Player.getCentre().getX(), Player.getCentre().getY(), 0.0f),
                new Rectangle((int)Player.getCentre().getX() + 5, (int)Player.getCentre().getY() + 5, 25, 25)));
    }

    private void movementLogic(Point3f checkForPlayerCollision, int distanceToTravel, boolean isX) {
        int x = 0;
        int y = 0;

        // if direction is on x axis set x to distanceToTravel else y is correct axis to move on
        if (isX) {
            x = distanceToTravel;
        } else {
            y = distanceToTravel;
        }

        checkForPlayerCollision.ApplyVector(new Vector3f(x, y, 0)); // apply vector to temp point
        detectCollision(checkForPlayerCollision); // detect if collision on temp point

        // set fields if collision has occurred
        if (getPlayer().isCollided()) {
            Score -= 250;

            if (livesList.size() > 0) {
                livesList.remove(0);

            } else {
                gameOver = true;

            }

            getPlayer().setCentre(new Point3f(300, 300, 0));

            if (!bossMode) {
                getEnemies().clear();
            }

            yVelocity = 5;

        } else { // else apply vector to player

            Player.getCentre().ApplyVector(new Vector3f(x, y, 0));
            Player.setCollider(
                    new Rectangle(
                            (int) Player.getCentre().getX() + 15,
                            (int) Player.getCentre().getY() + 20,
                            45,
                            35)
            );
        }
    }

    private void detectCollision(Point3f playerLocation) {
        // get x and y from temp player location
        int x = (int) playerLocation.getX();
        int y = (int) playerLocation.getY();

        // new box collider for temp point
        Rectangle playerRect = new Rectangle(x + 14, y + 22, 45, 45);

        // if playerRect and ground collider intersect collision = true
        getPlayer().setCollided(playerRect.intersects(groundCollider));
    }

    // updates x and y velocity
    public void update() {
        double gravity = 0.5; // value to deduct from yVelocity on each frame i.e. rate at which player falls
        if (!getPlayer().isCollided()) {
            yVelocity -= gravity;
        }

        // slows left and right movement eventually stops if player is not pressing direction keys
        if (left && xVelocity > 0) {
            xVelocity -= 0.2;
        } else if (right && xVelocity > 0) {
            xVelocity += 0.2;
        } else if (xVelocity == 0) {
            left = false;
            right = false;
        }

        if (jump > 0) {
            jump--;
        }

        Point3f checkForPlayerCollision = new Point3f(Player.getCentre().getX(), Player.getCentre().getY(), 0);
        movementLogic(checkForPlayerCollision, (int) yVelocity, false);
    }

    // move enemies based on type
    private void enemyLogic() {
        // TODO Auto-generated method stub
        float boundary = 0.0f;
        float axisToCheck;
        int a = 0;
        for (GameObject temp : EnemiesList) {
            // Move enemies
            if (spiderMode) { // spiders move from top to bottom
                temp.getCentre().ApplyVector(new Vector3f(0, -enemySpeed, 0));
                boundary = 665.0f;
                axisToCheck = temp.getCentre().getY();

            } else if (ufoMode) { // ufos move right to left and randomly on y axis. Bounce off ceiling or ground if hit

                if (temp.getCentre().getY() == 0) {

                    temp.setUfoModeY(-enemySpeed);

                } else if (floor(temp.getCentre().getY()) == 665) {

                    temp.setUfoModeY(enemySpeed);

                }
                temp.getCentre().ApplyVector(new Vector3f(-enemySpeed, temp.getUfoModeY(), 0));
                axisToCheck = temp.getCentre().getX();

            } else if (rocketMode) { // rockets move fast right to left

                int rocketSpeed = enemySpeed + 2;
                temp.getCentre().ApplyVector(new Vector3f(-rocketSpeed, 0, 0));
                axisToCheck = temp.getCentre().getX();

            } else if (bossMode) { // boss spawns from right random movement on y axis and bounces off all boundaries

                if (floor(temp.getCentre().getY()) == 0) {

                    temp.setUfoModeY(-enemySpeed);

                } else if (floor(temp.getCentre().getY()) == 665) {

                    temp.setUfoModeY(enemySpeed);

                } else if (floor(temp.getCentre().getX())  == 1020) {

                    temp.setUfoModeX(-enemySpeed);

                } else if (floor(temp.getCentre().getX()) == 0 ) {

                    temp.setUfoModeX(enemySpeed);
                }

                temp.getCentre().ApplyVector(new Vector3f(temp.getUfoModeX(), temp.getUfoModeY(), 0));
                axisToCheck = temp.getCentre().getX();

            } else { // bees move right to left and are first enemy type encountered

                temp.getCentre().ApplyVector(new Vector3f(-enemySpeed, 0, 0));
                axisToCheck = temp.getCentre().getX();
            }

            setEnemyCollider(temp); // set enemy collider based on type

            // if spider enemy check y axis and remove if reach ground else check x axis and remove if right of screen is reached
            if (axisToCheck == boundary && !bossMode) { // don't remove if enemy is boss

                EnemiesList.remove(temp);
                // enemies lose so score increased
                Score += 10;
                enemyCount++; // each enemy removed increases counter, used to spawn new enemies at higher values
            }
        }
    }

    private void setEnemyCollider(GameObject temp){
        // each enemy is different size so different offsets for box colliders needed
        if (rocketMode) {

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 15,
                            (int) temp.getCentre().getY() + 17,
                            40,
                            25)
            );

        } else if (spiderMode) {

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 24,
                            (int) temp.getCentre().getY() + 20,
                            50,
                            50)
            );

        } else if (ufoMode) {

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 24,
                            (int) temp.getCentre().getY() + 20,
                            50,
                            50)
            );

        } else if (bossMode) {

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 37,
                            (int) temp.getCentre().getY() + 20,
                            75,
                            80)
            );

        } else {

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 15,
                            (int) temp.getCentre().getY() + 17,
                            40,
                            30)
            );

        }
    }

    // power ups spawn from top and disappear when they hit the ground
    private void powerUpLogic() {
        // Move enemies
        for (GameObject temp : PowerUpList) {

            temp.getCentre().ApplyVector(new Vector3f(0, -1, 0));

            temp.setCollider(
                    new Rectangle(
                            (int) temp.getCentre().getX() + 5,
                            (int) temp.getCentre().getY() + 5,
                            temp.getWidth() - 10,
                            temp.getHeight() -10)
            );

            if (temp.getCentre().getY() == 665.0f)  // current boundary need to pass value to model
            {
                PowerUpList.remove(temp);
            }
        }
    }

    // getters and setters
    public GameObject getPlayer() {
        return Player;
    }

    public CopyOnWriteArrayList<GameObject> getEnemies() {
        return EnemiesList;
    }

    public CopyOnWriteArrayList<GameObject> getBullets() {
        return BulletList;
    }

    public CopyOnWriteArrayList<PowerUpObject> getPowerUpList() {
        return PowerUpList;
    }

    public CopyOnWriteArrayList<PowerUpObject> getPowerUpCollectedList() {
        return PowerUpCollectedList;
    }

    public CopyOnWriteArrayList<Integer> getLivesList() {
        return livesList;
    }

    public int getScore() {
        return Score;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isHasPower() {
        return hasPower;
    }

    public int getStrengthCounter() {
        return strengthCounter;
    }

    public int getBulletCounter() {
        return bulletCounter;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isBossMode() {
        return bossMode;
    }

    public int getBossLives() {
        return bossLives;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
}

/*
 * Created by Abraham Campbell on 15/01/2020.
 *   Copyright (c) 2020  Abraham Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

   (MIT LICENSE ) e.g do what you want with this :-)
 */

/* MODEL OF your GAME world 
 * MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWNNNXXXKKK000000000000KKKXXXNNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNXXK0OOkkxddddooooooolllllllloooooooddddxkkOO0KXXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNXK0OkxddooolllllllllllllllllllllllllllllllllllllllloooddxkO0KXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OkdooollllllllooddddxxxkkkOOOOOOOOOOOOOOOkkxxdddooolllllllllllooddxO0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kxdoollllllloddxkO0KKXNNNNWWWWWWMMMMMMMMMMMMMWWWWNNNXXK00Okkxdoollllllllloodxk0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXKOxdooolllllodxkO0KXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXK0OkxdolllllolloodxOKXWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOxdoolllllodxO0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXKOkdolllllllloodxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdolllllooxk0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kdolllllllllodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xdolllllodk0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWMMMMMMMMMMMWN0kdolllllllllodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoollllodxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWMMMMMMMMMMWNXKOkkkk0WMMMMMMMMMMMMWNKkdolllloololodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0kdolllllox0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0kxk0KNWWWWNX0OkdoolllooONMMMMMMMMMMMMMMMWXOxolllllllollodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdollllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0xooollloodkOOkdoollllllllloxXWMMMMMMMMMMMMMMMWXkolllllllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0koolllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllox0XWWMMMMMMMMMWNKOdoloooollllllllllllok0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoolllllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxllolllllllllllllllllloollllllolodxO0KXNNNXK0kdoooxO0K0Odolllollllllllox0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllllllloolllllllllllllllllllolllloddddoolloxOKNWMMMWNKOxdolollllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolllolllllllllllllloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxlllolllllloxkxolllllllllllllllllolllllllllllllxKWMWWWNNXXXKKOxoollllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWXOdollllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllxKNKOxooollolllllllllllllllllllolod0XX0OkxdddoooodoollollllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMN0xollllllllllllllllllllllld0NMMMMMMMMMMMMMMMMMMMMMMMWWNKKNMMMMMMMMMMMW0dlllllllllokXWMWNKkoloolllllllllllllllllllolokkxoolllllllllllllollllllllllllllox0NMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllloONMMMMMMMMMMMMMMMMMMMWNKOxdookNMMMMMMMMMWXkollllllodx0NWMMWWXkolooollllllllllllllllllllooollllllllllllllolllllllllllloooolloxKWMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMWXOdllllllllllllllooollllllllollld0WMMMMMMMMMMMMMMMMWXOxollllloOWMMMMMMMWNkollloodxk0KKXXK0OkdoollllllllllllllllllllllllllllllollllllllloollllllollllllllllllldOXWMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMN0xolllllllllllolllllllllllloodddddONMMMMMMMMMMMMMMMNOdolllllllokNMMMMMMWNkolllloddddddoooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllox0NMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMWXkolllllllllllllllllllodxxkkO0KXNNXXXWMMMMMMMMMMMMMMNkolllllllllod0NMMMMMNOollllloollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllolllllllllllllokXWMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMWKxollllllllllllllllllox0NWWWWWMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllookKNWWNOolollloolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMN0dlllllllllllllllllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloolllollllolloxO0Odllllllllllllllllllllllllllllllllllllllllllllollllllllllllllllllllllllllllllllllllllllllllllld0NWMMMMMMMMMMMMM
MMMMMMMMMMMMMXkolllllllllllllllllolllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXOO0KKOdollllllllllooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONWMMMMMMMMMMMM
MMMMMMMMMMMWXkollllllllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWMMMMWNKOxoollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMM
MMMMMMMMMMWKxollllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMMM
MMMMMMMMMWKxollllllllllllodxkkkkkkkO0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKOkO0KK0OkdolllllloolllllllllllloooollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMM
MMMMMMMMWKxllllllllllolodOXWWWWWWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxolloooollllllllllllllllloollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMMM
MMMMMMMWKxlllllllllollokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxololllllllooolloollllloolloooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMM
MMMMMMWXxllllllllooodkKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdloollllllllllololodxxddddk0KK0kxxxdollolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWMMMMMM
MMMMMMXkolllllodk0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdllollllllllllllodOXWWNXXNWMMMMWWWNX0xolollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokNMMMMMM
MMMMMNOollllodONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dooollllllllllllodOXNWWWWWWMMMMMMMMMWXOddxxddolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONMMMMM
MMMMW0dllllodKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKKK0kdlllllllllllloodxxxxkkOOKNWMMMMMMWNNNNNXKOkdooooollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllld0WMMMM
MMMWKxllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllollllllllllllllllodOKXWMMMMMMMMMMMMWNXKK0OOkkkxdooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMM
MMMNkollllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWXOdlllllolllllllllllloloolllooxKWMMMMMMMMMMMMMMMMMMMMWWWNXKOxoollllllllllllllllllllllllllllllllllllllllllllllllllllllllllolokNMMM
MMW0ollllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOOkxdollllllllllllllllllllllllllllox0NWMMMMWWNNXXKKXNWMMMMMMMMMWNKOxolllolllllllllllllllllllllllllllllllllllllllllllllllllllllllo0WMM
MMXxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXkolllllllllllllllllllllllllllllllllllooxO000OkxdddoooodkKWMMMMMMMMMMWXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWM
MWOollllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXkollllllllllllllllllllllllllllllllllllllllllllllllllllllld0WMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloOWM
MXxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXkollllllllllllllllllllllllllllllllllooollllllllllllllllllold0WMMMMMMWN0dolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXM
W0ollllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKkdolllllllllllllllllllllllllllllllllllllllllllllllllolllllllllokKXNWWNKkollllllllloxdollllllllolllllllllllllllllllllllllolllllllllllllolo0W
NkllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllodxkkdoolollllllllxKOolllllllllllllllllllllllollooollllllloolllllloolllllkN
KxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0doolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllkX0dlllllllllllllllllllloollloOKKOkxdddoollllllllllllllxK
Oolllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXXkollllooolllllllllllllllloONMMMWNNNXX0xolllllllllolloO
kolllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWXkollollllllllllllllllllodKMMMMMMMMMMWKxollllolollolok
kllllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloolllllllllxXWWXkolllllllllllllllolllloONMMMMMMMMMMMW0dllllllllllllk
xollolld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllllolloONMMN0xoolllllllolllllllloxXWMMMMMMMMMMMMXxollllllloollx
dollllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollld0WMMWWXOdollollollllllloxXWMMMMMMMMMMMMMNOollllllokkold
olllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNxlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllldONMMMMWXxollllolllllox0NWMMMMMMMMMMMMMMNOollllllxXOolo
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONMMMMMXxddxxxxkkO0XWMMMMMMMMMMMMMMMMMNOolllllxKW0olo
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllldONWMMMWNXNNWWWMMMMMMMMMMMMMMMMMMMMMMMW0dllollOWW0oll
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxO0KXXXXKKKXNWMMMMMMMMMMMMMMMMMMMMMMMNOdolllkNWOolo
ollllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllooooddooloodkKWMMMMMMMMMMMMMMMMMMMMMMWXOolldKNOooo
dollllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllo0WMMMMMMMMMMMMMMMMMMMMMMMMXkold0Nkold
xollllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllollokNMMMMMMMMMMMMMMMMMMMMMMMMMWOookXXxolx
xolllllloONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMN00XW0dlox
kollllllloxOKXXNNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOxollllllllllllllllllllllllllllllolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllolllllolo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOollk
OolllllllllloodddkKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOkkxddooooollllllllllooodxxdollolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloO
KdllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXXXK0OOkkkkkkkkOKXXXNNX0xolllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllllllloox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdlldK
NkllllollloolllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWWWMMMMMMMMMWNOdlllllllllllllllllllllllllllllllllllllllllllllllllllllllollllllllodOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOolokN
WOolllllllllllolllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllod0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXxolo0W
WXxllllllllllllllllox0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollxXM
MWOollllllllllllllooloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdllllllllllllllllllllllllllllllllllllllllllllllllllllloolld0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlloOWM
MWXxllolllllllllllllllldOXWWNNK00KXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllllllllllllllllllllllllllod0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollxXWM
MMWOollllllllloollllllolodxkxdollodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllllllllllllllllllllllllllllllllllllllllllodxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWN0dlllo0WMM
MMMXxllolllllllllllllllllllllllllllloox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dooollllllllllllllllllllllllllllllllllllllllllllodOXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKOkxxolllokNMMM
MMMW0dlllllllllllllllllllolllllllollollokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdoolllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNOoollllllldKWMMM
MMMMNOollllllllllllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXKOdolllllllllllllllllllllllllllllllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllloOWMMMM
MMMMMXkollllllllllllllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllllllllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllolllllokNMMMMM
MMMMMWXxlllllllllllllllllllllllllllllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0ollllllllllllllllllllllllllllllllllllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOollllllllxXWMMMMM
MMMMMMWKdlllllllllllllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxolllllllllllllllllllllllllllllllllllllloONWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllxKWMMMMMM
MMMMMMMW0dlllllllllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllloollllllllllllllllllllllllllllllllloxkOKKXXKKXNMMMMMMMMMMMMMMMMMMMMMMMMNOolllllldKWMMMMMMM
MMMMMMMMW0dllllllllllllllllllllllllllllldKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllloooood0WMMMMMMMMMMMMMMMMMMMMMMMNOollolldKWMMMMMMMM
MMMMMMMMMW0dlllllllllllllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllolllllllllllllllllld0WMMMMMMMMMMMMMMMMMMMMMMWKxllllldKWMMMMMMMMM
MMMMMMMMMMW0dlllllllllllllllllllllllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllllllllllllllllllllllllllllllllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMWXOdolllldKWMMMMMMMMMM
MMMMMMMMMMMWKxollllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllloolllllllolllollllkNMMMMMMMMMMMMMMMMMMMWXOdolllloxKWMMMMMMMMMMM
MMMMMMMMMMMMWKxollllllllllllllllllllllllod0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloollllllllllllllllllllllllllllloddollllllllllllld0WMMMMMMMMMMMMMMMWWNKOdolllllokXWMMMMMMMMMMMM
MMMMMMMMMMMMMWXkollllllllllllllllllllllllldKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllollllllllllllllllllllllllllllld0XOollllllllllllkNMMMMMMMMMMMMWNK0OkxollllllloONWMMMMMMMMMMMMM
MMMMMMMMMMMMMMMNOdlllllllllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dlllllllllllllllllllllllllolllld0NWN0dlllllloodxkKWMMMMMMMMMMMMNOollllllllllld0NMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMWKxolollllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllllllllllllllllllllllldONMMMWKkdoooxOXNNWMMMMMMMMMMMMMNOollllllllllokXWMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMWXOdlllllllllllllllllloONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdllllllllllllllllllllllllllld0NMMMMMWWXXXXNWMMMMMMMMMMMMMMMMW0dlllllllllod0NMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMWKxollolllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllokXWMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMWNOdollllllloolllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllloollllooolllllllllodONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkllllllolox0NMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMWXkollllllolllllox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllollllllllllllllodkKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllodOXWMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWKxoolllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dollllllllllooddxxk0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdollllldOXWMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMN0xolllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllodk0KXNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKkdollolodkXWMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMNKxoolllllllllodOKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolldOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoollllodkXWMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKkolllollllllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOx0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOdolllllldOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKOdollllllllllodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOdoollllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xollollollollodxOXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdooollllodk0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKkdooolllllllllooxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOxdollllllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0kdllllllllollllodkOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWXKOkdoolllllloodOKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdolllllllllllllodxO0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNX0OxdollloolllloxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdoolllllllllllllooxkO0XNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNX0OkxoololllllllooxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kdoolllllllllllloooodxkO0KXNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXK0Okxdoolllllollllloxk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOkdoollllllllloolllllloodxkkO00KXXNNWWWWWWMMMMMMMMMWWWWWWWNNXXK00Okxxdoolllllllllllloooxk0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kxdoollllllllllllllllllllloodddxxxkkOOOOOOOOOOOkkkxxxdddoollllllllllllllllloodxO0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OxdooollllllllllllooolllllllllllllllllllllllllllllllllllllllllllooodkO0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OkxdooollllllllllllllllllllllllllllllllllllllllllloooddxkO0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNNXK0OOkkxdddoooooollllllllllllllllooooooddxxkOO0KKXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNNXXXKK00OOOOOOOOOOOOOOOO00KKXXXNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
 */

