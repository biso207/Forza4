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
    public boolean powerSwap = false;
    public boolean powerFreeze = false;
    public boolean powerWild = false;

    // Swap: prima colonna selezionata
    public int selectedSwapColumn = -1;

    // Rettangoli UI
    protected final Rectangle exit;
    private Rectangle btn_no;
    private Rectangle btn_yes;

    // Power-up buttons
    public Rectangle btnExplosive;
    public Rectangle btnSwap;
    public Rectangle btnFreeze;
    public Rectangle btnWild;

    // Griglia
    public Rectangle[][] holes = new Rectangle[6][7];
    public Array<Rectangle> allHoles = new Array<>();

    // Dimensioni
    private final float cellWidth = 80f;
    private final float cellHeight = 700f / 6f;
    private final float holeSize = 52f;
    private final float offsetX = (cellWidth - holeSize) / 2f;
    private final float offsetY = (cellHeight - holeSize) / 2f;
    private final float gridOffsetX = (1000f - (cellWidth * 7)) / 2f;

    public GameInput(int mod) {

        // Cursor
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        // Exit
        exit = new Rectangle(840, 93, 52, 52);

        // Griglia
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                float x = gridOffsetX + col * cellWidth + offsetX;
                float y = 700f - ((row + 1) * cellHeight) + offsetY;

                Rectangle rect = new Rectangle(x, y, holeSize, holeSize);
                holes[row][col] = rect;
                allHoles.add(rect);
            }
        }

        // Replay buttons
        btn_no = new Rectangle(503, 408, 150, 50);
        btn_yes = new Rectangle(341, 408, 150, 50);

        // Power-up buttons (posizioni esempio)
        btnExplosive = new Rectangle(50, 600, 64, 64);
        btnSwap      = new Rectangle(130, 600, 64, 64);
        btnFreeze    = new Rectangle(210, 600, 64, 64);
        btnWild      = new Rectangle(290, 600, 64, 64);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        log.info("Touch at: " + x + ", " + y);

        checkHitBox(x, y);

        // POWER-UP BUTTONS
        if (btnExplosive.contains(x, y)) {
            activatePowerUp("explosive");
            return true;
        }

        if (btnSwap.contains(x, y)) {
            activatePowerUp("swap");
            return true;
        }

        if (btnFreeze.contains(x, y)) {
            activatePowerUp("freeze");
            return true;
        }

        if (btnWild.contains(x, y)) {
            activatePowerUp("wild");
            return true;
        }

        // Click sulla griglia
        int col = getColumnFromClick(x, y);
        if (col != -1) {
            isHole = true;
        }

        return false;
    }

    private void activatePowerUp(String type) {

        powerExplosive = false;
        powerSwap = false;
        powerFreeze = false;
        powerWild = false;

        selectedSwapColumn = -1;

        switch (type) {
            case "explosive":
                powerExplosive = true;
                log.info("Power-up Explosive attivato");
                break;

            case "swap":
                powerSwap = true;
                log.info("Power-up Swap attivato");
                break;

            case "freeze":
                powerFreeze = true;
                log.info("Power-up Freeze attivato");
                break;

            case "wild":
                powerWild = true;
                log.info("Power-up Wild attivato");
                break;
        }
    }

    private void checkHitBox(int x, int y) {

        if (exit.contains(x, y)) {
            isBtnExitClicked = true;

            new Timer().schedule(new TimerTask() {
                @Override public void run() {
                    isBtnExitClicked = false;
                }
            }, 100);
        }

        if (GameUI.rigioca) {

            if (btn_no.contains(x, y)) {
                btnNoExit = true;
            }

            if (btn_yes.contains(x, y)) {
                btnYesExit = true;
            }
        }
    }

    @Override
    public boolean mouseMoved(int x, int y) {

        isBtnExitHover = exit.contains(x, y);

        if (GameUI.rigioca) {
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
