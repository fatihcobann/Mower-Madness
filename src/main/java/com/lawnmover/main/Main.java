package com.lawnmover.main;

import com.lawnmover.util.Assets;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Görsel ve fontları yükle
        Assets.init();

        JFrame window = new JFrame("Çim Biçme Oyunu");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}