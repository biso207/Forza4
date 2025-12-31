package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyUI;
import sorgente.Main;

public class GameManager extends ScreenAdapter
{
    private static final Log log = LogFactory.getLog(GameManager.class);
    private final GameUI ui;
    private final GameInput input;
    private final Main game;
    private int difficult;


    // costruttore
    public GameManager(Main game, int d, boolean dark)
    {
        this.game = game;
        input = new GameInput();
        ui = new GameUI(game, dark,d);
        difficult=d;
        log.info(difficult);
    }

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
