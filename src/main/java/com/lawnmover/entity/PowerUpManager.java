package com.lawnmover.entity;

import com.lawnmover.main.GamePanel;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowerUpManager {
    private GamePanel gamePanel;
    private List<PowerUp> powerUps;
    private Random random;

    // Aktif güç artırımları
    private List<ActivePowerUp> activePowerUps;

    // Power-up oluşturma ayarları
    private int spawnInterval;
    private int spawnTimer;
    private float spawnChance;

    public PowerUpManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.powerUps = new ArrayList<>();
        this.activePowerUps = new ArrayList<>();
        this.random = new Random();

        // Başlangıç ayarları
        this.spawnInterval = 300; // 5 saniye (60 FPS x 5)
        this.spawnTimer = 0;
        this.spawnChance = 0.3f; // %30 şans
    }

    public void update() {
        // Mevcut güç artırımlarını güncelle
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();

            // Oyuncu ile çarpışma kontrolü
            if (powerUp.isActive() && gamePanel.getPlayer() != null &&
                    powerUp.getCollisionBox().intersects(gamePanel.getPlayer().getCollisionBox())) {

                activatePowerUp(powerUp);
                powerUp.setActive(false);
                powerUps.remove(i);
            }
        }

        // Aktif güç artırımlarını güncelle
        for (int i = activePowerUps.size() - 1; i >= 0; i--) {
            ActivePowerUp activePowerUp = activePowerUps.get(i);
            activePowerUp.duration--;

            // Süresi dolan güç artırımlarını kaldır
            if (activePowerUp.duration <= 0) {
                deactivatePowerUp(activePowerUp);
                activePowerUps.remove(i);
            }
        }

        // Yeni güç artırımları oluştur
        spawnTimer++;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            if (random.nextFloat() < spawnChance) {
                spawnRandomPowerUp();
            }
        }
    }

    private void spawnRandomPowerUp() {
        // Rastgele konum
        int tileSize = gamePanel.tileSize;
        int x = random.nextInt(gamePanel.maxScreenCol) * tileSize;
        int y = random.nextInt(gamePanel.maxScreenRow) * tileSize;

        // Oyuncunun başladığı yere yakın olmasını önle
        int playerX = gamePanel.getPlayer().getX();
        int playerY = gamePanel.getPlayer().getY();
        int minDistance = 3 * tileSize;

        while (Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2)) < minDistance) {
            x = random.nextInt(gamePanel.maxScreenCol) * tileSize;
            y = random.nextInt(gamePanel.maxScreenRow) * tileSize;
        }

        // Engeller ve diğer güç artırımlarıyla çakışmayı önle
        boolean overlap = false;
        for (PowerUp existingPowerUp : powerUps) {
            if (Math.abs(existingPowerUp.getX() - x) < tileSize &&
                    Math.abs(existingPowerUp.getY() - y) < tileSize) {
                overlap = true;
                break;
            }
        }

        // Engelleri kontrol et
        for (var obstacle : gamePanel.getLevelManager().getObstacles()) {
            if (Math.abs(obstacle.getX() - x) < tileSize &&
                    Math.abs(obstacle.getY() - y) < tileSize) {
                overlap = true;
                break;
            }
        }

        if (!overlap) {
            // Rastgele güç artırımı tipi
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp.Type randomType = types[random.nextInt(types.length)];

            PowerUp powerUp = new PowerUp(x, y, tileSize/2, tileSize/2, randomType);
            powerUps.add(powerUp);
        }
    }

    private void activatePowerUp(PowerUp powerUp) {
        Player player = gamePanel.getPlayer();
        int duration = 300; // 5 saniye (60 FPS x 5)

        switch (powerUp.getType()) {
            case SPEED:
                // Hız artışı: normal hızın 2 katı
                int originalSpeed = player.getSpeed();
                int boostedSpeed = originalSpeed * 2;
                player.setSpeed(boostedSpeed);
                activePowerUps.add(new ActivePowerUp(PowerUp.Type.SPEED, duration, originalSpeed));
                break;

            case CUTTING_WIDTH:
                // Kesme genişliği artışı: ekstra +1
                int originalWidth = player.getCuttingWidth();
                player.setCuttingWidth(originalWidth + 1);
                activePowerUps.add(new ActivePowerUp(PowerUp.Type.CUTTING_WIDTH, duration, originalWidth));
                break;

            case INVINCIBILITY:
                // Yenilmezlik
                player.setInvincible(true);
                activePowerUps.add(new ActivePowerUp(PowerUp.Type.INVINCIBILITY, duration, 0));
                break;

            case FUEL:
                // Anlık yakıt dolumu (%50)
                int fuelGain = player.getMaxFuel() / 2;
                player.refuel(fuelGain);
                // Süreli bir efekt olmadığı için activePowerUps'a eklenmez
                break;

            case TIME:
                // Zaman bonusu (30 saniye)
                if (gamePanel.isTimerActive()) {
                    gamePanel.addTime(30);
                    // Süreli bir efekt olmadığı için activePowerUps'a eklenmez
                }
                break;
        }

        // Ses efekti eklenebilir
    }

    private void deactivatePowerUp(ActivePowerUp activePowerUp) {
        Player player = gamePanel.getPlayer();

        switch (activePowerUp.type) {
            case SPEED:
                // Hız normal değerine dön
                player.setSpeed((int)activePowerUp.originalValue);
                break;

            case CUTTING_WIDTH:
                // Kesme genişliği normal değerine dön
                player.setCuttingWidth((int)activePowerUp.originalValue);
                break;

            case INVINCIBILITY:
                // Yenilmezlik kapat
                player.setInvincible(false);
                break;
        }
    }

    public void draw(Graphics2D g2) {
        // Güç artırımlarını çiz
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2);
        }
    }

    public void drawActivePowerUps(Graphics2D g2, int x, int y) {
        int iconSize = 30;
        int spacing = 10;

        for (int i = 0; i < activePowerUps.size(); i++) {
            ActivePowerUp powerUp = activePowerUps.get(i);

            // Icon arkaplanı
            g2.setColor(powerUp.type.getColor());
            g2.fillOval(x, y + i * (iconSize + spacing), iconSize, iconSize);

            // İkon
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            String letter = powerUp.type.name().substring(0, 1);
            g2.drawString(letter, x + iconSize/2 - 5, y + i * (iconSize + spacing) + iconSize/2 + 5);

            // Süre göstergesi
            int remainingPercentage = (int)(powerUp.duration / 300.0 * 100); // 300 = max süre
            g2.setColor(java.awt.Color.WHITE);
            g2.drawRect(x + iconSize + spacing, y + i * (iconSize + spacing) + iconSize/4, 100, iconSize/2);
            g2.fillRect(x + iconSize + spacing, y + i * (iconSize + spacing) + iconSize/4, remainingPercentage, iconSize/2);
        }
    }

    // Aktif güç artırımları için iç sınıf
    private class ActivePowerUp {
        PowerUp.Type type;
        int duration;
        float originalValue; // Önceki değeri sakla (hız, genişlik vb. için)

        public ActivePowerUp(PowerUp.Type type, int duration, float originalValue) {
            this.type = type;
            this.duration = duration;
            this.originalValue = originalValue;
        }
    }

    // Getter
    public List<ActivePowerUp> getActivePowerUps() {
        return activePowerUps;
    }

    // Power-up oluşturma ayarlarını güncelle
    public void setSpawnSettings(int interval, float chance) {
        this.spawnInterval = interval;
        this.spawnChance = chance;
    }

    // PowerUp listesini temizle
    public void clearPowerUps() {
        powerUps.clear();

        // Aktif power-up'ları devre dışı bırak
        for (ActivePowerUp activePowerUp : activePowerUps) {
            deactivatePowerUp(activePowerUp);
        }
        activePowerUps.clear();
    }
}