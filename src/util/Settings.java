package util;

import java.util.HashMap;

public class Settings {
    private final HashMap<String, Integer> enemySpeed;
    private final HashMap<String, Long> enemySpawnRate;
    private final HashMap<String, Long> powerUpSpawnRate;
    private final HashMap<String, Integer> numberOfLives;

    private int screenWidth;
    private int screenHeight;

    private String difficulty = "Easy";

    public Settings() {
        enemySpeed = new HashMap<>();
        enemySpeed.put("Easy", 1);
        enemySpeed.put("Medium", 2);
        enemySpeed.put("Hard", 3);

        enemySpawnRate = new HashMap<>();
        enemySpawnRate.put("Easy", 4500L);
        enemySpawnRate.put("Medium", 2500L);
        enemySpawnRate.put("Hard", 900L);

        powerUpSpawnRate = new HashMap<>();
        powerUpSpawnRate.put("Easy", 20000L);
        powerUpSpawnRate.put("Medium", 20000L);
        powerUpSpawnRate.put("Hard", 30000L);

        numberOfLives = new HashMap<>();
        numberOfLives.put("Easy", 6);
        numberOfLives.put("Medium", 3);
        numberOfLives.put("Hard", 1);
    }

    public int getEnemySpeed() {
        return enemySpeed.get(difficulty);
    }

    public long getEnemySpawnRate() {
        return enemySpawnRate.get(difficulty);
    }

    public long getPowerUpSpawnRate() {
        return powerUpSpawnRate.get(difficulty);
    }


    public int getNumberOfLives() {
        return numberOfLives.get(difficulty);
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
