/*
Forza4 • class GameInput •
Gestisce gli input della schermata di gioco
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Game;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.*;
import sorgente.Lobby.LobbyInput;
import java.util.Timer;
import java.util.TimerTask;

public class GameInput implements InputProcessor {

    private static final Log log = LogFactory.getLog(GameInput.class);

    // Cursor
    private final Pixmap mouse;
    private final Cursor cursor;

    // Flag generali
    protected boolean isBtnExitClicked;
    protected boolean isBtnExitHover;
    protected boolean isHole;

    protected boolean btnNoExit;
    protected boolean btnYesExit;

    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;

    // Power-up flags
    public boolean powerExplosive = false;
    public boolean powerExplosiveBig = false;
    public boolean powerFreeze = false;
    public boolean powerPredict = false;
    public boolean powerTarget=false;
    public boolean powerUndo=false;

    // Swap: prima colonna selezionata
    public int selectedSwapColumn = -1;
    private int mod;

    // Rettangoli UI
    protected final Rectangle exit;
    private Rectangle btn_no;
    private Rectangle btn_yes;

    // Power-up buttons
    public Rectangle btnExplosive;
    public Rectangle btnExplosiveBig;
    public Rectangle btnFreeze;
    public Rectangle btnPredict;
    public Rectangle btnTarget;
    public Rectangle btnUndo;

    // Griglia
    public Rectangle[][] holes = new Rectangle[6][7];
    public Array<Rectangle> allHoles = new Array<>();
    // abilita/disabilita click sulla griglia (serve per bloccare le mosse durante drop/bot)
    private boolean gridEnabled = true;


    public GameInput(int mod) {

        // Cursor
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        this.mod=mod;

        // Exit
        exit = new Rectangle(825, 58, 50, 50);

        // griglia (bottom-left = 261,605; dx=71; dy=61; hole=40)
        final float baseX = 261f; // x1
        final float baseY = 605f; // y1
        final float dx = 69f; // distanza tra colonne
        final float dy = 61f; // distanza tra righe
        final float holeSize = 40f; // dimensione cella

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {

                // row 0 = in basso, row 5 = in alto
                float x = baseX + col * dx;
                float y = baseY + row * dy;

                Rectangle rect = new Rectangle(x, y, holeSize, holeSize);
                holes[row][col] = rect;
                allHoles.add(rect);
            }
        }

        // Replay buttons
        btn_no = new Rectangle(503, 408, 150, 50);
        btn_yes = new Rectangle(341, 408, 150, 50);

        // Power-up buttons (posizioni esempio)

        btnFreeze    = new Rectangle(293, 165, 24, 24);
        btnExplosive = new Rectangle(368, 169, 24, 24);
        btnExplosiveBig = new Rectangle(457, 171, 24, 24);
        btnPredict = new Rectangle(530, 172, 24, 24);
        btnTarget= new  Rectangle(605,172,24,24);
        btnUndo=new Rectangle(689,172,24,24);
    }

    // genera il suono al click
    private boolean clicked() {
        SoundManager.playClickButton(LobbyInput.effectsPercent);
        return true;
    }

    // handles touch input; activates boosts; detects grid clicks
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        System.out.println(x + " " + y);

        checkHitBox(x, y);

        // POWER-UP BUTTONS

        if (btnExplosive.contains(x, y)) {
            activatePowerUp("explosive");

            return true;
        }


        if (btnExplosiveBig.contains(x, y)) {
            activatePowerUp("bigExplosive");
            return true;
        }



        if (btnFreeze.contains(x, y)) {
            activatePowerUp("freeze");
            return true;
        }



        if (btnPredict.contains(x, y))
        {
            activatePowerUp("predict");
            return true;
        }

        if (btnTarget.contains(x, y))
        {
            activatePowerUp("target");
            return true;
        }

        if(btnUndo.contains(x,y))
        {
            activatePowerUp("undo");
            return true;
        }



        // click sulla griglia
         // click sulla griglia
        int col = getColumnFromClick(x, y);
        if (gridEnabled && col != -1)
        {

            isHole = true;
        }




        return false;
    }

    private void activatePowerUp(String type) {

        powerExplosive = false;
        powerExplosiveBig = false;
        powerFreeze = false;
        powerPredict = false;
        powerTarget=false;
        powerUndo=false;

        selectedSwapColumn = -1;

        switch (type) {
            case "explosive":
                powerExplosive = true;
                log.info("Power-up Explosive attivato");
                break;

            case "bigExplosive":
                powerExplosiveBig = true;
                log.info("Power-up Swap attivato");
                break;

            case "freeze":
                powerFreeze = true;
                log.info("Power-up Freeze attivato");
                break;

            case "predict":
                powerPredict = true;
                log.info("Power-up Predict attivato");
                break;

            case "undo":
                powerUndo=true;
                log.info("Power-up Undo attivato");

            case "target":
                powerTarget=true;
                log.info("Power-up Target attivato");
        }
    }

    private void checkHitBox(int x, int y) {

        // todo: al click della X chiedere se chiudere o meno, non interrompere immediatamente
        if (exit.contains(x, y)) {
            clicked(); // genera suono
            isBtnExitClicked = true;

            // todo: rimuovere il thread e ricreare il sistema della lobby
            new Timer().schedule(new TimerTask() {
                @Override public void run() {
                    isBtnExitClicked = false;
                }
            }, 100);
        }

        if (GameUI.isMatchOver) {
            if (btn_no.contains(x, y)) btnNoExit = true;
            if (btn_yes.contains(x, y)) btnYesExit = true;
            SoundManager.playClickButton(LobbyInput.effectsPercent);
        }
    }

    // setter e getter stato click sulla griglia
    public void setGridEnabled(boolean enabled) {
        gridEnabled = enabled;
        if (!enabled) isHole = false; // pulizia: cancella click pendenti
    }
    public boolean isGridEnabled() {
        return gridEnabled;
    }

    @Override
    public boolean mouseMoved(int x, int y) {

        isBtnExitHover = exit.contains(x, y);

        if (GameUI.isMatchOver) {
            isBtnNoExitHover = btn_no.contains(x, y);
            isBtnYesExitHover = btn_yes.contains(x, y);
        }

        return false;
    }

    public int getColumnFromClick(int x, int y) {

        for (int col = 0; col < 7; col++) {
            Rectangle topCell = holes[0][col];
            float colX = topCell.x;
            float colWidth = topCell.width;

            if (x >= colX && x <= colX + colWidth) {
                return col;
            }
        }

        return -1;
    }

    /**
     * Restituisce la riga corrispondente al click.
     * Si aspetta x,y raw (come ricevuti da touchDown / Gdx.input.getX/Y).
     * Usa la conversione interna da origine top-left a bottom-left.
     * Ritorna -1 se il click è fuori dalla griglia o la col passata è invalida.
     */
    public int getRowFromClick(int x, int y) {
        // validazione col


        // converti Y da top-left a bottom-left
        int convertedY = Gdx.graphics.getHeight() - y;

        // parametri della griglia (mantieni coerenti con GameUI)
        final float baseY = 93f;   // stesso baseY usato in GameUI
        final float dy = 61f;      // distanza tra righe
        final float holeSize = 40f; // altezza cella

        // distanza dal bordo inferiore della griglia
        float delta = convertedY - baseY;

        // fuori griglia in basso
        if (delta < 0) return -1;

        // altezza totale coperta dalle 6 righe (dal bordo inferiore alla cima dell'ultima cella)
        float maxHeight = 5 * dy + holeSize;
        if (delta > maxHeight) return -1;

        // quante "step" di dy ci sono sopra baseY
        int step = (int) Math.floor(delta / dy);

        // mappa step -> row (poiché y = baseY + (5 - row) * dy)
        int row = 5 - step;

        // controllo finale
        if (row < 0 || row > 5) return -1;
        return row;
    }





    public int getLowestFreeRow(int col, boolean[][] board) {

        for (int row = 5; row >= 0; row--) {
            if (!board[row][col]) {
                return row;
            }
        }
        return -1;
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
