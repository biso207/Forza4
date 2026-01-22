/*
Forza4 • class GameUI •
Gestisce la grafica delle schermate di gioco
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Game;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.*;
import sorgente.Authentication.AuthAlgorithms;
import sorgente.Lobby.DailyChallenges;
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyManager;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.UserProgressService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameUI extends ScreenAdapter implements ResourceLoader
{
    private static final Log log = LogFactory.getLog(GameUI.class);
    private final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final GameInput gameInput;
    private final CPUBrain CPUBrain;

    private boolean darkMode;
    protected static int mod; // modalità di gioco

    private float modeTransitionTimer = 0f;
    private boolean modeTransition = false;
    static boolean isMatchOver, victory;

    private int pendingMode = -1;

    // Light Texture
    private Texture lightGameBG;
    private Texture lightTable;
    private Texture lightCells;
    private Texture lightExit;
    private Texture lightExitHover;
    private Texture lightReplay;

    // Dark Texture
    private Texture darkGameBG;
    private Texture darkTable;
    private Texture darkCells;
    private Texture darkExit;
    private Texture darkExitHover;
    private Texture darkReplay;

    // Editable Texture
    private Texture gameBG;
    private Texture table;
    private Texture cells;
    // X quit partita
    private Texture exit;
    private Texture exitHover;
    private Texture replay;
    // pedine
    private Texture red;
    private Texture yellow;
    // yes/no riavvio partita
    private Texture btn_no;
    private Texture btn_yes;
    private Texture btn_no_clicked;
    private Texture btn_yes_clicked;
    // stella difficoltà
    private Texture starDifficulty;
    private float timer;

    // --- DROP ANIMATION ---
    private enum DropDir { TOP, BOTTOM, RIGHT, LEFT }

    // Nuova fase animazione (caduta -> rimbalzo)
    private enum DropPhase { FALLING, BOUNCING }
    private DropPhase dropPhase = DropPhase.FALLING;

    // Parametri rimbalzo (tuning)
    private static final float BOUNCE_DURATION = 0.18f;   // secondi totali del rimbalzo
    private static final float BOUNCE_AMPLITUDE = 10f;    // pixel di rimbalzo (piccolo)
    private float bounceTimer = 0f;

    private boolean dropActive = false;
    private DropDir dropDir = DropDir.TOP;
    private int dropPlayer = 0;     // 1 = rosso, 2 = giallo
    private int dropRow = -1, dropCol = -1;
    private float dropX = 0f, dropY = 0f;
    private float dropTargetX = 0f, dropTargetY = 0f;
    private boolean isPredictDrop = false;

    // velocità animazione (px/sec) -> regola se troppo lenta/veloce
    private static final float DROP_SPEED = 600f;

    // --- GRAVITY4: ordine direzioni per OGNI mossa ---
    public static int gravityStep = 0; // 0 TOP, 1 BOTTOM, 2 RIGHT, 3 LEFT

    private int lastUserRow = -1;
    private int lastUserCol = -1;



    //--PREDICT: timer pedina fantasma
    private float predictTimer = 0f;
    private static final float PREDICT_DURATION = 4f; // 2 secondi


    // --- BOT THINK DELAY ---
    private boolean botPending = false;
    private float botDelayTimer = 0f;
    private int botPlannedRow = -1, botPlannedCol = -1;
    private static final float BOT_THINK_DELAY = 0.65f; // delay tra mossa utente e mossa bot CPU

    // Stato della griglia: 0 = vuoto, 1 = rosso / 2 = giallo
    private final int[][] boardState = new int[6][7];

    // Freeze power-up
    private int freezeColumn = -1;
    private int freezeTurns = 0;

    private int predictRow = -1;
    private int predictCol = -1;

    private boolean predictForcedMove = false;


    private int[][] lastBoardState = new int[6][7];
    private int lastGravityStep = 0;
    private int lastFreezeColumn = -1;
    private int lastFreezeTurns = 0;
    private int lastNumTokensBot = 0;

    private final String[] items = {"num_freezer", "num_token_cracker", "num_row_breaker", "num_peek", "num_precision", "num_undo"};

    // pedine utente e bot giocate
    protected static int numTokensUser=0, numTokensBot=0;
    // difficoltà di gioco, punti e crediti per partita
    private int gameDifficulty, points, credits;
    // nome modalità di gioco
    private String modName;
    // lettura punti e crediti utente
    private int punti, crediti;

    // flag per controllare l'utilizzo di un booster
    private boolean usedAnyBoostThisMatch = false;

    // costruttore
    public GameUI (GameInput in, boolean dark, int mod) {
        this.screen = GameManager.game.screen;
        GameUI.mod = mod;

        isMatchOver=victory=false;

        Fonts.load();
        darkMode = dark;
        gameInput = in;
        loadImages();


        // inizializza boardState
        for (int r = 0; r < 6; r++)
        {
            for (int c = 0; c < 7; c++) boardState[r][c] = 0;
        }

        // variabili basate sulla modalità di gioco
        switch(mod)
        {
            case 0 ->
            {
                // nome modalità
                modName = "CLASSIC";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_classic");
            }

            case 1 ->
            {
                // nome modalità
                modName = "GRAVITY4";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_gravity4");
            }

            case 2 ->
            {
                // nome modalità
                modName = "HORIZONTAL";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_horizontal");
            }

            case 3 ->
            {
                // nome modalità
                modName = "SPEEDY";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_speedy");
            }

        }

        CPUBrain = new CPUBrain(gameDifficulty, 2, mod);

        System.out.println(gameDifficulty);
        switch(gameDifficulty) {
            case 0 -> {
                points = 20;
                credits = 2;
            }
            case 1 -> {
                points = 60;
                credits = 6;
            }
            case 2 -> {
                points = 100;
                credits = 10;
            }
        }

        // progressi utente
        crediti = (int) UserProgressService.getProgress("credits");
        punti = (int) UserProgressService.getProgress("points");
        resetGame();

    }

    // metodo per disegnare un elemento su schermo
    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) screen.draw(texture, x, y);
    }

    private void loadDarkMode() {
        lightGameBG = new Texture("game_mods_screens/light/base_light.png");
        lightTable = new Texture("game_mods_screens/light/game_table.png");
        lightCells = new Texture("game_mods_screens/light/celle.png");
        lightExit = new Texture("ui/buttons/game/light/quit_light_clicked.png");
        lightExitHover = new Texture("ui/buttons/game/light/quit_light.png");
        lightReplay = new Texture("game_mods_screens/light/play_again_light.png");
    }

    private void loadLightMode() {
        darkGameBG = new Texture("game_mods_screens/dark/base_dark.png");
        darkTable = new Texture("game_mods_screens/dark/game_table.png");
        darkCells = new Texture("game_mods_screens/dark/celle.png");
        darkExit = new Texture("ui/buttons/game/dark/quit_dark_clicked.png");
        darkExitHover = new Texture("ui/buttons/game/dark/quit_dark.png");
        darkReplay = new Texture("game_mods_screens/dark/play_again_dark.png");
    }

    private void saveUserProgresses(int delta, int player) throws IOException {
        int nuovoPunteggio; // punteggio da aggiungere/rimuovere allo storico
        int nuoviCrediti=0; // crediti da aggiungere allo storico in caso di vittoria

        if (player == 1) { // vittoria
            nuovoPunteggio = punti + delta;
            nuoviCrediti = credits;
        }
        else nuovoPunteggio = Math.max(0, punti - delta/2); // sconfitta (punteggio minimo utente 0)

        // salvataggio progressi utente
        UserProgressService.setProgress("credits", crediti+nuoviCrediti); // salvataggio crediti
        UserProgressService.setProgress("points", nuovoPunteggio); // salvataggio nei progressi utente
        FirestoreUserRepository.setUserPoints(AuthAlgorithms.nickname, nuovoPunteggio); // salvataggio nel campo "points"

        // aggiornamento progresso daily
    }

    private void isDark(boolean dark) {
        if (dark) {
            gameBG = darkGameBG;
            table = darkTable;
            cells = darkCells;
            exit = darkExit;
            exitHover = darkExitHover;
            replay = darkReplay;
        }
        else {
            gameBG = lightGameBG;
            table = lightTable;
            cells = lightCells;
            exit = lightExit;
            exitHover = lightExitHover;
            replay = lightReplay;
        }
    }

    private boolean[][] boardStateAsBoolean() {
        boolean[][] b = new boolean[6][7];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) b[r][c] = boardState[r][c] != 0;
        }
        return b;
    }

    public void playNextGravitySound() {
        int next = getNextGravity();

        switch (next) {
            case 0 ->
            {
                SoundManager.playGravityTop(LobbyInput.effectsPercent);

                log.info("NEXT GRAVITY = TOP"+" "+LobbyInput.effectsPercent);
            }
            case 1 -> {
                SoundManager.playGravityRight(LobbyInput.effectsPercent);
                log.info("NEXT GRAVITY = RIGHT");
            }
            case 2 -> {
                SoundManager.playGravityLeft(LobbyInput.effectsPercent);
                log.info("NEXT GRAVITY = LEFT");
            }
        }
    }


    private int getNextGravity() {
        return (gravityStep + 1) % 3;
    }



    private void resetGame() {
        isMatchOver = false;
        victory = false;
        usedAnyBoostThisMatch = false;

        // reset griglia
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) boardState[r][c] = 0;
        }

        gameInput.isHole = false;
        gameInput.isBtnExitClicked = false;
        gameInput.isBtnExitHover = false;

        gameInput.btnYesExit = false;
        gameInput.btnNoExit = false;
        gameInput.isBtnYesExitHover = false;
        gameInput.isBtnNoExitHover = false;

        // reset power-up state
        gameInput.powerFreeze = false;
        gameInput.powerTokenCracker = false;
        gameInput.powerRowBreaker = false;
        gameInput.powerPeek = false;
        gameInput.selectedSwapColumn = -1;
        freezeColumn = -1;
        freezeTurns = 0;

        modeTransition = false;
        pendingMode = -1;
        modeTransitionTimer = 0f;

        numTokensBot=numTokensUser=0;

        // aggiornamento valore dei punti e crediti utente
        punti = (int) UserProgressService.getProgress("points");
        crediti = (int) UserProgressService.getProgress("credits");
    }

    // ---------------- POWER-UP METHODS ----------------
    private void startBotMoveAfterPowerUp() {

        // scegli colonna bot
        int botCol = CPUBrain.chooseMove(boardState);

        // evita colonna congelata
        if (freezeColumn == botCol) {
            botCol = findAlternativeColumnForBot();
        }

        if (botCol == -1) {
            log.info("Bot non può giocare dopo power-up");
            return;
        }

        int[] landing = getGravityLandingCellStatic(boardState, botCol, -1);
        int r = landing[0];
        int c = landing[1];

        if (r == -1 || c == -1) {
            log.info("Bot landing cell invalida dopo power-up");
            return;
        }

        // salva stato per Undo
        for (int rr = 0; rr < 6; rr++) {
            System.arraycopy(boardState[rr], 0, lastBoardState[rr], 0, 7);
        }
        lastGravityStep = gravityStep;
        lastFreezeColumn = freezeColumn;
        lastFreezeTurns = freezeTurns;
        lastNumTokensBot = numTokensBot;

        // avvia animazione bot
        botPlannedRow = r;
        botPlannedCol = c;
        botPending = true;
        botDelayTimer = 0f; // esegue subito
        gameInput.setGridEnabled(false);
    }


    // 🔥 Explosive → rimuove la pedina cliccata e compatta solo nella direzione della gravità
    public void applicaExplosive(int row, int col) {

        log.info("entro");

        if (row < 0 || row > 5 || col < 0 || col > 6) return;

        // modalità non-gravity → sempre TOP
        int gravity = (mod == 1) ? gravityStep : 0;

        // --- GRAVITY TOP (classico) ---
        if (gravity == 0)
        {
            // la pedina cliccata sparisce
            boardState[row][col] = 0;

            // fai scendere SOLO le pedine sopra
            for (int r = row; r > 0; r--)
            {
                boardState[r][col] = boardState[r - 1][col];
            }

            // la cella più in alto diventa vuota
            boardState[0][col] = 0;
            return;
        }

        // --- GRAVITY RIGHT ---
        if (gravity == 1)
        {
            boardState[row][col] = 0;

            // fai scorrere SOLO le pedine a sinistra verso destra
            for (int c = col; c > 0; c--)
            {
                boardState[row][c] = boardState[row][c - 1];
            }

            // la cella più a sinistra diventa vuota
            boardState[row][0] = 0;
            return;
        }

        // --- GRAVITY LEFT ---
        if (gravity == 2)
        {
            boardState[row][col] = 0;

            // fai scorrere SOLO le pedine a destra verso sinistra
            for (int c = col; c < 6; c++)
            {
                boardState[row][c] = boardState[row][c + 1];
            }

            // la cella più a destra diventa vuota
            boardState[row][6] = 0;
        }

        startBotMoveAfterPowerUp();


    }

    // 🧊 Freeze → blocca una colonna per 2 turni
    private void applicaFreeze(int col) {
        if (col < 0 || col > 6) return;

        freezeColumn = col;
        freezeTurns = 2;
        log.info("Power-up Freeze attivato sulla colonna " + col);

        startBotMoveAfterPowerUp();

    }

    public void applicaPredict() {

        int futureGravity = (gravityStep + 1) % 3;
        int preferredRow = lastUserRow;

        int botCol = CPUBrain.chooseMove(boardState, lastUserRow, lastUserCol);

        if (freezeColumn == botCol)
            botCol = findAlternativeColumnForBot();

        if (botCol == -1) {
            predictRow = -1;
            predictCol = -1;
            return;
        }

        int[] landing = getGravityLandingCellStatic(boardState, botCol, preferredRow);

        predictRow = landing[0];
        predictCol = landing[1];

        // 👇 forza la prossima mossa del bot
        predictForcedMove = true;

        isPredictDrop = true;
        startDrop(2, predictRow, predictCol);

        predictTimer = PREDICT_DURATION;
    }





    public void applicaUndo() {

        // se il bot non ha ancora giocato → niente da annullare
        if (lastNumTokensBot == numTokensBot) {
            log.info("Undo impossibile: nessuna mossa bot da annullare");
            return;
        }

        // ripristina board
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                boardState[r][c] = lastBoardState[r][c];
            }
        }

        gravityStep = lastGravityStep;
        freezeColumn = lastFreezeColumn;
        freezeTurns = lastFreezeTurns;
        numTokensBot = lastNumTokensBot;

        log.info("Undo eseguito: mossa bot annullata");
    }


    private void decrementPowerUp(String key) {
        int n = (int) UserProgressService.getProgress(key);
        if (n > 0) {
            UserProgressService.setProgress(key, n - 1);
        }
    }

    private boolean isBoardFull() {
        for (int c = 0; c < 7; c++) {
            if (boardState[0][c] == 0) return false;
        }
        return true;
    }




    /**
     * Gravity4 a 3 direzioni:
     * 0 = TOP   → cerca il primo spazio libero dal basso
     * 1 = RIGHT → cerca il primo spazio libero da sinistra
     * 2 = LEFT  → cerca il primo spazio libero da destra
     */
    /**
     * Gravity4 semplificata:
     * - cerca SOLO il primo blocco libero nella direzione della gravità
     * - ignora il punto cliccato SOLO in TOP
     * - usa clickedRow per LEFT/RIGHT
     * - restituisce {row, col} oppure {-1, -1}
     */

    public static int[] getGravityLandingCellStatic(int[][] board, int requestedCol, int preferredRow) {

        // --- CLASSIC / NON-GRAVITY ---
        if (GameUI.mod != 1) {
            for (int r = 5; r >= 0; r--) {
                if (board[r][requestedCol] == 0)
                    return new int[]{r, requestedCol};
            }
            return new int[]{-1, -1};
        }

        // --- GRAVITY4 ---
        int gravity = GameUI.gravityStep;

        switch (gravity) {

            // 0 = TOP → classico: cerca la prima riga libera nella colonna (dal basso)
            case 0 -> {
                for (int r = 5; r >= 0; r--) {
                    if (board[r][requestedCol] == 0)
                        return new int[]{r, requestedCol};
                }
                return new int[]{-1, -1};
            }

            // 1 = RIGHT → gravità verso destra
            case 1 -> {
                // prova prima la preferredRow (se valida), scorrendo colonne da destra verso sinistra
                if (preferredRow >= 0 && preferredRow <= 5) {
                    for (int c = 6; c >= 0; c--) {
                        if (board[preferredRow][c] == 0) return new int[]{preferredRow, c};
                    }
                }
                // fallback: per ogni riga dal basso verso l'alto cerca la prima cella libera scorrendo da destra verso sinistra
                for (int r = 5; r >= 0; r--) {
                    for (int c = 6; c >= 0; c--) {
                        if (board[r][c] == 0) return new int[]{r, c};
                    }
                }
                return new int[]{-1, -1};
            }

            // 2 = LEFT → gravità verso sinistra
            case 2 -> {
                // prova prima la preferredRow (se valida), scorrendo colonne da sinistra verso destra
                if (preferredRow >= 0 && preferredRow <= 5) {
                    for (int c = 0; c < 7; c++) {
                        if (board[preferredRow][c] == 0) return new int[]{preferredRow, c};
                    }
                }
                // fallback: per ogni riga dal basso verso l'alto cerca la prima cella libera scorrendo da sinistra verso destra
                for (int r = 5; r >= 0; r--) {
                    for (int c = 0; c < 7; c++) {
                        if (board[r][c] == 0) return new int[]{r, c};
                    }
                }
                return new int[]{-1, -1};
            }
        }

        return new int[]{-1, -1};
    }

    private int[] getGravityLandingCell(int clickedRow, int clickedCol) {

        // --- MODALITÀ NON GRAVITY ---
        if (mod != 1) {
            int row = gameInput.getLowestFreeRow(clickedCol, boardStateAsBoolean());
            return new int[]{row, clickedCol};
        }

        // --- GRAVITY4 ---
        switch (gravityStep) {

            // 0 = TOP → classico
            case 0 -> {
                int r = gameInput.getLowestFreeRow(clickedCol, boardStateAsBoolean());
                if (r == -1) return new int[]{-1, -1};
                return new int[]{r, clickedCol};
            }

            // 1 = RIGHT → cerca da sinistra verso destra nella riga cliccata
            case 1 -> {
                for (int c = 0; c < 7; c++) {
                    if (boardState[clickedRow][c] == 0)
                        return new int[]{clickedRow, c};
                }
                return new int[]{-1, -1};
            }

            // 2 = LEFT → cerca da destra verso sinistra nella riga cliccata
            case 2 -> {
                for (int c = 6; c >= 0; c--) {
                    if (boardState[clickedRow][c] == 0)
                        return new int[]{clickedRow, c};
                }
                return new int[]{-1, -1};
            }
        }

        return new int[]{-1, -1};
    }

    // disegno griglia di gioco
    public void drawGame() {
        // pedine sulla griglia
        float baseX = 270;
        float baseY = 93;

        for (int row = 5; row >= 0; row--)
        {
            float drawY = baseY + (5 - row) * 61;
            float drawX = baseX;

            for (int col = 0; col < 7; col++)
            {
                if (boardState[row][col] == 1) draw(red, true, drawX, drawY); // pedina rossa
                if (boardState[row][col] == 2) draw(yellow, true, drawX, drawY); // pedina gialla

                drawX += 70;
            }
        }

    }

    private float cellX(int col) {
        return 270 + col * 70f;
    }
    private float cellY(int row) {
        return 93 + (5 - row) * 61f;
    }

    private DropDir currentDropDir() {
        if (mod != 1) return DropDir.TOP;

        log.info("Gravity"+gravityStep);

        return switch (gravityStep) {
            case 0 -> DropDir.TOP;   // cade dall’alto
            case 1 -> DropDir.RIGHT; // entra da destra
            case 2 -> DropDir.LEFT;  // entra da sinistra
            default -> DropDir.TOP;
        };
    }

    private int findAlternativeColumnForBot() {
        for (int c = 0; c < 7; c++) {
            if (c == freezeColumn) continue;
            int[] landing = getGravityLandingCellStatic(boardState, c, -1);
            if (landing[0] != -1) return c;
        }
        return -1; // nessuna colonna disponibile
    }

    // metodo per avviare l'animazione
    private void startDrop(int player, int row, int col) {
        dropActive = true;
        dropPlayer = player;
        dropRow = row;
        dropCol = col;

        dropDir = currentDropDir();

        log.info("Dropdir: "+dropDir);

        dropTargetX = cellX(col);
        dropTargetY = cellY(row);

        // start position fuori schermo in base alla direzione
        switch (dropDir)
        {
            case TOP -> {
                dropX = dropTargetX;
                dropY = 430f; }
            case BOTTOM -> {
                dropX = dropTargetX;
                dropY = -120f; }
            case RIGHT ->
            {
                if(player == 2)
                {
                    dropX = -120f;
                }
                else
                {
                    dropX = Gdx.graphics.getWidth() + 120f;
                }

                dropY = dropTargetY;
            }
            case LEFT ->
            {

                if(player ==2)
                {
                    dropX = Gdx.graphics.getWidth() + 120f;
                }
                else
                {
                    dropX = -120f;
                }


                dropY = dropTargetY;
            }
        }

        // parametri per il bounce quando la pedina atterra
        dropPhase = DropPhase.FALLING;
        bounceTimer = 0f;
    }

    // metodo per aggiornare l'animazione
    private void updateDrop(float delta) throws IOException {
        if (!dropActive) return;

        // 1) FASE: CADUTA
        if (dropPhase == DropPhase.FALLING) {
            float dx = dropTargetX - dropX;
            float dy = dropTargetY - dropY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            float step = DROP_SPEED * delta;

            if (dist <= step) {
                // Arrivata al target: passa al rimbalzo (NON committare ancora)
                dropX = dropTargetX;
                dropY = dropTargetY;

                dropPhase = DropPhase.BOUNCING;
                bounceTimer = 0f;
                return;
            }




            // move verso target
            dropX += (dx / dist) * step;
            dropY += (dy / dist) * step;
            return;
        }

        // 2) FASE: RIMBALZO
        bounceTimer += delta;
        float t = bounceTimer / BOUNCE_DURATION;
        if (t > 1f) t = 1f;

        // Oscillazione semplice: 1 “su e giù” che si spegne (0 all’inizio e alla fine)
        // sin(2πt) fa: 0 -> su -> 0 -> giù -> 0
        float s = (1f - t) * (float) Math.sin(Math.PI * 2f * t);
        float disp = BOUNCE_AMPLITUDE * s;

        // Applica il rimbalzo lungo l’asse opposto alla direzione di entrata
        float axisX = 0f, axisY = 0f;
        float sign = 1f;

        switch (dropDir) {
            case TOP -> axisY = 1f; // entra dall'alto (va sotto), rimbalza verso sù
            case BOTTOM -> { axisY = 1f; sign = -1f; }  // entra dal basso (sale), rimbalza leggermente giù
            case RIGHT -> axisX = 1f; // entra dalla destra (va a sinistra), rimbalza verso destra
            case LEFT -> { axisX = 1f; sign = -1f; }    // entra da sinistra (va a destra), rimbalza verso sinistra
        }

        dropX = dropTargetX + axisX * sign * disp;
        dropY = dropTargetY + axisY * sign * disp;

        // Fine rimbalzo
        // Fine rimbalzo
        if (t >= 1f) {
            dropX = dropTargetX;
            dropY = dropTargetY;

            dropActive = false;
            dropPhase = DropPhase.FALLING;

            // 👇 Caso PREDICT: solo animazione, niente commit
            if (isPredictDrop) {
                isPredictDrop = false;   // reset per le prossime drop
                return;
            }

            // 👇 Caso reale: commit sulla board
            boardState[dropRow][dropCol] = dropPlayer;

            if (mod == 1)
            {
                gravityStep = (gravityStep + 1) % 3;

                if (dropPlayer == 1)
                {   // suono SOLO per il player
                    playNextGravitySound();
                }
            }


            if (dropPlayer == 1) numTokensUser++;
            else numTokensBot++;

            onTokenLanded(dropPlayer, dropRow, dropCol);
        }



    }

    // metodo che disegna la pedina 'in volo'
    private void drawDrop()
    {
        if (!dropActive) return;
        Texture tex = (dropPlayer == 1) ? red : yellow;

        screen.draw(tex, dropX, dropY);
    }

    // metodo per i controlli chiamato appena la pedina 'atterra' sulla tabella
    // viene chiamato quando la pedina "atterra" sulla tabella (dopo l'animazione)
    private void onTokenLanded(int player, int row, int col) throws IOException {
        // suono atterraggio
        SoundManager.playLand(LobbyInput.effectsPercent);
        // --- POWER-UP FREEZE ---
        // Applica il freeze SOLO dopo che la pedina è atterrata
        if (player == 1 && gameInput.powerFreeze)
        {
            decrementPowerUp("num_freezer");
            usedAnyBoostThisMatch = true;
            DailyChallenges.updateDailyOnBoostUse("num_freezer");
            applicaFreeze(col);
            gameInput.powerFreeze = false;
        }

        // --- check vittoria del giocatore che ha appena piazzato ---
        if (CPUBrain.checkWin(boardState, row, col, player)) {
            isMatchOver = true;
            victory = (player == 1);

            if (player == 1) SoundManager.playWin(LobbyInput.musicPercent);
            else SoundManager.playDefeat(LobbyInput.musicPercent);

            // aggiornamento punteggio utente
            saveUserProgresses(points, player);

            // aggiornamento progresso daily in corso
            DailyChallenges.updateDaily(victory, usedAnyBoostThisMatch, mod, points, credits, gameDifficulty);
            return;
        }

        // --- se ha appena giocato il player, programma la mossa del bot con delay ---
        if (player == 1)
        {

            lastUserRow = row;
            lastUserCol = col;


            int botCol;

            if (predictForcedMove) {
                // 👇 usa la colonna predetta
                botCol = predictCol;
                predictForcedMove = false;   // reset
            } else {
                botCol = CPUBrain.chooseMove(boardState, lastUserRow, lastUserCol);
            }




            // se la colonna è congelata, il bot NON può usarla
            if (freezeTurns > 0 && botCol == freezeColumn) botCol = findAlternativeColumnForBot();

            boolean botCanPlay = botCol != -1;

            int[] landing;

            if (!predictForcedMove) {
                landing = getGravityLandingCellStatic(boardState, botCol, row);
            } else {
                landing = new int[]{predictRow, predictCol};
                predictForcedMove = false;
            }


            if (landing[0] == -1 || landing[1] == -1) botCanPlay = false;


            if (botCanPlay) {
                botPlannedRow = landing[0];
                botPlannedCol = landing[1];
                botPending = true;
                botDelayTimer = BOT_THINK_DELAY;
                gameInput.setGridEnabled(false);
            } else {
                botPending = false;
                gameInput.setGridEnabled(true);
            }
        }

        // sblocco griglia per l'utente
        if (!isMatchOver && player == 2) gameInput.setGridEnabled(true);


        // decrementa freeze
        if (freezeTurns > 0) {
            freezeTurns--;
            if (freezeTurns == 0) freezeColumn = -1;
        }

    }

    // metodo per scrivere i testi in gioco
    public void drawTexts() {
        // modalità di gioco
        Fonts.bold70.draw(screen, modName, 55, 642);

        // stelle difficoltà
        for (int i = 0; i < 4; i++) {
            if (gameDifficulty == 1) {
                // prima stella
                draw(starDifficulty, true,860, 354);
            }
            if (gameDifficulty == 2) {
                // seconda stella e prima stella
                draw(starDifficulty, true, 860, 354);
                draw(starDifficulty, true, 889, 354);
            }
        }

        // punti e crediti vittoria/sconfitta
        Fonts.draw(screen, "+" + points, 860, 296, Fonts.bold25); // punti vinti alla vittoria
        Fonts.draw(screen, "+" + credits, 860, 264, Fonts.bold25); // crediti vinti alla vittoria

        // punti persi alla sconfitta
        int puntiPersi = Math.min(punti, points/2);
        Fonts.draw(screen, "-" + puntiPersi, 860, 183, Fonts.bold25);

        // NOME AZIENDA //
        Fonts.draw(screen, "Drop Logic", 49, 63, Fonts.medium20); // firma al gioco
        // VERSIONE DI GIOCO //
        String text = "Beta " + VersionInfo.getVersion();
        // calcolo larghezza del testo
        GlyphLayout layout = new GlyphLayout(Fonts.medium20, text);
        // stampa testo
        Fonts.medium20.draw(screen, layout, (955 - layout.width), 63);

        // nome utente
        Fonts.bold20.draw(screen, AuthAlgorithms.nickname, 78, 413);

        // played tokens
        Fonts.bold25.draw(screen, numTokensUser + "/21", 83, 331);
        Fonts.bold25.draw(screen, numTokensBot + "/21", 83, 135);

        // numero boosts
        int x=285;
        for (int i = 0; i < 6; i++) {
            // linea 1
            Fonts.bold13.draw(screen, UserProgressService.getProgress(items[i]).toString(), x, 497);
            x+=78;
        }
    }

    @Override
    public void render(float delta) {
        // update input timers (click delays / scheduled actions)
        gameInput.update(delta);

        Gdx.input.setInputProcessor(gameInput); // si può togliere? todo: controllare se si può gestire nel GameManager

        // init schermo
        screen.begin();

        if (isBoardFull() && !isMatchOver) {
            resetGame();
        }



        // cambio light/dark mode
        isDark(darkMode);

        // --- PREDICT: esegui subito quando il bottone è cliccato ---
        if (gameInput.powerPeek)
        {
            decrementPowerUp("num_peek");
            usedAnyBoostThisMatch = true;
            DailyChallenges.updateDailyOnBoostUse("num_peek");
            applicaPredict();
            gameInput.powerPeek = false;
        }

        // --- UNDO: esegui subito quando il bottone è cliccato ---
        if (gameInput.powerUndo)
        {
            decrementPowerUp("num_undo");
            usedAnyBoostThisMatch = true;
            DailyChallenges.updateDailyOnBoostUse("num_undo");
            applicaUndo();
            gameInput.powerUndo = false;
        }

        // aggiorna animazione caduta
        try {
            updateDrop(delta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // --- TIMER DI INATTIVITÀ UTENTE (solo in SPEEDY) ---
        if (mod == 3 && !dropActive && !botPending && !isMatchOver) {

            timer += delta;

            if (timer >= 2f && timer < 2f + delta) {
                SoundManager.playLand(100);
            }

            if (timer >= 4f && timer < 4f + delta) {
                SoundManager.playLand(100);
            }

            if (timer >= 5f) {
                // 5 secondi senza input → il bot gioca
                int botCol = CPUBrain.chooseMove(boardState);
                int botRow = gameInput.getLowestFreeRow(botCol, boardStateAsBoolean());

                log.info("sono passati 5 secondi");

                if (botRow != -1) {
                    botPlannedCol = botCol;
                    botPlannedRow = botRow;

                    botPending = true;
                    botDelayTimer = 0f; // esegue subito la mossa
                    gameInput.setGridEnabled(false);
                }

                timer = 0; // reset timer
            }

        }
        // se l’utente fa qualcosa → reset timer
        else timer = 0;

        // evita che l'utente annulli la mossa del bot
        gameInput.setGridEnabled(!dropActive && !botPending && !isMatchOver);

        // delay bot (solo se non sta già cadendo una pedina)
        if (botPending && !dropActive)
        {
            botDelayTimer -= delta;
            if (botDelayTimer <= 0f)
            {
                botPending = false;

                // griglia bloccata per l'utente
                gameInput.setGridEnabled(false);

                // avvia caduta del bot (player 2)
                if (botPlannedRow != -1 && botPlannedCol != -1)
                {

                    // SALVA STATO PRIMA DI OGNI MOSSA DEL BOT
                    for (int r = 0; r < 6; r++) {
                        System.arraycopy(boardState[r], 0, lastBoardState[r], 0, 7);
                    }
                    lastGravityStep = gravityStep;
                    lastFreezeColumn = freezeColumn;
                    lastFreezeTurns = freezeTurns;
                    lastNumTokensBot = numTokensBot;


                    startDrop(2, botPlannedRow, botPlannedCol);
                }
                else
                {
                    gameInput.isHole = false; // nessuna mossa possibile
                }
            }
        }

        // disegno background gioco
        screen.draw(gameBG, 0, 0);

        // 1) disegno celle
        screen.draw(cells, 270, 92);
        // 2) disegno griglia con pedine giocate
        drawGame();
        // 3) disegno animazione caduta pedina
        drawDrop();
        // 4) disegno tabella
        screen.draw(table, 248, 71);

        // testi
        drawTexts();

        // finestra riavvio/chiusura gioco => mostrata solo alla fine di una partita
        draw(replay, isMatchOver, 294, 204);

        // partita conclusa
        if (isMatchOver) {
            // testo vittoria/sconfitta
            if (victory) Fonts.draw(screen, "VICTORY", 415, 459, Fonts.bold40); // vittoria
            else Fonts.draw(screen, "DEFEAT", 425, 459, Fonts.bold40); // sconfitta

            draw(btn_yes, gameInput.isBtnYesExitHover, 342, 244);
            draw(btn_yes_clicked, gameInput.btnYesExit, 342, 244);

            draw(btn_no, gameInput.isBtnNoExitHover, 506, 244);
            draw(btn_no_clicked, gameInput.btnNoExit, 506, 244);

            // reset gioco per la prossima partita (con delay)
            if (gameInput.consumeRestartRequested()) resetGame();
        }

        // reset in caso di pareggio
        if (numTokensBot == 21 && !isMatchOver) resetGame();

        // pulsante in alto a dx per chiudere la partita
        draw(exitHover, gameInput.isBtnExitHover, 833, 588);
        draw(exit, gameInput.isBtnExitClicked, 833, 588);

        // LOGICA DI GIOCO (solo se non è aperta la finestra isMatchOver)
        if (!isMatchOver && gameInput.isHole)
        {
            // durante animazione o “pensiero bot” ignora input del player
            if (dropActive || botPending) gameInput.isHole = false;

            int col, row;

            col = gameInput.getColumnFromClick(Gdx.input.getX(), Gdx.input.getY());

            log.info(col);

            if (col == -1) gameInput.isHole = false;

            // calcolo la riga cliccata
            row = gameInput.getRowFromClick(Gdx.input.getX(), Gdx.input.getY());

            // MOSSA NORMALE (quando NON stai usando i power-up che richiedono click su cella)
            if (!gameInput.powerTokenCracker && !gameInput.powerRowBreaker && !gameInput.powerPrecision) {

                int[] landing = getGravityLandingCell(row, col);
                int lr = landing[0];
                int lc = landing[1];

                // se non c'è spazio, annulla
                if (lr == -1 || lc == -1) {
                    gameInput.isHole = false;
                } else {
                    // suono click
                    SoundManager.playClickButton(LobbyInput.effectsPercent);

                    // avvia caduta pedina player
                    startDrop(1, lr, lc);

                    // blocca input finché non finisce animazione
                    gameInput.setGridEnabled(false);
                    gameInput.isHole = false;
                }
            }

            if (gameInput.powerTokenCracker) {
                usedAnyBoostThisMatch = true;
                decrementPowerUp("num_token_cracker");
                DailyChallenges.updateDailyOnBoostUse("num_token_cracker");

                applicaExplosive(row, col);
                gameInput.powerTokenCracker = false;
                gameInput.isHole = false;

            }

            if (gameInput.powerRowBreaker) {
                usedAnyBoostThisMatch = true;
                decrementPowerUp("num_row_breaker");
                DailyChallenges.updateDailyOnBoostUse("num_row_breaker");

                applicaBigExplosive(row);
                gameInput.powerRowBreaker = false;
                gameInput.isHole = false;
            }

            if (gameInput.powerPrecision)
            {
                usedAnyBoostThisMatch = true;
                decrementPowerUp("num_precision");

                // sicurezza: click fuori griglia
                if (row < 0 || row > 5 || col < 0 || col > 6)
                {
                    log.info("non è una cella");
                }
                else
                {
                    DailyChallenges.updateDailyOnBoostUse("num_precision");

                    gameInput.powerPrecision = false;


                    log.info(col);

                    // se la cella è vuota → piazza la pedina
                    if (boardState[row][col] == 0) {
                        startDrop(1, row, col);
                        log.info("Target piazzato su (" + row + "," + col + ")");
                    } else
                    {
                        log.info("Target: cella occupata, nessuna azione");

                    }

                    // NON passa il turno al bot

                    gameInput.isHole = false;
                    gameInput.setGridEnabled(true);


                    // check vittoria immediata
                    if (CPUBrain.checkWin(boardState, row, col, 1)) {
                        isMatchOver = true;
                        victory = true;
                        SoundManager.playWin(LobbyInput.musicPercent);
                        try {
                            saveUserProgresses(points, 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    startBotMoveAfterPowerUp();
                }


            }
        }

        // chiusura screen
        screen.end();

        if (gameInput.consumeExitToLobbyRequested() && !modeTransition) {
            modeTransition = true;
            pendingMode = 0;
            modeTransitionTimer = 0f;
        }

        if (modeTransition) {
            modeTransitionTimer += delta;
            if (modeTransitionTimer >= 0.10f) {
                if (pendingMode == 0) {
                    GameManager.soundGame.stop(); // stop musica di gioco
                    GameManager.game.setScreen(new LobbyManager(GameManager.game)); // back to lobby
                    dispose(); // rilascio risorse
                }
            }
        }

        if (predictTimer > 0) {
            predictTimer -= delta;
            if (predictTimer <= 0) {
                predictRow = -1;
                predictCol = -1;
            }
        }
    }

    private void applicaBigExplosive(int row) {

        if (row < 0 || row > 5) return;

        int gravity = (mod == 1) ? gravityStep : 0;

        // --- GRAVITY TOP (classico) ---
        if (gravity == 0) {
            // Cancella tutta la riga
            for (int c = 0; c < 7; c++) {
                boardState[row][c] = 0;
            }

            // Fai scendere tutte le righe sopra
            for (int r = row; r > 0; r--) {
                System.arraycopy(boardState[r - 1], 0, boardState[r], 0, 7);
            }

            // La riga più in alto diventa vuota
            for (int c = 0; c < 7; c++) {
                boardState[0][c] = 0;
            }

            return;
        }

        // --- GRAVITY RIGHT ---
        if (gravity == 1) {

            // Cancella tutta la riga
            for (int c = 0; c < 7; c++) {
                boardState[row][c] = 0;
            }

            // Ricompatta da sinistra verso destra
            int write = 6;
            for (int c = 6; c >= 0; c--) {
                if (boardState[row][c] != 0) {
                    boardState[row][write] = boardState[row][c];
                    write--;
                }
            }

            // Svuota le celle rimaste a sinistra
            for (int c = write; c >= 0; c--) {
                boardState[row][c] = 0;
            }

            return;
        }

        // --- GRAVITY LEFT ---
        if (gravity == 2) {

            // Cancella tutta la riga
            for (int c = 0; c < 7; c++) {
                boardState[row][c] = 0;
            }

            // Ricompatta da destra verso sinistra
            int write = 0;
            for (int c = 0; c < 7; c++) {
                if (boardState[row][c] != 0) {
                    boardState[row][write] = boardState[row][c];
                    write++;
                }
            }

            // Svuota le celle rimaste a destra
            for (int c = write; c < 7; c++) {
                boardState[row][c] = 0;
            }
        }
    }

    @Override
    public void loadFont() {}

    @Override
    public void loadImages() {
        loadDarkMode();
        loadLightMode();

        red = new Texture("ui/icons/red.png");
        yellow = new Texture("ui/icons/yellow.png");

        btn_no = new Texture("ui/buttons/lobby/btn_no.png");
        btn_no_clicked = new Texture("ui/buttons/lobby/btn_no_clicked.png");

        btn_yes = new Texture("ui/buttons/lobby/btn_yes.png");
        btn_yes_clicked = new Texture("ui/buttons/lobby/btn_yes_clicked.png");

        // stella difficoltà
        starDifficulty=new Texture("ui/icons/star_selected.png");
    }

    @Override
    public void dispose() {
        if (lightGameBG != null) lightGameBG.dispose();
        if (lightTable != null) lightTable.dispose();
        if (lightCells != null) lightCells.dispose();
        if (lightExit != null) lightExit.dispose();
        if (lightExitHover != null) lightExitHover.dispose();
        if (lightReplay != null) lightReplay.dispose();

        if (darkGameBG != null) darkGameBG.dispose();
        if (darkTable != null) darkTable.dispose();
        if (darkCells != null) darkCells.dispose();
        if (darkExit != null) darkExit.dispose();
        if (darkExitHover != null) darkExitHover.dispose();
        if (darkReplay != null) darkReplay.dispose();

        if (red != null) red.dispose();
        if (yellow != null) yellow.dispose();
        if (btn_no != null) btn_no.dispose();
        if (btn_no_clicked != null) btn_no_clicked.dispose();
        if (btn_yes != null) btn_yes.dispose();
        if (btn_yes_clicked != null) btn_yes_clicked.dispose();
        if (starDifficulty != null) starDifficulty.dispose();
    }

}
