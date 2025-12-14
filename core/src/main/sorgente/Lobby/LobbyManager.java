package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import sorgente.Main;

public class LobbyManager extends ScreenAdapter {
    private final LobbyUI ui;
    private final LobbyInput input;
    private final Main game;
    protected static Music soundtrack;


    // costruttore
    public LobbyManager(Main game)
    {
        this.game = game;
        input = new LobbyInput();
        ui = new LobbyUI(game);

        soundtrack = Gdx.audio.newMusic(Gdx.files.internal("sounds/lobby_sound.ogg")); // file audio
        soundtrack.setLooping(true); // true=loop music; false=no loop
        soundtrack.play(); // avvio musica

    }

    // ====================== //
    //   METODI DELLO SCREEN  //
    // ====================== //

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ui.render(delta);
    }

    @Override
    public void resize(int width, int height)
    {
        ui.resize(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose()
    {
        ui.dispose();
        input.dispose();
    }
}
