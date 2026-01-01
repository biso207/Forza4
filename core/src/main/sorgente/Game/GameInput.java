package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.SoundManager;

import java.util.Timer;
import java.util.TimerTask;



public class GameInput implements InputProcessor
{

    private static final Log log = LogFactory.getLog(GameInput.class);


    // 🔽 Cursor personalizzato
    private final Pixmap mouse;
    private final Cursor cursor;

    // 🔽 Flag di stato per input
    protected boolean isBtnExitClicked;
    protected boolean isBtnExitHover;
    protected boolean isHole;

    // 🔽 Rettangolo del bottone di uscita
    protected final Rectangle exit;

    // 🔽 Griglia dei buchi (6 righe x 7 colonne)
    public Rectangle[][] holes = new Rectangle[6][7];
    public Array<Rectangle> allHoles = new Array<>();

    // 🔽 Dimensioni e posizionamento della griglia
    private final float cellWidth = 80f;                     // larghezza di ogni cella
    private final float cellHeight = 700f / 6f;              // altezza di ogni cella (≈116.66)
    private final float holeSize = 52f;                      // dimensione del buco (diametro)
    private final float offsetX = (cellWidth - holeSize) / 2f; // centratura orizzontale del buco nella cella
    private final float offsetY = (cellHeight - holeSize) / 2f; // centratura verticale del buco nella cella
    private final float gridOffsetX = (1000f - (cellWidth * 7)) / 2f; // centratura orizzontale dell’intera griglia

    // 🔽 Costruttore
    public GameInput()
    {

        // Carica e imposta il cursore personalizzato
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        // Definisce l’area cliccabile del bottone di uscita
        exit = new Rectangle(840, 93, 52, 52);

        // Genera la griglia dei buchi con posizionamento centrato
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

    // 🔽 Gestione click del mouse
    @Override
    public boolean touchDown(int x, int y, int pointer, int button)
    {
        log.info("Touch at: " + x + ", " + y);

        // Controlla se è stato cliccato il bottone di uscita
        checkHitBox(x, y);

        // Rileva la colonna cliccata
        int col = getColumnFromClick(x, y);
        if (col != -1) {
            log.info("Hai cliccato la colonna " + col);

            isHole = true;
        }

        return false;
    }

    // 🔽 Controlla se il click è avvenuto sul bottone di uscita
    private void checkHitBox(int x, int y) {
        if (exit.contains(x, y)) {
            log.info("Hai cliccato il bottone di uscita");
            isBtnExitClicked = true;


            // Reset del flag dopo 100ms per effetto visivo
            new Timer().schedule(new TimerTask() {
                @Override public void run() {
                    isBtnExitClicked = false;
                }
            }, 100);
        }
    }

    // 🔽 Gestione hover del mouse sul bottone di uscita
    @Override
    public boolean mouseMoved(int x, int y) {
        isBtnExitHover = exit.contains(x, y);
        return false;
    }

    // 🔽 Rileva la colonna cliccata in base alla posizione X
    public int getColumnFromClick(int x, int y) {
        for (int col = 0; col < 7; col++) {
            Rectangle topCell = holes[0][col]; // tutte le celle della colonna hanno la stessa X
            float colX = topCell.x;
            float colWidth = topCell.width;

            if (x >= colX && x <= colX + colWidth) {
                return col;
            }
        }
        return -1; // nessuna colonna cliccata
    }

    // 🔽 Ritorna la prima riga libera in una colonna (dal basso verso l’alto)
    public int getLowestFreeRow(int col, boolean[][] board) {
        for (int row = 5; row >= 0; row--) {
            if (!board[row][col]) {
                return row;
            }
        }
        return -1; // colonna piena
    }

    // 🔽 Metodi inutilizzati ma richiesti da InputProcessor
    @Override public boolean keyDown(int i) { return false; }
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchUp(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int i, int i1, int i2) { return false; }
    @Override public boolean scrolled(float v, float v1) { return false; }

    // 🔽 Libera le risorse del cursore
    public void dispose() {
        mouse.dispose();
    }
}
