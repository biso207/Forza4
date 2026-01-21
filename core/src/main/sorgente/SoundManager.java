/*
Forza4 • class SoundManager •
Gestisce i suoni in gioco
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private static float volume;
    private final Sound creditSound;
    private final Sound completedSound;
    private static final Sound winSound=Gdx.audio.newSound(Gdx.files.internal("sounds/victory.wav"));
    private static final Sound defeatSound=Gdx.audio.newSound(Gdx.files.internal("sounds/game_over.wav"));
    private static final Sound clickButtonSound = Gdx.audio.newSound(Gdx.files.internal("sounds/click_button.wav"));
    private static final Sound digitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/digit.wav"));
    private static final Sound landSound = Gdx.audio.newSound(Gdx.files.internal("sounds/token_land.ogg"));
    private static final Sound top = Gdx.audio.newSound(Gdx.files.internal("sounds/top.mp3"));
    private static final Sound right = Gdx.audio.newSound(Gdx.files.internal("sounds/right.mp3"));
    private static final Sound left = Gdx.audio.newSound(Gdx.files.internal("sounds/left.mp3"));
    private static final Sound power = Gdx.audio.newSound(Gdx.files.internal("sounds/power.mp3"));
    private static final Sound buy = Gdx.audio.newSound(Gdx.files.internal("sounds/buy.mp3"));

    // costruttore
    public SoundManager(float volume) {
        this.volume = volume;

        // caricamento suoni
        creditSound = Gdx.audio.newSound(Gdx.files.internal("sounds/credit_sound.wav"));
        completedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/completed_Missions.mp3"));
    }

    public static void playError(float volume)
    {
        defeatSound.play(volume);
    }

    public static void playGravityTop(float volume)
    {
        top.play(volume);
    }

    public static void playGravityRight(float volume)
    {
        right.play(volume);
    }

    public static void playGravityLeft(float volume)
    {
        left.play(volume);
    }

    public static void playPowerUp(float volume)
    {
        power.play(volume);
    }

    public static void playPurchase(float volume)
    {
        buy.play(volume);
    }

    /** Cambia il volume degli EFFETTI in tempo reale */
    public void setEffectsVolume(float value) { volume = value; }

    /** Suoni rari, li riproduciamo sempre **/
    // metodo per il suono dei crediti
    public void playCreditEarned() { creditSound.play(volume); }

    // metodo per il suono di completamento della missione Missions
    public void playCompletedMissions() { completedSound.play(volume); }

    // metodo per il suono di vittoria di una partita
    public static void playWin(float volume) { winSound.play(volume); }

    // metodo per il suono della sconfitta di una partita
    public static void  playDefeat(float volume) { defeatSound.play(volume); }

    // metodo per riprodurre il click dei pulsanti
    public static void playClickButton(float volume) { clickButtonSound.play(volume); }

    // metodo per riprodurre il suono della digitazione
    public static void playDigitSound(float volume) { digitSound.play(volume); }

    // metodo per riprodurre il suono della pedina atterrata
    public static void playLand(float volume) { landSound.play(volume); }

    public void dispose() {
        creditSound.dispose();
        completedSound.dispose();
        winSound.dispose();
        defeatSound.dispose();
        landSound.dispose();
    }


}
