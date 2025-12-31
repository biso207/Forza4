package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

public class GameInput implements InputProcessor
{
    private static final Log log = LogFactory.getLog(GameInput.class);
    private final Pixmap mouse;
    private final Cursor cursor;

    protected boolean isBtnExitClicked;
    protected boolean isBtnExitHover;
    protected boolean isHole;

    protected final Rectangle exit;

    // 🔷 Griglia dei buchi
    public Rectangle[][] holes = new Rectangle[6][7];
    public Array<Rectangle> allHoles = new Array<>();

    // 🔷 Dimensioni e offset corretti
    private final float cellWidth  = 80f;
    private final float cellHeight = 700f / 6f;
    private final float holeSize   = 52f;
    private final float offsetX    = (cellWidth - holeSize) / 2f; // ≈ 14
    private final float offsetY    = (cellHeight - holeSize) / 2f;
    private final float gridOffsetX = (1000f - (cellWidth * 7)) / 2f; // ≈ 220



    public GameInput()
    {
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        exit = new Rectangle(840, 93, 52, 52);

        // 🔷 Genera tutti i buchi
        for (int row = 0; row < 6; row++)
        {
            for (int col = 0; col < 7; col++)
            {
                float x = gridOffsetX + col * cellWidth + offsetX;
                float y = 700f - ((row + 1) * cellHeight) + offsetY;

                Rectangle rect = new Rectangle(x, y, holeSize, holeSize);
                holes[row][col] = rect;
                allHoles.add(rect);
            }
        }
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button)
    {

        log.info("Touch at: " + x + ", " + y);
        checkHitBox(x, y);

        // 🔷 Rileva colonna cliccata
        int col = getColumnFromClick(x, y);

        if (col != -1)
        {
            log.info("Hai cliccato la colonna " + col);
            isHole=true;
        }

        return false;
    }

    private void checkHitBox(int x, int y)
    {
        if (exit.contains(x, y))
        {
            log.info("Hai cliccato il bottone di uscita");
            isBtnExitClicked = true;

            new Timer().schedule(new TimerTask() {
                @Override public void run() {
                    isBtnExitClicked = false;
                }
            }, 100);
        }
    }

    @Override
    public boolean mouseMoved(int x, int y)
    {
        isBtnExitHover = exit.contains(x, y);
        return false;
    }


    public int getColumnFromClick(int x, int y)
    {
        for (int col = 0; col < 7; col++) {
            Rectangle topCell = holes[0][col]; // tutte le celle della colonna hanno la stessa x
            float colX = topCell.x;
            float colWidth = topCell.width;

            if (x >= colX && x <= colX + colWidth)
            {
                return col;
            }
        }
        return -1;
    }




    // 🔷 Ritorna la prima riga libera in una colonna (dal basso)
    public int getLowestFreeRow(int col, boolean[][] board)
    {
        for (int row = 5; row >= 0; row--)
        {
            if (!board[row][col])
            {
                return row;
            }
        }
        return -1; // colonna piena
    }

    @Override public boolean keyDown(int i) { return false; }
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchUp(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int i, int i1, int i2) { return false; }
    @Override public boolean scrolled(float v, float v1) { return false; }

    public void dispose() {
        mouse.dispose();
    }
}
