/*
Forza4 • class LobbyInput •
Gestisce gli input della lobby
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.SoundManager;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.UserProgressService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LobbyInput implements InputProcessor {
    private static final Log log = LogFactory.getLog(LobbyInput.class);

    // percentuale audio
    public static float effectsPercent;
    public static float musicPercent;

    // FALGS BUTTONS CLICKED //
    // difficoltà game mods
    protected static int[] difficolta=new int[8];
    protected boolean[] starClicked=new boolean[8];
    // volume audio
    protected boolean draggingMusic = false;
    protected boolean draggingEffects = false;
    // yes/no logout
    protected boolean btnNoExit;
    protected boolean btnYesExit; // 1 controllo in più in LobbyUI per la transition della chiusura
    // apertura schermate in sovra impressione
    protected boolean isBtnMarketClicked;
    protected boolean isBtnScoreboardClicked;
    protected boolean isBtnLogoutClicked; // 2 usages in LobbyUI & 2 in LobbyInput
    protected boolean isBtnSettingsClicked; // 2 usages in LobbyUI & 2 in LobbyInput
    protected boolean isBtnInfoClicked; // 2 usages in LobbyUI & 2 in LobbyInput
    protected boolean isBtnProfileInfosClicked;
    // X chiusura schermate in sovra impressione
    protected boolean btnCloseSettings;
    protected boolean btnCloseInfo;
    protected boolean btnCloseMarket;
    protected boolean btnCloseScoreboard;
    protected boolean btnCloseProfileInfos;
    // game mods
    protected boolean classic, gravity3, horizontal, speedy;
    // claim "daily" prize
    protected boolean isBtnClaimPrizeClicked;

    // FLAGS BUTTONS HOVER //
    // difficoltà game mods
    protected boolean[] starHover=new boolean[8];
    // apertura schermate in sovra impressione
    protected boolean isBtnScoreboardHover;
    protected boolean isBtnMarketHover;
    protected boolean isBtnProfileInfosHover;
    // yes/no logout
    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;
    // X chiusura schermate in sovra impressione
    protected boolean isBtnCloseInfoHover;
    protected boolean isBtnCloseSettingsHover;
    protected boolean isBtnCloseMarketHover;
    protected boolean isBtnCloseScoreboardHover;
    protected boolean isBtnCloseProfileInfosHover;
    // game mods
    protected boolean classicHover, gravity3Hover, horizontalHover, speedyHover;
    // claim "daily" prize
    protected boolean isBtnClaimPrizeHover;

    // FLAGS CONTROLLO SCHERMATE APERTE //
    // per aprire le finestre in sovra impressione
    protected boolean isInfoOpen, isLogoutOpen, isSettingsOpen, isScoreboardOpen,
        isMarketOpen, isProfileInfosOpen; // isMarketOpen è usata una volta in più in un controllo

    private static Pixmap mouse;
    private final Cursor cursor;

    // variabili per i delay
    protected float clickedTimer = 0f; // durata dell'icona "clicked"
    protected float screenChangeDelay = 0f;
    protected boolean pendingScreenChange = false;
    protected int pendingNextState = -1;
    private float timerUpdateUsersPointsMap; // tempo per aggiornare la mappa dei punti degli utenti

    // azione pronta da eseguire fuori (solo per cambio screen / exit)
    private boolean inputEnabled = true;

    // HITBOX //
    protected Rectangle musicBarArea;
    protected Rectangle effectsBarArea;
    protected Rectangle switchDL;
    // stelle difficoltà di gioco
    private Rectangle classicStar1, gravityStar1, horizontalStar1, speedyStar1;
    private final Rectangle[] classicStars = new Rectangle[3];
    private final Rectangle[] gravityStars = new Rectangle[3];
    private final Rectangle[] horizontalStars = new Rectangle[3];
    private final Rectangle[] speedyStars = new Rectangle[3];
    // logout yes/no
    private Rectangle btn_no;
    private Rectangle btn_yes;
    // modalità di gioco
    private Rectangle classicArea;
    private Rectangle gravity3Area;
    private Rectangle horizontalArea;
    private Rectangle speedyArea;
    // schermate in sovra impressione
    private Rectangle areaOpenSettings;
    private Rectangle areaOpenInfos;
    private Rectangle areaOpenLogout;
    private Rectangle areaBtnOpenMarket;
    private Rectangle areaBtnOpenScoreboard;
    private Rectangle areaBtnOpenProfileInfos;
    // X chiusura schermate in sovra impressione
    private Rectangle areaBtnCloseInfos;
    private Rectangle areaBtnCloseSettings;
    private Rectangle areaBtnCloseMarket;
    private Rectangle areaBtnCloseScoreboard;
    private Rectangle areaBtnCloseProfileInfos;
    // pulsanti dei volumi nelle impostazioni
    private Rectangle btnMusic, btnEffects;
    // pulsante raccolta premio 'daily'
    private Rectangle btnClaimPrize;

    // AZIONI CLICK //
    // logout yes/no
    public static final int ACT_CLOSE_EXIT = 1;
    public static final int ACT_YES_EXIT = 2;
    // avvio modalità di gioco
    public static final int ACT_START_CLASSIC = 3;
    public static final int ACT_START_GRAVITY3 = 4;
    public static final int ACT_START_HORIZONTAL = 5;
    public static final int ACT_START_SPEEDY = 6;
    // chiusura schermate in sovra impressione
    public static final int ACT_CLOSE_INFOS = 7;
    public static final int ACT_CLOSE_SETTINGS = 8;
    public static final int ACT_CLOSE_SCOREBOARD = 9;
    public static final int ACT_CLOSE_MARKET = 10;
    public static final int ACT_CLOSE_PROFILE_INFOS = 11;
    // apertura schermate in sovra impressione
    public static final int ACT_OPEN_INFOS = 12;
    public static final int ACT_OPEN_SETTINGS = 13;
    public static final int ACT_OPEN_EXIT = 14;
    public static final int ACT_OPEN_SCOREBOARD = 15;
    public static final int ACT_OPEN_MARKET = 16;
    public static final int ACT_OPEN_PROFILE_INFOS = 17;
    // raccolta premio 'daily'
    public static final int ACT_CLAIM_REWARD_DAILY = 18;

    // prezzi boosters (cambieranno in base al numero selezionato per l'acquisto)
    protected static int priceFreezer, priceTokenCracker, priceRowBreaker, pricePeek, pricePrecision, priceUndo;
    // numero item da acquistare
    protected static int numPurchaseItem1, numPurchaseItem2, numPurchaseItem3, numPurchaseItem4, numPurchaseItem5, numPurchaseItem6;

    // -- MARKETPLACE -- //
    // digitazione quantità (stessa logica stile Auth) ---
    // prezzi base (unitari) dei booster (usati per calcolare il massimo acquistabile e il totale mostrato)
    private static final int[] MARKET_UNIT_PRICE = {10, 5, 5, 20, 10, 20};

    // coordinate (in world coords di SpriteBatch, bottom-left) dove disegnare le quantità (tick compreso)
    // (i range/hitbox possono essere leggermente sballati: l'importante è che sia cliccabile e si veda il tick)
    protected static final float[] MARKET_QTY_X = {256f, 494f, 732f, 256f, 494f, 732f};
    protected static final float[] MARKET_QTY_Y = {325f, 325f, 325f, 462f, 462f, 462f};

    // hitbox per click sulle quantità (coordinate input: top-left)
    private final Rectangle[] marketQtyAreas = new Rectangle[6];

    // campo attivo (0 = nessuno, 1..6 = booster)
    protected int activeMarketQtyField = 0;

    // testo digitato nel campo attivo
    protected final StringBuilder marketQtyInput = new StringBuilder();

    // stato selezione (Ctrl+A) del campo attivo
    protected boolean marketQtySelected = false;

    // pulsanti ACQUISTA (icone purchase_item / purchase_item_clicked) ---
    protected static final float[] MARKET_BUY_X = {307f, 544f, 781f, 307f, 544f, 781f};
    protected static final float[] MARKET_BUY_Y = {320f, 320f, 320f, 457f, 457f, 457f};

    private final Rectangle[] marketBuyAreas = new Rectangle[6];
    protected final boolean[] marketBuyHover = new boolean[6];
    protected final boolean[] marketBuyClicked = new boolean[6];

    private final String[] items = {"num_freezer", "num_token_cracker", "num_row_breaker", "num_peek", "num_precision", "num_undo"};


    // mappa con i punti degli utenti (utile per la scoreboard)
    protected static Map<String, Integer> usersPointsMap = new HashMap<>();

    // costruttore
    public LobbyInput() {
        // creazione hit boxes
        createHitboxes();

        // Cursor personalizzato
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);

        // -- DATI UTENTE --
        // volumi
        effectsPercent = ((Number) UserProgressService.getProgress("effects_volume")).floatValue();
        musicPercent = ((Number) UserProgressService.getProgress("music_volume")).floatValue();
        // difficoltà
        difficolta[0] = (int) UserProgressService.getProgress("diff_classic");
        difficolta[1] = (int) UserProgressService.getProgress("diff_gravity3");
        difficolta[2] = (int) UserProgressService.getProgress("diff_horizontal");
        difficolta[3] = (int) UserProgressService.getProgress("diff_speedy");

        // aggiornamento punti subito all'apertura e poi ogni 60 secondi
        timerUpdateUsersPointsMap = 0.1f;
    }

    // metodo per la creazione dei rectangle
    private void createHitboxes() {
        // Hitbox principali
        switchDL=new Rectangle(449,302,85,36);

        // Barra volume musica
        musicBarArea = new Rectangle(312, 363, 361, 25); // x, y, width, height // Barra volume effetti
        effectsBarArea = new Rectangle(312, 415, 361, 25);

        // stelle difficoltà di gioco
        classicStar1 = new Rectangle( 93, 376, 20, 20);
        classicStars[0]= new Rectangle( 123, 376, 20, 20);
        classicStars[1]= new Rectangle(153,376,20,20);

        gravityStar1 = new Rectangle( 333, 376, 20, 20);
        gravityStars[0]= new Rectangle(363, 376, 20, 20);
        gravityStars[1]=new Rectangle(393,376,20,20);

        horizontalStar1 = new Rectangle( 572, 376, 20, 20);
        horizontalStars[0] = new Rectangle(602, 376, 20, 20);
        horizontalStars[1] = new Rectangle(632, 376, 20, 20);

        speedyStar1 = new Rectangle( 811, 376, 20, 20);
        speedyStars[0] = new Rectangle( 841, 376, 20, 20);
        speedyStars[1] = new Rectangle(871,376,20,20);

        classicArea     = new Rectangle(26, 163, 210, 200);
        gravity3Area    = new Rectangle(266, 163, 210, 200);
        horizontalArea  = new Rectangle(504, 163, 210, 200);
        speedyArea      = new Rectangle(745, 163, 210, 200);

        // schermate in sovra impressione
        areaOpenLogout          = new Rectangle(422, 629, 30, 30);
        areaOpenInfos           = new Rectangle(475, 629, 30, 30);
        areaOpenSettings        = new Rectangle(534, 629, 30, 30);
        areaBtnOpenScoreboard   = new Rectangle(360, 436, 260, 170);
        areaBtnOpenMarket       = new Rectangle(827,64,50,50);
        areaBtnOpenProfileInfos = new Rectangle(906,64,50,50);
        // X chiusura schermate in sovra impressione
        areaBtnCloseInfos        = new Rectangle(686,217,40,40);
        areaBtnCloseSettings     = new Rectangle(686,245,40,40);
        areaBtnCloseScoreboard   = new Rectangle(814,174,40,40);
        areaBtnCloseMarket       = new Rectangle(814,187,40,40);
        areaBtnCloseProfileInfos = new Rectangle(903,57,50,50);
        // pulsante raccolta premio 'daily'
        btnClaimPrize = new Rectangle(837, 561, 100, 30);

        // MARKET //
        // aree click quantità (calcolate dalle coordinate di disegno) ---
        final float boxW = 60f; // larghezza box
        final float boxH = 20f; // altezza box
        for (int i = 0; i < 6; i++) {
            float xBox = MARKET_QTY_X[i] - 20f; // un po' più largo del testo
            float yBox = MARKET_QTY_Y[i];
            marketQtyAreas[i] = new Rectangle(xBox, yBox, boxW, boxH);
        }

        // aree click "ACQUISTA"
        final float buyW = 30f;
        final float buyH = 30f;
        for (int i = 0; i < 6; i++) {
            marketBuyAreas[i] = new Rectangle(MARKET_BUY_X[i], MARKET_BUY_Y[i], buyW, buyH);
        }

        btn_no= new Rectangle(499,408,150,50);
        btn_yes= new Rectangle(336,408,150,50);

        btnMusic = new Rectangle(260, 363, 30, 30);
        btnEffects = new Rectangle(260, 415, 30, 30);
    }

    // metodo per aggiornare la mappa con i punti utente
    public void loadUsersPoints() throws IOException {
        // svuotamento mappa
        usersPointsMap.clear();
        // lettura e caricamento di tutti i punti degli utenti dal db
        usersPointsMap = FirestoreUserRepository.loadAllUserPoints();
    }

    // genera il suono al click
    private boolean clicked() {
        SoundManager.playClickButton(effectsPercent);
        return true;
    }
    // genera il suono di digitazione
    private boolean typed() {
        SoundManager.playDigitSound(effectsPercent);
        return true;
    }
    // genera il suono d'acquisto
    private boolean purchased() {
        SoundManager.playPurchase(effectsPercent);
        return true;
    }

    // --- MARKET: helpers quantità/prezzi ---
    private int getNumPurchaseForIndex(int idx1to6) {
        return switch (idx1to6) {
            case 1 -> numPurchaseItem1;
            case 2 -> numPurchaseItem2;
            case 3 -> numPurchaseItem3;
            case 4 -> numPurchaseItem4;
            case 5 -> numPurchaseItem5;
            case 6 -> numPurchaseItem6;
            default -> 1;
        };
    }

    private void setNumPurchaseForIndex(int idx1to6, int num) {
        switch (idx1to6) {
            case 1: numPurchaseItem1 = num; break;
            case 2: numPurchaseItem2 = num; break;
            case 3: numPurchaseItem3 = num; break;
            case 4: numPurchaseItem4 = num; break;
            case 5: numPurchaseItem5 = num; break;
            case 6: numPurchaseItem6 = num; break;
        }
        updateMarketPrices();
    }

    private int getMarketUnitPrice(int idx1to6) {
        if (idx1to6 < 1 || idx1to6 > 6) return 0;
        return MARKET_UNIT_PRICE[idx1to6 - 1];
    }

    // max acquistabile rispettando: credits - (num * priceUnit) > 0
    private int getMaxPurchasable(int idx1to6) {
        int credits = ((Number) UserProgressService.getProgress("credits")).intValue();
        int unit = getMarketUnitPrice(idx1to6);
        if (unit <= 0) return 0;
        // condizione richiesta: credits - num*unit > 0  <=>  num <= (credits-1)/unit
        int max = (credits - 1) / unit;
        return Math.max(0, max);
    }

    // aggiorna i prezzi totali mostrati in market (prezzo unitario * quantità selezionata)
    private void updateMarketPrices() {
        priceFreezer       = MARKET_UNIT_PRICE[0] * Math.max(1, numPurchaseItem1);
        priceTokenCracker  = MARKET_UNIT_PRICE[1] * Math.max(1, numPurchaseItem2);
        priceRowBreaker     = MARKET_UNIT_PRICE[2] * Math.max(1, numPurchaseItem3);
        pricePeek          = MARKET_UNIT_PRICE[3] * Math.max(1, numPurchaseItem4);
        pricePrecision     = MARKET_UNIT_PRICE[4] * Math.max(1, numPurchaseItem5);
        priceUndo          = MARKET_UNIT_PRICE[5] * Math.max(1, numPurchaseItem6);
    }

    // conteggio costo acquisto item mercato
    private int getItemCost(int idx, int qty) {
        // max acquistabile con credits attuali
        int max = getMaxPurchasable(idx);
        qty = Math.min(qty, max); // quantità item acquistati
        if (qty < 1) qty = 1;

        return getMarketUnitPrice(idx) * qty;
    }

    // rilascio unica risorsa grafica
    public static void dispose() {
        mouse.dispose();
    }

    // controllo click
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //System.out.println(screenX + " " + screenY);
        try {
            return checkHitboxes(screenX, screenY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // metodo per resettare i click
    private void resetClickedFlags() {
        // game mods
        classic = gravity3 = horizontal = speedy = false;

        // apertura schermate in sovra impressione
        isBtnProfileInfosClicked = isBtnScoreboardClicked = isBtnMarketClicked = isBtnSettingsClicked = isBtnInfoClicked = false;

        // chiusura schermate in sovra impressione
        btnCloseProfileInfos = btnCloseScoreboard = btnCloseMarket = btnCloseSettings = btnCloseInfo = false;

        // yes/no logout
        isBtnLogoutClicked = btnNoExit = btnYesExit = false;

        // pulsanti purchase market items
        for (int i = 0; i < 6; i++) marketBuyClicked[i] = false;

        // claim reward
        isBtnClaimPrizeClicked = false;
    }

    // resetta lo stato di Hover dei pulsanti
    private void resetHover() {
        // game mods
        classicHover = gravity3Hover = horizontalHover = speedyHover = false;

        // apertura schermate in sovra impressione
        isBtnProfileInfosHover = isBtnScoreboardHover = isBtnMarketHover = false;

        // chiusura schermate in sovra impressione
        isBtnCloseInfoHover         = false;
        isBtnCloseSettingsHover     = false;
        isBtnCloseMarketHover       = false;
        isBtnCloseScoreboardHover   = false;
        isBtnCloseProfileInfosHover = false;

        // yes/no logout
        isBtnYesExitHover = isBtnNoExitHover = false;

        // stelle difficoltà
        for ( int i=0; i<8; i++) starHover[i] = false;
        // hover pulsanti acquisto item mercato
        for (int i = 0; i < 6; i++) marketBuyHover[i] = false;

        // claim reward
        isBtnClaimPrizeHover = false;
    }

    // metodo per il controllo dei click
    private boolean checkHitboxes(int x, int y) throws IOException {
        // ACTIONS IN SECONDARY WINDOWS
        if(isInfoOpen || isSettingsOpen || isLogoutOpen || isScoreboardOpen || isMarketOpen || isProfileInfosOpen) {
            // chiusura crediti di gioco
            if (isInfoOpen && areaBtnCloseInfos.contains(x, y)) {
                btnCloseInfo = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_INFOS, 0.20f);
                return clicked();
            }

            // chiusura impostazioni
            if (isSettingsOpen && areaBtnCloseSettings.contains(x, y)) {
                // salvataggio modifiche volumi
                UserProgressService.setProgress("effects_volume", effectsPercent); // salvataggio volume audio
                UserProgressService.setProgress("music_volume", musicPercent); // salvataggio volume musica

                btnCloseSettings = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_SETTINGS, 0.20f);
                return clicked();
            }

            // logout
            if (isLogoutOpen) {
                // click sul NO
                if (btn_no.contains(x, y)) {
                    btnNoExit = true;
                    clickedTimer = 0.15f;
                    setInputEnabled(false);
                    scheduleScreenChange(ACT_CLOSE_EXIT, 0.20f);
                    return clicked();
                }
                // click sul YES
                if (btn_yes.contains(x, y)) {
                    btnYesExit = true;
                    clickedTimer = 0.15f;
                    setInputEnabled(false);
                    scheduleScreenChange(ACT_YES_EXIT, 0.20f);
                    return clicked();
                }
            }

            // impostazioni
            if (isSettingsOpen) {

                // CLICK SULLA BARRA MUSICA
                if (musicBarArea.contains(x, y)) {
                    draggingMusic = true;
                    return clicked();
                }

                // CLICK SULLA BARRA EFFETTI
                if (effectsBarArea.contains(x, y)) {
                    draggingEffects = true;
                    return clicked();
                }

                // on/off dark mode switch
                if (switchDL.contains(x, y)) {
                    SoundManager.playClickButton(effectsPercent); // riproduzione suono click
                    if ((boolean) UserProgressService.getProgress("dark_mode"))
                        UserProgressService.setProgress("dark_mode", false);
                    else UserProgressService.setProgress("dark_mode", true);
                    return clicked();
                }

                // CLICK ICONE VOLUMI (il volume è in range tra 0 e 1 => 40% = 0.4f)
                // musica
                if (btnEffects.contains(x, y)) {
                    if (effectsPercent == 0f) effectsPercent = 0.5f;
                    else effectsPercent = 0f;
                    return clicked();
                }

                // effetti sonori
                if (btnMusic.contains(x, y)) {
                    if (musicPercent == 0) musicPercent = 0.5f;
                    else musicPercent = 0;
                    return clicked();
                }
            }

            // chiusura scoreboard
            if (isScoreboardOpen && areaBtnCloseScoreboard.contains(x, y)) {
                btnCloseScoreboard = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);

                scheduleScreenChange(ACT_CLOSE_SCOREBOARD, 0.20f);
                return clicked();
            }

            // market (acquisto, digit numero items, chiusura)
            if (isMarketOpen) {
                // iterazione sulle aree cliccabili per la digitazione delle quantità
                for (int i = 0; i < 6; i++) {
                    if (marketQtyAreas[i] != null && marketQtyAreas[i].contains(x, y)) {
                        activeMarketQtyField = i + 1;
                        marketQtySelected = false;
                        marketQtyInput.setLength(0);
                        marketQtyInput.append(getNumPurchaseForIndex(activeMarketQtyField));
                        return clicked();
                    }
                }

                // chiusura mercato
                if (areaBtnCloseMarket.contains(x, y)) {
                    btnCloseMarket = true;
                    //clickedTimer = 0.15f;
                    setInputEnabled(false);

                    scheduleScreenChange(ACT_CLOSE_MARKET, 0.20f);
                    return clicked();
                }

                // acquisto elementi
                for (int i = 0; i < 6; i++) {
                    if (marketBuyAreas[i] != null && marketBuyAreas[i].contains(x, y)) {
                        // elementi indicizzati da 1 a 6 per semplicità
                        int idx = i + 1; // 1..6

                        // crediti utente
                        int credits = ((Number) UserProgressService.getProgress("credits")).intValue();
                        // quantità selezionata
                        int qty = getNumPurchaseForIndex(idx);

                        // costo totale acquisto
                        int cost = getItemCost(idx, qty);

                        // regola richiesta: credits - cost >= 0
                        if (credits - cost >= 0) {
                            int oldQty = (int) UserProgressService.getProgress(items[i]);

                            credits -= cost;

                            // salva subito i crediti
                            UserProgressService.setProgress("credits", credits);

                            // incremento numero di boost
                            UserProgressService.setProgress(items[i], oldQty + qty);

                            marketBuyClicked[i] = true;
                            clickedTimer = 0.12f; // giusto un flash veloce
                            return purchased();
                        }
                    }
                }
            }

            // chiusura profile infos
            if (isProfileInfosOpen && areaBtnCloseProfileInfos.contains(x, y)) {
                btnCloseProfileInfos = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_PROFILE_INFOS, 0.20f);
                return clicked();
            }

            return false;
        }

        // controllo click stelle difficoltà 1 per disattivare le altre
        if (classicStar1.contains(x, y))    {
            difficolta[0]=0;
            // salvataggio in remoto
            UserProgressService.setProgress("diff_classic", LobbyInput.difficolta[0]);
            return clicked();
        }
        if (gravityStar1.contains(x, y))    {
            difficolta[1]=0;
            // salvataggio in remoto
            UserProgressService.setProgress("diff_gravity3", LobbyInput.difficolta[1]);
            return clicked();
        }
        if (horizontalStar1.contains(x, y)) {
            difficolta[2]=0;
            // salvataggio in remoto
            UserProgressService.setProgress("diff_horizontal", LobbyInput.difficolta[2]);
            return clicked();
        }
        if (speedyStar1.contains(x, y))     {
            difficolta[3]=0;
            // salvataggio in remoto
            UserProgressService.setProgress("diff_speedy", LobbyInput.difficolta[3]);
            return clicked();
        }

        // -- Difficoltà Game Mode "Classic" --
        // stella difficoltà 2
        if (classicStars[0].contains(x, y)) {
            if (difficolta[0] == 1 || difficolta[0] == 2) difficolta[0] = 0;   // se era 1 o 2 -> torna 0
            else difficolta[0] = 1;   // se era 0 -> diventa 1

            // salvataggio in remoto
            UserProgressService.setProgress("diff_classic", LobbyInput.difficolta[0]);

            return clicked();
        }
        // stella difficoltà 3
        if (classicStars[1].contains(x, y)) {
            if (difficolta[0] == 2) difficolta[0] = 1;   // se era 2 -> torna 1
            else difficolta[0] = 2;   // se era 0 o 1 -> diventa 2

            // salvataggio in remoto
            UserProgressService.setProgress("diff_classic", LobbyInput.difficolta[0]);

            return clicked();
        }

        // -- Difficoltà Game Mode "gravity3" --
        // stella difficoltà 2
        if (gravityStars[0].contains(x, y)) {
            if (difficolta[1] == 1 || difficolta[1] == 2) difficolta[1] = 0;
            else difficolta[1] = 1;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_gravity3", LobbyInput.difficolta[1]);

            return clicked();
        }
        // stella difficoltà 3
        if (gravityStars[1].contains(x, y)) {
            if (difficolta[1] == 2) difficolta[1] = 1;
            else difficolta[1] = 2;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_gravity3", LobbyInput.difficolta[1]);

            return clicked();
        }

        // -- Difficoltà Game Mode "Horizontal" --
        // stella difficoltà 1
        if (horizontalStars[0].contains(x, y)) {
            if (difficolta[2] == 1 || difficolta[2] == 2) difficolta[2] = 0;
            else difficolta[2] = 1;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_horizontal", LobbyInput.difficolta[2]);

            return clicked();
        }
        // stella difficoltà 2
        if (horizontalStars[1].contains(x, y)) {
            if (difficolta[2] == 2) difficolta[2] = 1;
            else difficolta[2] = 2;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_horizontal", LobbyInput.difficolta[2]);

            return clicked();
        }

        // -- Difficoltà Game Mode "Speedy" --
        // stella difficoltà 1
        if (speedyStars[0].contains(x, y)) {
            if (difficolta[3] == 1 || difficolta[3] == 2) difficolta[3] = 0;
            else difficolta[3] = 1;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_speedy", LobbyInput.difficolta[3]);

            return clicked();
        }
        // stella difficoltà 2
        if (speedyStars[1].contains(x, y)) {
            if (difficolta[3] == 2) difficolta[3] = 1;
            else difficolta[3] = 2;

            // salvataggio in remoto
            UserProgressService.setProgress("diff_speedy", LobbyInput.difficolta[3]);

            return clicked();
        }

        // GAME MODES //
        // "classic"
        if (classicArea.contains(x, y)) {
            classic = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_CLASSIC, 0.20f);
            return clicked();
        }
        // "gravity3"
        if (gravity3Area.contains(x, y)) {
            gravity3 = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_GRAVITY3, 0.20f);
            return clicked();
        }
        // "horizontal"
        if (horizontalArea.contains(x, y)) {
            horizontal = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_HORIZONTAL, 0.20f);
            return clicked();
        }
        // "speedy"
        if (speedyArea.contains(x, y)) {
            speedy = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_SPEEDY, 0.20f);
            return clicked();
        }

        // SECONDARY WINDOWS //
        // scoreboard
        if (areaBtnOpenScoreboard.contains(x, y)) {
            isBtnScoreboardClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_SCOREBOARD, 0.20f);
            return clicked();
        }
        // market
        if(areaBtnOpenMarket.contains(x,y)) {
            isBtnMarketClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_MARKET, 0.20f);
            return clicked();
        }
        // profile infos, il controllo supplementare permette di chiudere la schermata "Profile Infos" quando aperta
        if (areaBtnOpenProfileInfos.contains(x, y) && !isProfileInfosOpen) {
            isBtnProfileInfosClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_PROFILE_INFOS, 0.20f);
            return clicked();
        }

        // pulsante claim reward 'daily'
        if (btnClaimPrize.contains(x, y) &&
            !((boolean) UserProgressService.getProgress("is_daily_reward_claimed")) &&
            ((boolean) UserProgressService.getProgress("is_daily_completed"))
        ) {
            isBtnClaimPrizeClicked=true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_CLAIM_REWARD_DAILY, 0.20f);
            return clicked();
        }

        // -- COMMAND BAR --
        // logout
        if (areaOpenLogout.contains(x, y)) {
            isBtnLogoutClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_EXIT, 0.20f);
            return clicked();
        }
        // game infos
        if (areaOpenInfos.contains(x, y)) {
            isBtnInfoClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_INFOS, 0.20f);
            return clicked();
        }
        // settings
        if (areaOpenSettings.contains(x, y)) {
            isBtnSettingsClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_SETTINGS, 0.20f);
            return clicked();
        }

        return false;
    }

    // METODI DI InputProcessor //
    // "ascolta" il movimento del mouse
    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        if (!inputEnabled) return false;

        // reset hover
        resetHover();

        // sets hover states for open window buttons
        if(isInfoOpen || isSettingsOpen || isLogoutOpen || isScoreboardOpen || isMarketOpen || isProfileInfosOpen) {
            // close game credits
            if(areaBtnCloseInfos.contains(screenX,screenY) && isInfoOpen) {
                isBtnCloseInfoHover = true; return true;
            }
            // close settings
            if(areaBtnCloseSettings.contains(screenX,screenY) && isSettingsOpen) {
                isBtnCloseSettingsHover = true; return true;
            }
            // no logout
            if(btn_no.contains(screenX,screenY)) {
                isBtnNoExitHover=true; return  true;
            }
            // yes logout
            if(btn_yes.contains(screenX,screenY)) {
                isBtnYesExitHover=true; return  true;
            }
            // close scoreboard
            if (areaBtnCloseScoreboard.contains(screenX,screenY) && isScoreboardOpen) {
                isBtnCloseScoreboardHover=true; return true;
            }
            // close market -> l'ulteriore controllo con market server per evitare l'overlay con la X della scoreboard
            if(areaBtnCloseMarket.contains(screenX,screenY) && isMarketOpen) {
                isBtnCloseMarketHover=true; return true;
            }

            // close profile infos
            if (areaBtnCloseProfileInfos.contains(screenX, screenY) && isProfileInfosOpen) {
                isBtnCloseProfileInfosHover = true; return true;
            }

            // MARKET: hover pulsanti acquisto
            for (int i = 0; i < 6; i++) {
                if (marketBuyAreas[i] != null && marketBuyAreas[i].contains(screenX, screenY)) {

                    // elementi indicizzati da 1 a 6 per semplicità
                    int idx = i + 1; // 1..6

                    // crediti utente
                    int credits = ((Number) UserProgressService.getProgress("credits")).intValue();
                    // quantità selezionata
                    int qty = getNumPurchaseForIndex(idx);
                    // costo totale acquisto
                    int cost = getItemCost(idx, qty);

                    // regola richiesta: credits - cost >= 0
                    if (credits - cost >= 0) marketBuyHover[i] = true;
                    return true;
                }
            }

            return false;
        }


        // hover per le stelle difficoltà modalità "classic"
        if (classicStars[0].contains(screenX, screenY)) {
            if (!starClicked[0]) starHover[0] = true;
            return true;
        }
        else if (classicStars[1].contains(screenX, screenY)) {
            if (!starClicked[1]) { starHover[1] = true; starHover[0] = true; }
            return true;
        }

        // hover per le stelle difficoltà modalità "4 gravità"
        if (gravityStars[0].contains(screenX, screenY)) {
            if (!starClicked[2]) starHover[2] = true;
            return true;
        }
        else if (gravityStars[1].contains(screenX, screenY)) {
            if (!starClicked[3]) { starHover[3] = true; starHover[2] = true; }
            return true;
        }

        // hover per le stelle difficoltà modalità "orizzontale"
        if (horizontalStars[0].contains(screenX, screenY)) {
            if (!starClicked[4]) starHover[4] = true;
            return true;
        }
        else if (horizontalStars[1].contains(screenX, screenY)) {
            if (!starClicked[5]) { starHover[5] = true; starHover[4] = true; }
            return true;
        }

        // hover per le stelle difficoltà modalità "speedy"
        if (speedyStars[0].contains(screenX, screenY)) {
            if (!starClicked[6]) starHover[6] = true;
            return true;
        }
        else if (speedyStars[1].contains(screenX, screenY)) {
            if (!starClicked[7]) { starHover[7] = true; starHover[6] = true; }
            return true;
        }

        // GAME MODS //
        // classic
        if (classicArea.contains(screenX, screenY)) {
            classicHover = true; return true;
        }
        // gravity3
        if (gravity3Area.contains(screenX, screenY)) {
            gravity3Hover = true; return true;
        }
        // horizontal
        if (horizontalArea.contains(screenX, screenY)) {
            horizontalHover= true; return true;
        }
        // speedy
        if (speedyArea.contains(screenX, screenY)) {
            speedyHover= true; return true;
        }

        // SECONDARY WINDOWS
        // scoreboard
        if (areaBtnOpenScoreboard.contains(screenX, screenY)) {
            isBtnScoreboardHover = true; return true;
        }
        // market
        if (areaBtnOpenMarket.contains(screenX,screenY)) {
            isBtnMarketHover=true; return true;
        }
        // profile infos
        if (areaBtnOpenProfileInfos.contains(screenX, screenY)) {
            isBtnProfileInfosHover=true; return true;
        }

        // btn claim reward 'daily'
        if (btnClaimPrize.contains(screenX, screenY) &&
            !((boolean) UserProgressService.getProgress("is_daily_reward_claimed")) &&
            ((boolean) UserProgressService.getProgress("is_daily_completed"))
        ) {
            isBtnClaimPrizeHover=true; return true;
        }

        return false;
    }

    // controllo rilascio del mouse
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (!inputEnabled) return false;
        draggingMusic = false;
        draggingEffects = false;
        return false;
    }

    // controllo drag del mouse
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!inputEnabled) return false;

        // barra volume effetti
        if (draggingEffects) effectsPercent = Math.min(1f, Math.max(0f, (screenX - 285) / (float) (685 - 285)));
        // barra volume musica
        if (draggingMusic) musicPercent = Math.min(1f, Math.max(0f, (screenX - 285) / (float) (685 - 285)));


        return false;
    }

    // controllo click tasto tastiera
    @Override public boolean keyDown(int keycode) {
        if (!inputEnabled) return false;

        // Ctrl + A: seleziona tutto il testo del campo quantità attivo nel market
        if (isMarketOpen && activeMarketQtyField != 0 &&
            keycode == Input.Keys.A &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {

            if (!marketQtyInput.isEmpty()) marketQtySelected = true;
            return true;
        }

        // ESC chiude le finestre aperte (come click sulla X)
        if (keycode == Input.Keys.ESCAPE)
        {
            // pagina crediti di gioco
            if (isInfoOpen) {
                isInfoOpen = false;
                return true;
            }

            // pagina impostazioni di gioco
            if (isSettingsOpen) {
                // salvataggio modifiche volumi
                UserProgressService.setProgress("effects_volume", effectsPercent); // salvataggio volume audio
                UserProgressService.setProgress("music_volume", musicPercent); // salvataggio volume musica

                scheduleScreenChange(ACT_CLOSE_SETTINGS, 0);
                return true;
            }

            // pagina per il logout
            if (isLogoutOpen) {
                isLogoutOpen = false;
                return true;
            }
            else if (!isMarketOpen && !isScoreboardOpen) isLogoutOpen = true;

            // mercato
            if (isMarketOpen) {
                isMarketOpen = false;
                return true;
            }

            // scoreboard
            if (isScoreboardOpen) {
                isScoreboardOpen = false;
                return true;
            }
        }

        return false;
    }

    // setter stato input
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }
    // getter stato input
    public boolean isInputEnabled() { return inputEnabled; }

    // metodo per impostare lo stato al click e il tempo di passaggio alla nuova schermata
    public void scheduleScreenChange(int nextState, float delaySeconds) {
        pendingScreenChange = true;
        pendingNextState = nextState;
        screenChangeDelay = delaySeconds;
    }

    // metodo per aggiornare i tempi di delay dopo i click
    public void update(float delta) throws IOException {

        // timer per spegnere "clicked"
        if (clickedTimer > 0f) {
            clickedTimer -= delta;
            if (clickedTimer <= 0f) resetClickedFlags();
        }

        // timer per azione ritardata
        if (pendingScreenChange) {
            screenChangeDelay -= delta;
            if (screenChangeDelay <= 0f) {
                pendingScreenChange = false;
                executePendingAction(pendingNextState);
                pendingNextState = -1;
            }
        }

        // timer per aggiornare la mappa con i punti utente
        if (timerUpdateUsersPointsMap > 0f) {
            timerUpdateUsersPointsMap -= delta;
            if (timerUpdateUsersPointsMap <= 0f) {
                loadUsersPoints();
                timerUpdateUsersPointsMap += 60f; // di nuovo 60 secondi
            }
        }
    }

    // azioni al click
    private void executePendingAction(int act) throws IOException {
        // qui stai ancora nella lobby -> riaccendi input
        setInputEnabled(true);

        switch (act) {
            case ACT_OPEN_MARKET:
                // init numero di item da acquistare (minimo 1)
                numPurchaseItem1 = numPurchaseItem2 = numPurchaseItem3 = numPurchaseItem4 = numPurchaseItem5 = numPurchaseItem6 = 1;

                // reset stato digitazione
                activeMarketQtyField = 0;
                marketQtySelected = false;
                marketQtyInput.setLength(0);

                // init prezzi (totali mostrati = unitario * quantità)
                updateMarketPrices();

                isMarketOpen = true;
                break;

            case ACT_OPEN_SCOREBOARD:
                loadUsersPoints();// caricamento punti utenti
                isScoreboardOpen = true;
                break;

            case ACT_OPEN_PROFILE_INFOS:
                isProfileInfosOpen = true;
                break;

            case ACT_OPEN_INFOS:
                isInfoOpen = true;
                break;

            case ACT_OPEN_SETTINGS:
                isSettingsOpen = true;
                break;

            case ACT_OPEN_EXIT:
                isLogoutOpen = true;
                break;

            case ACT_CLOSE_INFOS:
                isInfoOpen = false;
                break;

            case ACT_CLOSE_SETTINGS:
                isSettingsOpen = false;
                draggingMusic = false;
                draggingEffects = false;
                break;

            case ACT_CLOSE_EXIT, ACT_YES_EXIT:
                isLogoutOpen = false;
                break;

            case ACT_CLOSE_MARKET:
                isMarketOpen=false;
                break;

            case ACT_CLOSE_SCOREBOARD:
                isScoreboardOpen=false;
                break;

            case ACT_CLOSE_PROFILE_INFOS:
                isProfileInfosOpen=false;
                break;

            case ACT_CLAIM_REWARD_DAILY: {
                // salvataggio raccolta premio
                UserProgressService.setProgress("is_daily_reward_claimed", true);

                // salva lo sblocco alla prossima mezzanotte (ms)
                long unlockAt = DailyChallenges.nextMidnightFromNow().toEpochMilli();
                UserProgressService.setProgress("daily_next_unlock_at", unlockAt);

                // incremento missione
                int numMission = (int) UserProgressService.getProgress("num_mission");
                UserProgressService.setProgress("num_mission", numMission + 1);

                // assegnazione premio
                int credits = (int) UserProgressService.getProgress("credits");
                UserProgressService.setProgress("credits", credits + DailyChallenges.getPrize());
                break;
            }
        }
    }

    // ALTRI METODI DI InputProcessor //
    @Override public boolean keyUp(int i) { return false; }
    @Override
    public boolean keyTyped(char c) {
        if (!inputEnabled) return false;

        // digitazione solo nel MARKET e solo se un campo è attivo
        if (!isMarketOpen || activeMarketQtyField == 0) return false;

        // ENTER: chiude la digitazione del campo
        if (c == '\n' || c == '\r') {
            activeMarketQtyField = 0;
            marketQtySelected = false;
            marketQtyInput.setLength(0);
            return typed();
        }

        // BACKSPACE: cancella (tutto se selezionato, altrimenti un carattere)
        if (c == '\b') {
            if (marketQtySelected) {
                marketQtyInput.setLength(0);
                marketQtySelected = false;
            } else if (!marketQtyInput.isEmpty()) marketQtyInput.deleteCharAt(marketQtyInput.length() - 1);

            // se vuoto, lasciamo minimo 1 come valore logico
            if (marketQtyInput.isEmpty()) setNumPurchaseForIndex(activeMarketQtyField, 1);
            else applyMarketQtyCandidate(marketQtyInput.toString());
            return typed();
        }

        // se selezionato e arriva un carattere "normale", sostituiamo tutto
        if (marketQtySelected) {
            marketQtyInput.setLength(0);
            marketQtySelected = false;
        }

        // accetta solo cifre (0..9) -> ma valore finale deve rimanere >= 1
        if (c < '0' || c > '9') return typed();

        // evita zeri iniziali (es: "0", "01")
        if (marketQtyInput.isEmpty() && c == '0') return typed();

        // limita lunghezza per evitare numeri enormi
        if (marketQtyInput.length() >= 4) return typed();

        String candidate = marketQtyInput.toString() + c;
        applyMarketQtyCandidate(candidate);
        return typed();
    }

    // prova ad applicare il valore digitato rispettando il massimo acquistabile
    private void applyMarketQtyCandidate(String candidate) {
        int value;
        try {
            value = Integer.parseInt(candidate);
        } catch (NumberFormatException ex) {
            return;
        }

        if (value < 1) return;

        int max = getMaxPurchasable(activeMarketQtyField);
        // se max == 0 significa che non puoi comprare neanche 1 (con la condizione credits - cost >= 0)
        if (max >= 0 && value <= max) {
            marketQtyInput.setLength(0);
            marketQtyInput.append(value);
            setNumPurchaseForIndex(activeMarketQtyField, value);
        }
        // se value eccede max, ignoriamo la digitazione (non aggiorniamo nulla)
    }

    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }
    @Override public boolean scrolled(float v, float v1) {
        return false;
    }
}
