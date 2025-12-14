package sorgente.Lobby;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private Texture lobby,big_clicked,big_hover,center_clicked,center_hover,mode_clicked,mode_hover;

    public LobbyUI(Main game) {
        this.game = game;
        this.screen = game.screen;
        Fonts.load();
        this.loadImages();

    }

    @Override
    public void loadFont() {}

    @Override
    public void loadImages()
    {
      lobby= new Texture("lobby_screens/lobby.png");
      big_clicked=new Texture("ui/buttons/lobby/bottom_big_clicked.png");
      big_hover=new Texture("ui/buttons/lobby/bottom_big_hover.png");
      center_clicked=new Texture("ui/buttons/lobby/bottom_center_clicked.png");
      center_hover=new Texture("ui/buttons/lobby/bottom_center_hover.png");
      mode_clicked=new Texture("ui/buttons/lobby/game_mode_clicked.png");
      mode_hover= new Texture("ui/buttons/lobby/game_mode_clicked.png");


    }

    @Override
    public void render(float delta)
    {
        screen.begin();
        screen.draw(lobby, 0, 0);
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
