package com.lawnmover.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // Tuş durumları
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean anyKeyPressed;

    // Yeni tuşlar
    private boolean shopKeyPressed;
    private boolean buyKeyPressed;
    private boolean selectKeyPressed;
    private boolean debugKeyPressed;

    @Override
    public void keyTyped(KeyEvent e) {
        // Bu metot bu oyunda kullanılmıyor
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Tuşa basıldı
        anyKeyPressed = true;

        // WASD kontrolleri
        if (keyCode == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (keyCode == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (keyCode == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (keyCode == KeyEvent.VK_D) {
            rightPressed = true;
        }

        // Mağaza ve ilave tuşlar
        if (keyCode == KeyEvent.VK_M) {
            shopKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_E) {
            buyKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_ENTER) {
            selectKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_F3) {
            debugKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
            pauseKeyPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Tuş bırakıldı
        anyKeyPressed = false;

        // WASD kontrolleri
        if (keyCode == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (keyCode == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        }

        // Mağaza ve ilave tuşlar
        if (keyCode == KeyEvent.VK_M) {
            shopKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_E) {
            buyKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_ENTER) {
            selectKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_F3) {
            debugKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
            pauseKeyPressed = false;
        }
    }

    // Yön tuşlarını sıfırla (hızlı seçim için)
    public void resetDirectionKeys() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
    }

    // Getter metotları
    public boolean isUpPressed() {
        return upPressed;
    }

    public boolean isDownPressed() {
        return downPressed;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isAnyKeyPressed() {
        return anyKeyPressed;
    }

    public boolean isShopKeyPressed() {
        return shopKeyPressed;
    }

    public boolean isBuyKeyPressed() {
        return buyKeyPressed;
    }

    private boolean pauseKeyPressed;

    public boolean isPauseKeyPressed() {
        return pauseKeyPressed;
    }

    public boolean isSelectKeyPressed() {
        return selectKeyPressed;
    }

    public boolean isDebugKeyPressed() {
        return debugKeyPressed;
    }
}