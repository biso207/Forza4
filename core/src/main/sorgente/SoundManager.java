package sorgente;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;

public class SoundManager
{


        private final float volume;
        private final HashMap<String, Long> lastPlayedTimeMap;

        private final Sound shotSound, hitSound, creditSound, completedSound, winSound, defeatSound;
        private static final Sound clickButtonSound = Gdx.audio.newSound(Gdx.files.internal("sounds/click_button.wav"));
        private static final Sound digitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/digit.wav"));

        // intervallo minimo (in ms) tra due riproduzioni dello stesso suono
        private static final long MIN_INTERVAL_SHOT = 50;  // 20 volte al secondo
        private static final long MIN_INTERVAL_HIT = 100;  // 10 volte al secondo

        // costruttore
    public SoundManager(float volume) {
        this.volume = volume;
        this.lastPlayedTimeMap = new HashMap<>();

        // caricamento suoni
        shotSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shot_sound.mp3"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit_sound.wav"));
        creditSound = Gdx.audio.newSound(Gdx.files.internal("sounds/credit_sound.wav"));
        completedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/completed_Missions.mp3"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("sounds/victory.wav"));
        defeatSound = Gdx.audio.newSound(Gdx.files.internal("sounds/game_over.wav"));
    }

        /** Metodi con controllo di "throttle" **/
        // metodo per riprodurre il suono di sparo
        public void playLaser() {
        playThrottled("shot", shotSound, MIN_INTERVAL_SHOT);
    }

        // metodo per riprodurre il suono della collisione
        public void playHit() {
        playThrottled("hit", hitSound, MIN_INTERVAL_HIT);
    }

        /** Suoni rari, li riproduciamo sempre **/
        // metodo per il suono dei crediti
        public void playCreditEarned() {
        creditSound.play(volume);
    }

        // metodo per il suono di completamento della missione Missions
        public void playCompletedMissions() {
        completedSound.play(volume);
    }

        // metodo per il suono di vittoria di una partita
        public void playWin() {
        winSound.play(volume);
    }

        // metodo per il suono della sconfitta di una partita
        public void playDefeat() {
        defeatSound.play(volume);
    }

        // metodo per riprodurre il click dei pulsanti
        public static void playClickButton(float volume) {
        clickButtonSound.play(volume);
    }

        // metodo per riprodurre il suono della digitazione
        public static void playDigitSound(float volume) {
        digitSound.play(volume);
    }

        private void playThrottled(String key, Sound sound, long minIntervalMillis) {
        long now = System.currentTimeMillis();
        Long lastPlayed = lastPlayedTimeMap.getOrDefault(key, 0L);

        if (now - lastPlayed >= minIntervalMillis) {
            sound.play(volume);
            lastPlayedTimeMap.put(key, now);
        }
    }

        public void dispose() {
        shotSound.dispose();
        hitSound.dispose();
        creditSound.dispose();
        completedSound.dispose();
        winSound.dispose();
        defeatSound.dispose();
    }


}
