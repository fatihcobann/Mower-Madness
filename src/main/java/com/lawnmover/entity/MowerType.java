package com.lawnmover.entity;

import java.awt.image.BufferedImage;

public class MowerType {
    private final String name;
    private final int speed;
    private final int cuttingWidth;
    private final int turnRate;
    private final int fuelCapacity;
    private final int price;
    private final BufferedImage image;
    private final String description;
    private boolean unlocked;

    public MowerType(String name, int speed, int cuttingWidth, int turnRate,
                     int fuelCapacity, int price, BufferedImage image, String description) {
        this.name = name;
        this.speed = speed;
        this.cuttingWidth = cuttingWidth;
        this.turnRate = turnRate;
        this.fuelCapacity = fuelCapacity;
        this.price = price;
        this.image = image;
        this.description = description;
        this.unlocked = false; // Başlangıçta kilitli
    }

    // Getter ve Setter metodları
    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public int getCuttingWidth() {
        return cuttingWidth;
    }

    public int getTurnRate() {
        return turnRate;
    }

    public int getFuelCapacity() {
        return fuelCapacity;
    }

    public int getPrice() {
        return price;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}