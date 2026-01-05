package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.*;
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyManager;
import sorgente.UserData.UserProgressService;

public class GameUI extends ScreenAdapter implements ResourceLoader
{
    private static final Log log = LogFactory.getLog(GameUI.class);
    private final Main game;
    private final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private final int difficolta;
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final GameInput gameInput;
    private final CPUBrain CPUBrain;

    private boolean darkMode;
    static int mod;

    private float modeTransitionTimer = 0f;
    private boolean modeTransition = false;
    static boolean isMatchOver, victory;

    private int pendingMode = -1;

    // Light Texture
    private Texture lightGameBG;
    private Texture lightTable;
    private Texture lightExit;
    private Texture lightExitHover;
    private Texture lightReplay;

    // Dark Texture
    private Texture darkGameBG;
    private Texture darkTable;
    private Texture darkExit;
    private Texture darkExitHover;
    private Texture darkReplay;

    // Editable Texture
    private Texture gameBG;
    private Texture table;
    private Texture exit;
    private Texture exitHover;
    private Texture replay;

    private Texture red;
    private Texture yellow;

    private Texture btn_no;
    private Texture btn_yes;

    private Texture btn_no_clicked;
    private Texture btn_yes_clicked;

    // --- DROP ANIMATION ---
    private enum DropDir { TOP, BOTTOM, RIGHT, LEFT }

    private boolean dropActive = false;
    private DropDir dropDir = DropDir.TOP;
    private int dropPlayer = 0;     // 1 = rosso, 2 = giallo
    private int dropRow = -1, dropCol = -1;
    private float dropX = 0f, dropY = 0f;
    private float dropTargetX = 0f, dropTargetY = 0f;

    // velocità animazione (px/sec) -> regola se troppo lenta/veloce
    private static final float DROP_SPEED = 800f;

    // --- GRAVITY4: ordine direzioni per OGNI mossa ---
    private int gravityStep = 0; // 0 TOP, 1 BOTTOM, 2 RIGHT, 3 LEFT

    // --- BOT THINK DELAY ---
    private boolean botPending = false;
    private float botDelayTimer = 0f;
    private int botPlannedRow = -1, botPlannedCol = -1;
    private static final float BOT_THINK_DELAY = 0.65f; // delay tra mossa utente e mossa bot CPU

    // Stato della griglia: 0 = vuoto, 1 = rosso / 2 = giallo
    private final int[][] boardState = new int[6][7];
    private int punti = (int) UserProgressService.getProgress("points");
    private int vittorie = 0;

    // Freeze power-up
    private int freezeColumn = -1;
    private int freezeTurns = 0;

