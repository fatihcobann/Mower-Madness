package com.lawnmover.level;

import com.lawnmover.main.GamePanel;

public class TimeChallenge {
    private GamePanel gamePanel;
    private boolean active;
    private int timeLimit; // Saniye cinsinden
    private int remainingTime; // Saniye cinsinden
    private int bonusReward; // Ödül miktarı

    public TimeChallenge(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.active = false;
        this.timeLimit = 60; // Varsayılan 60 saniye
        this.remainingTime = timeLimit;
        this.bonusReward = 500; // Varsayılan ödül
    }

    public void start(int timeLimit, int bonusReward) {
        this.active = true;
        this.timeLimit = timeLimit;
        this.remainingTime = timeLimit;
        this.bonusReward = bonusReward;
    }

    public void stop() {
        this.active = false;
    }

    // Saniyede bir çağrılmalı
    public void update() {
        if (!active) return;

        remainingTime--;

        // Süre bitince oyunu bitir
        if (remainingTime <= 0) {
            remainingTime = 0;
            if (gamePanel.getLevelManager().getTimeChallengeMode()) {
                // Süre bitti ve başarısız oldu
                gamePanel.setGameOver(true);
            } else {
                // Normal seviyede süre bitti, sadece zaman bonusu yok
                active = false;
            }
        }
    }

    // Kalan süreye göre bonus hesapla
    public int calculateBonus() {
        // Kalan süreye göre ekstra bonus (her saniye için bonusun %1'i)
        return (int)(bonusReward * (remainingTime / (float)timeLimit));
    }

    // Getter ve setter metodları
    public boolean isActive() {
        return active;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getBonusReward() {
        return bonusReward;
    }
}