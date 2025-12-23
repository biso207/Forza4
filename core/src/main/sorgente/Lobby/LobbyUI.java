package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthManager;
import sorgente.Fonts;
import sorgente.Main;
import sorgente.ResourceLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class LobbyUI extends ScreenAdapter implements ResourceLoader
{
    private final Main game;
    private  final SpriteBatch screen;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final LobbyInput lobbyInput;
    private ShapeRenderer shapeRenderer;


    //DARK MODE

    private Texture darkLobby,darkLogout,darkSettings,darkSoftware;
    private Texture darkBigClicked,darkBigHover,darkCenterClicked,darkCenterHover;
    private Texture darkBtnClose,darkBtnCloseClicked;


    //LIGHT MODE

    private Texture lightLobby,lightLogout,lightSettings,lightSoftware;
    private Texture lightBigClicked,lightBigHover,lightCenterClicked,lightCenterHover;
    private Texture lightBtnClose,lightBtnCloseClicked;

    //MODE MODIFICABILE

    private Texture lobby, software_infos, logout, settings,logout_clicked;
    private Texture big_clicked,big_hover,center_clicked,center_hover,mode_clicked,mode_hover;
    private Texture access_clicked,access_hover,infos_clicked,infos_hover;
    private Texture logout_hover,settings_clicked,settings_hover;
    private Texture btn_close,btn_close_clicked;
    private Texture btn_no,btn_yes,btn_no_clicked,btn_yes_clicked;
    private Texture star,star_selected;
    private Texture noMusic,noEffects;
    private int volume;


    public LobbyUI(Main game)
    {
        this.game = game;
        this.screen = game.screen;
        Fonts.load();
        lobbyInput=new LobbyInput();
        shapeRenderer = new ShapeRenderer();

        this.loadImages();

    }

    @Override
    public void loadFont() {}


    public void  loadDarkMode()
    {
         darkLobby=new Texture("lobby_screens/dark/lobby_dark.png");
         darkLogout=new Texture("lobby_screens/dark/logout_dark.png");
         darkSettings=new Texture("lobby_screens/dark/settings_dark.png");
         darkSoftware=new Texture("lobby_screens/dark/software_info_dark.png");

         darkBigClicked=new Texture("ui/buttons/lobby/dark/bottom_big_clicked.png");
         darkBigHover=new Texture("ui/buttons/lobby/dark/bottom_big_hover.png");
         darkCenterClicked=new Texture("ui/buttons/lobby/dark/bottom_center_clicked.png");
         darkCenterHover=new Texture("ui/buttons/lobby/dark/bottom_center_hover.png");
         darkBtnClose=new Texture("ui/buttons/lobby/dark/btn_close.png");

         darkBtnCloseClicked=new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
    }

    public void loadLightMode()
    {
        lightLobby=new Texture("lobby_screens/light/lobby_light.png");
        lightLogout=new Texture("lobby_screens/light/logout_light.png");
        lightSettings=new Texture("lobby_screens/light/settings_light.png");
        lightSoftware=new Texture("lobby_screens/light/software_infos_light.png");

        lightBigClicked=new Texture("ui/buttons/lobby/light/bottom_big_clicked.png");
        lightBigHover=new Texture("ui/buttons/lobby/light/bottom_big_hover.png");
        lightCenterClicked=new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
        lightCenterHover=new Texture("ui/buttons/lobby/light/bottom_center_hover.png");
        lightBtnClose=new Texture("ui/buttons/lobby/light/btn_close.png");

        darkBtnCloseClicked=new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
    }

    public void darkMode(boolean r)
    {
        if(r)
        {
           lobby=darkLobby;
           software_infos=darkSoftware;
           logout=darkLogout;
           settings=darkSettings;

           big_clicked=darkBigClicked;
           big_hover=darkBigHover;
           center_clicked=darkCenterClicked;
           center_hover=darkCenterHover;
           btn_close=darkBtnClose;
           btn_close_clicked=darkBtnCloseClicked;
        }
        else
        {
            lobby=lightLobby;
            software_infos=lightSoftware;
            logout=lightLogout;
            settings=lightSettings;

            big_clicked=lightBigClicked;
            big_hover=lightBigHover;
            center_clicked=darkCenterClicked;
            center_hover=lightCenterHover;
            btn_close=lightBtnClose;
            btn_close_clicked=lightBtnCloseClicked;
        }
    }

    @Override
    public void loadImages()
    {

     //DARK MODE

        loadDarkMode();
        loadLightMode();

        darkMode(false);



      noMusic=new Texture("ui/icons/no_music.png");
      noEffects=new Texture("ui/icons/no_sound.png");

      star=new Texture("ui/icons/star_selected.png");
      star_selected=new Texture("ui/icons/star.png");




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

      btn_no=new Texture("ui/buttons/lobby/btn_no.png");
      btn_no_clicked=new Texture("ui/buttons/lobby/btn_no_clicked.png");

      btn_yes=new Texture("ui/buttons/lobby/btn_yes.png");
      btn_yes_clicked=new Texture("ui/buttons/lobby/btn_yes_clicked.png");


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

        draw(software_infos, lobbyInput.isWindowOpenInfo, 244, 194);
        draw(logout,lobbyInput.isWindowOpenExit,294,204);
        draw(settings,lobbyInput.isWindowOpenSettings,244,223);

        //--- CHIUDI ---


        if(lobbyInput.isWindowOpenInfo)
        {
            draw(btn_close,lobbyInput.isBtnCloseInfoHover,694,438);
            draw(btn_close_clicked, lobbyInput.btnCloseInfo, 694, 438);
        }

        if(lobbyInput.isWindowOpenSettings)
        {

                // 1️⃣ Disegno la finestra normalmente
                screen.draw(settings, 244, 223);

                draw(btn_close,lobbyInput.isBtnCloseSettingsHover,694,410);
                draw(btn_close,lobbyInput.btnCloseSettings,694,410);

                // 2️⃣ Chiudo il batch
                screen.end();

                // 3️⃣ Disegno le barre con ShapeRenderer
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                // --- MUSIC BAR BACKGROUND ---
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
                shapeRenderer.rect(
                    lobbyInput.musicBarArea.x,
                    680-lobbyInput.musicBarArea.y,
                    lobbyInput.musicBarArea.width-8,
                    lobbyInput.musicBarArea.height-10
                );

                // --- MUSIC BAR FILL ---
                shapeRenderer.setColor(0.9f, 0.3f, 0.3f, 1f);
                float musicWidth = AudioSettings.musicVolume * lobbyInput.musicBarArea.width;
                shapeRenderer.rect(
                    lobbyInput.musicBarArea.x,
                    680-lobbyInput.musicBarArea.y,
                    musicWidth-8,
                    lobbyInput.musicBarArea.height-10
                );

                // --- EFFECTS BAR BACKGROUND ---
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
                shapeRenderer.rect(
                    lobbyInput.effectsBarArea.x,
                    680-lobbyInput.effectsBarArea.y,
                    lobbyInput.effectsBarArea.width-8,
                    lobbyInput.effectsBarArea.height-10
                );

                // --- EFFECTS BAR FILL ---
                shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 1f);
                float effectsWidth = AudioSettings.effectsVolume * lobbyInput.effectsBarArea.width;
                shapeRenderer.rect(
                    lobbyInput.effectsBarArea.x,
                    680-lobbyInput.effectsBarArea.y,
                    effectsWidth-8,
                    lobbyInput.effectsBarArea.height-10
                );

                shapeRenderer.end();

                // 4️⃣ Riapro il batch PRIMA di disegnare i font
                screen.begin();


            // --- TEXT PERCENTAGES ---
            Fonts.draw(
                screen,
                (int)(AudioSettings.musicVolume * 100) + "%",
                lobbyInput.musicBarArea.x + lobbyInput.musicBarArea.width + 9,
                690 - lobbyInput.musicBarArea.y + 15,
                Fonts.bold20
            );

            Fonts.draw(
                screen,
                (int)(AudioSettings.effectsVolume * 100) + "%",
                lobbyInput.effectsBarArea.x + lobbyInput.effectsBarArea.width + 9,
                690 - lobbyInput.effectsBarArea.y + 15,
                Fonts.bold20
            );

            volume=(int) (AudioSettings.musicVolume*100);

            if(volume == 0)
            {
               draw(noMusic,true,266,308);
            }

            volume=(int) (AudioSettings.effectsVolume*100);

            if(volume == 0 )
            {
               draw(noEffects,true,266,256);
            }

        }

        if(lobbyInput.isWindowOpenExit)
        {
            draw(btn_yes,lobbyInput.isBtnYesExitHover,341,246);
            draw(btn_yes_clicked,lobbyInput.btnYesExit,341,246);

            if(lobbyInput.btnYesExit)
            {
                goToAuth=true;
            }

            draw(btn_no_clicked,lobbyInput.btnNoExit,507,246);
            draw(btn_no,lobbyInput.isBtnNoExitHover,507,246);

        }

        for (int i = 0; i < 8; i++)
        {
            if (!lobbyInput.starHover[i])
            {
                continue; // se è false, salta
            }

            switch (i)
            {
                case 0:
                    draw(star_selected, true, 130, 312);   // Classic
                    break;

                case 1:
                    draw(star_selected, true, 160, 312);
                    break;

                case 2:
                    draw(star_selected, true, 370, 312);
                    break;

                case 3:
                    draw(star_selected, true, 400, 312);
                    break;

                case 4:
                    draw(star_selected, true, 610, 312);
                    break;

                case 5:
                    draw(star_selected, true, 640, 312);
                    break;

                case 6:
                    draw(star_selected, true, 850, 312);
                    break;

                case 7:
                    draw(star_selected, true, 880, 312);
                    break;
            }

        }

        for (int i = 0; i < 8; i++)
        {
            // Disegna solo se la stella è in hover OPPURE è cliccata
            if (!lobbyInput.starClicked[i])
                continue;

            switch (i)
            {
                case 0:
                    draw(star, true, 130, 312);   // Classic stella 1
                    break;

                case 1:
                    draw(star, true, 160, 312);   // Classic stella 2
                    break;

                case 2:
                    draw(star, true, 370, 312);   // Gravity4 stella 1
                    break;

                case 3:
                    draw(star, true, 400, 312);   // Gravity4 stella 2
                    break;

                case 4:
                    draw(star, true, 610, 312);   // Horizontal stella 1
                    break;

                case 5:
                    draw(star, true, 640, 312);   // Horizontal stella 2
                    break;

                case 6:
                    draw(star, true, 850, 312);   // Speedy stella 1
                    break;

                case 7:
                    draw(star, true, 880, 312);   // Speedy stella 2
                    break;
            }

        }


        darkMode(lobbyInput.isBtnSwitch);


        screen.end();

        if (goToAuth)
        {
            game.setScreen(new AuthManager(game));
            return;
        }


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
