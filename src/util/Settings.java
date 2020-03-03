package util;

import java.util.HashMap;

/* Settings class holds all the different settings for each difficulty setting */

public class Settings {
    // hashmaps map difficulty string to a specific value
    private final HashMap<String, Integer> enemySpeed;
    private final HashMap<String, Long> enemySpawnRate;
    private final HashMap<String, Long> powerUpSpawnRate;
    private final HashMap<String, Integer> numberOfLives;
    private final HashMap<String, Integer> numberOfBossLives;

    private String difficulty = "Easy"; // default difficulty

    public Settings() {
        // higher number = more difficult
        enemySpeed = new HashMap<>();
        enemySpeed.put("Easy", 1);
        enemySpeed.put("Medium", 2);
        enemySpeed.put("Hard", 3);

        // lower number = more difficult, enemies spwan faster
        enemySpawnRate = new HashMap<>();
        enemySpawnRate.put("Easy", 4500L);
        enemySpawnRate.put("Medium", 2500L);
        enemySpawnRate.put("Hard", 900L);

        // lower number = more difficult, power ups spawn at longer intervals
        powerUpSpawnRate = new HashMap<>();
        powerUpSpawnRate.put("Easy", 5000L);
        powerUpSpawnRate.put("Medium", 7500L);
        powerUpSpawnRate.put("Hard", 10000L);

        // lower number = more difficult
        numberOfLives = new HashMap<>();
        numberOfLives.put("Easy", 6);
        numberOfLives.put("Medium", 3);
        numberOfLives.put("Hard", 1);

        // higher number = more difficult
        numberOfBossLives = new HashMap<>();
        numberOfBossLives.put("Easy", 3);
        numberOfBossLives.put("Medium", 6);
        numberOfBossLives.put("Hard", 10);
    }

    // methods called in model, returns appropriate value for current difficulty
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

    public int getNumberOfBossLives() {
        return numberOfBossLives.get(difficulty);
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
