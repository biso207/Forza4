package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import sorgente.Authentication.AuthAlgorithms;

public class LobbyInput implements InputProcessor
{
    protected boolean classic;
    protected boolean gravity4;
    protected boolean horizontal;
    protected boolean speedy;
    protected boolean market;
    protected boolean scoreboard;
    protected boolean daily;
    protected boolean settings;
    protected boolean information;
    protected boolean exit;
    protected boolean man;

    private final Pixmap mouse;
    private final Cursor cursor;

    public LobbyInput()
    {
      this.classic= false;
      this.gravity4=false;
      this.speedy=false;
      this.market=false;
      this.scoreboard=false;
      this.daily=false;
      this.settings=false;
      this.information=false;
      this.exit=false;
      this.man=false;

      mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));

      cursor = Gdx.graphics.newCursor(mouse, 0, 0);
    }

    public void dispose()
    {
        mouse.dispose();
    }

    public void hitAreas()
    {

    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }
}
