package com.lawnmover.level;

import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GrassTile {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean isCut;
    private final Rectangle collisionBox;

    public GrassTile(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isCut = false;
        this.collisionBox = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2) {
        if (isCut) {
            // Biçilmiş çim görseli
            if (Assets.CUT_GRASS_TILE != null) {
                g2.drawImage(Assets.CUT_GRASS_TILE, x, y, width, height, null);
            } else {
                // Görsel yüklenemezse elle çiz
                g2.setColor(new Color(144, 238, 144));
                g2.fillRect(x, y, width, height);
            }
        } else {
            // Biçilmemiş çim görseli
            if (Assets.GRASS_TILE != null) {
                g2.drawImage(Assets.GRASS_TILE, x, y, width, height, null);
            } else {
                // Görsel yüklenemezse elle çiz
                g2.setColor(new Color(0, 128, 0));
                g2.fillRect(x, y, width, height);

                // Çim detayları
                g2.setColor(new Color(0, 100, 0, 100));
                int numDetails = 3;
                int detailWidth = 1;
                int detailHeight = height / 2;

                for (int i = 0; i < numDetails; i++) {
                    int detailX = x + (width / (numDetails + 1)) * (i + 1);
                    int detailY = y + (height - detailHeight) / 2;
                    g2.fillRect(detailX, detailY, detailWidth, detailHeight);
                }
            }
        }
    }

    // Getter ve setter metotları
    public boolean isCut() {
        return isCut;
    }

    public void setCut(boolean cut) {
        isCut = cut;
    }

    public Rectangle getCollisionBox() {
        return collisionBox;
    }
}