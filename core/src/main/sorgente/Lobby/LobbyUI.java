/*
Forza4 • class LobbyUI •
Gestisce la grafica della lobby
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthManager;
import sorgente.Fonts;
import sorgente.Game.GameManager;
import sorgente.Main;
import sorgente.ResourceLoader;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.UserProgressService;
import sorgente.VersionInfo;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class LobbyUI implements ResourceLoader {
    private final Main game;
    private  final SpriteBatch screen;

    private Boolean darkMode;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;

    private final LobbyInput lobbyInput;


    // DARK MODE
    private Texture darkLobby,darkLogout,darkSettings,darkCredits, darkScoreboard, darkMarket;
    private Texture darkGameModeClicked, darkGameModeHover, darkCenterClicked,darkCenterHover;
    private Texture darkBtnClose,darkBtnCloseClicked;
    private Texture darkBtnMarket,darkBtnMarketClicked;
    private Texture volume_bar_dark;


    // LIGHT MODE
    private Texture lightLobby,lightLogout,lightSettings,lightCredits, lightScoreboard, lightMarket;
    private Texture lightGameModeClicked, lightGameModeHover, lightCenterClicked,lightCenterHover;
    private Texture lightBtnClose,lightBtnCloseClicked;
    private Texture lightBtnMarket,lightBtnMarketClicked;
    private Texture volume_bar_light;

    // MODE MODIFICABILE
    private Texture lobby, software_infos, logout, settings,btn_logout_clicked, scoreboard, market;
    private Texture gameMode_clicked, gameMode_hover, center_clicked,center_hover;
    private Texture btn_infos_clicked,btn_infos;
    private Texture btn_logout,btn_settings_clicked,btn_settings;
    private Texture btn_close,btn_close_clicked;
    private Texture btn_no,btn_yes,btn_no_clicked,btn_yes_clicked;
    private Texture star,star_selected;
    private Texture music, effects, noMusic,noEffects;
    private Texture market_hover;
    private Texture market_clicked;
    private Texture volume_bar;

    private int effectsVolume, musicVolume;

    // --- GAME MODE TRANSITION (delay per mostrare "clicked") ---
    private static final float MODE_CLICK_DELAY = 0.14f; // puoi cambiare (0.10f–0.18f)

    private boolean modeTransition = false;
    private float modeTransitionTimer = 0f;
    private int pendingMode = -1; // 0..3 (classic, gravity4, horizontal, speedy)

    // formatter per la virgola delle migliaia !in automatico converte l'intero in stringa
    private final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

    // costruttore
    public LobbyUI(Main game, LobbyInput lobbyInput) {
        modeTransition = false;
        modeTransitionTimer = 0f;

        this.game = game;
        this.screen = game.screen;
        this.lobbyInput = lobbyInput;

        // caricamento font
        Fonts.load();
        // caricamento immagini
        this.loadImages();
    }

    @Override
    public void loadFont() {}


    public void  loadDarkMode() {
        darkLobby=new Texture("lobby_screens/dark/lobby_dark.png");
        darkLogout=new Texture("lobby_screens/dark/logout_dark.png");
        darkSettings=new Texture("lobby_screens/dark/settings_dark.png");
        darkCredits=new Texture("lobby_screens/dark/software_info_dark.png");
        darkScoreboard=new Texture("lobby_screens/dark/scoreboard_dark.png");
        darkMarket=new Texture("lobby_screens/dark/market_dark.png");

        darkGameModeHover=new Texture("ui/buttons/lobby/dark/game_mode_hover.png");
        darkGameModeClicked=new Texture("ui/buttons/lobby/dark/game_mode_clicked.png");
        darkCenterClicked=new Texture("ui/buttons/lobby/dark/bottom_center_clicked.png");
        darkCenterHover=new Texture("ui/buttons/lobby/dark/bottom_center_hover.png");
        darkBtnClose=new Texture("ui/buttons/lobby/dark/btn_close.png");
        darkCenterClicked=new Texture("ui/buttons/lobby/dark/bottom_center_clicked.png");
        darkCenterHover=new Texture("ui/buttons/lobby/dark/bottom_center_hover.png");
        darkBtnClose=new Texture("ui/buttons/lobby/dark/btn_close.png");

        darkBtnCloseClicked=new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
        darkBtnMarket=new Texture("ui/buttons/lobby/dark/market.png");
        darkBtnMarketClicked=new Texture("ui/buttons/lobby/dark/market_clicked.png");

        volume_bar_dark=new Texture("ui/buttons/lobby/dark/volume_bar_dark.png");
    }

    public void loadLightMode() {
        lightLobby=new Texture("lobby_screens/light/lobby_light.png");
        lightLogout=new Texture("lobby_screens/light/logout_light.png");
        lightSettings=new Texture("lobby_screens/light/settings_light.png");
        lightCredits=new Texture("lobby_screens/light/software_infos_light.png");
        lightScoreboard=new Texture("lobby_screens/light/scoreboard_light.png");
        lightMarket=new Texture("lobby_screens/light/market_light.png");

        lightGameModeHover=new Texture("ui/buttons/lobby/light/game_mode_hover.png");
        lightGameModeClicked=new Texture("ui/buttons/lobby/light/game_mode_clicked.png");
        lightCenterClicked=new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
        lightCenterHover=new Texture("ui/buttons/lobby/light/bottom_center_hover.png");
        lightBtnClose=new Texture("ui/buttons/lobby/light/btn_close.png");

        lightBtnCloseClicked=new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
        lightBtnMarket=new Texture("ui/buttons/lobby/light/market.png");
        lightBtnMarketClicked=new Texture("ui/buttons/lobby/light/market_clicked.png");

        volume_bar_light=new Texture("ui/buttons/lobby/light/volume_bar_light.png");
    }

    public void darkMode(boolean isDarkMode) {
        darkMode=isDarkMode;

        if(darkMode) {
            lobby=darkLobby;
            software_infos=darkCredits;
            logout=darkLogout;
            settings=darkSettings;
            scoreboard=darkScoreboard;
            market=darkMarket;

            gameMode_hover = darkGameModeHover;
            gameMode_clicked = darkGameModeClicked;
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

            gameMode_hover = lightGameModeHover;
            gameMode_clicked = lightGameModeClicked;
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
        // caricamento immagini dark-mode/light-mode
        loadDarkMode();
        loadLightMode();

        music     = new Texture("ui/icons/music.png");
        effects   = new Texture("ui/icons/sound.png");
        noMusic   = new Texture("ui/icons/no_music.png");
        noEffects = new Texture("ui/icons/no_sound.png");

        star=new Texture("ui/icons/star.png");
        star_selected=new Texture("ui/icons/star_selected.png");

        btn_infos=new Texture("ui/icons/infos.png");
        btn_infos_clicked=new Texture("ui/icons/infos_clicked.png");

        btn_logout=new Texture("ui/icons/logout.png");
        btn_logout_clicked=new Texture("ui/icons/logout_clicked.png");

        btn_settings=new Texture("ui/icons/settings.png");
        btn_settings_clicked=new Texture("ui/icons/settings_clicked.png");

        btn_no=new Texture("ui/buttons/lobby/btn_no.png");
        btn_no_clicked=new Texture("ui/buttons/lobby/btn_no_clicked.png");

        btn_yes=new Texture("ui/buttons/lobby/btn_yes.png");
        btn_yes_clicked=new Texture("ui/buttons/lobby/btn_yes_clicked.png");
    }

    // metodo per stampare la top 5 nella lobby
    public void drawScoreboard(int numUsers) {
        Map<String, Integer> map = FirestoreUserRepository.userPointsMap;

        // ordinamento: prima per punti (desc), a parità per nome (asc)
        TreeMap<String, Integer> sortedMap = new TreeMap<>(
            (s1, s2) -> {
                int cmp = map.get(s2).compareTo(map.get(s1));
                return (cmp != 0) ? cmp : s1.compareTo(s2);
            }
        );

        sortedMap.putAll(map);

        int y = 207;
        int count = 0;

        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            String name = entry.getKey();
            int points = entry.getValue();

            // stampa nome e punti
            Fonts.draw(screen, name, 410, y, Fonts.bold15);
            Fonts.draw(screen, formatter.format(points), 555, y, Fonts.bold15);

            y -= 22;
            count++;

            if (count == numUsers) break;
        }
    }

    // metodo per disegnare un elemento su schermo
    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) screen.draw(texture, x, y);
    }

    // metodo per creare la grafica e renderizzare lo schermo
    public void lobbyRender(float delta) {
        // init screen
        screen.begin();

        // stampa light o dark mode
        darkMode((boolean)UserProgressService.getProgress("darkMode")); // lettura da progressi utente

        screen.draw(lobby, 0, 0);

        // NOME AZIENDA //
        Fonts.draw(screen, "Drop Logic", 49, 63, Fonts.medium20); // firma al gioco
        // VERSIONE DI GIOCO //
        String text = "Beta " + VersionInfo.getVersion();
        // calcolo larghezza del testo
        GlyphLayout layout = new GlyphLayout(Fonts.medium20, text);
        // stampa testo
        Fonts.medium20.draw(screen, layout, (955 - layout.width), 63);

        // CREDITI UTENTE //
        int credits = (int) UserProgressService.getProgress("credits");
        // versione di gioco
        String text2 = String.valueOf(credits);
        // calcolo larghezza del testo
        GlyphLayout layout2 = new GlyphLayout(Fonts.bold25, text2);
        // stampa testo
        Fonts.bold25.draw(screen, layout2, (775 - layout2.width), 623);

        // SCOREBOARD TOP 5 USERS //
        drawScoreboard(5);

        // star selected
        int[][] posX = {
            {131, 161}, // blocco 0 -> diff 1, diff 2
            {371, 401}, // blocco 1
            {609, 639}, // blocco 2
            {849, 879}  // blocco 3
        };
        for (int i = 0; i < 4; i++) {
            int diff = LobbyInput.difficolta[i]; // 0, 1 o 2
            if (diff == 1) {
                // prima stella
                draw(star_selected, true, posX[i][0], 301);
            }
            if (diff == 2) {
                // seconda stella e prima stella
                draw(star_selected, true, posX[i][0], 301);
                draw(star_selected, true, posX[i][1], 301);
            }
        }
        // star hover
        for (int i = 0; i < 8; i++) {
            // Disegna solo se la stella è in hover OPPURE è cliccata
            if (!lobbyInput.starHover[i]) continue;

            switch (i) {
                case 0:
                    draw(star, true, 131, 301);   // Classic stella 1
                    break;

                case 1:
                    draw(star, true, 161, 301);   // Classic stella 2
                    break;

                case 2:
                    draw(star, true, 371, 301);   // Gravity4 stella 1
                    break;

                case 3:
                    draw(star, true, 401, 301);   // Gravity4 stella 2
                    break;

                case 4:
                    draw(star, true, 609, 301);   // Horizontal stella 1
                    break;

                case 5:
                    draw(star, true, 639, 301);   // Horizontal stella 2
                    break;

                case 6:
                    draw(star, true, 849, 301);   // Speedy stella 1
                    break;

                case 7:
                    draw(star, true, 879, 301);   // Speedy stella 2
                    break;
            }
        }

        // --- GAME MODES ---
        draw(gameMode_hover, lobbyInput.classicHover,35, 334);
        draw(gameMode_hover, lobbyInput.gravity4Hover,275, 334);
        draw(gameMode_hover, lobbyInput.horizontalHover,513, 334);
        draw(gameMode_hover, lobbyInput.speedyHover,753, 334);

        draw(gameMode_clicked, lobbyInput.classic,35, 334);
        draw(gameMode_clicked, lobbyInput.gravity4,275, 334);
        draw(gameMode_clicked, lobbyInput.horizontal,512, 334);
        draw(gameMode_clicked, lobbyInput.speedy,752, 334);

        draw(market_hover,   lobbyInput.isBtnMarketHover,833,588);
        draw(market_clicked, lobbyInput.isBtnMarketClicked,833,588);


        // --- SECONDARY BUTTONS ---
        draw(center_hover, lobbyInput.isBtnScoreboardHover,369,91);
        draw(center_clicked, lobbyInput.isBtnScoreboardClicked,369,91);

        // --- COMMAND BAR ICONS ---
        draw(btn_logout,   !lobbyInput.isBtnLogoutClicked,429,41);
        draw(btn_infos,    !lobbyInput.isBtnInfoClicked,481,41);
        draw(btn_settings, !lobbyInput.isBtnSettingsClicked,541,41);

        draw(btn_logout_clicked,   lobbyInput.isBtnLogoutClicked,429,41);
        draw(btn_infos_clicked,    lobbyInput.isBtnInfoClicked,481,41);
        draw(btn_settings_clicked, lobbyInput.isBtnSettingsClicked,541,41);

        // --- SECONDARY WINDOWS ---
        draw(software_infos, lobbyInput.isInfoOpen,      244,194);
        draw(logout,         lobbyInput.isLogoutOpen,      294,204);
        draw(settings,       lobbyInput.isSettingsOpen,  244,223);
        draw(scoreboard,     lobbyInput.isScoreboardOpen,93,142);
        draw(market,         lobbyInput.isMarketOpen,    93,155);

        //--- INSIDE SECONDARY WINDOWS ---
        // marketPlace
        if (lobbyInput.isMarketOpen) {
            draw(btn_close,lobbyInput.isBtnCloseMarketHover,822,470);
            draw(btn_close_clicked, lobbyInput.btnCloseMarket, 822, 470);
        }

        // crediti sviluppo gioco
        if (lobbyInput.isInfoOpen) {
            draw(btn_close,lobbyInput.isBtnCloseInfoHover,694,440);
            draw(btn_close_clicked, lobbyInput.btnCloseInfo, 694, 440);
        }

        // impostazioni
        if (lobbyInput.isSettingsOpen) {
            // finestra impostazioni
            screen.draw(settings, 244, 223);

            // pulsante chiusura
            draw(btn_close, lobbyInput.isBtnCloseSettingsHover, 694,411);
            draw(btn_close_clicked, lobbyInput.btnCloseSettings,694,411);

            // --- VOLUME BAR ---
            // disegno barre volume
            float filledWidth1 = (LobbyInput.musicPercent) * 361;
            float filledWidth2 = (LobbyInput.effectsPercent) * 361;
            if (filledWidth1 > 0.0) screen.draw(volume_bar, 320, 310, filledWidth1, 25);
            if (filledWidth2 > 0.0) screen.draw(volume_bar, 320, 258, filledWidth2, 25);

            // --- icone per il volume a 0 ---
            if (LobbyInput.effectsPercent==0f) screen.draw(noEffects, 266, 256);
            else screen.draw(effects, 266, 256);
            if (LobbyInput.musicPercent==0f) screen.draw(noMusic, 266, 308);
            else screen.draw(music, 266, 308);

            // percentuale volumi
            Fonts.bold15.draw(screen, Math.round(LobbyInput.musicPercent*100)+"%", 700, 327);
            Fonts.bold15.draw(screen, Math.round(LobbyInput.effectsPercent*100)+"%", 700, 275);
        }

        // logout
        if (lobbyInput.isLogoutOpen) {
            draw(btn_yes,lobbyInput.isBtnYesExitHover,342,244);
            draw(btn_yes_clicked,lobbyInput.btnYesExit,342,244);

            draw(btn_no_clicked,lobbyInput.btnNoExit,506,244);
            draw(btn_no,lobbyInput.isBtnNoExitHover,506,244);
        }

        // scoreboard
        if (lobbyInput.isScoreboardOpen) {
            draw(btn_close, lobbyInput.isBtnCloseScoreboardHover,822,482);
            draw(btn_close_clicked, lobbyInput.btnCloseScoreboard, 822, 482);

            // SCOREBOARD TOP 20 USERS //
            drawScoreboard(20);
        }

        // chiusura batch -> prima chiudere poi passare alla nuova schermata in caso di transizione
        screen.end();

        // --- GAME MODE TRANSITION (delay per mostrare "clicked") ---
        if (!modeTransition) {

            if (lobbyInput.classic) {
                pendingMode = 0;
                modeTransition = true;
                modeTransitionTimer = 0f;
                lobbyInput.setInputEnabled(false);

            } else if (lobbyInput.gravity4) {
                pendingMode = 1;
                modeTransition = true;
                modeTransitionTimer = 0f;
                lobbyInput.setInputEnabled(false);

            } else if (lobbyInput.horizontal) {
                pendingMode = 2;
                modeTransition = true;
                modeTransitionTimer = 0f;
                lobbyInput.setInputEnabled(false);

            } else if (lobbyInput.speedy) {
                pendingMode = 3;
                modeTransition = true;
                modeTransitionTimer = 0f;
                lobbyInput.setInputEnabled(false);

            } else if (lobbyInput.btnYesExit) {
                pendingMode = 4;
                modeTransition = true;
                modeTransitionTimer = 0f;
                lobbyInput.setInputEnabled(false);
            }
        }
        if (modeTransition) {
            modeTransitionTimer += delta;

            if (modeTransitionTimer >= MODE_CLICK_DELAY) {

                disposeUI();

                switch (pendingMode) {
                    case 0:
                        game.setScreen(new GameManager(game, LobbyInput.difficolta[0], darkMode, pendingMode));
                        LobbyManager.soundtrack.stop();
                        return;
                    case 1:
                        game.setScreen(new GameManager(game, LobbyInput.difficolta[1], darkMode,pendingMode));
                        LobbyManager.soundtrack.stop();
                        return;
                    case 2:
                        game.setScreen(new GameManager(game, LobbyInput.difficolta[2], darkMode,pendingMode));
                        LobbyManager.soundtrack.stop();
                        return;
                    case 3:
                        game.setScreen(new GameManager(game, LobbyInput.difficolta[3], darkMode,pendingMode));
                        LobbyManager.soundtrack.stop();
                        return;
                    case 4:
                        // interruzione musica
                        LobbyManager.soundtrack.stop();

                        // salvataggio difficoltà modalità di gioco
                        UserProgressService.setProgress("diff_classic", LobbyInput.difficolta[0]);
                        UserProgressService.setProgress("diff_gravity4", LobbyInput.difficolta[1]);
                        UserProgressService.setProgress("diff_horizontal", LobbyInput.difficolta[2]);
                        UserProgressService.setProgress("diff_speedy", LobbyInput.difficolta[3]);

                        // passaggio schermata di autenticazione
                        game.setScreen(new AuthManager(game));
                }
            }
        }
    }

    // metodo per il rilascio delle risorse
    public void disposeUI() {
        lobby.dispose();
        center_clicked.dispose();
        center_hover.dispose();

        darkLobby.dispose();
        darkLogout.dispose();
        darkSettings.dispose();
        darkCredits.dispose();
        darkScoreboard.dispose();
        darkMarket.dispose();
        darkGameModeHover.dispose();
        darkGameModeClicked.dispose();
        darkCenterClicked.dispose();
        darkCenterHover.dispose();
        darkBtnClose.dispose();
        darkBtnCloseClicked.dispose();
        darkBtnMarket.dispose();
        darkBtnMarketClicked.dispose();

        lightLobby.dispose();
        lightLogout.dispose();
        lightSettings.dispose();
        lightCredits.dispose();
        lightScoreboard.dispose();
        lightMarket.dispose();
        lightGameModeHover.dispose();
        lightGameModeClicked.dispose();
        lightCenterClicked.dispose();
        lightCenterHover.dispose();
        lightBtnClose.dispose();
        lightBtnCloseClicked.dispose();
        lightBtnMarket.dispose();
        lightBtnMarketClicked.dispose();

        software_infos.dispose();
        logout.dispose();
        settings.dispose();
        btn_logout_clicked.dispose();
        scoreboard.dispose();
        market.dispose();
        btn_infos_clicked.dispose();
        btn_infos.dispose();
        btn_logout.dispose();
        btn_settings_clicked.dispose();
        btn_settings.dispose();
        btn_close.dispose();
        btn_close_clicked.dispose();
        btn_no.dispose();
        btn_yes.dispose();
        btn_no_clicked.dispose();
        btn_yes_clicked.dispose();
        star.dispose();
        star_selected.dispose();
        noMusic.dispose();
        noEffects.dispose();
        market_hover.dispose();
        market_clicked.dispose();
    }
}