    public GameUI (Main game, GameInput in, boolean dark, int d, int mod) {
        this.game = game;
        this.screen = game.screen;
        this.mod = mod;

        difficolta = d;
        isMatchOver=victory=false;

        Fonts.load();
        darkMode = dark;
        gameInput = in;
        CPUBrain = new CPUBrain(d, 2, mod);
        loadImages();

        //log.info(punti);

        // inizializza boardState
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) boardState[r][c] = 0;
        }
    }

    // metodo per disegnare un elemento su schermo
    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) screen.draw(texture, x, y);
    }

    private void loadDarkMode()
    {
        lightGameBG = new Texture("game_mods_screens/light/base_light.png");
        lightTable = new Texture("game_mods_screens/dark/game_table.png");
        lightExit = new Texture("ui/buttons/game/light/quit_light_clicked.png");
        lightExitHover = new Texture("ui/buttons/game/light/quit_light.png");
        lightReplay = new Texture("game_mods_screens/light/play_again_light.png");
    }

    private void loadLightMode()
    {
        darkGameBG = new Texture("game_mods_screens/dark/base_dark.png");
        darkTable = new Texture("game_mods_screens/dark/game_table.png");
        darkExit = new Texture("ui/buttons/game/dark/quit_dark_clicked.png");
        darkExitHover = new Texture("ui/buttons/game/dark/quit_dark.png");
        darkReplay = new Texture("game_mods_screens/dark/play_again_dark.png");
    }

    private void aggiornaPunteggio(int delta)
    {
        punti += delta;

        if (punti < 0) {
            punti = 0;
        }

        UserProgressService.setProgress("points", punti);
    }

    private void isDark(boolean dark)
    {
        if (dark) {
            gameBG = darkGameBG;
            table = darkTable;
            exit = darkExit;
            exitHover = darkExitHover;
            replay = darkReplay;
        }
        else
        {
            gameBG = lightGameBG;
            table = lightTable;
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
        vittorie = 0;

        // reset griglia
        for (int r = 0; r < 6; r++)
        {
            for (int c = 0; c < 7; c++)
            {
                boardState[r][c] = 0;
            }
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

        log.info("Gioco resettato correttamente");
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

    // disegno griglia di gioco
    public void drawGame() {
        // pedine sulla griglia
        float baseX = 270;
        float baseY = 93;

        for (int row = 5; row >= 0; row--)
        {
            float drawY = baseY + (5 - row) * 61; // todo: correggere, le pedine verso l'alto sono leggermente più in basso
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
        return 273 + col * 69f;
    }
    private float cellY(int row) {
        return 95 + (5 - row) * 61f;
    }

    // metodo per la direzione corrente della pedina inserita
    private DropDir currentDropDir() {
        if (mod != 1) return DropDir.TOP; // classic/speedy/horizontal -> sempre dall'alto
        return switch (gravityStep) {
            case 0 -> DropDir.TOP;
            case 1 -> DropDir.BOTTOM;
            case 2 -> DropDir.RIGHT;
            default -> DropDir.LEFT;
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
                dropY = 460f; }
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
    }

    // metodo per aggiornare l'animazione
    private void updateDrop(float delta) {
        if (!dropActive) return;

        float dx = dropTargetX - dropX;
        float dy = dropTargetY - dropY;
        float dist = (float) Math.sqrt(dx*dx + dy*dy);

        float step = DROP_SPEED * delta;
        if (dist <= step) {
            // ARRIVATA: commit in boardState
            dropX = dropTargetX;
            dropY = dropTargetY;
            dropActive = false;

            boardState[dropRow][dropCol] = dropPlayer;

            // Gravity4: avanza direzione SOLO dopo una mossa completata
            if (mod == 1) gravityStep = (gravityStep + 1) % 4;

            // ora che la pedina è davvero piazzata, fai logica (win/bot/etc.)
            onTokenLanded(dropPlayer, dropRow, dropCol);
            return;
        }

        // move verso target
        dropX += (dx / dist) * step;
        dropY += (dy / dist) * step;
    }

    // metodo che disegna la pedina 'in volo'
    private void drawDrop() {
        if (!dropActive) return;
        Texture tex = (dropPlayer == 1) ? red : yellow;
        screen.draw(tex, dropX, dropY);
    }

    // metodo per i controlli chiamato appena la pedina 'atterra' sulla tabella
    // viene chiamato quando la pedina "atterra" sulla tabella (dopo l'animazione)
    private void onTokenLanded(int player, int row, int col) {
        // --- check vittoria del giocatore che ha appena piazzato ---
        if (CPUBrain.checkWin(boardState, row, col, player)) {
            isMatchOver = true;
            victory = (player == 1);

            if (player == 1)
            {
                // punti vittoria player
                if (difficolta == 0) aggiornaPunteggio(20);
                else if (difficolta == 1) aggiornaPunteggio(100);
                else if (difficolta == 2) aggiornaPunteggio(200);

                vittorie++;
                if (vittorie == 3) aggiornaPunteggio(100);

                SoundManager.playWin(LobbyInput.effectsPercent);
            }
            else
            {
                // punti sconfitta vs bot
                if (difficolta == 0) aggiornaPunteggio(-20);
                else if (difficolta == 1) aggiornaPunteggio(-100);
                else if (difficolta == 2) aggiornaPunteggio(-200);

                SoundManager.playDefeat(LobbyInput.effectsPercent);
            }
            return;
        }

        // --- se ha appena giocato il player, programma la mossa del bot con delay ---
        if (player == 1) {
            int botCol = CPUBrain.chooseMove(boardState);
            int botRow = gameInput.getLowestFreeRow(botCol, boardStateAsBoolean());

            if (botRow != -1) {
                botPlannedCol = botCol;
                botPlannedRow = botRow;

                botPending = true;
                botDelayTimer = BOT_THINK_DELAY;

                // griglia bloccata per l'utente
                gameInput.setGridEnabled(false);
            }
        }

        // sblocco griglia per l'utente
        if (!isMatchOver && player == 2) {
            gameInput.setGridEnabled(true);
        }
    }


    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(gameInput); // si può togliere? todo: controllare se si può gestire nel GameManager
        // init schermo
        screen.begin();

        // cambio light/dark mode
        isDark(darkMode);

        // aggiorna animazione caduta
        updateDrop(delta);
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
        // disegno griglia con pedine giocate
        drawGame();
        // disegno animazione caduta pedina
        drawDrop();
        // disegno tabella
        screen.draw(table, 248, 71);

        // NOME AZIENDA //
        Fonts.draw(screen, "Drop Logic", 49, 63, Fonts.medium20); // firma al gioco
        // VERSIONE DI GIOCO //
        String text = "Beta " + VersionInfo.getVersion();
        // calcolo larghezza del testo
        GlyphLayout layout = new GlyphLayout(Fonts.medium20, text);
        // stampa testo
        Fonts.medium20.draw(screen, layout, (955 - layout.width), 63);

        // finestra riavvio/chiusura gioco => mostrata solo alla fine di una partita
        draw(replay, isMatchOver, 294, 204);

        // partita conclusa
        if (isMatchOver) {
            // todo: correggere le posizioni dei testi VICTORY/DEFEAT
            // testo vittoria/sconfitta
            if (victory) Fonts.draw(screen, "VICTORY", 49, 63, Fonts.bold40); // vittoria
            else Fonts.draw(screen, "DEFEAT", 49, 63, Fonts.bold40); // sconfitta

            draw(btn_yes, gameInput.isBtnYesExitHover, 342, 244);
            draw(btn_yes_clicked, gameInput.btnYesExit, 342, 244);

            draw(btn_no_clicked, gameInput.btnNoExit, 506, 244);
            draw(btn_no, gameInput.isBtnNoExitHover, 506, 244);

            // reset gioco per la prossima partita
            if (gameInput.btnYesExit) resetGame();
        }

        // LOGICA DI GIOCO (solo se non è aperta la finestra isMatchOver)
        if (!isMatchOver && gameInput.isHole) {
            // durante animazione o “pensiero bot” ignora input del player
            if (dropActive || botPending) gameInput.isHole = false;

            // todo: capire perché non funzionano mai e risolvere il bug
            // pulsante in alto a dx per chiudere la partita
            draw(exit, gameInput.isBtnExitClicked, 825, 591);
            draw(exitHover, gameInput.isBtnExitHover, 825, 591);

            int col = gameInput.getColumnFromClick(
                Gdx.input.getX(),
                Gdx.graphics.getHeight() - Gdx.input.getY()
            );
            int row = (col != -1) ? gameInput.getLowestFreeRow(col, boardStateAsBoolean()) : -1;

            if (col != -1 && row != -1) {
                // controlla colonna congelata
                if (freezeColumn == col && freezeTurns > 0) {
                    log.info("Colonna " + col + " congelata per ancora " + freezeTurns + " turni");
                    gameInput.isHole = false;
                }
                else {

                    // POWER-UP USO SUL PROSSIMO CLICK
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
                    startDrop(1, row, col);
                    // chiudi l’input del player fino a fine mossa (animazione + eventuale bot)
                    gameInput.isHole = false;
                }
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
                    dispose(); // rilascio risorse
                    GameManager.soundGame.stop(); // stop musica di gioco
                    game.setScreen(new LobbyManager(game)); // back to lobby
                }
            }
        }
    }

    @Override
    public void loadFont() {}

    @Override
    public void loadImages()
    {
        loadDarkMode();
        loadLightMode();

        red = new Texture("ui/icons/red.png");
        yellow = new Texture("ui/icons/yellow.png");

        btn_no = new Texture("ui/buttons/lobby/btn_no.png");
        btn_no_clicked = new Texture("ui/buttons/lobby/btn_no_clicked.png");

        btn_yes = new Texture("ui/buttons/lobby/btn_yes.png");
        btn_yes_clicked = new Texture("ui/buttons/lobby/btn_yes_clicked.png");
    }

    @Override
    public void dispose()
    {
        gameBG.dispose();
        exit.dispose();
        exitHover.dispose();
        red.dispose();
        yellow.dispose();
        lightGameBG.dispose();
        lightExit.dispose();
        lightExitHover.dispose();
        darkGameBG.dispose();
        darkExit.dispose();
        darkExitHover.dispose();
    }

}
