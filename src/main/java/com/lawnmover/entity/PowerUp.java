package com.lawnmover.entity;

import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;

public class PowerUp {
    // Power-up türleri
    public enum Type {
        SPEED("Hız Artışı", Color.CYAN),
        CUTTING_WIDTH("Geniş Kesim", Color.GREEN),
        INVINCIBILITY("Yenilmezlik", Color.YELLOW),
        FUEL("Yakıt Dolumu", Color.RED),
        TIME("Ek Süre", Color.MAGENTA);

        private final String name;
        private final Color color;

        Type(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }

    private int x;
    private int y;
    private int width;
    private int height;
    private Type type;
    private BufferedImage image;
    private Rectangle collisionBox;
    private boolean active;
    private float animationTick;

    public PowerUp(int x, int y, int width, int height, Type type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.collisionBox = new Rectangle(x, y, width, height);
        this.active = true;
        this.animationTick = 0;

        // Güç artırımı görseli
        this.image = createPowerUpImage(type);
    }

    private BufferedImage createPowerUpImage(Type type) {
        // Basit bir güç artırımı görseli oluştur
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Arkaplan
        g2.setColor(type.getColor());
        g2.fillOval(0, 0, width, height);

        // Simge (P harfi)
        g2.setColor(Color.WHITE);
        int fontSize = width / 2;
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize));
        g2.drawString("P", width/4, height/2 + fontSize/3);

        g2.dispose();
        return img;
    }

    public void update() {
        // Animasyon için tick artır
        animationTick += 0.1f;
    }

    public void draw(Graphics2D g2) {
        if (!active) return;

        // Animasyon efekti: Boyut değişimi
        float scale = (float) (1.0 + 0.1 * Math.sin(animationTick));
        int drawWidth = (int)(width * scale);
        int drawHeight = (int)(height * scale);
        int drawX = x - (drawWidth - width) / 2;
        int drawY = y - (drawHeight - height) / 2;

        // Parıltı efekti
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.setColor(type.getColor());
        g2.fillOval(drawX - 5, drawY - 5, drawWidth + 10, drawHeight + 10);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        if (image != null) {
            g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            // Fallback çizim
            g2.setColor(type.getColor());
            g2.fillOval(drawX, drawY, drawWidth, drawHeight);

            // Güç artırımı tipini göster
            g2.setColor(Color.WHITE);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            String letter = type.name().substring(0, 1);
            g2.drawString(letter, drawX + drawWidth/2 - 4, drawY + drawHeight/2 + 4);
        }
    }

    // Getter ve Setter metodları
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Type getType() {
        return type;
    }

    public Rectangle getCollisionBox() {
        return collisionBox;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}