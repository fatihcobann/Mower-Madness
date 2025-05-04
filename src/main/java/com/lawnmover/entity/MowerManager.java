package com.lawnmover.entity;

import com.lawnmover.util.Assets;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MowerManager {
    private List<MowerType> mowerTypes;
    private int selectedMowerIndex;

    public MowerManager() {
        mowerTypes = new ArrayList<>();
        initializeMowerTypes();
        selectedMowerIndex = 0; // Başlangıçta ilk çim biçme makinesi seçili
        mowerTypes.get(0).setUnlocked(true); // İlk makine başlangıçta açık
    }

    private void initializeMowerTypes() {
        // Placeholder görüntüler - uygun görüntüleri Assets sınıfından yüklemelisiniz
        BufferedImage basicMowerImg = Assets.PLAYER_IMAGE;
        BufferedImage fastMowerImg = createColorVariant(Assets.PLAYER_IMAGE, Color.BLUE);
        BufferedImage wideMowerImg = createColorVariant(Assets.PLAYER_IMAGE, Color.GREEN);
        BufferedImage proMowerImg = createColorVariant(Assets.PLAYER_IMAGE, Color.YELLOW);

        // Temel çim biçme makinesi - başlangıçta erişilebilir
        mowerTypes.add(new MowerType(
                "Temel Çim Biçme Makinesi",
                4,  // hız
                1,  // kesme genişliği
                5,  // dönüş hızı
                100, // yakıt kapasitesi
                0,   // fiyat
                basicMowerImg,
                "Temel bir çim biçme makinesi. Başlangıç için uygun."
        ));

        // Hızlı çim biçme makinesi
        mowerTypes.add(new MowerType(
                "Hızlı Çim Biçme Makinesi",
                6,  // daha hızlı
                1,  // aynı kesme genişliği
                7,  // daha hızlı dönüş
                80, // daha az yakıt kapasitesi
                1000, // fiyat
                fastMowerImg,
                "Daha hızlı hareket eder, ancak yakıt tüketimi fazladır."
        ));

        // Geniş çim biçme makinesi
        mowerTypes.add(new MowerType(
                "Geniş Çim Biçme Makinesi",
                3,  // daha yavaş
                2,  // daha geniş kesme alanı
                3,  // daha yavaş dönüş
                120, // daha fazla yakıt kapasitesi
                2000, // fiyat
                wideMowerImg,
                "Daha geniş alanları keser, ancak daha yavaş hareket eder."
        ));

        // Profesyonel çim biçme makinesi
        mowerTypes.add(new MowerType(
                "Profesyonel Çim Biçme Makinesi",
                5,  // iyi hız
                2,  // geniş kesme alanı
                6,  // iyi dönüş hızı
                150, // yüksek yakıt kapasitesi
                5000, // yüksek fiyat
                proMowerImg,
                "Her açıdan mükemmel performans gösteren premium model."
        ));
    }

    // Renk varyasyonu oluşturmak için yardımcı metod
    private BufferedImage createColorVariant(BufferedImage original, Color color) {
        if (original == null) {
            return null;
        }

        BufferedImage variant = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x, y);
                if (pixel != 0) { // Şeffaf değilse
                    variant.setRGB(x, y, color.getRGB());
                }
            }
        }

        return variant;
    }

    // Getter ve Setter metodları
    public MowerType getSelectedMower() {
        return mowerTypes.get(selectedMowerIndex);
    }

    public void setSelectedMowerIndex(int index) {
        if (index >= 0 && index < mowerTypes.size() && mowerTypes.get(index).isUnlocked()) {
            selectedMowerIndex = index;
        }
    }

    public List<MowerType> getMowerTypes() {
        return mowerTypes;
    }

    public void unlockMower(int index) {
        if (index >= 0 && index < mowerTypes.size()) {
            mowerTypes.get(index).setUnlocked(true);
        }
    }

    public int getSelectedMowerIndex() {
        return selectedMowerIndex;
    }
}