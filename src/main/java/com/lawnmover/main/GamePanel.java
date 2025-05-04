package com.lawnmover.main;

import com.lawnmover.entity.*;
import com.lawnmover.level.LevelManager;
import com.lawnmover.level.TimeChallenge;
import com.lawnmover.ui.GameState;
import com.lawnmover.util.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List; // Changed from java.awt.List to java.util.List

public class GamePanel extends JPanel implements Runnable {

    // Ekran boyutları
    public final int tileSize = 48; // 32x32 pixel
    public final int maxScreenCol = 20;
    public final int maxScreenRow = 15;
    public final int screenWidth = tileSize * maxScreenCol; // 640 pixels
    public final int screenHeight = tileSize * maxScreenRow; // 480 pixels

    private PowerUpManager powerUpManager;
    private boolean timerActive = false;
    private int gameTimer = 0;
    private int maxGameTime = 3600; // 60 saniye (60 FPS x 60)

    private boolean paused = false;

    // FPS
    private final int FPS = 60;

    // Sistem
    private Thread gameThread;
    private final KeyHandler keyHandler;
    private Player player;
    private LevelManager levelManager;

    private MowerManager mowerManager;
    private int money = 0;
    private boolean isShopOpen = false;
    private int updateCount = 0;
    private boolean debugMode = false;
    private ResourceManager resourceManager;

    // Oyun durumu
    private GameState gameState;
    private int currentLevel = 1;
    private int score = 0;
    private boolean gameOver = false;

    // Level geçiş ekranı için zamanlayıcı
    private int stateTimer = 0;
    private boolean shouldLoadNextLevel = false;



    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        keyHandler = new KeyHandler();
        this.addKeyListener(keyHandler);
        this.setFocusable(true);

