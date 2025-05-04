package com.lawnmover.entity;

import com.lawnmover.main.GamePanel;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResourceManager {
    private GamePanel gamePanel;
    private List<Resource> resources;
    private Random random;

    // Kaynak oluşturma ayarları
    private int fuelSpawnInterval;
    private int moneySpawnInterval;
    private int repairSpawnInterval;
    private int fuelSpawnTimer;
    private int moneySpawnTimer;
    private int repairSpawnTimer;

    public ResourceManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.resources = new ArrayList<>();
        this.random = new Random();

        // Başlangıç ayarları
        this.fuelSpawnInterval = 600; // 10 saniye (60 FPS x 10)
        this.moneySpawnInterval = 900; // 15 saniye
        this.repairSpawnInterval = 1800; // 30 saniye

        this.fuelSpawnTimer = 300; // İlk yakıt daha erken gelsin
        this.moneySpawnTimer = 0;
        this.repairSpawnTimer = 0;
    }

    public void update() {
        // Mevcut kaynakları güncelle
        for (int i = resources.size() - 1; i >= 0; i--) {
            Resource resource = resources.get(i);
            resource.update();

            // Oyuncu ile çarpışma kontrolü
            if (resource.isActive() && gamePanel.getPlayer() != null &&
                    resource.getCollisionBox().intersects(gamePanel.getPlayer().getCollisionBox())) {

                collectResource(resource);
                resource.setActive(false);
                resources.remove(i);
            }
        }

        // Zamanlayıcıları güncelle ve yeni kaynaklar oluştur
        updateFuelSpawn();
        updateMoneySpawn();
        updateRepairSpawn();
    }

    private void updateFuelSpawn() {
        fuelSpawnTimer++;
        if (fuelSpawnTimer >= fuelSpawnInterval) {
            fuelSpawnTimer = 0;
            // Yakıt düşükse %100 şans, değilse %50 şans
            boolean lowFuel = gamePanel.getPlayer().getFuel() < gamePanel.getPlayer().getMaxFuel() * 0.3;
            if (lowFuel || random.nextFloat() < 0.5f) {
                spawnResource(Resource.Type.FUEL);
            }
        }
    }

    private void updateMoneySpawn() {
        moneySpawnTimer++;
        if (moneySpawnTimer >= moneySpawnInterval) {
            moneySpawnTimer = 0;
            if (random.nextFloat() < 0.7f) { // %70 şans
                spawnResource(Resource.Type.MONEY);
            }
        }
    }

    private void updateRepairSpawn() {
        repairSpawnTimer++;
        if (repairSpawnTimer >= repairSpawnInterval) {
            repairSpawnTimer = 0;
            if (random.nextFloat() < 0.3f) { // %30 şans
                spawnResource(Resource.Type.REPAIR);
            }
        }
    }

    private void spawnResource(Resource.Type type) {
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

        // Engeller ve diğer kaynaklarla çakışmayı önle
        boolean overlap = false;
        for (Resource existingResource : resources) {
            if (Math.abs(existingResource.getX() - x) < tileSize &&
                    Math.abs(existingResource.getY() - y) < tileSize) {
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
            // Kaynak tipi ve değeri
            int value = 0;
            switch (type) {
                case FUEL:
                    value = 30 + random.nextInt(21); // 30-50 arası yakıt
                    break;
                case MONEY:
                    value = 50 + random.nextInt(51); // 50-100 arası para
                    break;
                case REPAIR:
                    value = 100; // Tam tamir
                    break;
            }

            // Yeni kaynak oluştur
            Resource resource = new Resource(x, y, tileSize/2, tileSize/2, type, value);
            resources.add(resource);
        }
    }

    private void collectResource(Resource resource) {
        Player player = gamePanel.getPlayer();

        switch (resource.getType()) {
            case FUEL:
                player.refuel(resource.getValue());
                // Ses efekti eklenebilir
                break;

            case MONEY:
                gamePanel.addMoney(resource.getValue());
                // Ses efekti eklenebilir
                break;

            case REPAIR:
                // Oyuncuyu tam onar/doltur
                player.fullRefuel();
                // İleride hasar sistemi eklenirse burada tamir edilebilir
                // Ses efekti eklenebilir
                break;
        }

        // Toplama animasyonu veya efekti eklenebilir
    }

    public void draw(Graphics2D g2) {
        // Kaynakları çiz
        for (Resource resource : resources) {
            resource.draw(g2);
        }
    }

    // Kaynakları temizle
    public void clearResources() {
        resources.clear();
    }

    // Kaynak oluşturma ayarlarını güncelle
    public void setSpawnSettings(int levelNumber) {
        // Level yükseldikçe daha az yakıt ve para, daha çok tamir oluşsun
        fuelSpawnInterval = 600 + (levelNumber * 50); // Daha seyrek yakıt
        moneySpawnInterval = 900 - (levelNumber * 30); // Daha sık para (min 300)
        if (moneySpawnInterval < 300) moneySpawnInterval = 300;

        // Daha zor levellerde daha sık tamir kutusu
        if (levelNumber > 3) {
            repairSpawnInterval = 1800 - (levelNumber * 100); // Daha sık tamir
            if (repairSpawnInterval < 600) repairSpawnInterval = 600;
        }
    }
}