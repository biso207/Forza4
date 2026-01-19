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

import java.io.IOException;

public class LobbyManager extends ScreenAdapter {
    // istanze grafica e input
    private final LobbyUI ui;
    private final LobbyInput input;

    protected static Main game; // variabile di riferimento tipo gioco

    protected static Music soundtrack;

    // costruttore
    public LobbyManager(Main game) {
        LobbyManager.game = game;

        input = new LobbyInput();
        ui = new LobbyUI(input);

        // musica di sottofondo
        soundtrack = Gdx.audio.newMusic(Gdx.files.internal("sounds/lobby_sound.mp3")); // file audio
        soundtrack.setLooping(true); // true=loop music; false=no loop
        soundtrack.play(); // avvio musica
    }

    // ====================== //
    //   METODI DELLO SCREEN  //
    // ====================== //

    @Override
    public void render(float delta) {
        // setting dell'input
        Gdx.input.setInputProcessor(input);

        /*
        setting del volume di sottofondo
        fondamentale che sia qui perché in caso di cambiamento durante una sessione di gioco
        il volume deve cambiare dinamicamente
        */
        soundtrack.setVolume(LobbyInput.musicPercent); // volume musica

        // update per variabili temporanee di input
        try {
            input.update(delta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render grafico
        try {
            ui.lobbyRender(delta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        ui.disposeUI();
    }
}
