package com.lawnmover.level;

import com.lawnmover.entity.Player;
import com.lawnmover.main.GamePanel;
import com.lawnmover.ui.GameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelManager {

    private final GamePanel gamePanel;
    private final List<GrassTile> grassTiles;
    private final List<Obstacle> obstacles;
    private final Random random;
    private TimeChallenge timeChallenge;
    private boolean timeChallengeMode;
    private int secondTimer;
    private int totalGrass;
    private int cutGrass;
    private boolean levelCompleted;

    public LevelManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.grassTiles = new ArrayList<>();
        this.obstacles = new ArrayList<>();
        this.random = new Random();
        this.levelCompleted = false;
        this.timeChallenge = new TimeChallenge(gamePanel);
        this.timeChallengeMode = false;
        this.secondTimer = 0;

        // İlk level'i yükle
        loadLevel(gamePanel.getCurrentLevel());
    }

    public void loadLevel(int levelNumber) {
        // Levelı yüklerken tamamlanma durumunu sıfırla
        levelCompleted = false;

        // Her level değişiminde listeleri temizle
        grassTiles.clear();
        obstacles.clear();

        // Çimleri oluştur
        createGrassTiles();

        // Engelleri oluştur (levele göre zorluk artar)
        createObstacles(levelNumber);

        // İstatistikleri sıfırla
        totalGrass = grassTiles.size();
        cutGrass = 0;

        if (levelNumber % 3 == 0) {
            timeChallengeMode = true;
            // 60 saniyeden başlayarak, level arttıkça azalan süre (min 30 saniye)
            int timeLimit = Math.max(30, 60 - ((levelNumber / 3 - 1) * 5));
            int reward = 500 + (levelNumber * 100); // Artan ödül
            timeChallenge.start(timeLimit, reward);
            System.out.println("Zaman meydan okuması başladı! Süre: " + timeLimit + " saniye");
        } else {
            timeChallengeMode = false;
            // Normal levellerde, ek süre bonusu için yumuşak bir zaman sınırı
            int softTimeLimit = 120 + (levelNumber * 10); // Level başına 10 saniye artış
            int reward = 200 + (levelNumber * 50); // Daha küçük ödül
            timeChallenge.start(softTimeLimit, reward);
        }

        System.out.println("Level " + levelNumber + " yüklendi. Toplam çim: " + totalGrass);
    }


    private void createGrassTiles() {
        // Ekranı çim karoları ile kapla
        int tileSize = gamePanel.tileSize;

        for (int row = 0; row < gamePanel.maxScreenRow; row++) {
            for (int col = 0; col < gamePanel.maxScreenCol; col++) {
                int x = col * tileSize;
                int y = row * tileSize;
                grassTiles.add(new GrassTile(x, y, tileSize, tileSize));
            }
        }
    }

    private void createObstacles(int level) {
        int tileSize = gamePanel.tileSize;
        int numObstacles = 5 + (level * 2); // Her level için engel sayısı artar

        for (int i = 0; i < numObstacles; i++) {
            int x, y;
            boolean overlap;

            // Engellerin üst üste gelmemesini sağla
            do {
                overlap = false;
                x = random.nextInt(gamePanel.maxScreenCol) * tileSize;
                y = random.nextInt(gamePanel.maxScreenRow) * tileSize;

                Rectangle newObstacleRect = new Rectangle(x, y, tileSize, tileSize);

                // Diğer engellerle çakışma kontrolü
                for (Obstacle obstacle : obstacles) {
                    if (newObstacleRect.intersects(obstacle.getCollisionBox())) {
                        overlap = true;
                        break;
                    }
                }

                // Oyuncunun başlangıç pozisyonuyla çakışmayı önle
                Rectangle playerStart = new Rectangle(
                        gamePanel.screenWidth / 2 - tileSize,
                        gamePanel.screenHeight / 2 - tileSize,
                        tileSize * 2,
                        tileSize * 2
                );

                if (newObstacleRect.intersects(playerStart)) {
                    overlap = true;
                }

            } while (overlap);

            // Farklı engel tipleri ekle (level arttıkça daha karmaşık engeller)
            if (level > 2 && random.nextBoolean()) {
                // Hareketli engel (level 3'ten sonra)
                obstacles.add(new MovingObstacle(x, y, tileSize, tileSize, gamePanel));
            } else {
                // Sabit engel
                obstacles.add(new Obstacle(x, y, tileSize, tileSize));
            }
        }
    }

    public void update() {
        if (gamePanel.getGameState() != GameState.PLAYING) {
            return;
        }

        secondTimer++;
        if (secondTimer >= 60) { // 60 FPS varsayılarak
            secondTimer = 0;
            if (timeChallenge.isActive()) {
                timeChallenge.update();
            }
        }

        Player player = gamePanel.getPlayer();

        // Level tamamlandıysa tekrar kontrol etme
        if (levelCompleted) {
            return;
        }

        // Hareketli engelleri güncelle
        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof MovingObstacle) {
                ((MovingObstacle) obstacle).update();
            }

            if (!player.isInvincible() && player.getCollisionBox().intersects(obstacle.getCollisionBox())) {
                gamePanel.setGameOver(true);
                return;
            }
        }

        // Çim biçme kontrolü
        for (GrassTile grass : grassTiles) {
            if (!grass.isCut() && player.getCollisionBox().intersects(grass.getCollisionBox())) {
                grass.setCut(true);
                cutGrass++;

                // Her biçilen çim için puan ver
                gamePanel.addScore(10);

                // Debug için
                System.out.println("Çim biçildi. Biçilen: " + cutGrass + "/" + totalGrass + " (" + (float)cutGrass/totalGrass*100 + "%)");
            }
        }

        // Tüm çimler biçildi mi (level tamamlandı mı) kontrolü
        float percentComplete = (float) cutGrass / totalGrass;
        if (percentComplete >= 0.9f) { // %90'ı biçildi mi
            System.out.println("Level tamamlandı! Yüzde: " + (percentComplete * 100) + "%");

            // Level tamamlandı işaretini koy
            levelCompleted = true;

            // Zaman meydan okuması aktifse ekstra ödül ver
            if (timeChallenge.isActive()) {
                int timeBonus = timeChallenge.calculateBonus();
                gamePanel.addScore(timeBonus);
                gamePanel.addMoney(timeBonus / 2); // Paranın yarısı kadar bonus
                System.out.println("Zaman bonusu: " + timeBonus);
            }

            // Sonraki levele geç
            int nextLevel = gamePanel.getCurrentLevel() + 1;
            gamePanel.setCurrentLevel(nextLevel);

            // Bonus puan
            gamePanel.addScore(100 * nextLevel);

            // Level geçiş ekranını göster
            gamePanel.levelUp();
        }
    }


    public void draw(Graphics2D g2) {
        // Çimleri çiz
        for (GrassTile grass : grassTiles) {
            grass.draw(g2);
        }

        // Engelleri çiz
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g2);
        }
    }

    // Getter metotları
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getTotalGrass() {
        return totalGrass;
    }

    public TimeChallenge getTimeChallenge() {
        return timeChallenge;
    }

    public boolean getTimeChallengeMode() {
        return timeChallengeMode;
    }

    public int getCutGrass() {
        return cutGrass;
    }
}