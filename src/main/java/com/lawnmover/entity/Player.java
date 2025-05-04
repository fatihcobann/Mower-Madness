package com.lawnmover.entity;

import com.lawnmover.main.GamePanel;
import com.lawnmover.main.KeyHandler;
import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;

public class Player {

    private final GamePanel gamePanel;
    private final KeyHandler keyHandler;
    private int cuttingWidth;
    private boolean invincible;


    // Oyuncu konumu
    private int x;
    private int y;

    // Oyuncu hızı
    private int speed;

    // Oyuncu boyutu
    private final int width;
    private final int height;

    // Oyuncunun biçtiği alan
    private Rectangle collisionBox;
    private Rectangle cuttingBox; // Kesme alanı için yeni kapsam

    // Animasyon
    private float rotation;
    private int bladeAnimation;

    // Çim biçme makinesi tipi
    private MowerType mowerType;

    // Yakıt sistemi
    private int fuel;
    private int maxFuel;
    private boolean outOfFuel;

    public Player(GamePanel gamePanel, KeyHandler keyHandler) {
        this.gamePanel = gamePanel;
        this.keyHandler = keyHandler;
        this.cuttingWidth = 1;
        this.invincible = false;


        // Başlangıç konumu
        this.x = gamePanel.screenWidth / 2 - gamePanel.tileSize / 2;
        this.y = gamePanel.screenHeight / 2 - gamePanel.tileSize / 2;

        this.width = gamePanel.tileSize;
        this.height = gamePanel.tileSize;

        // Çarpışma kutusu oluştur
        collisionBox = new Rectangle(x, y, width, height);
        cuttingBox = new Rectangle(x, y, width, height);

        // Animasyon
        rotation = 0;
        bladeAnimation = 0;

        // Varsayılan değerler (MowerType ile değiştirilecek)
        this.speed = 4;
        this.fuel = 100;
        this.maxFuel = 100;
        this.outOfFuel = false;
    }

    // Çim biçme makinesini ayarla
    public void setMowerType(MowerType mowerType) {
        this.mowerType = mowerType;
        this.speed = mowerType.getSpeed();
        this.maxFuel = mowerType.getFuelCapacity();
        this.fuel = this.maxFuel; // Yeni makine seçildiğinde yakıt doldur

        // Kesme alanını güncelle
        updateCuttingBox();
    }

    private void updateCuttingBox() {
        int width = this.width * cuttingWidth;
        int height = this.height * cuttingWidth;
        int x = this.x - (width - this.width) / 2;
        int y = this.y - (height - this.height) / 2;
        cuttingBox.setBounds(x, y, width, height);
    }


