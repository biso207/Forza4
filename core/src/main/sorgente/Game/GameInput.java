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
import sorgente.UserData.UserProgressService;


public class GameInput implements InputProcessor {

    private static final Log log = LogFactory.getLog(GameInput.class);

    // Cursor
    private final Pixmap mouse;
    private final Cursor cursor;

    // Flag generali
    protected boolean isBtnExitClicked;
    protected boolean isBtnExitHover;
    protected boolean isHole;
    // yes/no quit e riavvio partita
    protected boolean btnNoExit;
    protected boolean btnYesExit;
    // hover pulsante X quit
    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;

    // flag per la schermata di quit
    protected static boolean isQuitOpen;

    // --- delay & transition (no threads) ---
    // visual click feedback timer (resets clicked flags after a short delay)
    private float clickedTimer = 0f;

    // delayed actions (used for restart)
    private boolean pendingAction = false;
    private float pendingDelay = 0f;
    private int pendingAct = -1;

    // block inputs during short transitions
    private boolean inputEnabled = true;

    // actions
    private static final int ACT_RESTART_GAME = 1;
    private static final int ACT_EXIT_TO_LOBBY = 2;
    private static final int ACT_CLOSE_QUIT_CONTINUE = 3;

    // requests consumed by GameUI
    private boolean requestExitToLobby = false;
    private boolean requestRestartGame = false;

    // Power-up flags
    public boolean powerTokenCracker = false;
    public boolean powerRowBreaker = false;
    public boolean powerFreeze = false;
    public boolean powerPeek = false;
    public boolean powerPrecision=false;
    public boolean powerUndo=false;

    // Swap: prima colonna selezionata
    public int selectedSwapColumn = -1;

    // Rettangoli UI
    private final Rectangle exit, btn_no, btn_yes;

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
    private final boolean powerUpsEnabled;

    // costruttore
    public GameInput(int mod) {
        // cursor
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        powerUpsEnabled = mod != 1;

        // Exit
        exit = new Rectangle(892, 58, 50, 50);

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
        btnFreeze       = new Rectangle(281,150,50,50);
        btnExplosive    = new Rectangle(356,150,50,50);
        btnExplosiveBig = new Rectangle(431,150,50,50);
        btnPredict      = new Rectangle(506,150,50,50);
        btnTarget       = new Rectangle(581,150,50,50);
        btnUndo         = new Rectangle(656,150,50,50);

        isQuitOpen=false;
    }

    // reset boosters
    public void resetAllPowers() {
        powerTokenCracker = false;
        powerRowBreaker = false;
        powerFreeze = false;
        powerPeek = false;
        powerPrecision = false;
        powerUndo = false;
    }

    // schedule a delayed action (used to add a short delay after a click, like in LobbyInput)
    private void scheduleAction() {
        scheduleAction(ACT_RESTART_GAME);
    }

    void scheduleAction(int act) {
        pendingAction = true;
        pendingAct = act;
        pendingDelay = 0.2f;
    }

    // update timers (call once per frame from gameui.render)
    public void update(float delta) {
        // timer for turning off "clicked" visuals
        if (clickedTimer > 0f) {
            clickedTimer -= delta;
            if (clickedTimer <= 0f) resetClickedFlags();
        }

        // timer for delayed actions
        if (pendingAction) {
            pendingDelay -= delta;
            if (pendingDelay <= 0f) {
                pendingAction = false;
                executePendingAction(pendingAct);
                pendingAct = -1;
            }
        }
    }

    private void executePendingAction(int act) {
        // re-enable inputs (we're still on the same screen unless GameUI switches)
        setInputEnabled(true);

        if (act == ACT_RESTART_GAME) {
            requestRestartGame = true;
            return;
        }

        if (act == ACT_EXIT_TO_LOBBY) {
            requestExitToLobby = true;
            return;
        }

        if (act == ACT_CLOSE_QUIT_CONTINUE) {
            isQuitOpen = false;
            requestExitToLobby = false;
            setGridEnabled(true);
        }
    }

    private void resetClickedFlags() {
        isBtnExitClicked = false;
        btnNoExit = false;
        btnYesExit = false;
    }

    public void setInputEnabled(boolean enabled) {
        inputEnabled = enabled;
    }

    // one-shot consumable requests (GameUI should call these)
    public boolean consumeExitToLobbyRequested() {
        if (requestExitToLobby) {
            requestExitToLobby = false;
            return true;
        }
        return false;
    }

    public boolean consumeRestartRequested() {
        if (requestRestartGame) {
            requestRestartGame = false;
            return true;
        }
        return false;
    }

    boolean clicked() {
        SoundManager.playClickButton(LobbyInput.effectsPercent);
        return true;
    }

    // handles touch input; activates boosts; detects grid clicks
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        // block any input during short transitions (click delay / screen transition)
        if (!inputEnabled) return false;

        //System.out.println(x + " " + y);

        // ui buttons (exit / match over yes-no)
        if (handleUiClick(x, y)) return true;

        // POWER-UP BUTTONS //
        if (btnFreeze.contains(x, y)) {
            activatePowerUp("freeze");
            return clicked();
        }

        if (btnExplosive.contains(x, y)) {
            activatePowerUp("tokenCracker");
            return clicked();
        }