        // Başlangıç durumu: Title ekranı
        gameState = GameState.TITLE;
    }

    public void setupGame() {
        mowerManager = new MowerManager();
        player = new Player(this, keyHandler);
        player.setMowerType(mowerManager.getSelectedMower());
        levelManager = new LevelManager(this);
        powerUpManager = new PowerUpManager(this);
        resourceManager = new ResourceManager(this);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS; // 1 saniye / FPS (nanosaniye cinsinden)
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        // Oyunu başlat
        setupGame();

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                // 1. GÜNCELLEŞTİR: Karakter konumu vs.
                update();

                // 2. ÇİZ: Ekranı yenile
                repaint();

                delta--;
            }
        }
    }

    public void update() {
        // Oyun durumuna göre güncelleme yap
        switch (gameState) {
            case TITLE:
                // Başlangıç ekranında bir tuşa basılıp basılmadığını kontrol et
                if (keyHandler.isAnyKeyPressed()) {
                    gameState = GameState.PLAYING;
                }
                break;

            case PLAYING:
                // Normal oyun akışı
                player.update();
                levelManager.update();
                powerUpManager.update();
                resourceManager.update();

                if (keyHandler.isPauseKeyPressed()) {
                    gameState = GameState.PAUSE;
                }

                // Oyun sonu kontrolü
                if (gameOver) {
                    gameState = GameState.GAME_OVER;
                }

                updateCount++;
                if (keyHandler.isShopKeyPressed() && !isShopOpen) {
                    isShopOpen = true;
                    gameState = GameState.SHOP;
                }
                break;

            case PAUSE:
                // Oyunu devam ettir
                if (keyHandler.isPauseKeyPressed()) {
                    gameState = GameState.PLAYING;
                }
                break;

            case SHOP:
                if (keyHandler.isShopKeyPressed() && isShopOpen) {
                    isShopOpen = false;
                    gameState = GameState.PLAYING;
                }
                if (keyHandler.isLeftPressed() || keyHandler.isRightPressed()) {
                    // Mower seçimi için tuş kontrolü
                    int currentIndex = mowerManager.getSelectedMowerIndex();
                    if (keyHandler.isLeftPressed()) {
                        currentIndex = (currentIndex - 1 + mowerManager.getMowerTypes().size()) % mowerManager.getMowerTypes().size();
                    } else {
                        currentIndex = (currentIndex + 1) % mowerManager.getMowerTypes().size();
                    }
                    mowerManager.setSelectedMowerIndex(currentIndex);
                    keyHandler.resetDirectionKeys(); // Tuşları sıfırla
                }
                if (keyHandler.isBuyKeyPressed()) {
                    // Seçili çim biçme makinesini satın al
                    MowerType selectedMower = mowerManager.getMowerTypes().get(mowerManager.getSelectedMowerIndex());
                    if (!selectedMower.isUnlocked() && money >= selectedMower.getPrice()) {
                        money -= selectedMower.getPrice();
                        mowerManager.unlockMower(mowerManager.getSelectedMowerIndex());
                    }
                }
                if (keyHandler.isSelectKeyPressed()) {
                    // Seçili çim biçme makinesini etkinleştir (açıksa)
                    MowerType selectedMower = mowerManager.getMowerTypes().get(mowerManager.getSelectedMowerIndex());
                    if (selectedMower.isUnlocked()) {
                        player.setMowerType(selectedMower);
                        isShopOpen = false;
                        gameState = GameState.PLAYING;
                    }
                }
                break;

            case LEVEL_UP:
                // Level geçiş ekranı için zamanlayıcı
                stateTimer++;
                if (stateTimer > 180) { // 3 saniye (60 FPS * 3)
                    stateTimer = 0;
                    gameState = GameState.PLAYING;

                    // Yeni level'i yükle
                    if (shouldLoadNextLevel) {
                        levelManager.loadLevel(currentLevel);
                        shouldLoadNextLevel = false;
                    }
                }
                break;

            case GAME_OVER:
                // Oyun sonunda bir tuşa basılıp basılmadığını kontrol et
                if (keyHandler.isAnyKeyPressed()) {
                    resetGame();
                    gameState = GameState.TITLE;
                }
                break;
        }
    }

    private void resetGame() {
        gameOver = false;
        currentLevel = 1;
        score = 0;
        setupGame();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Anti-aliasing ekleyelim (daha pürüzsüz çizimler için)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Oyun durumuna göre çizim yap
        switch (gameState) {
            case TITLE:
                drawTitleScreen(g2);
                break;

            case PLAYING:
                // Arkaplan
                g2.setColor(Assets.BACKGROUND_COLOR);
                g2.fillRect(0, 0, screenWidth, screenHeight);

                // Çimleri ve engelleri çiz
                levelManager.draw(g2);

                // Oyuncuyu çiz
                player.draw(g2);

                // PowerUp'ları çiz (BURAYA EKLEYİN)
                powerUpManager.draw(g2);

                // Aktif PowerUp'ları göster (BURAYA EKLEYİN)
                powerUpManager.drawActivePowerUps(g2, 20, 70);
                resourceManager.draw(g2);


                // Zamanlayıcıyı çiz (etkinse) (BURAYA EKLEYİN)
                if (timerActive) {
                    drawTimer(g2);
                }

                // Skoru çiz
                drawUI(g2);
                break;

            case PAUSE:
                // Oyun arkaplanını çiz
                g2.setColor(Assets.BACKGROUND_COLOR);
                g2.fillRect(0, 0, screenWidth, screenHeight);

                // Çimleri ve engelleri çiz
                levelManager.draw(g2);

                // Oyuncuyu çiz
                player.draw(g2);

                // UI'ı çiz
                drawUI(g2);

                // Pause menüsünü çiz
                drawPauseScreen(g2);
                break;

            case LEVEL_UP:
                // Arka planı çiz
                g2.setColor(Assets.BACKGROUND_COLOR);
                g2.fillRect(0, 0, screenWidth, screenHeight);

                levelManager.draw(g2);
                player.draw(g2);

                // Level geçiş mesajı
                drawLevelUpScreen(g2);
                break;

            case GAME_OVER:
                // Arka planı çiz
                g2.setColor(Assets.BACKGROUND_COLOR);
                g2.fillRect(0, 0, screenWidth, screenHeight);

                levelManager.draw(g2);
                player.draw(g2);

                // Oyun sonu mesajı
                drawGameOverScreen(g2);
                break;

            case SHOP:
                drawShopScreen(g2);
                break;
        }

        g2.dispose();
    }

    private void drawTitleScreen(Graphics2D g2) {
        // Arkaplan
        g2.setColor(Assets.PRIMARY_COLOR);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Dekoratif çim desenler
        g2.setColor(Assets.DARK_GREEN);
        for (int i = 0; i < 20; i++) {
            int x = (int) (Math.random() * screenWidth);
            int y = (int) (Math.random() * screenHeight);
            int width = 20 + (int) (Math.random() * 60);
            int height = 10 + (int) (Math.random() * 30);
            g2.fillRoundRect(x, y, width, height, 10, 10);
        }

        // Logo arka planı
        g2.setColor(Assets.SECONDARY_COLOR);
        RoundRectangle2D.Double logoBackground = new RoundRectangle2D.Double(
                screenWidth/2 - 250, screenHeight/4 - 60, 500, 120, 20, 20);
        g2.fill(logoBackground);

        // Başlık
        g2.setColor(Assets.ACCENT_COLOR);
        g2.setFont(Assets.TITLE_FONT);
        String title = "ÇİM BİÇME OYUNU";
        FontMetrics metrics = g2.getFontMetrics(Assets.TITLE_FONT);
        int titleLength = metrics.stringWidth(title);
        g2.drawString(title, screenWidth/2 - titleLength/2, screenHeight/4);

        // Başlama butonu arka planı
        g2.setColor(Assets.ACCENT_COLOR);
        RoundRectangle2D.Double startButton = new RoundRectangle2D.Double(
                screenWidth/2 - 150, screenHeight/2, 300, 60, 20, 20);
        g2.fill(startButton);

        // Başlama yazısı
        g2.setColor(Assets.SECONDARY_COLOR);
        g2.setFont(Assets.MAIN_FONT);
        String startText = "BAŞLA";
        FontMetrics startMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int startLength = startMetrics.stringWidth(startText);
        g2.drawString(startText, screenWidth/2 - startLength/2, screenHeight/2 + 35);

        // Kontrol talimatı paneli
        g2.setColor(new Color(255, 255, 255, 200));
        RoundRectangle2D.Double controlsPanel = new RoundRectangle2D.Double(
                screenWidth/2 - 200, screenHeight/2 + 100, 400, 120, 20, 20);
        g2.fill(controlsPanel);

        // Kontrol bilgileri
        g2.setColor(Assets.SECONDARY_COLOR);
        g2.setFont(Assets.SMALL_FONT);
        String controls1 = "Kontroller: W, A, S, D tuşları";
        String controls2 = "Amaç: Tüm çimleri biç, engellerden kaçın!";
        String controls3 = "Her level daha zorlaşacak, dikkatli ol!";

        g2.drawString(controls1, screenWidth/2 - 140, screenHeight/2 + 130);
        g2.drawString(controls2, screenWidth/2 - 170, screenHeight/2 + 160);
        g2.drawString(controls3, screenWidth/2 - 170, screenHeight/2 + 190);
    }

    private void drawUI(Graphics2D g2) {
        // Üst panel
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, 60);

        if (levelManager.getTimeChallenge().isActive()) {
            drawTimeChallenge(g2);
        }

        // Level ve skor
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.MAIN_FONT);
        g2.drawString("Level: " + currentLevel, 20, 40);

        String scoreText = "Skor: " + score;
        FontMetrics metrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int scoreLength = metrics.stringWidth(scoreText);
        g2.drawString(scoreText, screenWidth - scoreLength - 20, 40);

        // Para göstergesi
        String moneyText = "Para: " + money;
        int moneyLength = g2.getFontMetrics(Assets.MAIN_FONT).stringWidth(moneyText);
        g2.setColor(Color.YELLOW);
        g2.drawString(moneyText, screenWidth - moneyLength - 20, 70);

        // İlerleme göstergesi
        int barWidth = 300;
        int barHeight = 20;
        int x = screenWidth/2 - barWidth/2;
        int y = 20;

        // Arkaplan
        g2.setColor(new Color(100, 100, 100, 150));
        g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        // İlerleme
        float progress = (float) levelManager.getCutGrass() / levelManager.getTotalGrass();
        int progressWidth = (int) (barWidth * progress);

        // Renk değişimi (kırmızıdan yeşile)
        float hue = progress * 0.3f; // 0=kırmızı, 0.3=yeşil
        g2.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
        g2.fillRoundRect(x, y, progressWidth, barHeight, 10, 10);

        // Yüzde
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String percent = Math.round(progress * 100) + "%";
        FontMetrics percentMetrics = g2.getFontMetrics();
        int percentWidth = percentMetrics.stringWidth(percent);
        g2.drawString(percent, x + barWidth/2 - percentWidth/2, y + 15);

        // Yakıt göstergesi
        drawFuelGauge(g2, 20, 60, 150, 15);
    }
    private void drawLevelUpScreen(Graphics2D g2) {
        // Yarı saydam siyah arka plan
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Level tamamlandı paneli
        g2.setColor(Assets.ACCENT_COLOR);
        RoundRectangle2D.Double panel = new RoundRectangle2D.Double(
                screenWidth/2 - 250, screenHeight/2 - 150, 500, 300, 30, 30);
        g2.fill(panel);

        // Panel kenarlığı
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5));
        g2.draw(panel);

        // Tebrik mesajı
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.TITLE_FONT);
        String congrats = "TEBRİKLER!";
        FontMetrics metrics = g2.getFontMetrics(Assets.TITLE_FONT);
        int congratsWidth = metrics.stringWidth(congrats);
        g2.drawString(congrats, screenWidth/2 - congratsWidth/2, screenHeight/2 - 80);

        // Level tamamlandı mesajı
        g2.setFont(Assets.MAIN_FONT);
        String levelUp = "LEVEL " + (currentLevel - 1) + " TAMAMLANDI";
        FontMetrics levelMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int levelWidth = levelMetrics.stringWidth(levelUp);
        g2.drawString(levelUp, screenWidth/2 - levelWidth/2, screenHeight/2 - 30);

        // Bonus puan
        g2.setFont(Assets.MAIN_FONT);
        String bonus = "Bonus: +" + (100 * (currentLevel - 1));
        FontMetrics bonusMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int bonusWidth = bonusMetrics.stringWidth(bonus);
        g2.drawString(bonus, screenWidth/2 - bonusWidth/2, screenHeight/2 + 20);

        if (levelManager.getTimeChallenge().isActive()) {
            int timeBonus = levelManager.getTimeChallenge().calculateBonus();
            g2.setColor(Color.YELLOW);
            String timeBonusText = "Zaman Bonusu: +" + timeBonus;
            FontMetrics timeBonusMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
            int timeBonusWidth = timeBonusMetrics.stringWidth(timeBonusText);
            g2.drawString(timeBonusText, screenWidth/2 - timeBonusWidth/2, screenHeight/2 - 10);
        }

        // Toplam puan
        g2.setFont(Assets.MAIN_FONT);
        String totalScore = "Toplam Skor: " + score;
        FontMetrics scoreMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int scoreWidth = scoreMetrics.stringWidth(totalScore);
        g2.drawString(totalScore, screenWidth/2 - scoreWidth/2, screenHeight/2 + 60);

        // Sonraki level mesajı
        g2.setFont(Assets.MAIN_FONT);
        String nextLevel = "Level " + currentLevel + " başlıyor...";
        FontMetrics nextMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int nextWidth = nextMetrics.stringWidth(nextLevel);
        g2.drawString(nextLevel, screenWidth/2 - nextWidth/2, screenHeight/2 + 100);

        // İlerleme çubuğu (timer'a bağlı)
        int barWidth = 400;
        int barHeight = 20;
        int barX = screenWidth/2 - barWidth/2;
        int barY = screenHeight/2 + 140;

        // Bar arkaplanı
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        // İlerleme
        float progress = (float) stateTimer / 180; // 180 frame = 3 saniye
        int progressWidth = (int) (barWidth * progress);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(barX, barY, progressWidth, barHeight, 10, 10);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        // Yarı saydam siyah arka plan
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Game Over paneli
        g2.setColor(new Color(139, 0, 0));
        RoundRectangle2D.Double panel = new RoundRectangle2D.Double(
                screenWidth/2 - 250, screenHeight/2 - 150, 500, 300, 30, 30);
        g2.fill(panel);

        // Panel kenarlığı
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5));
        g2.draw(panel);

        // Oyun bitti mesajı
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.TITLE_FONT);
        String gameOver = "OYUN BİTTİ";
        FontMetrics metrics = g2.getFontMetrics(Assets.TITLE_FONT);
        int gameOverWidth = metrics.stringWidth(gameOver);
        g2.drawString(gameOver, screenWidth/2 - gameOverWidth/2, screenHeight/2 - 80);

        // Final skor
        g2.setFont(Assets.MAIN_FONT);
        String finalScore = "Final Skor: " + score;
        FontMetrics scoreMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int scoreWidth = scoreMetrics.stringWidth(finalScore);
        g2.drawString(finalScore, screenWidth/2 - scoreWidth/2, screenHeight/2 - 20);

        // Ulaşılan level
        g2.setFont(Assets.MAIN_FONT);
        String levelReached = "Ulaşılan Level: " + currentLevel;
        FontMetrics levelMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int levelWidth = levelMetrics.stringWidth(levelReached);
        g2.drawString(levelReached, screenWidth/2 - levelWidth/2, screenHeight/2 + 20);

        // Tekrar başlatma düğmesi
        g2.setColor(Assets.ACCENT_COLOR);
        RoundRectangle2D.Double button = new RoundRectangle2D.Double(
                screenWidth/2 - 150, screenHeight/2 + 60, 300, 60, 20, 20);
        g2.fill(button);

        // Düğme yazısı
        g2.setColor(Color.WHITE);
        String restart = "TEKRAR OYNA";
        FontMetrics restartMetrics = g2.getFontMetrics(Assets.MAIN_FONT);
        int restartWidth = restartMetrics.stringWidth(restart);
        g2.drawString(restart, screenWidth/2 - restartWidth/2, screenHeight/2 + 100);

        // Yönlendirme
        g2.setFont(Assets.SMALL_FONT);
        String press = "Herhangi bir tuşa basın";
        FontMetrics pressMetrics = g2.getFontMetrics(Assets.SMALL_FONT);
        int pressWidth = pressMetrics.stringWidth(press);
        g2.drawString(press, screenWidth/2 - pressWidth/2, screenHeight/2 + 140);
    }

    private void drawTimeChallenge(Graphics2D g2) {
        TimeChallenge challenge = levelManager.getTimeChallenge();
        int remainingTime = challenge.getRemainingTime();

        // Renk (süre azaldıkça kırmızıya döner)
        Color timerColor;
        float timeRatio = (float) remainingTime / challenge.getTimeLimit();

        if (timeRatio > 0.6f) {
            timerColor = Color.GREEN;
        } else if (timeRatio > 0.3f) {
            timerColor = Color.YELLOW;
        } else {
            // Son 30% için yanıp sönen kırmızı
            boolean blink = System.currentTimeMillis() / 500 % 2 == 0;
            timerColor = blink ? Color.RED : Color.ORANGE;
        }

        // Zaman göstergesini çiz
        g2.setFont(Assets.MAIN_FONT);
        String timeText = "SÜRE: " + remainingTime;

        // Arkaplan
        int textWidth = g2.getFontMetrics().stringWidth(timeText);
        int textHeight = g2.getFontMetrics().getHeight();

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(screenWidth/2 - textWidth/2 - 10, 70, textWidth + 20, textHeight + 10, 10, 10);

        // Zamanı çiz
        g2.setColor(timerColor);
        g2.drawString(timeText, screenWidth/2 - textWidth/2, 70 + textHeight);

        // Meydan okuması tipini göster
        g2.setFont(Assets.SMALL_FONT);
        String challengeText = "ZAMAN MEYDAN OKUMASI!";
        int challengeWidth = g2.getFontMetrics().stringWidth(challengeText);

        g2.setColor(Color.WHITE);
        g2.drawString(challengeText, screenWidth/2 - challengeWidth/2, 100 + textHeight);
    }


    // Level ilerleme metodları
    public void levelUp() {
        gameState = GameState.LEVEL_UP;
        shouldLoadNextLevel = true;

        // Level sonu ödülleri
        int levelBonus = 100 * currentLevel;
        addScore(levelBonus);

        // Para ödülü
        int moneyBonus = 200 * currentLevel;
        addMoney(moneyBonus);

        // Yakıt doldur
        player.fullRefuel();

        System.out.println("Level yükseltiliyor: " + currentLevel);
    }

    private void drawShopScreen(Graphics2D g2) {
        // Arkaplan
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Shop başlığı
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.TITLE_FONT);
        String title = "ÇİM BİÇME MAKİNESİ MAĞAZASI";
        FontMetrics titleMetrics = g2.getFontMetrics(Assets.TITLE_FONT);
        int titleWidth = titleMetrics.stringWidth(title);
        g2.drawString(title, screenWidth/2 - titleWidth/2, 80);

        // Para gösterimi
        g2.setFont(Assets.MAIN_FONT);
        String moneyText = "Mevcut Para: " + money;
        g2.drawString(moneyText, 50, 150);

        // Çim biçme makineleri listesi
        List<MowerType> mowers = mowerManager.getMowerTypes();
        int selectedIndex = mowerManager.getSelectedMowerIndex();

        int startY = 200;
        int itemHeight = 80;

        for (int i = 0; i < mowers.size(); i++) {
            MowerType mower = mowers.get(i);

            // Seçili olan vurgulanır
            if (i == selectedIndex) {
                g2.setColor(new Color(255, 255, 0, 100));
                g2.fillRect(50, startY + i * itemHeight, screenWidth - 100, itemHeight);
            }

            // Makine adı
            g2.setColor(Color.WHITE);
            g2.setFont(Assets.MAIN_FONT);
            g2.drawString(mower.getName(), 70, startY + i * itemHeight + 30);

            // Makine görseli
            if (mower.getImage() != null) {
                g2.drawImage(mower.getImage(), screenWidth - 200, startY + i * itemHeight + 10, 60, 60, null);
            }

            // Özellikler
            g2.setFont(Assets.SMALL_FONT);
            String stats = "Hız: " + mower.getSpeed() + " | Kesme: " + mower.getCuttingWidth() +
                    " | Yakıt: " + mower.getFuelCapacity();
            g2.drawString(stats, 70, startY + i * itemHeight + 60);

            // Fiyat/durum
            if (mower.isUnlocked()) {
                g2.setColor(Color.GREEN);
                g2.drawString("AÇIK", screenWidth - 80, startY + i * itemHeight + 40);
            } else {
                g2.setColor(money >= mower.getPrice() ? Color.YELLOW : Color.RED);
                g2.drawString(mower.getPrice() + " para", screenWidth - 100, startY + i * itemHeight + 40);
            }
        }

        // Kontrol bilgileri
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.SMALL_FONT);
        int infoY = screenHeight - 100;
        g2.drawString("Sol/Sağ Ok Tuşları: Makine Seç", 50, infoY);
        g2.drawString("E Tuşu: Satın Al", 50, infoY + 25);
        g2.drawString("Enter: Makineyi Seç ve Devam Et", 50, infoY + 50);
        g2.drawString("M Tuşu: Mağazadan Çık", 50, infoY + 75);
    }

    // Getter ve Setter metotları
    public Player getPlayer() {
        return player;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void toggleDebugMode() {
        debugMode = !debugMode;
    }

    private void drawTimer(Graphics2D g2) {
        int minutes = gameTimer / 3600;
        int seconds = (gameTimer % 3600) / 60;

        String timeText = String.format("Süre: %02d:%02d", minutes, seconds);
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.MAIN_FONT);
        g2.drawString(timeText, screenWidth - 150, 40);
    }


    public boolean isTimerActive() {
        return timerActive;
    }

    public void setTimerActive(boolean timerActive) {
        this.timerActive = timerActive;
    }

    public void addTime(int seconds) {
        gameTimer += seconds * 60; // FPS bazında süre ekle
        if (gameTimer > maxGameTime) {
            gameTimer = maxGameTime;
        }
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    // When level changes
// Level yüklendiğinde

    public void loadLevel(int level) {
        levelManager.loadLevel(level);
        powerUpManager.clearPowerUps();
        resourceManager.clearResources();

        // Level ayarlarını güncelle
        float spawnChance = 0.3f + (level * 0.05f); // Max %80 şans
        if (spawnChance > 0.8f) spawnChance = 0.8f;
        powerUpManager.setSpawnSettings(300 - (level * 20), spawnChance);

        // Kaynak oluşturma ayarlarını güncelle
        resourceManager.setSpawnSettings(level);

        // Oyuncuyu yeniden doldur
        player.fullRefuel();
    }


    private void drawFuelGauge(Graphics2D g2, int x, int y, int width, int height) {
        // Arkaplan
        g2.setColor(new Color(50, 50, 50, 150));
        g2.fillRoundRect(x, y, width, height, 5, 5);

        // Yakıt seviyesi
        float fuelRatio = (float) player.getFuel() / player.getMaxFuel();
        int fuelWidth = (int) (width * fuelRatio);

        // Renk (yeşilden kırmızıya)
        Color fuelColor;
        if (fuelRatio > 0.6f) {
            fuelColor = Color.GREEN;
        } else if (fuelRatio > 0.3f) {
            fuelColor = Color.YELLOW;
        } else {
            fuelColor = Color.RED;
        }

        g2.setColor(fuelColor);
        g2.fillRoundRect(x, y, fuelWidth, height, 5, 5);

        // Yakıt simgesi
        g2.setColor(Color.WHITE);
        g2.drawString("⛽", x - 20, y + height - 1);

        // Yakıt yüzdesi
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String fuelPercent = Math.round(fuelRatio * 100) + "%";
        g2.drawString(fuelPercent, x + width/2 - 10, y + height - 3);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    private void drawPauseScreen(Graphics2D g2) {
        // Yarı saydam siyah arka plan
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Pause başlık
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.TITLE_FONT);
        String pauseTitle = "OYUN DURAKLATILDI";
        FontMetrics metrics = g2.getFontMetrics(Assets.TITLE_FONT);
        int titleWidth = metrics.stringWidth(pauseTitle);
        g2.drawString(pauseTitle, screenWidth/2 - titleWidth/2, screenHeight/3);

        // Bilgiler
        g2.setFont(Assets.MAIN_FONT);
        String[] pauseInfo = {
                "Devam etmek için ESC veya P tuşuna basın",
                "WASD tuşları ile hareket edin",
                "M tuşu ile mağazaya erişin",
                "F3 ile debug modunu açın/kapatın"
        };

        int startY = screenHeight/2;
        for (int i = 0; i < pauseInfo.length; i++) {
            int infoWidth = g2.getFontMetrics().stringWidth(pauseInfo[i]);
            g2.drawString(pauseInfo[i], screenWidth/2 - infoWidth/2, startY + i * 40);
        }
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}