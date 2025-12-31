package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.Fonts;
import sorgente.Lobby.LobbyManager;
import sorgente.Main;
import sorgente.ResourceLoader;

public class GameUI extends ScreenAdapter implements ResourceLoader {
    private static final Log log = LogFactory.getLog(GameUI.class);
    private final Main game;
    private final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final GameInput gameInput = new GameInput();
    private ShapeRenderer shapeRenderer;

    private boolean darkMode;

    private float modeTransitionTimer = 0f;
    private boolean modeTransition = false;
    private int pendingMode = -1;

    // Light Texture
    private Texture lightForza;
    private Texture lightExit;
    private Texture lightExitHover;

    // Dark Texture
    private Texture darkForza;
    private Texture darkExit;
    private Texture darkExitHover;

    // Editable Texture
    private Texture forza;
    private Texture exit;
    private Texture exitHover;

    private Texture red;
    private Texture yellow;

    // Stato della griglia: 0 = vuoto, 1 = rosso
    private int[][] boardState = new int[6][7];

    public GameUI(Main game, boolean dark) {
        this.game = game;
        this.screen = game.screen;
        Fonts.load();
        shapeRenderer = new ShapeRenderer();
        darkMode = dark;
        this.loadImages();

        // Inizializza boardState
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                boardState[r][c] = 0;
            }
        }
    }

    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) {
            screen.draw(texture, x, y);
        }
    }

    private void loadDarkMode() {
        lightForza = new Texture("game_mods_screens/base_light.png");
        lightExit = new Texture("ui/buttons/lobby/light/btn_close.png");
        lightExitHover = new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
    }

    private void loadLightMode() {
        darkForza = new Texture("game_mods_screens/base_dark.png");
        darkExit = new Texture("ui/buttons/lobby/dark/btn_close.png");
        darkExitHover = new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
    }

    private void isDark(boolean dark) {
        if (dark) {
            forza = darkForza;
            exit = darkExit;
            exitHover = darkExitHover;
        } else {
            forza = lightForza;
            exit = lightExit;
            exitHover = lightExitHover;
        }
    }

    private boolean[][] boardStateAsBoolean() {
        boolean[][] b = new boolean[6][7];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                b[r][c] = boardState[r][c] != 0;
            }
        }
        return b;
    }

    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(gameInput);
        screen.begin();

        isDark(darkMode);
        screen.draw(forza, 0, 0);

        draw(exit, gameInput.isBtnExitClicked, 840, 600);
        draw(exitHover, gameInput.isBtnExitHover, 840, 600);

        float baseX = 269;
        float baseY = 130;

// 🔴 Disegna le pedine rosse
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

                drawX += 71;
            }
        }


        screen.end();

        if (gameInput.isHole) {
            int col = gameInput.getColumnFromClick(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

            if (col != -1) {
                int row = gameInput.getLowestFreeRow(col, boardStateAsBoolean());

                if (row != -1) {
                    boardState[row][col] = 1; // 1 = rosso
                } else {
                    log.warn("Colonna " + col + " piena, impossibile inserire pedina.");
                }

                gameInput.isHole = false; // resetta sempre il flag
            }
        }


        if (gameInput.isBtnExitClicked) {
            modeTransition = true;
            pendingMode = 0;
        }

        if (modeTransition) {
            modeTransitionTimer += delta;
            if (modeTransitionTimer >= 0.14f) {
                if (pendingMode == 0) {
                    game.setScreen(new LobbyManager(game));
                    return;
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
    }

    @Override
    public void dispose() {
        forza.dispose();
    }
}