        if (btnExplosiveBig.contains(x, y)) {
            activatePowerUp("rowBreaker");
            return clicked();
        }

        if (btnPredict.contains(x, y)) {
            activatePowerUp("peek");
            return clicked();
        }

        if (btnTarget.contains(x, y)) {
            activatePowerUp("precision");
            return clicked();
        }

        if(btnUndo.contains(x,y)) {
            activatePowerUp("undo");
            return clicked();
        }

        // click sulla griglia
        int col = getColumnFromClick(x, y);
        if (gridEnabled && col != -1) isHole = true;

        return true;
    }



    private void activatePowerUp(String type) {

        if (!powerUpsEnabled) { log.info("Power-up disabilitati in Gravity3"); return; }

        // --- TOGGLE: se il potere era già attivo, disattivalo e basta ---
        switch (type) {
            case "freeze":
                if (powerFreeze) { powerFreeze = false;  log.info("disattivato"); return; }
                break;
            case "tokenCracker":
                if (powerTokenCracker) { powerTokenCracker = false; return; }
                break;
            case "rowBreaker":
                if (powerRowBreaker) { powerRowBreaker = false; return; }
                break;
            case "peek":
                if (powerPeek) { powerPeek = false; return; }
                break;
            case "precision":
                if (powerPrecision) { powerPrecision = false; return; }
                break;
            case "undo":
                if (powerUndo) { powerUndo = false; return; }
                break;
        }

        // --- Se non era attivo → disattiva tutti gli altri ---
        resetAllPowers();
        selectedSwapColumn = -1;

        // recupero numero power up in possesso
        int numFreezer      = (int) UserProgressService.getProgress("num_freezer");
        int numTokenCracker = (int) UserProgressService.getProgress("num_token_cracker");
        int numRowBraker    = (int) UserProgressService.getProgress("num_row_breaker");
        int numPeek         = (int) UserProgressService.getProgress("num_peek");
        int numPrecision    = (int) UserProgressService.getProgress("num_precision");
        int numUndo         = (int) UserProgressService.getProgress("num_undo");

        // attivazione
        switch (type) {
            case "freeze":
                if (numFreezer >= 1) {
                    powerFreeze = true;
                    log.info("Power-up Freeze attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            case "tokenCracker":
                if (numTokenCracker >= 1 && GameUI.numTokensBot >= 1) {
                    powerTokenCracker = true;
                    log.info("Power-up Explosive attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            case "rowBreaker":
                if (numRowBraker >= 1) {
                    powerRowBreaker = true;
                    log.info("Power-up RowBreaker attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            case "peek":
                if (numPeek >= 1) {
                    powerPeek = true;
                    log.info("Power-up Peek attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            case "precision":
                if (numPrecision >= 1) {
                    powerPrecision = true;
                    log.info("Power-up Precision attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            case "undo":
                if (numUndo >= 1) {
                    powerUndo = true;
                    log.info("Power-up Undo attivato");
                    SoundManager.playPowerUp(LobbyInput.effectsPercent);
                }
                break;

            default:
                SoundManager.playError(LobbyInput.effectsPercent);
        }
    }

    private boolean handleUiClick(int x, int y) {

        // exit button (top-right) -> opens QUIT modal
        if (exit.contains(x, y) && !isQuitOpen && !GameUI.isMatchOver) {
            isBtnExitClicked = true;
            clickedTimer = 0.15f;

            isQuitOpen = true;

            // modal: block grid + any other click targets (handled by swallowing below)
            setGridEnabled(false);
            return clicked();
        }

        // modal dialogs (match over OR quit): only YES/NO are clickable, everything else is ignored
        if (GameUI.isMatchOver || isQuitOpen) {

            // CLICK NO
            if (btn_no.contains(x, y)) {
                btnNoExit = true;
                clickedTimer = 0.15f;

                // block inputs during delay
                setInputEnabled(false);

                if (isQuitOpen) {
                    // QUIT: NO = continue (close modal after delay)
                    scheduleAction(ACT_CLOSE_QUIT_CONTINUE);
                } else {
                    // MATCH OVER: NO = back to lobby
                    scheduleAction(ACT_EXIT_TO_LOBBY);
                }

                return clicked();
            }

            // CLICK YES
            if (btn_yes.contains(x, y)) {
                btnYesExit = true;
                clickedTimer = 0.15f;

                // block inputs during delay
                setInputEnabled(false);

                if (isQuitOpen) {
                    // QUIT: YES = back to lobby
                    scheduleAction(ACT_EXIT_TO_LOBBY);
                } else {
                    // MATCH OVER: YES = restart
                    scheduleAction(ACT_RESTART_GAME);
                }

                return clicked();
            }

            // swallow any other click while modal is open
            return true;
        }

        return false;
    }

    // setter e getter stato click sulla griglia
    public void setGridEnabled(boolean enabled) {
        gridEnabled = enabled;
        if (!enabled) isHole = false; // pulizia: cancella click pendenti
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        isBtnNoExitHover=isBtnYesExitHover=false;

        isBtnExitHover = exit.contains(x, y) && !isQuitOpen && !GameUI.isMatchOver;

        if (GameUI.isMatchOver || isQuitOpen) {
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
