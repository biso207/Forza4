package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
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
    private final LobbyInput lobbyInput;

    private Texture lobby, software_infos_light, logout_light,settings_light,logout_clicked;
    private Texture big_clicked,big_hover,center_clicked,center_hover,mode_clicked,mode_hover;
    private Texture access_clicked,access_hover,infos_clicked,infos_hover;
    private Texture logout_hover,settings_clicked,settings_hover;
    private Texture btn_close,btn_close_clicked;

    public LobbyUI(Main game)
    {
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

      lobby= new Texture("lobby_screens/light/lobby_light.png");
      software_infos_light= new Texture("lobby_screens/light/software_infos_light.png");

      logout_light=new Texture("lobby_screens/light/logout_light.png");
      settings_light=new Texture("lobby_screens/light/settings_light.png");

      btn_close=new Texture("ui/buttons/lobby/light/btn_close.png");
      btn_close_clicked=new Texture("ui/buttons/lobby/light/btn_close_clicked.png");

      big_clicked=new Texture("ui/buttons/lobby/light/bottom_big_clicked.png");
      big_hover=new Texture("ui/buttons/lobby/light/bottom_big_hover.png");

      center_clicked=new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
      center_hover=new Texture("ui/buttons/lobby/light/bottom_center_hover.png");

      mode_clicked=new Texture("ui/buttons/lobby/light/game_mode_clicked.png");
      mode_hover= new Texture("ui/buttons/lobby/light/game_mode_hover.png");

      access_hover=new Texture("ui/icons/accessibility.png");
      access_clicked= new Texture("ui/icons/accessibility_clicked.png");

      infos_hover=new Texture("ui/icons/infos.png");
      infos_clicked=new Texture("ui/icons/infos_clicked.png");

      logout_hover=new Texture("ui/icons/logout.png");
      logout_clicked=new Texture("ui/icons/logout_clicked.png");

      settings_hover=new Texture("ui/icons/settings.png");
      settings_clicked=new Texture("ui/icons/settings_clicked.png");


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
        Gdx.input.setInputProcessor(lobbyInput);

        screen.begin();
        screen.draw(lobby, 0, 0);

        // --- GAME MODES ---
        draw(mode_hover, lobbyInput.classicHover,37, 305);
        draw(mode_hover, lobbyInput.gravity4Hover,275, 305);
        draw(mode_hover, lobbyInput.horizontalHover,512, 305);
        draw(mode_hover, lobbyInput.speedyHover,752, 305);

        draw(mode_clicked, lobbyInput.classic,37, 305);
        draw(mode_clicked, lobbyInput.gravity4,275, 305);
        draw(mode_clicked, lobbyInput.horizontal,512, 305);
        draw(mode_clicked, lobbyInput.speedy,752, 305);


        // --- SECONDARY BUTTONS ---

        draw(big_hover,    lobbyInput.marketHover,37,90);
        draw(center_hover, lobbyInput.scoreboardHover,370,90);
        draw(big_hover,    lobbyInput.dailyHover,660,90);

        draw(big_clicked,    lobbyInput.market,38,90);
        draw(center_clicked, lobbyInput.scoreboard,370,90);
        draw(big_clicked,    lobbyInput.daily,660,90);

        // --- BOTTOM ICONS ---
        draw(logout_hover, lobbyInput.exitHover,398,41);
        draw(infos_hover, lobbyInput.informationHover,452,40);
        draw(access_hover, lobbyInput.accessibilityHover,570,40);
        draw(settings_hover, lobbyInput.settingsHover,510,40);

        draw(logout_clicked, lobbyInput.exit,398,41);
        draw(infos_clicked, lobbyInput.information,452,40);
        draw(access_clicked, lobbyInput.man,570,40);
        draw(settings_clicked, lobbyInput.settings,510,40);

        // --- WINDOWS ---

        draw(software_infos_light, lobbyInput.isWindowOpenInfo, 244, 194);
        draw(logout_light,lobbyInput.isWindowOpenExit,294,204);
        draw(settings_light,lobbyInput.isWindowOpenSettings,244,223);

        //--- CHIUDI ---


        if(lobbyInput.isWindowOpenInfo)
        {
            draw(btn_close,lobbyInput.isBtnCloseInfoHover,694,438);
            draw(btn_close_clicked, lobbyInput.btnCloseInfo, 694, 438);
        }

        if(lobbyInput.isWindowOpenSettings)
        {
            draw(btn_close,lobbyInput.isBtnCloseSettingsHover,694,410);
            draw(btn_close,lobbyInput.btnCloseSettings,694,410);
        }

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
