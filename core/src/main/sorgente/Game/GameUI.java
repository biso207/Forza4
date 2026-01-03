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

    // Stato della griglia: 0 = vuoto, 1 = rosso
    private int[][] boardState = new int[6][7];
    private int  punti= (int) UserProgressService.getProgress("points");
    private int vittorie=0;


    public GameUI(Main game, GameInput in, boolean dark, int d, int mod)
    {
        this.game = game;
        this.screen = game.screen;
        this.mod=mod;

        difficolta=d;
        rigioca=false;

        Fonts.load();
        shapeRenderer = new ShapeRenderer();
        darkMode = dark;
        gameInput=in;
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
        lightReplay =new Texture("lobby_screens/light/logout_light.png");
    }

    private void loadLightMode()
    {
        darkForza = new Texture("game_mods_screens/base_dark.png");
        darkExit = new Texture("ui/buttons/lobby/dark/btn_close.png");
        darkExitHover = new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
        darkReplay =new Texture("lobby_screens/dark/logout_dark.png");
    }

    private void aggiornaPunteggio(int delta)
    {
        punti += delta;

        // Evita punteggi negativi
        if (punti < 0) {
            punti = 0;
        }

        // Salva il punteggio aggiornato
        UserProgressService.setProgress("points", punti);
    }


    private void isDark(boolean dark)
    {
        if (dark)
        {
            forza = darkForza;
            exit = darkExit;
            exitHover = darkExitHover;
            replay=darkReplay;
        }
        else
        {
            forza = lightForza;
            exit = lightExit;
            exitHover = lightExitHover;
            replay=lightReplay;
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

        // Reset vittorie
        vittorie = 0;

        // Reset input
        gameInput.isHole = false;
        gameInput.isBtnExitClicked = false;
        gameInput.isBtnExitHover = false;

        gameInput.btnYesExit = false;
        gameInput.btnNoExit = false;
        gameInput.isBtnYesExitHover = false;
        gameInput.isBtnNoExitHover = false;

        // Reset transizioni
        modeTransition = false;
        pendingMode = -1;
        modeTransitionTimer = 0f;

        log.info("Gioco resettato correttamente");
    }


    @Override
    public void render(float delta)
    {
        Gdx.input.setInputProcessor(gameInput);
        screen.begin();

        isDark(darkMode);
        screen.draw(forza, 0, 0);


        draw(replay,rigioca,294,204);

        // logout
        if (rigioca)
        {

            draw(btn_yes,gameInput.isBtnYesExitHover,342,244);
            draw(btn_yes_clicked,gameInput.btnYesExit,342,244);

            draw(btn_no_clicked,gameInput.btnNoExit,506,244);
            draw(btn_no,gameInput.isBtnNoExitHover,506,244);

            if(gameInput.btnYesExit)
            {
                resetGame();
                rigioca=false;
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

                for (int col = 0; col < 7; col++) {
                    if (boardState[row][col] == 1) {
                        draw(red, true, drawX, drawY);
                    }

                    if (boardState[row][col] == 2) {
                        draw(yellow, true, drawX, drawY);
                    }

                    drawX += 71;
                }
            }
        }

        screen.end();

        if (gameInput.isHole)
        {
            int col = gameInput.getColumnFromClick(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            int row = gameInput.getLowestFreeRow(col, boardStateAsBoolean());

            if (col != -1 && row != -1)
            {
                boardState[row][col] = 1; // rosso

                SoundManager.playDigitSound(LobbyInput.effectsPercent);

                if (modeInputManager.checkWin(boardState, row, col, 1))
                {

                    // Aggiunge punti in base alla difficoltà
                    if (difficolta == 0) aggiornaPunteggio(20);
                    if (difficolta == 1) aggiornaPunteggio(100);
                    if (difficolta == 2) aggiornaPunteggio(200);

                    vittorie++;

                    // Bonus ogni 3 vittorie
                    if (vittorie == 3) {
                        aggiornaPunteggio(100);
                        vittorie = 0;
                    }

                    log.info("Giocatore rosso ha vinto!");
                    log.info(punti);

                    rigioca=true;

                    SoundManager.playWin(LobbyInput.effectsPercent);
                }

            }

            gameInput.isHole = false;

            // 🔁 Mossa del bot
            int botCol = modeInputManager.chooseMove(boardState);
            int botRow = gameInput.getLowestFreeRow(botCol, boardStateAsBoolean());

            if (botRow != -1)
            {
                boardState[botRow][botCol] = 2; // giallo

                SoundManager.playDigitSound(LobbyInput.effectsPercent);

                if (modeInputManager.checkWin(boardState, botRow, botCol, 2)) {

                    // Toglie punti in base alla difficoltà
                    if (difficolta == 0) aggiornaPunteggio(-20);
                    if (difficolta == 1) aggiornaPunteggio(-100);
                    if (difficolta == 2) aggiornaPunteggio(-200);

                    log.info("Giocatore giallo ha vinto!");

                    rigioca=true;

                    SoundManager.playDefeat(LobbyInput.effectsPercent);
                }

            }





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

        btn_no=new Texture("ui/buttons/lobby/btn_no.png");
        btn_no_clicked=new Texture("ui/buttons/lobby/btn_no_clicked.png");

        btn_yes=new Texture("ui/buttons/lobby/btn_yes.png");
        btn_yes_clicked=new Texture("ui/buttons/lobby/btn_yes_clicked.png");
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
