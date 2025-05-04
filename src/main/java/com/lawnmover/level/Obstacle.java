package com.lawnmover.level;

import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Obstacle {

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Rectangle collisionBox;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.collisionBox = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2) {
        if (Assets.OBSTACLE_IMAGE != null) {
            // Görseli çiz
            g2.drawImage(Assets.OBSTACLE_IMAGE, x, y, width, height, null);
        } else {
            // Görsel yüklenemezse elle çiz
            // Gölge efekti
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRect(x + 3, y + 3, width, height);

            // Ana taş
            g2.setColor(Color.DARK_GRAY);
            g2.fillRoundRect(x, y, width, height, 8, 8);

            // Taş detayları
            g2.setColor(Color.GRAY);
            g2.drawLine(x + width/4, y + height/4, x + width/2, y + 3*height/4);
            g2.drawLine(x + 3*width/4, y + height/4, x + width/2, y + 2*height/3);

            // Taşın üstünde ışık yansıması
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillOval(x + width/4, y + height/4, width/6, height/6);
        }
    }

    // Getter metotları
    public Rectangle getCollisionBox() {
        return collisionBox;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}