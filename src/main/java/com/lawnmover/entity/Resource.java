package com.lawnmover.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;

public class Resource {

    public enum Type {
        FUEL("Yakıt", Color.RED),
        MONEY("Para", Color.YELLOW),
        REPAIR("Tamir", Color.BLUE);

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
    private int value;
    private BufferedImage image;
    private Rectangle collisionBox;
    private boolean active;
    private float animationTick;

    public Resource(int x, int y, int width, int height, Type type, int value) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.value = value;
        this.collisionBox = new Rectangle(x, y, width, height);
        this.active = true;
        this.animationTick = 0;

        // Kaynak görseli oluştur
        this.image = createResourceImage(type);
    }

    private BufferedImage createResourceImage(Type type) {
        // Basit bir kaynak görseli oluştur
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        switch (type) {
            case FUEL:
                // Yakıt bidonu
                g2.setColor(Color.RED);
                g2.fillRect(width/4, 0, width/2, height);
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(width/3, height/4, width/3, height/10);
                g2.fillRect(width/3, height/2, width/3, height/3);
                break;

            case MONEY:
                // Para çantası
                g2.setColor(Color.YELLOW);
                g2.fillOval(0, 0, width, height);
                g2.setColor(Color.BLACK);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, width/3));
                g2.drawString("$", width/3, height/2 + width/6);
                break;

            case REPAIR:
                // Tamir çantası
                g2.setColor(Color.WHITE);
                g2.fillRect(width/6, height/6, 2*width/3, 2*height/3);
                g2.setColor(Color.RED);
                g2.fillRect(width/3, height/6, width/3, height/6);
                g2.fillRect(width/3, height/3, width/3, height/3);
                g2.setColor(Color.BLUE);
                g2.drawRect(width/6, height/6, 2*width/3, 2*height/3);
                break;
        }

        g2.dispose();
        return img;
    }

    public void update() {
        // Animasyon için tick artır
        animationTick += 0.1f;
    }

    public void draw(Graphics2D g2) {
        if (!active) return;

        // Animasyon efekti: Yukarı-aşağı hareket
        float offset = (float) (2 * Math.sin(animationTick));
        int drawY = y + (int)offset;

        // Hafif parıltı efekti
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.setColor(type.getColor());
        g2.fillOval(x - 2, drawY - 2, width + 4, height + 4);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        if (image != null) {
            g2.drawImage(image, x, drawY, width, height, null);
        } else {
            // Fallback çizim
            g2.setColor(type.getColor());
            g2.fillRect(x, drawY, width, height);
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

    public int getValue() {
        return value;
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