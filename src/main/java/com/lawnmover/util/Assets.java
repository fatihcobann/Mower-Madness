package com.lawnmover.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Assets {

    // Oyun renkleri
    public static final Color PRIMARY_COLOR = new Color(76, 175, 80);
    public static final Color SECONDARY_COLOR = new Color(33, 33, 33);
    public static final Color ACCENT_COLOR = new Color(255, 193, 7);
    public static final Color DARK_GREEN = new Color(27, 94, 32);
    public static final Color LIGHT_GREEN = new Color(129, 199, 132);
    public static final Color BACKGROUND_COLOR = new Color(238, 238, 238);

    // Fontlar
    public static Font TITLE_FONT;
    public static Font MAIN_FONT;
    public static Font SMALL_FONT;

    // Görseller
    public static BufferedImage PLAYER_IMAGE;
    public static BufferedImage GRASS_TILE;
    public static BufferedImage CUT_GRASS_TILE;
    public static BufferedImage OBSTACLE_IMAGE;
    public static BufferedImage MOVING_OBSTACLE_IMAGE;
    public static BufferedImage BACKGROUND_IMAGE;

    // Yeni eklenen görseller için harita
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    // Görselleri ve fontları yükle
    public static void init() {
        try {
            // Fontları yükle
            TITLE_FONT = new Font("Arial", Font.BOLD, 48);
            MAIN_FONT = new Font("Arial", Font.BOLD, 24);
            SMALL_FONT = new Font("Arial", Font.PLAIN, 18);

            // Görselleri yükle
            System.out.println("Görseller yükleniyor...");

            PLAYER_IMAGE = loadImage("/images/player.png", Color.RED);
            GRASS_TILE = loadImage("/images/grass.jpg", DARK_GREEN);
            CUT_GRASS_TILE = loadImage("/images/cut_grass.png", LIGHT_GREEN);
            OBSTACLE_IMAGE = loadImage("/images/obstacle.jpg", Color.DARK_GRAY);
            MOVING_OBSTACLE_IMAGE = loadImage("/images/moving_obstacle.png", Color.RED);
            BACKGROUND_IMAGE = loadImage("/images/background.png", BACKGROUND_COLOR);

            System.out.println("Tüm görseller yüklendi veya yerleştirildi!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Genel hata: " + e.getMessage());
        }
    }

    // İsimle görsel yükleme
    public static BufferedImage getImage(String name) {
        return imageCache.getOrDefault(name, null);
    }

    // Yeni görsel ekleme
    public static void addImage(String name, BufferedImage image) {
        imageCache.put(name, image);
    }

    // Geliştirilmiş görsel yükleme metodu
    private static BufferedImage loadImage(String path, Color fallbackColor) {
        try {
            InputStream is = Assets.class.getResourceAsStream(path);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                System.out.println(path + " başarıyla yüklendi!");
                return img;
            } else {
                System.out.println("Kaynak bulunamadı: " + path);
                return createPlaceholderImage(48, 48, fallbackColor);
            }
        } catch (IOException e) {
            System.out.println(path + " yüklenemedi: " + e.getMessage());
            return createPlaceholderImage(48, 48, fallbackColor);
        }
    }

    // Yer tutucu görsel oluştur
    private static BufferedImage createPlaceholderImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }
}