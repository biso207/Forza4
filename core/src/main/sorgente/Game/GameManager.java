/*
Forza4 • class GameManager •
Gestisce la grafica e gli input delle schermate di gioco
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Game;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.Lobby.LobbyInput;
import sorgente.Main;

public class GameManager extends ScreenAdapter {
    private static final Log log = LogFactory.getLog(GameManager.class);
    private final GameUI ui;
    private final GameInput input;
    protected static Main game = null;

    protected static Music soundGame;

    // costruttore
    public GameManager(Main game, boolean dark, int mod) {
        GameManager.game = game;
        input = new GameInput();
        ui = new GameUI(input, dark, mod);

        // musica di gioco
        switch (mod) {
            case 0 -> soundGame= Gdx.audio.newMusic(Gdx.files.internal("sounds/game_classic_sound.ogg"));
            case 1 -> soundGame= Gdx.audio.newMusic(Gdx.files.internal("sounds/game_gravity4_sound.ogg"));
            case 2 -> soundGame= Gdx.audio.newMusic(Gdx.files.internal("sounds/game_horizontal_sound.mp3")); // todo: change in .ogg
            case 3 -> soundGame= Gdx.audio.newMusic(Gdx.files.internal("sounds/game_speedy_sound.ogg"));
        }

        soundGame.setLooping(true); // true=loop music; false=no loop
        soundGame.play(); // avvio musica
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        soundGame.setVolume(LobbyInput.musicPercent);

        ui.render(delta);
    }

    @Override
    public void resize(int width, int height)
    {
        ui.resize(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide()
    {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        ui.dispose();
        input.dispose();
    }

}
