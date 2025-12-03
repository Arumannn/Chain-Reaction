package com.silent.treatment.chainreaction.core;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class untuk mengelola Audio (BGM & SFX).
 */
public class SoundManager {
    private static SoundManager instance;
    
    private MediaPlayer bgmPlayer;

    private boolean isBgmMuted = false;
    private boolean isSfxMuted = false;
    
    // Cache untuk SFX agar tidak reload file berulang kali
    private Map<String, AudioClip> sfxCache = new HashMap<>();

    // Konstanta nama file (Pastikan file ini ada di resources)
    // Anda bisa mengganti URL ini dengan file lokal atau URL online
    public static final String BGM_MAIN = "/com/silent/treatment/chainreaction/assets/sounds/bgm.mp3";
    public static final String SFX_CLICK = "/com/silent/treatment/chainreaction/assets/sounds/click.mp3";
    public static final String SFX_EXPLOSION = "/com/silent/treatment/chainreaction/assets/sounds/expload.mp3";
    public static final String SFX_POP = "/com/silent/treatment/chainreaction/assets/sounds/pop.mp3";
    public static final String SFX_WIN = "/com/silent/treatment/chainreaction/assets/sounds/win.mp3";

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Memulai Background Music.
     * @param resourcePath Path ke file audio di resources (dimulai dengan /)
     */
    public void playBGM(String resourcePath) {
        try {
            if (bgmPlayer != null) {
                bgmPlayer.stop();
                bgmPlayer.dispose();
            }

            URL url = getClass().getResource(resourcePath);
            if (url == null) return;

            Media media = new Media(url.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(0.4); 
            
            if (!isBgmMuted) { // Cek flag BGM
                bgmPlayer.play();
            }
            
        } catch (Exception e) {
            System.err.println("Error playing BGM");
        }
    }

    /**
     * Memainkan Sound Effect sekali jalan (pendek).
     * Cocok untuk klik, ledakan, dll.
     */
    public void playSFX(String resourcePath) {
        if (isSfxMuted) return; // Cek flag SFX

        try {
            if (!sfxCache.containsKey(resourcePath)) {
                URL url = getClass().getResource(resourcePath);
                if (url != null) {
                    sfxCache.put(resourcePath, new AudioClip(url.toExternalForm()));
                } else {
                    return;
                }
            }
            sfxCache.get(resourcePath).play();
        } catch (Exception e) {
            // System.err.println("Error playing SFX");
        }
    }

    // [BARU] Toggle khusus BGM
    public void toggleBGM() {
        isBgmMuted = !isBgmMuted;
        if (bgmPlayer != null) {
            if (isBgmMuted) bgmPlayer.pause();
            else bgmPlayer.play();
        }
    }

    // [BARU] Toggle khusus SFX
    public void toggleSFX() {
        isSfxMuted = !isSfxMuted;
    }
    
    public boolean isBgmMuted() { return isBgmMuted; }
    public boolean isSfxMuted() { return isSfxMuted; }

    /**
     * Menghentikan BGM yang sedang berjalan (digunakan saat reset ke Main Menu).
     */
    public void stopBGM() {
        try {
            if (bgmPlayer != null) {
                bgmPlayer.stop();
                bgmPlayer.dispose();
                bgmPlayer = null;
            }
        } catch (Exception e) {
            // ignore error stop BGM
        }
    }
} 