    public void update() {
        // Yakıt kontrolü
        if (fuel <= 0) {
            outOfFuel = true;
            return; // Yakıt bittiyse hareket etme
        }

        // WASD tuşlarına göre hareket
        boolean moving = false;

        if (keyHandler.isUpPressed()) {
            y -= speed;
            moving = true;
        }
        if (keyHandler.isDownPressed()) {
            y += speed;
            moving = true;
        }
        if (keyHandler.isLeftPressed()) {
            x -= speed;
            moving = true;
        }
        if (keyHandler.isRightPressed()) {
            x += speed;
            moving = true;
        }

        // Ekrandan çıkmamasını sağla
        if (x < 0) {
            x = 0;
        }
        if (x + width > gamePanel.screenWidth) {
            x = gamePanel.screenWidth - width;
        }
        if (y < 0) {
            y = 0;
        }
        if (y + height > gamePanel.screenHeight) {
            y = gamePanel.screenHeight - height;
        }

        // Çarpışma kutusunu güncelle
        collisionBox.x = x;
        collisionBox.y = y;

        // Kesme alanını güncelle
        updateCuttingBox();

        // Animasyon güncelle
        if (moving) {
            rotation += mowerType.getTurnRate() * 2; // Dönme hızını makine tipine göre belirle
            bladeAnimation = (bladeAnimation + 1) % 8;

            // Hareket ettiğinde yakıt tüketimi
            if (gamePanel.getUpdateCount() % 15 == 0) { // Her 15 güncellemede bir
                fuel--;
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Makine görselini çiz
        BufferedImage mowerImage = mowerType != null ? mowerType.getImage() : Assets.PLAYER_IMAGE;

        if (mowerImage != null) {
            // Görsel ile çiz
            g2.drawImage(mowerImage, x, y, width, height, null);

            // Biçme etkisi (hareket edildiğinde çevresinde yarı saydam bir biçme efekti)
            if (bladeAnimation > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.setColor(Color.WHITE);

                // Kesme genişliğine göre biçme efekti
                int effectSize = cuttingBox.width;
                g2.fillOval(cuttingBox.x, cuttingBox.y, effectSize, effectSize);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            // Yakıt bittiyse uyarı işareti
            if (outOfFuel) {
                g2.setColor(Color.RED);
                g2.fillRect(x + width/2 - 5, y - 15, 10, 10);
            }
        } else {
            // Görsel yüklenemezse elle çiz
            // Ana gövde
            g2.setColor(Color.RED);
            g2.fillRoundRect(x, y, width, height, 8, 8);

            // Tekerlekler
            g2.setColor(Color.BLACK);
            int wheelSize = width / 4;
            g2.fillOval(x + 2, y + 2, wheelSize, wheelSize); // Sol üst tekerlek
            g2.fillOval(x + width - wheelSize - 2, y + 2, wheelSize, wheelSize); // Sağ üst tekerlek
            g2.fillOval(x + 2, y + height - wheelSize - 2, wheelSize, wheelSize); // Sol alt tekerlek
            g2.fillOval(x + width - wheelSize - 2, y + height - wheelSize - 2, wheelSize, wheelSize); // Sağ alt tekerlek

            // Jantlar
            g2.setColor(Color.LIGHT_GRAY);
            int jantSize = wheelSize / 2;
            g2.fillOval(x + 2 + wheelSize/4, y + 2 + wheelSize/4, jantSize, jantSize);
            g2.fillOval(x + width - wheelSize - 2 + wheelSize/4, y + 2 + wheelSize/4, jantSize, jantSize);
            g2.fillOval(x + 2 + wheelSize/4, y + height - wheelSize - 2 + wheelSize/4, jantSize, jantSize);
            g2.fillOval(x + width - wheelSize - 2 + wheelSize/4, y + height - wheelSize - 2 + wheelSize/4, jantSize, jantSize);

            // Biçici bıçak (dönme animasyonu)
            g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
            g2.setColor(Color.LIGHT_GRAY);
            int bladeWidth = width / 2;
            g2.fillRect(x + width/2 - bladeWidth/2, y + height/2 - 1, bladeWidth, 2);
            g2.fillRect(x + width/2 - 1, y + height/2 - bladeWidth/2, 2, bladeWidth);
            g2.rotate(-Math.toRadians(rotation), x + width/2, y + height/2);

            // Biçme etkisi
            if (bladeAnimation > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.setColor(Color.WHITE);

                // Kesme genişliğine göre biçme efekti
                int effectSize = cuttingBox.width;
                g2.fillOval(cuttingBox.x, cuttingBox.y, effectSize, effectSize);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }

        // Debug: kesme alanını göster
        if (gamePanel.isDebugMode()) {
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillRect(cuttingBox.x, cuttingBox.y, cuttingBox.width, cuttingBox.height);
        }

        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(Color.YELLOW);
            g2.fillOval(x - width/4, y - height/4, width * 3/2, height * 3/2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    // Yakıt doldurma
    public void refuel(int amount) {
        fuel += amount;
        if (fuel > maxFuel) {
            fuel = maxFuel;
        }
        outOfFuel = false;
    }

    // Tamamen doldurma
    public void fullRefuel() {
        fuel = maxFuel;
        outOfFuel = false;
    }

    // Getter ve setter metotları
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getCollisionBox() {
        return collisionBox;
    }

    public Rectangle getCuttingBox() {
        return cuttingBox;
    }

    public int getFuel() {
        return fuel;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public boolean isOutOfFuel() {
        return outOfFuel;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setCuttingWidth(int cuttingWidth) {
        this.cuttingWidth = cuttingWidth;
        updateCuttingBox();
    }

    public int getCuttingWidth() {
        return cuttingWidth;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
        if (this.fuel <= 0) {
            this.fuel = 0;
            outOfFuel = true;
        }
    }
}