/*
Forza4 • class LobbyInput •
Gestisce la grafica della lobby
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthManager;
import sorgente.Fonts;
import sorgente.Game.GameManager;
import sorgente.LoadingScreen;
import sorgente.Main;
import sorgente.ResourceLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import sorgente.UserData.UserProgressService;


public class LobbyUI implements ResourceLoader {
    private final Main game;
    private  final SpriteBatch screen;

    private Boolean darkMode;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;
    private boolean goToAuth = false;

    private final LobbyInput lobbyInput;
    private ShapeRenderer shapeRenderer;


    // DARK MODE
    private Texture darkLobby,darkLogout,darkSettings,darkCredits, darkScoreboard, darkMarket;
    private Texture darkBigClicked,darkBigHover,darkCenterClicked,darkCenterHover;
    private Texture darkBtnClose,darkBtnCloseClicked;
    private Texture darkBtnMarket,darkBtnMarketClicked;
    private Texture volume_bar_dark;


    // LIGHT MODE
    private Texture lightLobby,lightLogout,lightSettings,lightCredits, lightScoreboard, lightMarket;
    private Texture lightBigClicked,lightBigHover,lightCenterClicked,lightCenterHover;
    private Texture lightBtnClose,lightBtnCloseClicked;
    private Texture lightBtnMarket,lightBtnMarketClicked;
    private Texture volume_bar_light;

    // MODE MODIFICABILE
    private Texture lobby, software_infos, logout, settings,logout_clicked, scoreboard, market;
    private Texture big_clicked,big_hover,center_clicked,center_hover,mode_clicked,mode_hover;
    private Texture infos_clicked,infos_hover;
    private Texture logout_hover,settings_clicked,settings_hover;
    private Texture btn_close,btn_close_clicked;
    private Texture btn_no,btn_yes,btn_no_clicked,btn_yes_clicked;
    private Texture star,star_selected;
    private Texture noMusic,noEffects;
    private Texture market_hover;
    private Texture market_clicked;
    private Texture volume_bar;

    private LoadingScreen loadingScreen;
    private int volume;

    // --- GAME MODE TRANSITION (delay per mostrare "clicked") ---
    private static final float MODE_CLICK_DELAY = 0.14f; // puoi cambiare (0.10f–0.18f)

    private boolean modeTransition = false;
    private float modeTransitionTimer = 0f;
    private int pendingMode = -1; // 0..3 (classic, gravity4, horizontal, speedy)


    // costruttore
    public LobbyUI(Main game, LobbyInput lobbyInput) {
        modeTransition = false;
        modeTransitionTimer = 0f;
        pendingMode = -1;

        this.game = game;
        this.screen = game.screen;
        Fonts.load();

        this.lobbyInput = lobbyInput;   // ✅ usa quello passato
        shapeRenderer = new ShapeRenderer();
        loadingScreen = new LoadingScreen(game, false);

        this.loadImages();
    }

    @Override
    public void loadFont() {}


    public void  loadDarkMode()
    {
        darkLobby=new Texture("lobby_screens/dark/lobby_dark.png");
        darkLogout=new Texture("lobby_screens/dark/logout_dark.png");
        darkSettings=new Texture("lobby_screens/dark/settings_dark.png");
        darkCredits=new Texture("lobby_screens/dark/software_info_dark.png");
        darkScoreboard=new Texture("lobby_screens/dark/scoreboard_dark.png");
        darkMarket=new Texture("lobby_screens/dark/market_dark.png");

        darkBigClicked=new Texture("ui/buttons/lobby/dark/bottom_big_clicked.png");
        darkBigHover=new Texture("ui/buttons/lobby/dark/bottom_big_hover.png");
        darkCenterClicked=new Texture("ui/buttons/lobby/dark/bottom_center_clicked.png");
        darkCenterHover=new Texture("ui/buttons/lobby/dark/bottom_center_hover.png");
        darkBtnClose=new Texture("ui/buttons/lobby/dark/btn_close.png");

        darkBtnCloseClicked=new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
        darkBtnMarket=new Texture("ui/buttons/lobby/dark/market.png");
        darkBtnMarketClicked=new Texture("ui/buttons/lobby/dark/market_clicked.png");

        volume_bar_dark=new Texture("ui/buttons/lobby/dark/volume_bar_dark.png");

    }

    public void loadLightMode()
    {
        lightLobby=new Texture("lobby_screens/light/lobby_light.png");
        lightLogout=new Texture("lobby_screens/light/logout_light.png");
        lightSettings=new Texture("lobby_screens/light/settings_light.png");
        lightCredits=new Texture("lobby_screens/light/software_infos_light.png");
        lightScoreboard=new Texture("lobby_screens/light/scoreboard_light.png");
        lightMarket=new Texture("lobby_screens/light/market_light.png");

        lightBigClicked=new Texture("ui/buttons/lobby/light/bottom_big_clicked.png");
        lightBigHover=new Texture("ui/buttons/lobby/light/bottom_big_hover.png");
        lightCenterClicked=new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
        lightCenterHover=new Texture("ui/buttons/lobby/light/bottom_center_hover.png");
        lightBtnClose=new Texture("ui/buttons/lobby/light/btn_close.png");

        lightBtnCloseClicked=new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
        lightBtnMarket=new Texture("ui/buttons/lobby/light/market.png");
        lightBtnMarketClicked=new Texture("ui/buttons/lobby/light/market_clicked.png");

        volume_bar_light=new Texture("ui/buttons/lobby/light/volume_bar_light.png");
    }

    public void darkMode(boolean isDarkMode)
    {
        darkMode=isDarkMode;

        if(darkMode) {
            lobby=darkLobby;
            software_infos=darkCredits;
            logout=darkLogout;
            settings=darkSettings;
            scoreboard=darkScoreboard;
            market=darkMarket;

            big_clicked=darkBigClicked;
            big_hover=darkBigHover;
            center_clicked=darkCenterClicked;
            center_hover=darkCenterHover;
            btn_close=darkBtnClose;
            btn_close_clicked=darkBtnCloseClicked;

            market_hover=darkBtnMarket;
            market_clicked=darkBtnMarketClicked;

            volume_bar=volume_bar_dark;
        }
        else {
            lobby=lightLobby;
            software_infos=lightCredits;
            logout=lightLogout;
            settings=lightSettings;
            scoreboard=lightScoreboard;
            market=lightMarket;

            big_clicked=lightBigClicked;
            big_hover=lightBigHover;
            center_clicked=lightCenterClicked;
            center_hover=lightCenterHover;
            btn_close=lightBtnClose;
            btn_close_clicked=lightBtnCloseClicked;

            market_hover=lightBtnMarket;
            market_clicked=lightBtnMarketClicked;

            volume_bar=volume_bar_light;
        }
    }

    @Override
    public void loadImages() {
        //DARK MODE

        loadDarkMode();
        loadLightMode();

        darkMode((boolean)UserProgressService.getProgress("darkMode")); // lettura da progressi utente

        noMusic=new Texture("ui/icons/no_music.png");
        noEffects=new Texture("ui/icons/no_sound.png");

        star=new Texture("ui/icons/star.png");
        star_selected=new Texture("ui/icons/star_selected.png");

        mode_clicked=new Texture("ui/buttons/lobby/light/game_mode_clicked.png");
        mode_hover= new Texture("ui/buttons/lobby/light/game_mode_hover.png");

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

    public void lobbyRender(float delta) {
        screen.begin();
        screen.draw(lobby, 0, 0);

        // --- GAME MODES ---
        draw(mode_hover, lobbyInput.classicHover,35, 334);
        draw(mode_hover, lobbyInput.gravity4Hover,275, 334);
        draw(mode_hover, lobbyInput.horizontalHover,513, 334);
        draw(mode_hover, lobbyInput.speedyHover,753, 334);

        draw(mode_clicked, lobbyInput.classic,35, 334);
        draw(mode_clicked, lobbyInput.gravity4,275, 334);
        draw(mode_clicked, lobbyInput.horizontal,512, 334);
        draw(mode_clicked, lobbyInput.speedy,752, 334);

        draw(market_hover,   lobbyInput.isBtnMarket,833,588);
        draw(market_clicked, lobbyInput.isBtnMarketClicked,833,588);


        // --- SECONDARY BUTTONS ---

        draw(big_hover,    lobbyInput.marketHover,35,91);
        draw(center_hover, lobbyInput.scoreboardHover,369,91);

        draw(center_clicked, lobbyInput.scoreboard,369,91);
        draw(big_clicked,    lobbyInput.daily,660,91);

        // --- COMMAND BAR ICONS ---
        draw(logout_hover,   lobbyInput.exitHover,430,41);
        draw(infos_hover,    lobbyInput.informationHover,481,40);
        draw(settings_hover, lobbyInput.settingsHover,540,40);

        draw(logout_clicked,   lobbyInput.exit,430,41);
        draw(infos_clicked,    lobbyInput.information,481,40);
        draw(settings_clicked, lobbyInput.settings,540,40);

        // --- SECONDARY WINDOWS ---
        draw(software_infos, lobbyInput.isWindowOpenInfo,      244,194);
        draw(logout,         lobbyInput.isWindowOpenExit,      294,204);
        draw(settings,       lobbyInput.isWindowOpenSettings,  244,223);
        draw(scoreboard,     lobbyInput.isWindowOpenScoreboard,100,163);
        draw(market,         lobbyInput.isWindowOpenMarket,    100,150);

        //--- CHIUDI ---


        //schermata marketPlace

        if(lobbyInput.isWindowOpenMarket)
        {
            draw(btn_close,lobbyInput.isBtnCloseMarketHover,830,465);
            draw(btn_close_clicked, lobbyInput.isBtnCloseMarket, 830, 465);
        }

        // schermata crediti di gioco
        if(lobbyInput.isWindowOpenInfo) {
            draw(btn_close,lobbyInput.isBtnCloseInfoHover,696,440);
            draw(btn_close_clicked, lobbyInput.btnCloseInfo, 696, 440);
        }

        // schermata impostazioni
        if(lobbyInput.isWindowOpenSettings) {

            // 1️⃣ Disegno la finestra normalmente (contiene già la barra vuota)
            screen.draw(settings, 244, 223);

            draw(btn_close, lobbyInput.isBtnCloseSettingsHover, 694, 410);
            draw(btn_close, lobbyInput.btnCloseSettings, 694, 410);

            // 2️⃣ Chiudo il batch per sicurezza (non serve ShapeRenderer)
            screen.end();

            // 3️⃣ Riapro il batch per disegnare la barra piena
            screen.begin();

            // --- MUSIC BAR FILL (texture) ---
            float fullWidthMusic = volume_bar.getWidth();              // larghezza originale texture
            float fillWidthMusic = AudioSettings.musicVolume * fullWidthMusic;

            screen.draw(
                volume_bar,
                lobbyInput.musicBarArea.x,
                685 - lobbyInput.musicBarArea.y,
                fillWidthMusic,                 // larghezza dinamica
                volume_bar.getHeight()            // altezza originale
            );

            // --- EFFECTS BAR FILL (texture) ---
            float fullWidthEffects = volume_bar.getWidth();
            float fillWidthEffects = AudioSettings.effectsVolume * fullWidthEffects;

            screen.draw(
                volume_bar,
                lobbyInput.effectsBarArea.x,
                683 - lobbyInput.effectsBarArea.y,
                fillWidthEffects,
                volume_bar.getHeight()
            );

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

            // --- ICONS WHEN VOLUME = 0 ---
            volume = (int)(AudioSettings.musicVolume * 100);
            if(volume == 0) {
                draw(noMusic, true, 266, 308);
            }

            volume = (int)(AudioSettings.effectsVolume * 100);
            if(volume == 0) {
                draw(noEffects, true, 266, 256);
            }
        }

        // schermata logout
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
            if (!lobbyInput.starClicked[i])
            {
                continue; // se è false, salta
            }

            switch (i)
            {
                case 0:
                    draw(star_selected, true, 130, 302);   // Classic
                    break;

                case 1:
                    draw(star_selected, true, 160, 302);
                    break;

                case 2:
                    draw(star_selected, true, 370, 302);
                    break;

                case 3:
                    draw(star_selected, true, 400, 302);
                    break;

                case 4:
                    draw(star_selected, true, 610, 302);
                    break;

                case 5:
                    draw(star_selected, true, 640, 302);
                    break;

                case 6:
                    draw(star_selected, true, 850, 302);
                    break;

                case 7:
                    draw(star_selected, true, 880, 302);
                    break;
            }

        }

        // todo: errore -> vengono stampate le stelle solo dell'ultima cliccata, devono rimanere stampate anche le altre modalità di gioco
        for (int i = 0; i < 8; i++)
        {
            // Disegna solo se la stella è in hover OPPURE è cliccata
            if (!lobbyInput.starHover[i])
                continue;

            switch (i)
            {
                case 0:
                    draw(star, true, 130, 302);   // Classic stella 1
                    break;

                case 1:
                    draw(star, true, 160, 302);   // Classic stella 2
                    break;

                case 2:
                    draw(star, true, 370, 302);   // Gravity4 stella 1
                    break;

                case 3:
                    draw(star, true, 400, 302);   // Gravity4 stella 2
                    break;

                case 4:
                    draw(star, true, 610, 302);   // Horizontal stella 1
                    break;

                case 5:
                    draw(star, true, 640, 302);   // Horizontal stella 2
                    break;

                case 6:
                    draw(star, true, 850, 302);   // Speedy stella 1
                    break;

                case 7:
                    draw(star, true, 880, 302);   // Speedy stella 2
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

        // --- GAME MODE TRANSITION (delay per mostrare "clicked") ---
        if (!modeTransition)
        {
            if (lobbyInput.classic)      { pendingMode = 0; modeTransition = true; modeTransitionTimer = 0f; lobbyInput.setInputEnabled(false); }
            else if (lobbyInput.gravity4){ pendingMode = 1; modeTransition = true; modeTransitionTimer = 0f; lobbyInput.setInputEnabled(false); }
            else if (lobbyInput.horizontal){ pendingMode = 2; modeTransition = true; modeTransitionTimer = 0f; lobbyInput.setInputEnabled(false); }
            else if (lobbyInput.speedy)  { pendingMode = 3; modeTransition = true; modeTransitionTimer = 0f; lobbyInput.setInputEnabled(false); }
        }

        if (modeTransition)
        {
            modeTransitionTimer += delta;

            if (modeTransitionTimer >= MODE_CLICK_DELAY)
            {
                switch (pendingMode)
                {
                    case 0:
                        game.setScreen(new GameManager(game, lobbyInput.difficolta[0], darkMode));
                        return;
                    case 1:
                        game.setScreen(new GameManager(game, lobbyInput.difficolta[1], darkMode));
                        return;
                    case 2:
                        game.setScreen(new GameManager(game, lobbyInput.difficolta[2], darkMode));
                        return;
                    case 3:
                        game.setScreen(new GameManager(game, lobbyInput.difficolta[3], darkMode));
                        return;
                }
            }
        }

    }


    public void hide()
    {
        // Quando si cambia schermata, spegni input della lobby e ripristina input processor
        try { lobbyInput.setInputEnabled(false); } catch (Exception ignored) {}
        try { Gdx.input.setInputProcessor(null); } catch (Exception ignored) {}
    }

    // metodo per il rilascio delle risorse
    public void disposeUI()
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
