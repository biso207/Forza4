package sorgente.Lobby;

import com.badlogic.gdx.ScreenAdapter;

public class LobbyManager extends ScreenAdapter {
    private LobbyUI ui;
    private LobbyInput input;

    // costruttore
    LobbyManager() {
        input = new LobbyInput();
        ui = new LobbyUI();
    }

    // ====================== //
    //   METODI DELLO SCREEN  //
    // ====================== //

    @Override
    public void render(float delta) {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
