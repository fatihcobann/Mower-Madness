package com.lawnmover.level;

import com.lawnmover.main.GamePanel;
import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.util.Random;

public class MovingObstacle extends Obstacle {

    private int speedX;
    private int speedY;
    private final GamePanel gamePanel;
    private final Random random;
    private float animationTick;

    public MovingObstacle(int x, int y, int width, int height, GamePanel gamePanel) {
        super(x, y, width, height);
        this.gamePanel = gamePanel;
        this.random = new Random();

        // Rastgele hız (1 ile 3 arasında)
        this.speedX = random.nextInt(2) + 1;
        this.speedY = random.nextInt(2) + 1;

        // Rastgele yön (pozitif veya negatif)
        if (random.nextBoolean()) speedX *= -1;
        if (random.nextBoolean()) speedY *= -1;

        animationTick = 0;
    }

    public void update() {
        // Hareket et
        x += speedX;
        y += speedY;

        // Ekran sınırlarında sek
        if (x <= 0 || x + width >= gamePanel.screenWidth) {
            speedX *= -1;
        }

        if (y <= 0 || y + height >= gamePanel.screenHeight) {
            speedY *= -1;
        }

        // Çarpışma kutusunu güncelle
        collisionBox.x = x;
        collisionBox.y = y;

        // Animasyon güncelle
        animationTick += 0.1f;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (Assets.MOVING_OBSTACLE_IMAGE != null) {
            // Görsel ile çiz
            g2.drawImage(Assets.MOVING_OBSTACLE_IMAGE, x, y, width, height, null);

            // Hareket efekti ekleyelim
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.setColor(Color.RED);
            int effectSize = 5;
            if (Math.abs(speedX) > 0 || Math.abs(speedY) > 0) {
                g2.fillOval(x - effectSize, y - effectSize, width + effectSize*2, height + effectSize*2);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            // Görsel yüklenemezse elle çiz
            // Gölge
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillOval(x + 2, y + height - 4, width, 8);

            // Ana gövde
            g2.setColor(new Color(139, 0, 0));
            g2.fillRoundRect(x, y, width, height, 16, 16);

            // Gözler
            g2.setColor(Color.WHITE);
            int eyeSize = width / 5;
            g2.fillOval(x + width/4 - eyeSize/2, y + height/3 - eyeSize/2, eyeSize, eyeSize);
            g2.fillOval(x + 3*width/4 - eyeSize/2, y + height/3 - eyeSize/2, eyeSize, eyeSize);

            // Göz bebekleri
            g2.setColor(Color.BLACK);
            int pupilSize = eyeSize / 2;
            int pupilOffsetX = speedX > 0 ? pupilSize/2 : -pupilSize/2;
            int pupilOffsetY = speedY > 0 ? pupilSize/2 : -pupilSize/2;

            g2.fillOval(x + width/4 - pupilSize/2 + pupilOffsetX, y + height/3 - pupilSize/2 + pupilOffsetY, pupilSize, pupilSize);
            g2.fillOval(x + 3*width/4 - pupilSize/2 + pupilOffsetX, y + height/3 - pupilSize/2 + pupilOffsetY, pupilSize, pupilSize);

            // Ağız (animasyonlu)
            g2.setColor(Color.BLACK);
            int mouthY = y + 2*height/3;
            int mouthHeight = (int)(height/8 * (Math.sin(animationTick) + 1)) + 2;
            g2.fillRoundRect(x + width/4, mouthY, width/2, mouthHeight, 8, 8);

            // Hareket efekti
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.setColor(Color.RED);
            int effectSize = 5;
            if (Math.abs(speedX) > 0 || Math.abs(speedY) > 0) {
                g2.fillOval(x - effectSize, y - effectSize, width + effectSize*2, height + effectSize*2);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }
}