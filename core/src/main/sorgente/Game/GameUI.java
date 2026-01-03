package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.Fonts;
import sorgente.Lobby.LobbyInput;
import sorgente.Lobby.LobbyManager;
import sorgente.Main;
import sorgente.ResourceLoader;
import sorgente.SoundManager;
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
    private final ModeInputManager modeInputManager;

    private ShapeRenderer shapeRenderer;

    private boolean darkMode;
    static int mod;

    private float modeTransitionTimer = 0f;
    private boolean modeTransition = false;
    static boolean rigioca;

    private int pendingMode = -1;

    // Light Texture
    private Texture lightForza;
    private Texture lightExit;
    private Texture lightExitHover;
    private Texture lightReplay;

    // Dark Texture
    private Texture darkForza;
    private Texture darkExit;
    private Texture darkExitHover;
    private Texture darkReplay;

    // Editable Texture
    private Texture forza;
    private Texture exit;
    private Texture exitHover;
    private Texture replay;

    private Texture red;
    private Texture yellow;

    private Texture btn_no;
    private Texture btn_yes;

    private Texture btn_no_clicked;
    private Texture btn_yes_clicked;

    // Stato della griglia: 0 = vuoto, 1 = rosso / 2 = giallo
    private int[][] boardState = new int[6][7];
    private int punti = (int) UserProgressService.getProgress("points");
    private int vittorie = 0;

    // Freeze power-up
    private int freezeColumn = -1;
    private int freezeTurns = 0;

    public GameUI(Main game, GameInput in, boolean dark, int d, int mod)
    {
        this.game = game;
        this.screen = game.screen;
        this.mod = mod;

        difficolta = d;
        rigioca = false;

        Fonts.load();
        shapeRenderer = new ShapeRenderer();
        darkMode = dark;
        gameInput = in;
        modeInputManager = new ModeInputManager(d, 2, mod);
        loadImages();

        log.info(punti);

        // Inizializza boardState
        for (int r = 0; r < 6; r++)
        {
            for (int c = 0; c < 7; c++)
            {
                boardState[r][c] = 0;
            }
        }
    }

    private void draw(Texture texture, boolean response, float x, float y)
    {
        if (response) {
            screen.draw(texture, x, y);
        }
    }

    private void loadDarkMode()
    {
        lightForza = new Texture("game_mods_screens/base_light.png");
        lightExit = new Texture("ui/buttons/lobby/light/btn_close.png");
        lightExitHover = new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
        lightReplay = new Texture("lobby_screens/light/logout_light.png");
    }

    private void loadLightMode()
    {
        darkForza = new Texture("game_mods_screens/base_dark.png");
        darkExit = new Texture("ui/buttons/lobby/dark/btn_close.png");
        darkExitHover = new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
        darkReplay = new Texture("lobby_screens/dark/logout_dark.png");
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
        if (dark)
        {
            forza = darkForza;
            exit = darkExit;
            exitHover = darkExitHover;
            replay = darkReplay;
        }
        else
        {
            forza = lightForza;
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
        // Reset griglia
        for (int r = 0; r < 6; r++)
        {
            for (int c = 0; c < 7; c++)
            {
                boardState[r][c] = 0;
            }
        }

        vittorie = 0;

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

    @Override
    public void render(float delta)
    {
        Gdx.input.setInputProcessor(gameInput);
        screen.begin();

        isDark(darkMode);
        screen.draw(forza, 0, 0);

        // finestra rigioca
        draw(replay, rigioca, 294, 204);

        if (rigioca)
        {
            draw(btn_yes, gameInput.isBtnYesExitHover, 342, 244);
            draw(btn_yes_clicked, gameInput.btnYesExit, 342, 244);

            draw(btn_no_clicked, gameInput.btnNoExit, 506, 244);
            draw(btn_no, gameInput.isBtnNoExitHover, 506, 244);

            if (gameInput.btnYesExit)
            {
                resetGame();
                rigioca = false;
            }
        }
        else
        {
            draw(exit, gameInput.isBtnExitClicked, 840, 600);
            draw(exitHover, gameInput.isBtnExitHover, 840, 600);

            float baseX = 269;
            float baseY = 130;

            for (int row = 5; row >= 0; row--)
            {
                float drawY = baseY + (5 - row) * 64;
                float drawX = baseX;

                for (int col = 0; col < 7; col++)
                {
                    if (boardState[row][col] == 1)
                    {
                        draw(red, true, drawX, drawY);
                    }

                    if (boardState[row][col] == 2)
                    {
                        draw(yellow, true, drawX, drawY);
                    }

                    drawX += 71;
                }
            }
        }

        screen.end();

        // LOGICA DI GIOCO (solo se non è aperta la finestra rigioca)
        if (!rigioca && gameInput.isHole)
        {
            int col = gameInput.getColumnFromClick(
                Gdx.input.getX(),
                Gdx.graphics.getHeight() - Gdx.input.getY()
            );
            int row = (col != -1) ? gameInput.getLowestFreeRow(col, boardStateAsBoolean()) : -1;

            if (col != -1 && row != -1)
            {
                // controlla colonna congelata
                if (freezeColumn == col && freezeTurns > 0)
                {
                    log.info("Colonna " + col + " congelata per ancora " + freezeTurns + " turni");
                    gameInput.isHole = false;
                    return;
                }

                // POWER-UP USO SUL PROSSIMO CLICK
                if (gameInput.powerExplosive)
                {
                    applicaExplosive(row, col);
                    gameInput.powerExplosive = false;
                }
                else if (gameInput.powerSwap)
                {
                    if (gameInput.selectedSwapColumn == -1)
                    {
                        // primo click: seleziona colonna
                        gameInput.selectedSwapColumn = col;
                        log.info("Prima colonna per Swap selezionata: " + col);
                    }
                    else
                    {
                        // secondo click: esegue swap
                        applicaSwap(gameInput.selectedSwapColumn, col);
                        gameInput.selectedSwapColumn = -1;
                        gameInput.powerSwap = false;
                    }
                }
                else if (gameInput.powerFreeze)
                {
                    applicaFreeze(col);
                    gameInput.powerFreeze = false;
                }
                else if (gameInput.powerWild)
                {
                    applicaWild(row, col);
                    gameInput.powerWild = false;
                }
                else
                {
                    // normale pedina rossa
                    boardState[row][col] = 1;
                }

                SoundManager.playDigitSound(LobbyInput.effectsPercent);

                // decrementa freeze se attivo
                if (freezeTurns > 0)
                {
                    freezeTurns--;
                    if (freezeTurns == 0) freezeColumn = -1;
                }

                // check vittoria giocatore
                if (modeInputManager.checkWin(boardState, row, col, 1))
                {
                    if (difficolta == 0) aggiornaPunteggio(20);
                    if (difficolta == 1) aggiornaPunteggio(100);
                    if (difficolta == 2) aggiornaPunteggio(200);

                    vittorie++;

                    if (vittorie == 3) {
                        aggiornaPunteggio(100);
                        vittorie = 0;
                    }

                    log.info("Giocatore rosso ha vinto!");
                    log.info(punti);

                    rigioca = true;

                    SoundManager.playWin(LobbyInput.effectsPercent);
                }
                else
                {
                    // se non ha vinto, turno del bot
                    int botCol = modeInputManager.chooseMove(boardState);
                    int botRow = gameInput.getLowestFreeRow(botCol, boardStateAsBoolean());

                    if (botRow != -1)
                    {
                        boardState[botRow][botCol] = 2;

                        SoundManager.playDigitSound(LobbyInput.effectsPercent);

                        if (modeInputManager.checkWin(boardState, botRow, botCol, 2))
                        {
                            if (difficolta == 0) aggiornaPunteggio(-20);
                            if (difficolta == 1) aggiornaPunteggio(-100);
                            if (difficolta == 2) aggiornaPunteggio(-200);

                            log.info("Giocatore giallo ha vinto!");

                            rigioca = true;

                            SoundManager.playDefeat(LobbyInput.effectsPercent);
                        }
                    }
                }
            }

            gameInput.isHole = false;
        }

        if (gameInput.isBtnExitClicked || gameInput.btnNoExit) {
            modeTransition = true;
            pendingMode = 0;
        }

        if (modeTransition) {
            modeTransitionTimer += delta;
            if (modeTransitionTimer >= 0.14f) {
                if (pendingMode == 0)
                {
                    GameManager.soundGame.stop();
                    game.setScreen(new LobbyManager(game));
                    return;
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
        forza.dispose();
        exit.dispose();
        exitHover.dispose();
        red.dispose();
        yellow.dispose();
        lightForza.dispose();
        lightExit.dispose();
        lightExitHover.dispose();
        darkForza.dispose();
        darkExit.dispose();
        darkExitHover.dispose();
    }

}
