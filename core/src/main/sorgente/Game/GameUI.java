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
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyManager;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.UserProgressService;

import java.io.IOException;

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

    // velocità animazione (px/sec) -> regola se troppo lenta/veloce
    private static final float DROP_SPEED = 600f;

    // --- GRAVITY4: ordine direzioni per OGNI mossa ---
    public static int gravityStep = 0; // 0 TOP, 1 BOTTOM, 2 RIGHT, 3 LEFT

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

    // pedine utente e bot giocate
    private int numTokensUser=0, numTokensBot=0;
    // difficoltà di gioco, punti e crediti per partita
    private int gameDifficulty, points, credits;
    // nome modalità di gioco
    private String modName;
    // lettura punti e crediti utente
    private int punti = (int) UserProgressService.getProgress("points");
    private int crediti = (int) UserProgressService.getProgress("credits");

    public GameUI (GameInput in, boolean dark, int mod) {
        this.screen = GameManager.game.screen;
        GameUI.mod = mod;

        isMatchOver=victory=false;

        Fonts.load();
        darkMode = dark;
        gameInput = in;
        loadImages();


        // inizializza boardState
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) boardState[r][c] = 0;
        }

        // variabili basate sulla modalità di gioco
        switch(mod) {
            case 0 -> {
                // nome modalità
                modName = "CLASSIC";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_classic");
            }
            case 1 -> {
                // nome modalità
                modName = "GRAVITY4";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_gravity4");
            }
            case 2 -> {
                // nome modalità
                modName = "HORIZONTAL";
                // difficoltà
                gameDifficulty = (int) UserProgressService.getProgress("diff_horizontal");
            }
            case 3 -> {
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
    }

    // metodo per disegnare un elemento su schermo
    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) screen.draw(texture, x, y);
    }

    private void loadDarkMode()
    {
        lightGameBG = new Texture("game_mods_screens/light/base_light.png");
        lightTable = new Texture("game_mods_screens/light/game_table.png");
        lightCells = new Texture("game_mods_screens/light/celle.png");
        lightExit = new Texture("ui/buttons/game/light/quit_light_clicked.png");
        lightExitHover = new Texture("ui/buttons/game/light/quit_light.png");
        lightReplay = new Texture("game_mods_screens/light/play_again_light.png");
    }

    private void loadLightMode()
    {
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

        UserProgressService.setProgress("credits", crediti+nuoviCrediti); // salvataggio crediti
        UserProgressService.setProgress("points", nuovoPunteggio); // salvataggio nei progressi utente
        FirestoreUserRepository.setUserPoints(AuthAlgorithms.nickname, nuovoPunteggio); // salvataggio nel campo "points"

    }

    private void isDark(boolean dark)
    {
        if (dark) {
            gameBG = darkGameBG;
            table = darkTable;
            cells = darkCells;
            exit = darkExit;
            exitHover = darkExitHover;
            replay = darkReplay;
        }
        else
        {
            gameBG = lightGameBG;
            table = lightTable;
            cells = lightCells;
            exit = lightExit;
            exitHover = lightExitHover;
            replay = lightReplay;
        }
    }

    private boolean[][] boardStateAsBoolean()
    {
        boolean[][] b = new boolean[6][7];
        for (int r = 0; r < 6; r++)
        {
            for (int c = 0; c < 7; c++)
            {
                b[r][c] = boardState[r][c] != 0;
            }
        }
        return b;
    }

    private void resetGame()
    {
        isMatchOver = false;
        victory = false;

        // reset griglia
        for (int r = 0; r < 6; r++)
        {
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
        gameInput.powerExplosive = false;
        gameInput.powerSwap = false;
        gameInput.powerFreeze = false;
        gameInput.powerWild = false;
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

    // 🔥 Explosive → distrugge pedine vicine (3x3)
    private void applicaExplosive(int row, int col)
    {
        for (int r = row - 1; r <= row + 1; r++)
        {
            for (int c = col - 1; c <= col + 1; c++)
            {
                if (r >= 0 && r < 6 && c >= 0 && c < 7)
                {
                    boardState[r][c] = 0;
                }
            }
        }
        log.info("Power-up Explosive attivato su (" + row + "," + col + ")");
    }

    // 🔄 Swap → scambia due colonne
    private void applicaSwap(int col1, int col2)
    {
        if (col1 < 0 || col1 > 6 || col2 < 0 || col2 > 6 || col1 == col2) return;

        for (int r = 0; r < 6; r++)
        {
            int temp = boardState[r][col1];
            boardState[r][col1] = boardState[r][col2];
            boardState[r][col2] = temp;
        }

        log.info("Power-up Swap attivato tra colonne " + col1 + " e " + col2);
    }

    // 🧊 Freeze → blocca una colonna per 2 turni
    private void applicaFreeze(int col)
    {
        if (col < 0 || col > 6) return;

        freezeColumn = col;
        freezeTurns = 2;
        log.info("Power-up Freeze attivato sulla colonna " + col);
    }

    // ✨ Wild → piazza una pedina jolly (per ora rossa)
    private void applicaWild(int row, int col)
    {
        if (row < 0 || row > 5 || col < 0 || col > 6) return;

        boardState[row][col] = 1;
        log.info("Power-up Wild attivato su (" + row + "," + col + ")");
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

        return switch (gravityStep) {
            case 0 -> DropDir.TOP;   // cade dall’alto
            case 1 -> DropDir.RIGHT; // entra da destra
            case 2 -> DropDir.LEFT;  // entra da sinistra
            default -> DropDir.TOP;
        };
    }


    // metodo per avviare l'animazione
    private void startDrop(int player, int row, int col) {
        dropActive = true;
        dropPlayer = player;
        dropRow = row;
        dropCol = col;

        dropDir = currentDropDir();

        dropTargetX = cellX(col);
        dropTargetY = cellY(row);

        // start position fuori schermo in base alla direzione
        switch (dropDir) {
            case TOP -> {
                dropX = dropTargetX;
                dropY = 430f; }
            case BOTTOM -> {
                dropX = dropTargetX;
                dropY = -120f; }
            case RIGHT -> {
                dropX = Gdx.graphics.getWidth() + 120f;
                dropY = dropTargetY; }
            case LEFT -> {
                dropX = -120f;
                dropY = dropTargetY; }
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

        // Fine rimbalzo: “atterra” definitivamente e ora fai commit + logica
        if (t >= 1f) {
            dropX = dropTargetX;
            dropY = dropTargetY;

            dropActive = false;
            dropPhase = DropPhase.FALLING;

            boardState[dropRow][dropCol] = dropPlayer;

            // Gravity4: avanza direzione SOLO dopo una mossa completata (a fine rimbalzo)
            if (mod == 1) gravityStep = (gravityStep + 1) % 3;

            if (dropPlayer == 1) numTokensUser++;
            else numTokensBot++;

            onTokenLanded(dropPlayer, dropRow, dropCol);
        }
    }

    // metodo che disegna la pedina 'in volo'
    private void drawDrop() {
        if (!dropActive) return;
        Texture tex = (dropPlayer == 1) ? red : yellow;
        screen.draw(tex, dropX, dropY);
    }

    // metodo per i controlli chiamato appena la pedina 'atterra' sulla tabella
    // viene chiamato quando la pedina "atterra" sulla tabella (dopo l'animazione)
    private void onTokenLanded(int player, int row, int col) throws IOException {
        // suono atterraggio
        SoundManager.playLand(LobbyInput.effectsPercent);

        // --- check vittoria del giocatore che ha appena piazzato ---
        if (CPUBrain.checkWin(boardState, row, col, player)) {
            isMatchOver = true;
            victory = (player == 1);

            if (player == 1) SoundManager.playWin(LobbyInput.musicPercent);
            else SoundManager.playDefeat(LobbyInput.musicPercent);

            // aggiornamento punteggio utente
            saveUserProgresses(points, player);
            return;
        }

        // --- se ha appena giocato il player, programma la mossa del bot con delay ---
        // --- se ha appena giocato il player, programma la mossa del bot con delay ---
        if (player == 1) {

            int botCol = CPUBrain.chooseMove(boardState, row, col);

            boolean botCanPlay = true;

            if (botCol == -1) botCanPlay = false;

            int[] landing = getGravityLandingCellStatic(boardState, botCol, row);

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

    }


    @Override
    public void render(float delta)
    {


        Gdx.input.setInputProcessor(gameInput); // si può togliere? todo: controllare se si può gestire nel GameManager

        // init schermo
        screen.begin();

        // cambio light/dark mode
        isDark(darkMode);

        // aggiorna animazione caduta
        try {
            updateDrop(delta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // --- TIMER DI INATTIVITÀ UTENTE (solo in SPEEDY) ---
        if (mod == 3 && !dropActive && !botPending && !isMatchOver) {

            timer +=  delta;

            if(timer == 2f || timer ==4f)
            {
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

        } else {
            // se l’utente fa qualcosa → reset timer
            timer = 0;
        }

        // evita che l'utente annulli la mossa del bot
        gameInput.setGridEnabled(!dropActive && !botPending && !isMatchOver);

        // delay bot (solo se non sta già cadendo una pedina)
        if (botPending && !dropActive) {
            botDelayTimer -= delta;
            if (botDelayTimer <= 0f) {
                botPending = false;

                // griglia bloccata per l'utente
                gameInput.setGridEnabled(false);

                // avvia caduta del bot (player 2)
                if (botPlannedRow != -1 && botPlannedCol != -1) startDrop(2, botPlannedRow, botPlannedCol);
                else gameInput.isHole = false; // nessuna mossa possibile
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

            draw(btn_no_clicked, gameInput.btnNoExit, 506, 244);
            draw(btn_no, gameInput.isBtnNoExitHover, 506, 244);

            // reset gioco per la prossima partita
            if (gameInput.btnYesExit) resetGame();
        }

        // reset in caso di pareggio
        if (numTokensBot==21) resetGame();

        // LOGICA DI GIOCO (solo se non è aperta la finestra isMatchOver)
        if (!isMatchOver && gameInput.isHole) {
            // durante animazione o “pensiero bot” ignora input del player
            if (dropActive || botPending) gameInput.isHole = false;

            // todo: capire perché non funzionano mai e risolvere il bug
            // pulsante in alto a dx per chiudere la partita
            draw(exit, gameInput.isBtnExitClicked, 825, 591);
            draw(exitHover, gameInput.isBtnExitHover, 825, 591);

            int col = 0;
            int row=0;



            col = gameInput.getColumnFromClick(
                Gdx.input.getX(),
                Gdx.graphics.getHeight() - Gdx.input.getY()
            );

            if (col == -1) {
                gameInput.isHole = false;
                return;
            }


            int[] landing = getGravityLandingCell(
                gameInput.getRowFromClick(Gdx.input.getX(), Gdx.input.getY()),
                col
            );

            row = landing[0];
            col = landing[1];

            if (row == -1 || col == -1)
            {
                gameInput.isHole = false;

            }



            //    if (col != -1 && row != -1) {
            // POWER-UP USO SUL PROSSIMO CLICK
                /*
                if (gameInput.powerExplosive) {
                    applicaExplosive(row, col);
                    gameInput.powerExplosive = false;
                } else if (gameInput.powerSwap) {
                    if (gameInput.selectedSwapColumn == -1) {
                        // primo click: seleziona colonna
                        gameInput.selectedSwapColumn = col;
                        log.info("Prima colonna per Swap selezionata: " + col);
                    } else {
                        // secondo click: esegue swap
                        applicaSwap(gameInput.selectedSwapColumn, col);
                        gameInput.selectedSwapColumn = -1;
                        gameInput.powerSwap = false;
                    }
                } else if (gameInput.powerFreeze) {
                    applicaFreeze(col);
                    gameInput.powerFreeze = false;
                } else if (gameInput.powerWild) {
                    applicaWild(row, col);
                    gameInput.powerWild = false;
                }

                 */

            log.info(mod);


            if(row != -1 && col !=-1)
            {

            // suono click
            SoundManager.playClickButton(LobbyInput.effectsPercent);
            // caduta pedina
            startDrop(1, row, col);
            // chiusura dell’input del player fino a fine mossa (animazione + eventuale bot)
            gameInput.isHole = false;
            }
        }

        screen.end();

        if (gameInput.isBtnExitClicked || gameInput.btnNoExit) {
            modeTransition = true;
            pendingMode = 0;
        }

        if (modeTransition) {
            modeTransitionTimer += delta;
            if (modeTransitionTimer >= 0.14f) {
                if (pendingMode == 0) {
                    GameManager.soundGame.stop(); // stop musica di gioco
                    GameManager.game.setScreen(new LobbyManager(GameManager.game)); // back to lobby
                    System.out.println("reached");
                    dispose(); // rilascio risorse
                }
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
