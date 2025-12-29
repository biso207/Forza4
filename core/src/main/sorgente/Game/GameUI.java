package sorgente.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.w3c.dom.Text;
import sorgente.Fonts;
import sorgente.LoadingScreen;
import sorgente.Lobby.LobbyInput;
import sorgente.Main;
import sorgente.ResourceLoader;

public class GameUI extends ScreenAdapter implements ResourceLoader
{
    private final Main game;
    private  final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final GameInput gameInput = new GameInput();
    private ShapeRenderer shapeRenderer;

    private boolean darkMode;

    private Texture forza;

    private Texture exit;
    private Texture exitHover;




    public GameUI(Main game, boolean dark)
    {
        this.game = game;
        this.screen = game.screen;
        Fonts.load();
        shapeRenderer = new ShapeRenderer();

        darkMode=dark;


        this.loadImages();

    }

    private void draw(Texture texture, boolean response, float x, float y)
    {
        if (response)
        {
            screen.draw(texture, x, y);
        }
    }

    @Override
    public void render(float delta)
    {
        Gdx.input.setInputProcessor(gameInput);
        screen.begin();

        screen.draw(forza,0,0);


           draw(exit, gameInput.isBtnExitClicked, 122,122);


        screen.end();
    }

    @Override
    public void loadFont() {

    }

    @Override
    public void loadImages()
    {

       if(darkMode)
       {
           forza = new Texture("game_mods_screens/base_dark.png");
           exit= new Texture("ui/buttons/lobby/dark/btn_close.png");
           exitHover=new Texture("ui/buttons/lobby/light/btn_close.png");
       }
       else
       {
           forza = new Texture("game_mods_screens/base_light.png");
           exit= new Texture("ui/buttons/lobby/light/btn_close.png");
           exitHover=new Texture("ui/buttons/lobby/light/btn_close.png");
       }
    }

    @Override
    public void dispose()
    {
        forza.dispose();

    }
}
