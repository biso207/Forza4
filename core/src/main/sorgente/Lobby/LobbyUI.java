package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthAlgorithms;
import sorgente.Fonts;
import sorgente.Main;
import sorgente.ResourceLoader;
import sorgente.UserData.UserProgressService;

public class LobbyUI extends ScreenAdapter implements ResourceLoader
{
    private final Main game;
    private  final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private final LobbyInput lobbyInput;

    private final boolean isDark = (boolean) UserProgressService.getProgress("darkMode"); // recuperata dai dati utente caricati all'accesso

    // light and dark mode textures
    private Texture lobby, settings, bigClicked, bigHover, centerClicked, centerHover, modeClicked, modeHover;
    private Texture lobby_dark, settings_light, bigClicked_dark, bigHover_dark, centerClicked_dark, centerHover_dark, modeClicked_dark, modeHover_dark;
    private Texture lobby_light, settings_dark, bigClicked_light, bigHover_light, centerClicked_light, centerHover_light, modeClicked_light, modeHover_light;

    public LobbyUI(Main game) {
        this.game = game;
        this.screen = game.screen;
        Fonts.load();
        lobbyInput=new LobbyInput();
        this.loadImages();

    }

    @Override
    public void loadFont() {}

    @Override
    public void loadImages() {
        // -- Light Mode --
        lobby_light = new Texture("lobby_screens/light/lobby_light.png");
        settings_light = new Texture("lobby_screens/light/settings_light.png");
        bigClicked_light = new Texture("ui/buttons/lobby/light/bottom_bigClicked.png");
        bigHover_light = new Texture("ui/buttons/lobby/light/bottom_bigHover.png");
        centerClicked_light = new Texture("ui/buttons/lobby/light/bottom_centerClicked.png");
        centerHover_light = new Texture("ui/buttons/lobby/light/bottom_centerHover.png");
        modeClicked_light = new Texture("ui/buttons/lobby/light/game_modeClicked.png");
        modeHover_light = new Texture("ui/buttons/lobby/light/game_modeHover.png");

        // -- Dark Mode --
        lobby_dark = new Texture("lobby_screens/dark/lobby_dark.png");
        settings_dark = new Texture("lobby_screens/dark/settings_dark.png");
        bigClicked_dark = new Texture("ui/buttons/lobby/dark/bottom_bigClicked.png");
        bigHover_dark = new Texture("ui/buttons/lobby/dark/bottom_bigHover.png");
        centerClicked_dark = new Texture("ui/buttons/lobby/dark/bottom_centerClicked.png");
        centerHover_dark = new Texture("ui/buttons/lobby/dark/bottom_centerHover.png");
        modeClicked_dark = new Texture("ui/buttons/lobby/dark/game_modeClicked.png");
        modeHover_dark = new Texture("ui/buttons/lobby/dark/game_modeHover.png");

        // background
        lobby = isDark ? lobby_dark : lobby_light;

        // hover textures
        modeHover   = isDark ? modeHover_dark   : modeHover_light;
        bigHover    = isDark ? bigHover_dark    : bigHover_light;
        centerHover = isDark ? centerHover_dark : centerHover_light;

        // clicked textures
        modeClicked = isDark ? modeClicked_dark : modeClicked_light;
        bigClicked  = isDark ? bigClicked_dark  : bigClicked_light;
        centerClicked = isDark ? centerClicked_dark : centerClicked_light;

        // secondary pages
        settings = isDark ? settings_dark : settings_light;
    }

    // metodo per creare l'effetto hover dei pulsanti
    private void drawHover(Texture texture, boolean hover, float x, float y) {if (hover)screen.draw(texture, x, y);}

    // metodo per disegnare le grafiche delle impostazioni
    private void drawSettings() {
        screen.draw(settings, 250, (float)228.5);

        // pulsante audio
        // pulsante suono
        // pulsante dark-mode/light-mode
        // pulsante animations on/off
        // pulsante X close settings
    }

    @Override
    public void render(float delta) {
        Gdx.input.setInputProcessor(lobbyInput);

        screen.begin();

        // background
        screen.draw(lobby, 0, 0);

        // --- Game Mods ---
        drawHover(modeHover, lobbyInput.classicHover,    35, 304);
        drawHover(modeHover, lobbyInput.gravity4Hover,   275, 304);
        drawHover(modeHover, lobbyInput.horizontalHover, 513, 304);
        drawHover(modeHover, lobbyInput.speedyHover,     753, 304);

        // --- Secondary Buttons ---
        drawHover(bigHover,    lobbyInput.marketHover,     35,  91);
        drawHover(centerHover, lobbyInput.scoreboardHover, 369, 91);
        drawHover(bigHover,    lobbyInput.dailyHover,      660, 91);

        // --- Bottom Icons ---
        drawHover(centerHover, lobbyInput.exitHover,          397, 649);
        drawHover(centerHover, lobbyInput.informationHover,   459, 637);
        drawHover(centerHover, lobbyInput.settingsHover,      519, 649);
        drawHover(centerHover, lobbyInput.accessibilityHover, 584, 649);

        // --- Secondary Screens ---
        if (lobbyInput.settings) drawSettings();

        screen.end();
    }

    @Override
    public void dispose() {
        // Dispose light mode textures
        lobby_light.dispose();
        settings_light.dispose();
        bigClicked_light.dispose();
        bigHover_light.dispose();
        centerClicked_light.dispose();
        centerHover_light.dispose();
        modeClicked_light.dispose();
        modeHover_light.dispose();

        // Dispose dark mode textures
        lobby_dark.dispose();
        settings_dark.dispose();
        bigClicked_dark.dispose();
        bigHover_dark.dispose();
        centerClicked_dark.dispose();
        centerHover_dark.dispose();
        modeClicked_dark.dispose();
        modeHover_dark.dispose();

        // Dispose current mode textures
        lobby.dispose();
        settings.dispose();
        bigClicked.dispose();
        bigHover.dispose();
        centerClicked.dispose();
        centerHover.dispose();
        modeHover.dispose();
        modeClicked.dispose();
    }
}
