/*
Forza4 • class LobbyUI •
Gestisce la grafica della lobby
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.Authentication.AuthAlgorithms;
import sorgente.Authentication.AuthManager;
import sorgente.Fonts;
import sorgente.Game.GameManager;
import sorgente.Main;
import sorgente.ResourceLoader;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.SessionLockService;
import sorgente.UserData.UserProgressService;
import sorgente.VersionInfo;
import java.awt.*;
import java.io.IOException;
import java.text.Format;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class LobbyUI implements ResourceLoader {
    private  final SpriteBatch screen;

    private Boolean darkMode;

    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;

    private final LobbyInput lobbyInput;

    // texture che non cambiano
    private Texture btn_claim, btn_claim_clicked;
    private Texture bgProgressBar, progressBar;
    private Texture creditImg;

    // DARK MODE
    private Texture darkLobby, darkLogout, darkSettings, darkCredits, darkScoreboard, darkMarket, darkProfileInfos;
    private Texture darkGameModeClicked, darkGameModeHover, darkCenterClicked,darkCenterHover;
    private Texture darkBtnClose, darkBtnCloseClicked; // X chiusura
    private Texture darkBtnCloseBig, darkBtnCloseClickedBig; // X chiusura 50x50
    private Texture darkBtnMarket, darkBtnMarketClicked; // mercato
    private Texture darkBtnProfileInfos, darkBtnProfileInfosClicked; // profilo utente
    private Texture volume_bar_dark; // barra volume
    private Texture purchase_item_dark, purchase_item_clicked_dark; // acquisto item nel mercato

    // LIGHT MODE
    private Texture lightLobby, lightLogout, lightSettings, lightCredits, lightScoreboard, lightMarket, lightProfileInfos;
    private Texture lightGameModeClicked, lightGameModeHover, lightCenterClicked,lightCenterHover;
    private Texture lightBtnClose, lightBtnCloseClicked; // X chiusura
    private Texture lightBtnCloseBig, lightBtnCloseClickedBig; // X chiusura 50x50
    private Texture lightBtnMarket, lightBtnMarketClicked; // mercato
    private Texture lightBtnProfileInfos, lightBtnProfileInfosClicked; // profilo utente
    private Texture volume_bar_light; // barra volume
    private Texture purchase_item_light, purchase_item_clicked_light; // acquisto item nel mercato

    // MODE MODIFICABILE
    private Texture lobby, software_infos, logout, settings,btn_logout_clicked, scoreboard, market, profile_infos;
    private Texture gameMode_clicked, gameMode_hover, center_clicked,center_hover;
    private Texture btn_infos_clicked, btn_infos;
    private Texture btn_logout, btn_settings_clicked, btn_settings;
    private Texture btn_close, btn_close_clicked;
    private Texture btn_close_big, btn_close_clicked_big;
    private Texture btn_no, btn_yes, btn_no_clicked, btn_yes_clicked;
    private Texture star, star_selected;
    private Texture music, effects, noMusic,noEffects;
    private Texture market_hover, market_clicked;
    private Texture profile_infos_hover, profile_infos_clicked;
    private Texture volume_bar;
    private Texture purchase_item, purchase_item_clicked;

    // --- GAME MODE TRANSITION (delay per mostrare "clicked") ---
    private static final float MODE_CLICK_DELAY = 0.14f; // puoi cambiare (0.10f–0.18f)

    private boolean modeTransition = false;
    private float modeTransitionTimer = 0f;
    private int pendingMode = -1; // 0..3 (classic, gravity3, horizontal, speedy)

    // formatter per la virgola delle migliaia !in automatico converte l'intero in stringa
    private final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

    // costruttore
    public LobbyUI(LobbyInput lobbyInput) {
        modeTransition = false;
        modeTransitionTimer = 0f;

        this.screen = LobbyManager.game.screen;
        this.lobbyInput = lobbyInput;

        // caricamento font
        Fonts.load();
        // caricamento immagini
        this.loadImages();
    }

    @Override
    public void loadFont() {}

    // caricamento assets per la dark mode
    public void  loadDarkMode() {
        // lobby bg
        darkLobby=new Texture("lobby_screens/dark/lobby_dark.png");

        // schermate in sovra impressione
        darkLogout=new Texture("lobby_screens/dark/logout_dark.png");
        darkSettings=new Texture("lobby_screens/dark/settings_dark.png");
        darkCredits=new Texture("lobby_screens/dark/software_info_dark.png");
        darkScoreboard=new Texture("lobby_screens/dark/scoreboard_dark.png");
        darkMarket=new Texture("lobby_screens/dark/market_dark.png");
        darkProfileInfos=new Texture("lobby_screens/dark/profile_infos_dark.png");

        // game mods
        darkGameModeHover=new Texture("ui/buttons/lobby/dark/game_mode_hover.png");
        darkGameModeClicked=new Texture("ui/buttons/lobby/dark/game_mode_clicked.png");
        darkCenterClicked=new Texture("ui/buttons/lobby/dark/bottom_center_clicked.png");
        darkCenterHover=new Texture("ui/buttons/lobby/dark/bottom_center_hover.png");

        // X chiusura
        darkBtnClose=new Texture("ui/buttons/lobby/dark/btn_close.png");
        darkBtnCloseClicked=new Texture("ui/buttons/lobby/dark/btn_close_clicked.png");
        darkBtnCloseBig = new Texture("ui/buttons/game/dark/quit_dark.png");
        darkBtnCloseClickedBig = new Texture("ui/buttons/game/dark/quit_dark_clicked.png");

        // apertura market e profile infos
        darkBtnMarket=new Texture("ui/buttons/lobby/dark/market.png");
        darkBtnMarketClicked=new Texture("ui/buttons/lobby/dark/market_clicked.png");
        darkBtnProfileInfos=new Texture("ui/buttons/lobby/dark/profile.png");
        darkBtnProfileInfosClicked=new Texture("ui/buttons/lobby/dark/profile_clicked.png");

        // barra volume
        volume_bar_dark=new Texture("ui/buttons/lobby/dark/volume_bar_dark.png");

        // icona acquisto item nel mercato
        purchase_item_dark=new Texture("ui/buttons/lobby/dark/purchase_item.png");
        purchase_item_clicked_dark=new Texture("ui/buttons/lobby/dark/purchase_item_clicked.png");
    }
    // caricamento assets per la light mode
    public void loadLightMode() {
        // lobby bg
        lightLobby=new Texture("lobby_screens/light/lobby_light.png");

        // schermate in sovra impressione
        lightLogout=new Texture("lobby_screens/light/logout_light.png");
        lightSettings=new Texture("lobby_screens/light/settings_light.png");
        lightCredits=new Texture("lobby_screens/light/software_infos_light.png");
        lightScoreboard=new Texture("lobby_screens/light/scoreboard_light.png");
        lightMarket=new Texture("lobby_screens/light/market_light.png");
        lightProfileInfos=new Texture("lobby_screens/light/profile_infos_light.png");

        // game mods
        lightGameModeHover=new Texture("ui/buttons/lobby/light/game_mode_hover.png");
        lightGameModeClicked=new Texture("ui/buttons/lobby/light/game_mode_clicked.png");
        lightCenterClicked=new Texture("ui/buttons/lobby/light/bottom_center_clicked.png");
        lightCenterHover=new Texture("ui/buttons/lobby/light/bottom_center_hover.png");

        // X chiusura
        lightBtnClose=new Texture("ui/buttons/lobby/light/btn_close.png");
        lightBtnCloseClicked=new Texture("ui/buttons/lobby/light/btn_close_clicked.png");
        lightBtnCloseBig = new Texture("ui/buttons/game/light/quit_light.png");
        lightBtnCloseClickedBig = new Texture("ui/buttons/game/light/quit_light_clicked.png");

        // apertura market e profile infos
        lightBtnMarket=new Texture("ui/buttons/lobby/light/market.png");
        lightBtnMarketClicked=new Texture("ui/buttons/lobby/light/market_clicked.png");
        lightBtnProfileInfos=new Texture("ui/buttons/lobby/light/profile.png");
        lightBtnProfileInfosClicked=new Texture("ui/buttons/lobby/light/profile_clicked.png");

        // barra volume
        volume_bar_light=new Texture("ui/buttons/lobby/light/volume_bar_light.png");

        // icona acquisto item nel mercato
        purchase_item_light=new Texture("ui/buttons/lobby/light/purchase_item.png");
        purchase_item_clicked_light=new Texture("ui/buttons/lobby/light/purchase_item_clicked.png");
    }

    public void darkMode(boolean isDarkMode) {
        darkMode=isDarkMode;

        if(darkMode) {
            // lobby bg
            lobby=darkLobby;

            // secondary windows
            software_infos=darkCredits;
            logout=darkLogout;
            settings=darkSettings;
            scoreboard=darkScoreboard;
            market=darkMarket;
            profile_infos=darkProfileInfos;

            // game mods
            gameMode_hover = darkGameModeHover;
            gameMode_clicked = darkGameModeClicked;
            center_clicked=darkCenterClicked;
            center_hover=darkCenterHover;

            // X chiusura
            btn_close=darkBtnClose;
            btn_close_clicked=darkBtnCloseClicked;
            btn_close_big=darkBtnCloseBig;
            btn_close_clicked_big=darkBtnCloseClickedBig;

            // apertura market e profile infos
            market_hover=darkBtnMarket;
            market_clicked=darkBtnMarketClicked;
            profile_infos_hover=darkBtnProfileInfos;
            profile_infos_clicked=darkBtnProfileInfosClicked;

            // barra volume
            volume_bar=volume_bar_dark;

            // icona acquisto item nel mercato
            purchase_item=purchase_item_dark;
            purchase_item_clicked=purchase_item_clicked_dark;
        }
        else {
            // lobby bg
            lobby=lightLobby;

            // secondary windows
            software_infos=lightCredits;
            logout=lightLogout;
            settings=lightSettings;
            scoreboard=lightScoreboard;
            market=lightMarket;
            profile_infos=lightProfileInfos;

            // game mods
            gameMode_hover = lightGameModeHover;
            gameMode_clicked = lightGameModeClicked;
            center_clicked=lightCenterClicked;
            center_hover=lightCenterHover;

            // X chiusura
            btn_close=lightBtnClose;
            btn_close_clicked=lightBtnCloseClicked;
            btn_close_big=lightBtnCloseBig;
            btn_close_clicked_big=lightBtnCloseClickedBig;

            // apertura market e profile infos
            market_hover=lightBtnMarket;
            market_clicked=lightBtnMarketClicked;
            profile_infos_hover=lightBtnProfileInfos;
            profile_infos_clicked=lightBtnProfileInfosClicked;

            // barra volume
            volume_bar=volume_bar_light;

            // icona acquisto item nel mercato
            purchase_item=purchase_item_light;
            purchase_item_clicked=purchase_item_clicked_light;
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

        btn_claim = new Texture("ui/buttons/lobby/btn_claim.png");
        btn_claim_clicked = new Texture("ui/buttons/lobby/btn_claim_clicked.png");

        bgProgressBar = new Texture("ui/icons/bg_progress_bar.png");
        progressBar = new Texture("ui/icons/progress_bar_daily.png");

        creditImg = new Texture("ui/icons/credit.png");
    }

    // metodo per stampare la top 5 nella lobby
    public void drawScoreboard(int numUsers) {
        // lettura e caricamento di tutti i punti degli utenti dal db
        Map<String, Integer> map = LobbyInput.usersPointsMap;

        // ordinamento: prima per punti (desc), a parità per nome (asc)
        TreeMap<String, Integer> sortedMap = new TreeMap<>(
            (s1, s2) -> {
                int cmp = map.get(s2).compareTo(map.get(s1));
                return (cmp != 0) ? cmp : s1.compareTo(s2);
            }
        );
        sortedMap.putAll(map);

        // posizioni iniziali e incrementi
        int startY, nameStartX, pointsStartX, changeY;
        BitmapFont font;

        if (numUsers==5) { // scoreboard con 5 utenti
            nameStartX = 410;
            pointsStartX = 555;
            startY = 207;
            changeY = 22;
            font = Fonts.bold15;
        }
        else { // scoreboard con 20 utenti
            nameStartX = 195;
            pointsStartX = 380;
            startY = 460;
            changeY = 30;
            font = Fonts.bold20;
        }

        // posizioni variabili della scoreboard
        int nameX = nameStartX, y = startY;
        int pointsX = pointsStartX;
        // contatore utenti mostrati
        int count = 0;

        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {

            String name = entry.getKey();
            int points = entry.getValue();

            // stampa nome e punti
            Fonts.draw(screen, name, nameX, y, font);
            Fonts.draw(screen, formatter.format(points), pointsX, y, font);

            y -= changeY;
            count++;

            if (count == numUsers) break;
            // metà classifica per la scoreboard da 20 utenti
            if (count == 10) {
                nameX = 570;
                pointsX = 760;
                y = startY;
            }
        }
    }

    // metodo per disegnare un elemento su schermo
    private void draw(Texture texture, boolean response, float x, float y) {
        if (response) screen.draw(texture, x, y);
    }

    // metodo per creare la grafica e renderizzare lo schermo
    public void lobbyRender(float delta) throws IOException {
        // init screen
        screen.begin();

        // aggiornamento lampeggio cursore (tick) per i campi editabili del market
        cursorTimer += delta;
        if (cursorTimer >= 0.5f) {
            cursorVisible = !cursorVisible;
            cursorTimer = 0f;
        }

        // stampa light o dark mode
        darkMode((boolean)UserProgressService.getProgress("dark_mode")); // lettura da progressi utente

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
        Fonts.bold25.draw(screen, formatter.format(credits), (770 - layout2.width), 623);

        // SCOREBOARD TOP 5 USERS //
        drawScoreboard(5);

        // NUMERO DI BOOSTS
        // linea 1
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_freezer").toString(), 65, 175); //
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_token_cracker").toString(), 170, 175); //
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_row_breaker").toString(), 275, 175); //
        // linea 2
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_peek").toString(), 65, 110); //
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_precision").toString(), 170, 110); //
        Fonts.bold13.draw(screen, UserProgressService.getProgress("num_undo").toString(), 275, 110); //

        // DAILY CHALLENGE
        // numero missione
        Fonts.bold30.draw(screen, "• Quest " + UserProgressService.getProgress("num_mission").toString(), 785, 248);

        // progressi utente
        boolean isCompleted = (boolean) UserProgressService.getProgress("is_daily_completed"); // recupero stato di completamento
        boolean isClaimed = (boolean) UserProgressService.getProgress("is_daily_reward_claimed");
        long unlockAt = ((Number) UserProgressService.getProgress("daily_next_unlock_at")).longValue();
        long remainingMs = unlockAt - System.currentTimeMillis();

        if (isCompleted && remainingMs > 0) { // stampa tempo allo sblocco della prossima
            String txt = DailyChallenges.formatCountdown(remainingMs);
            Fonts.bold25.draw(screen, txt, 678, 207);
        }
        else { // missione da compiere + barra progresso
            // stampa missione da fare e premio //
            Fonts.drawWrapped(screen, DailyChallenges.getMission(), 678, 207, 280, Fonts.bold25); // missione
            Fonts.bold25.draw(screen, "+" + DailyChallenges.getPrize(), 720, 128); // premio
            // immagine crediti
            screen.draw(creditImg, 672, 100);

            // disegno pulsante claim al completamento //
            if (isCompleted && !isClaimed) {
                screen.draw(btn_claim, 845, 105); // disegno pulsante claim

                // hover e click
                draw(btn_claim, lobbyInput.isBtnClaimPrizeHover,845, 105);
                draw(btn_claim_clicked, lobbyInput.isBtnClaimPrizeClicked,845, 105);
            }
            // barra progresso compiuto //
            else {
                final float barX = 760f;
                final float barY = 110f;
                final float barW = 180f;

                // sfondo barra (180px)
                screen.draw(bgProgressBar, barX, barY, barW, bgProgressBar.getHeight());

                // recupero progresso utente e obiettivo missione
                int dailyCurrent = (int) UserProgressService.getProgress("daily_progress");
                int dailyTarget = DailyChallenges.targetN((int) UserProgressService.getProgress("num_mission"));

                // fill
                float ratio = dailyCurrent / (float) dailyTarget;
                float fillW = ratio * 175;
                screen.draw(progressBar, barX+2, barY+2, fillW, progressBar.getHeight());

                // testo tipo 3/10
                Fonts.bold15.draw(screen, dailyCurrent + "/" + dailyTarget, barX+8, barY+15);
            }
        }


        // DIFFICOLTÀ IN GIOCO //
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
                case 0 -> draw(star, true, 131, 301);   // Classic stella 1
                case 1 -> draw(star, true, 161, 301);   // Classic stella 2
                case 2 -> draw(star, true, 371, 301);   // Gravity3 stella 1
                case 3 -> draw(star, true, 401, 301);   // Gravity3 stella 2
                case 4 -> draw(star, true, 609, 301);   // Horizontal stella 1
                case 5 -> draw(star, true, 639, 301);   // Horizontal stella 2
                case 6 -> draw(star, true, 849, 301);   // Speedy stella 1
                case 7 -> draw(star, true, 879, 301);   // Speedy stella 2
            }
        }

        // --- GAME MODES AREAS --- //
        // hover
        draw(gameMode_hover, lobbyInput.classicHover,35, 334); // classic
        draw(gameMode_hover, lobbyInput.gravity3Hover,275, 334); // gravity3
        draw(gameMode_hover, lobbyInput.horizontalHover,513, 334); // horizontal
        draw(gameMode_hover, lobbyInput.speedyHover,753, 334); // speedy
        // clicked
        draw(gameMode_clicked, lobbyInput.classic,35, 334); // classic
        draw(gameMode_clicked, lobbyInput.gravity3,275, 334); // gravity3
        draw(gameMode_clicked, lobbyInput.horizontal,512, 334); // horizontal
        draw(gameMode_clicked, lobbyInput.speedy,752, 334); // speedy

        // --- SECONDARY WINDOWS BUTTONS --- //
        // hover
        draw(btn_logout,         !lobbyInput.isBtnLogoutClicked,429,41); // logout
        draw(btn_infos,          !lobbyInput.isBtnInfoClicked,481,41); // game infos
        draw(btn_settings,       !lobbyInput.isBtnSettingsClicked,541,41); // settings
        draw(center_hover,        lobbyInput.isBtnScoreboardHover,369,91); // scoreboard
        draw(market_hover,        lobbyInput.isBtnMarketHover,834,588); // market
        draw(profile_infos_hover, lobbyInput.isBtnProfileInfosHover,913,588); // profile infos
        // clicked
        draw(btn_logout_clicked,    lobbyInput.isBtnLogoutClicked,429,41); // logout
        draw(btn_infos_clicked,     lobbyInput.isBtnInfoClicked,481,41); // game infos
        draw(btn_settings_clicked,  lobbyInput.isBtnSettingsClicked,541,41); // settings
        draw(center_clicked,        lobbyInput.isBtnScoreboardClicked,369,91); // scoreboard
        draw(market_clicked,        lobbyInput.isBtnMarketClicked,834,588); // market
        draw(profile_infos_clicked, lobbyInput.isBtnProfileInfosClicked,913,588); // profile infos

        // --- SECONDARY WINDOWS OPEN --- //
        draw(logout,         lobbyInput.isLogoutOpen,      294,204); // logout
        draw(software_infos, lobbyInput.isInfoOpen,      244,194); // game infos
        draw(settings,       lobbyInput.isSettingsOpen,  244,223); // settings
        draw(scoreboard,     lobbyInput.isScoreboardOpen,93,142); // scoreboard
        draw(market,         lobbyInput.isMarketOpen,    93,155); // market
        draw(profile_infos,  lobbyInput.isProfileInfosOpen,    0,0); // profile infos

        //--- INSIDE SECONDARY WINDOWS --- //
        // marketplace
        if (lobbyInput.isMarketOpen) {
            draw(btn_close,lobbyInput.isBtnCloseMarketHover,822,470);
            draw(btn_close_clicked, lobbyInput.btnCloseMarket, 822, 470);

            // prezzi boosters
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.priceFreezer), 255, 401);
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.priceTokenCracker), 493, 401);
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.priceRowBreaker), 731, 401);
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.pricePeek), 255, 264);
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.pricePrecision), 493, 264);
            Fonts.bold20.draw(screen, String.valueOf(LobbyInput.priceUndo), 731, 264);

            // quantità da acquistare (digitazione stile Auth: click -> tick lampeggiante -> input da tastiera)
            for (int i = 1; i <= 6; i++) {
                float xQty = LobbyInput.MARKET_QTY_X[i - 1]-1;
                float yQty = Gdx.graphics.getHeight()-LobbyInput.MARKET_QTY_Y[i - 1]-5;

                String qtyText;
                boolean isActive = (lobbyInput.activeMarketQtyField == i);

                if (isActive) qtyText = lobbyInput.marketQtyInput.toString();
                else {
                    qtyText = switch (i) {
                        case 1 -> String.valueOf(LobbyInput.numPurchaseItem1);
                        case 2 -> String.valueOf(LobbyInput.numPurchaseItem2);
                        case 3 -> String.valueOf(LobbyInput.numPurchaseItem3);
                        case 4 -> String.valueOf(LobbyInput.numPurchaseItem4);
                        case 5 -> String.valueOf(LobbyInput.numPurchaseItem5);
                        case 6 -> String.valueOf(LobbyInput.numPurchaseItem6);
                        default -> "1";
                    };
                }

                // evidenzia se Ctrl+A attivo
                if (isActive && lobbyInput.marketQtySelected) Fonts.bold20.setColor(com.badlogic.gdx.graphics.Color.SKY);
                else Fonts.bold20.setColor(com.badlogic.gdx.graphics.Color.WHITE);

                Fonts.bold20.draw(screen, qtyText, xQty, yQty);

                // tick lampeggiante
                if (isActive && !lobbyInput.marketQtySelected && cursorVisible) {
                    layout.setText(Fonts.bold20, qtyText);
                    float cursorX = xQty + layout.width + 2f;
                    Fonts.bold20.draw(screen, "|", cursorX, yQty+1);
                }

                Fonts.bold20.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            }

            // pulsanti acquisto
            // pulsanti "acquista" (icone)
            final float buyW = 30f;
            final float buyH = 30f;
            final float H = Gdx.graphics.getHeight();

            for (int i = 1; i <= 6; i++) {
                float xBuy = LobbyInput.MARKET_BUY_X[i - 1]+8;
                float yBuy = H - LobbyInput.MARKET_BUY_Y[i - 1]-33;

                boolean isClicked = lobbyInput.marketBuyClicked[i - 1];
                boolean isHover = lobbyInput.marketBuyHover[i - 1];

                if (isClicked) screen.draw(purchase_item_clicked, xBuy, yBuy, buyW, buyH);
                if (isHover) screen.draw(purchase_item, xBuy, yBuy, buyW, buyH);
            }
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

            draw(btn_no,lobbyInput.isBtnNoExitHover,506,244);
            draw(btn_no_clicked,lobbyInput.btnNoExit,506,244);
        }

        // scoreboard
        if (lobbyInput.isScoreboardOpen) {
            draw(btn_close, lobbyInput.isBtnCloseScoreboardHover,822,482);
            draw(btn_close_clicked, lobbyInput.btnCloseScoreboard, 822, 482);

            // SCOREBOARD TOP 20 USERS //
            drawScoreboard(20);
        }

        // profile infos
        if (lobbyInput.isProfileInfosOpen) {
            draw(btn_close_big,         lobbyInput.isBtnCloseProfileInfosHover,911,588);
            draw(btn_close_clicked_big, lobbyInput.btnCloseProfileInfos, 911, 588);
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

            } else if (lobbyInput.gravity3) {
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

        // cambio schermata
        if (modeTransition) {
            modeTransitionTimer += delta;

            if (modeTransitionTimer >= MODE_CLICK_DELAY) {
                // interruzione musica
                LobbyManager.soundtrack.stop();

                // cambio schermata
                switch (pendingMode) {
                    case 0, 1, 2, 3: // avvio gioco
                        LobbyManager.game.setScreen(new GameManager(LobbyManager.game, darkMode, pendingMode));
                        break;
                    case 4: // ritorno all'autenticazione
                        SessionLockService.shutdownAll(); // rilascia il lock
                        LobbyManager.game.setScreen(new AuthManager(LobbyManager.game));
                        break;
                }

                // rilascio risorse
                LobbyInput.dispose(); // icona mouse
                disposeUI();
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
