package sorgente.Lobby;

import com.badlogic.gdx.math.MathUtils;
import sorgente.SoundManager;

public class AudioSettings {

    public static float musicVolume = 1.0f;     // 0 → muto, 1 → 100%
    public static float effectsVolume = 1.0f;


    public static SoundManager sound = new SoundManager(100);// 0 → muto, 1 → 100%

    public AudioSettings() {
        SoundManager.playLobby(100);
    }

    public static void setMusicVolume(float value) {
        musicVolume = MathUtils.clamp(value, 0f, 1f);
        // Se hai un MusicManager, aggiorna la musica in tempo reale
        SoundManager.setMusicVolume(musicVolume);
    }

    public static void setEffectsVolume(float value) {
        effectsVolume = MathUtils.clamp(value, 0f, 1f);
        sound.setEffectsVolume(effectsVolume);
    }

}
