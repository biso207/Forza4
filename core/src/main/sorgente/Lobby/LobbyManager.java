/*
Forza4 • class LobbyManager •
Gestisce la grafica e gli input della lobby
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthManager;
import sorgente.Game.GameManager;
import sorgente.Main;
import sorgente.UserData.UserProgressService;

public class LobbyManager extends ScreenAdapter {
    // istanze grafica e input
    private final LobbyUI ui;
    private final LobbyInput input;

    protected static Main game; // variabile di riferimento tipo gioco

    // dichiarazione screen
    private final SpriteBatch screen;
    protected static Music soundtrack;

    // costruttore
    public LobbyManager(Main game) {
        LobbyManager.game = game;

        input = new LobbyInput();
        ui = new LobbyUI(game, input);

        // init dello screen
        this.screen = game.screen;
    }

    // "interruttore" per l'input
    private void setLobbyInputEnabled(boolean enabled) {
        input.setInputEnabled(enabled);
        Gdx.input.setInputProcessor(enabled ? input : null);
    }

    // ====================== //
    //   METODI DELLO SCREEN  //
    // ====================== //

    @Override
    public void render(float delta) {
        input.update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ui.lobbyRender(delta);
    }


    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
        input.setInputEnabled(true);
    }

    @Override
    public void hide() { setLobbyInputEnabled(false); }

    @Override
    public void dispose() {
        setLobbyInputEnabled(false);
        ui.disposeUI();
        input.dispose();
    }
}
