package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class GameInput implements InputProcessor
{
    private static final Log log = LogFactory.getLog(GameInput.class);
    private final Pixmap mouse;
    private final Cursor cursor;

    protected boolean isBtnExitClicked;
    protected boolean isBtnExitHover;

    protected final Rectangle exit;

    public GameInput()
    {
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        exit=new Rectangle(100,199,52,52);

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

    private void checkHitBox(int x, int y)
    {
      if(exit.contains(x,y))
      {
        isBtnExitClicked=true;
        new Timer().schedule(new TimerTask() { @Override public void run() { isBtnExitClicked = false; } }, 100);
      }
    }

    @Override
    public boolean touchDown(int x, int y, int i2, int i3)
    {

        log.info(x+" "+y);

        checkHitBox(x,y);

        return false;
    }

    private void resetHover()
    {

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

    public void dispose() {

    }
}
