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

public class LobbyUI extends ScreenAdapter implements ResourceLoader
{
    private final Main game;
    private  final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private final LobbyInput lobbyInput;

    private Texture lobby, big_clicked, big_hover, center_clicked, center_hover, mode_clicked, mode_hover;

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
    public void loadImages()
    {
      lobby = new Texture("lobby_screens/light/lobby_light.png");
      big_clicked = new Texture("ui/buttons/lobby/light/bottom_big_clicked.png");
      big_hover = new Texture("ui/buttons/lobby/light/bottom_big_hover.png");
      center_clicked = new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
      center_hover = new Texture("ui/buttons/lobby/light/bottom_center_hover.png");
      mode_clicked = new Texture("ui/buttons/lobby/light/game_mode_clicked.png");
      mode_hover = new Texture("ui/buttons/lobby/light/game_mode_hover.png");

      // todo: caricare anche le dark mode, poi vedi te come gestire le stampe
    }

    private void drawHover(Texture texture, boolean hover, float x, float y)
    {
        if (hover)
        {
            screen.draw(texture, x, y);
        }
    }


    @Override
    public void render(float delta)
    {
        Gdx.input.setInputProcessor(lobbyInput);

        screen.begin();
        screen.draw(lobby, 0, 0);

        // --- GAME MODES ---
        drawHover(mode_hover, lobbyInput.classicHover,    35, 304);
        drawHover(mode_hover, lobbyInput.gravity4Hover,   275, 304);
        drawHover(mode_hover, lobbyInput.horizontalHover, 513, 304);
        drawHover(mode_hover, lobbyInput.speedyHover,     753, 304);

        // --- SECONDARY BUTTONS ---
        drawHover(big_hover,    lobbyInput.marketHover,     35,  91);
        drawHover(center_hover, lobbyInput.scoreboardHover, 369, 91);
        drawHover(big_hover,    lobbyInput.dailyHover,      660, 91);

        // --- BOTTOM ICONS ---
        drawHover(center_hover, lobbyInput.exitHover,          397, 649);
        drawHover(center_hover, lobbyInput.informationHover,   459, 637);
        drawHover(center_hover, lobbyInput.accessibilityHover, 584, 649);
        drawHover(center_hover, lobbyInput.settingsHover,      519, 649);

        screen.end();
    }

    @Override
    public void dispose()
    {
        lobby.dispose();
        big_clicked.dispose();
        big_hover.dispose();
        center_clicked.dispose();
        center_hover.dispose();
        mode_hover.dispose();
        mode_clicked.dispose();
    }
}
