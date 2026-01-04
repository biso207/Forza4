package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyManager;
import sorgente.Lobby.LobbyUI;
import sorgente.Main;

public class GameManager extends ScreenAdapter
{
    private static final Log log = LogFactory.getLog(GameManager.class);
    private final GameUI ui;
    private final GameInput input;
    private final Main game;

    protected static Music soundGame;



    // costruttore
    public GameManager(Main game, int d, boolean dark, int mod)
    {
        this.game = game;
        input = new GameInput(mod);
        ui = new GameUI(game,input, dark,d,mod);


        //log.info(d);

        soundGame= Gdx.audio.newMusic(Gdx.files.internal("sounds/game.mp3")); // file audio
        soundGame.setLooping(true); // true=loop music; false=no loop
        soundGame.play(); // avvio musica
    }

    @Override
    public void render(float delta)
    {
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
    public void show() {
    }

    @Override
    public void hide()
    {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose()
    {
        ui.dispose();
        input.dispose();
    }

}
