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
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.SoundManager;
import sorgente.UserData.UserProgressService;

public class LobbyInput implements InputProcessor {
    private static final Log log = LogFactory.getLog(LobbyInput.class);
    private int numero=0;

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
    // X chiusura schermate in sovra impressione
    protected boolean btnCloseSettings;
    protected boolean btnCloseInfo;
    protected boolean btnCloseMarket;
    protected boolean btnCloseScoreboard;
    // game mods
    protected boolean classic, gravity4, horizontal, speedy;


    // FLAGS BUTTONS HOVER //
    // difficoltà game mods
    protected boolean[] starHover=new boolean[8];
    // apertura schermate in sovra impressione
    protected boolean isBtnScoreboardHover;
    protected boolean isBtnMarketHover;
    // yes/no logout
    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;
    // X chiusura schermate in sovra impressione
    protected boolean isBtnCloseInfoHover;
    protected boolean isBtnCloseSettingsHover;
    protected boolean isBtnCloseMarketHover;
    protected boolean isBtnCloseScoreboardHover;
    // game mods
    protected boolean classicHover, gravity4Hover, horizontalHover, speedyHover;

    // FLAGS CONTROLLO SCHERMATE APERTE //
    // per aprire le finestre in sovra impressione
    protected boolean isInfoOpen, isLogoutOpen, isSettingsOpen, isScoreboardOpen,
        isMarketOpen; // isMarketOpen è usata una volta in più in un controllo

    private final Pixmap mouse;
    private final Cursor cursor;

    // variabili per i delay tra le schermate
    protected float clickedTimer = 0f; // durata dell'icona "clicked"
    protected float screenChangeDelay = 0f;
    protected boolean pendingScreenChange = false;
    protected int pendingNextState = -1;

    // azione pronta da eseguire fuori (solo per cambio screen / exit)
    private boolean inputEnabled = true;

    // HITBOX //
    protected Rectangle musicBarArea;
    protected Rectangle effectsBarArea;
    protected Rectangle switchDL;

    private Rectangle classicStar1, gravityStar1, horizontalStar1, speedyStar1;
    private final Rectangle[] classicStars = new Rectangle[3];
    private final Rectangle[] gravityStars = new Rectangle[3];
    private final Rectangle[] horizontalStars = new Rectangle[3];
    private final Rectangle[] speedyStars = new Rectangle[3];

    private Rectangle btnCloseInfoArea;
    private Rectangle btnCloseSettingsArea;
    private Rectangle btnCloseMarketArea;
    private Rectangle btnCloseScoreboardArea;

    private Rectangle btn_no;
    private Rectangle btn_yes;

    private Rectangle classicArea;
    private Rectangle gravity4Area;
    private Rectangle horizontalArea;

    private Rectangle speedyArea;
    private Rectangle scoreboardArea;

    private Rectangle settingsArea;
    private Rectangle informationArea;

    private Rectangle exitArea;
    private Rectangle marketButton;

    // AZIONI CLICK //
    public static final int ACT_CLOSE_INFO = 1;
    public static final int ACT_CLOSE_SETTINGS = 2;
    public static final int ACT_CLOSE_MARKET = 3;
    public static final int ACT_OPEN_INFO = 4;
    public static final int ACT_OPEN_SETTINGS = 5;
    public static final int ACT_OPEN_EXIT = 6;
    public static final int ACT_CLOSE_SCOREBOARD = 7;

    public static final int ACT_START_CLASSIC = 10;
    public static final int ACT_START_GRAVITY4 = 11;
    public static final int ACT_START_HORIZONTAL = 12;
    public static final int ACT_START_SPEEDY = 13;
    public static final int ACT_OPEN_SCOREBOARD = 30;
    public static final int ACT_OPEN_MARKET = 31;
    public static final int ACT_CLOSE_EXIT = 32;
    public static final int ACT_YES_EXIT = 33;

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
        difficolta[1] = (int) UserProgressService.getProgress("diff_gravity4");
        difficolta[2] = (int) UserProgressService.getProgress("diff_horizontal");
        difficolta[3] = (int) UserProgressService.getProgress("diff_speedy");
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
        gravity4Area    = new Rectangle(266, 163, 210, 200);
        horizontalArea  = new Rectangle(504, 163, 210, 200);
        speedyArea      = new Rectangle(745, 163, 210, 200);

        // Hitbox secondarie
        scoreboardArea  = new Rectangle(360, 436, 260, 170);

        // Bottoni
        exitArea        = new Rectangle(422, 629, 30, 30);
        informationArea = new Rectangle(475, 629, 30, 30);
        settingsArea    = new Rectangle(534, 629, 30, 30);

        btnCloseInfoArea = new Rectangle(686,217,40,40);
        btnCloseSettingsArea = new Rectangle(686,245,40,40);
        btnCloseMarketArea= new Rectangle(814,187,40,40);
        btnCloseScoreboardArea= new Rectangle(814,174,40,40);

        btn_no= new Rectangle(503,408,150,50);
        btn_yes= new Rectangle(341,408,150,50);

        marketButton= new Rectangle(831,64,50,50);
    }

    // genera il suono al click
    private boolean clicked() {
        SoundManager.playClickButton(effectsPercent);
        return true;
    }

    // rilascio unica risorsa grafica
    public void dispose() {
        mouse.dispose();
    }

    // controllo click
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println(screenX + " " + screenY);
        return checkHitboxes(screenX, screenY);
    }

    // metodo per resettare i click
    private void resetClickedFlags() {
        // game mods
        classic = gravity4 = horizontal = speedy = false;

        // apertura schermate in sovra impressione
        isBtnScoreboardClicked = isBtnMarketClicked = isBtnSettingsClicked = isBtnInfoClicked = false;

        // chiusura schermate in sovra impressione
        btnCloseScoreboard = btnCloseMarket = btnCloseSettings = btnCloseInfo = false;

        // yes/no logout
        isBtnLogoutClicked = btnNoExit = btnYesExit = false;
    }

    // resetta lo stato di Hover dei pulsanti
    private void resetHover() {
        // game mods
        classicHover = gravity4Hover = horizontalHover = speedyHover = false;

        // apertura schermate in sovra impressione
        isBtnScoreboardHover = isBtnMarketHover = false;

        // chiusura schermate in sovra impressione
        isBtnCloseInfoHover       = false;
        isBtnCloseSettingsHover   = false;
        isBtnCloseMarketHover     = false;
        isBtnCloseScoreboardHover = false;

        // yes/no logout
        isBtnYesExitHover = isBtnNoExitHover = false;

        // stelle difficoltà
        for ( int i=0; i<8; i++) starHover[i] = false;
    }

    // metodo per il controllo dei click
    private boolean checkHitboxes(int x, int y) {
        // pagine in sovra impressione
        if(isInfoOpen || isSettingsOpen || isLogoutOpen || isScoreboardOpen || isMarketOpen) {
            // chiusura crediti di gioco
            if (isInfoOpen && btnCloseInfoArea.contains(x, y)) {
                btnCloseInfo = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_INFO, 0.20f);
                return clicked();
            }

            // chiusura impostazioni
            if (isSettingsOpen && btnCloseSettingsArea.contains(x, y)) {
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
                if (switchDL.contains(x,y)) {
                    SoundManager.playClickButton(effectsPercent); // riproduzione suono click
                    if ((boolean)UserProgressService.getProgress("darkMode")) UserProgressService.setProgress("darkMode", false);
                    else UserProgressService.setProgress("darkMode", true);

                    System.out.println((boolean)UserProgressService.getProgress("darkMode") ? "on" : "off");
                    return clicked();
                }
            }

            // chiusura mercato
            if(isMarketOpen && btnCloseMarketArea.contains(x,y)) {
                btnCloseMarket=true;
                //clickedTimer = 0.15f;
                setInputEnabled(false);

                scheduleScreenChange(ACT_CLOSE_MARKET, 0.20f);
                return clicked();

            }

            // chiusura scoreboard
            if(isScoreboardOpen && btnCloseScoreboardArea.contains(x, y)) {
                btnCloseScoreboard = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);

                scheduleScreenChange(ACT_CLOSE_SCOREBOARD, 0.20f);
                return clicked();
            }
            return false;
        }

        // controllo click stelle difficoltà 1 per disattivare le altre
        if (classicStar1.contains(x, y))    { difficolta[0]=0; return clicked(); }
        if (gravityStar1.contains(x, y))    { difficolta[1]=0; return clicked(); }
        if (horizontalStar1.contains(x, y)) { difficolta[2]=0; return clicked(); }
        if (speedyStar1.contains(x, y))     { difficolta[3]=0; return clicked(); }

        // -- Difficoltà Game Mode "Classic" --
        // stella difficoltà 2
        if (classicStars[0].contains(x, y)) {
            if (difficolta[0] == 1 || difficolta[0] == 2) difficolta[0] = 0;   // se era 1 o 2 -> torna 0
            else difficolta[0] = 1;   // se era 0 -> diventa 1

            return clicked();
        }
        // stella difficoltà 3
        if (classicStars[1].contains(x, y)) {
            if (difficolta[0] == 2) difficolta[0] = 1;   // se era 2 -> torna 1
            else difficolta[0] = 2;   // se era 0 o 1 -> diventa 2

            return clicked();
        }

        // -- Difficoltà Game Mode "Gravity4" --
        // stella difficoltà 2
        if (gravityStars[0].contains(x, y)) {
            if (difficolta[1] == 1 || difficolta[1] == 2) difficolta[1] = 0;
            else difficolta[1] = 1;
            return clicked();
        }
        // stella difficoltà 3
        if (gravityStars[1].contains(x, y)) {
            if (difficolta[1] == 2) difficolta[1] = 1;
            else difficolta[1] = 2;
            return clicked();
        }

        // -- Difficoltà Game Mode "Horizontal" --
        // stella difficoltà 1
        if (horizontalStars[0].contains(x, y)) {
            if (difficolta[2] == 1 || difficolta[2] == 2) difficolta[2] = 0;
            else difficolta[2] = 1;
            return clicked();
        }
        // stella difficoltà 2
        if (horizontalStars[1].contains(x, y)) {
            if (difficolta[2] == 2) difficolta[2] = 1;
            else difficolta[2] = 2;
            return clicked();
        }

        // -- Difficoltà Game Mode "Speedy" --
        // stella difficoltà 1
        if (speedyStars[0].contains(x, y)) {
            if (difficolta[3] == 1 || difficolta[3] == 2) difficolta[3] = 0;
            else difficolta[3] = 1;
            return clicked();
        }
        // stella difficoltà 2
        if (speedyStars[1].contains(x, y)) {
            if (difficolta[3] == 2) difficolta[3] = 1;
            else difficolta[3] = 2;
            return clicked();
        }

        // click pulsante modalità "classic"
        if (classicArea.contains(x, y)) {
            classic = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_CLASSIC, 0.20f);
            return clicked();
        }

        // click pulsante modalità "gravity4"
        if (gravity4Area.contains(x, y)) {
            gravity4 = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_GRAVITY4, 0.20f);
            return clicked();
        }

        // click pulsante modalità "horizontal"
        if (horizontalArea.contains(x, y)) {
            horizontal = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_HORIZONTAL, 0.20f);
            return clicked();
        }

        // click pulsante modalità "speedy"
        if (speedyArea.contains(x, y)) {
            speedy = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_SPEEDY, 0.20f);
            return clicked();
        }

        if(marketButton.contains(x,y)) {
            isBtnMarketClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_MARKET, 0.20f);
            return clicked();
        }

        // click pulsante classifica
        if (scoreboardArea.contains(x, y)) {
            isBtnScoreboardClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_SCOREBOARD, 0.20f);
            return clicked();
        }

        // -- COMMAND BAR --
        if (exitArea.contains(x, y)) {
            isBtnLogoutClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_EXIT, 0.20f);
            return clicked();
        }

        if (informationArea.contains(x, y)) {
            isBtnInfoClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_INFO, 0.20f);
            return clicked();
        }

        if (settingsArea.contains(x, y)) {
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
        if(isInfoOpen || isSettingsOpen || isLogoutOpen || isScoreboardOpen || isMarketOpen) {
            // close game credits
            if(btnCloseInfoArea.contains(screenX,screenY)) {
                isBtnCloseInfoHover = true;
                return true;
            }

            // close settings
            if(btnCloseSettingsArea.contains(screenX,screenY)) {
                isBtnCloseSettingsHover = true;
                return true;
            }
            // no logout
            if(btn_no.contains(screenX,screenY)) {
                isBtnNoExitHover=true;
                return  true;
            }
            // yes logout
            if(btn_yes.contains(screenX,screenY)) {
                isBtnYesExitHover=true;
                return  true;
            }
            // close market -> l'ulteriore controllo con market server per evitare l'overlay della scoreboard
            if(btnCloseMarketArea.contains(screenX,screenY) && isMarketOpen) {
                isBtnCloseMarketHover=true;
                return true;
            }
            // close scoreboard
            if (btnCloseScoreboardArea.contains(screenX,screenY)) {
                isBtnCloseScoreboardHover=true;
                return true;
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

        // hover per i pulsanti delle schermate secondarie
        if(marketButton.contains(screenX,screenY)) { // mercato
            isBtnMarketHover=true;
            return true;
        }

        if (classicArea.contains(screenX, screenY)) { // game "classic"
            classicHover = true;

            return true;
        }

        if (gravity4Area.contains(screenX, screenY)) { // game "gravity4"
            gravity4Hover = true;
            return true;
        }

        if (horizontalArea.contains(screenX, screenY)) { // game "horizontal"

            horizontalHover= true;
            return true;
        }

        if (speedyArea.contains(screenX, screenY)) { // game "speedy"
            speedyHover= true;
            return true;
        }

        if (scoreboardArea.contains(screenX, screenY)) { // schermata classifica
            isBtnScoreboardHover = true;
            return true;
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
    public void update(float delta) {

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
    }

    // azioni al click
    private void executePendingAction(int act) {
        // qui stai ancora nella lobby -> riaccendi input
        setInputEnabled(true);

        switch (act) {
            case ACT_OPEN_MARKET:
                isMarketOpen = true;
                break;

            case ACT_OPEN_SCOREBOARD:
                isScoreboardOpen = true;
                break;

            case ACT_OPEN_INFO:
                isInfoOpen = true;
                break;

            case ACT_OPEN_SETTINGS:
                isSettingsOpen = true;
                break;

            case ACT_OPEN_EXIT:
                isLogoutOpen = true;
                break;

            case ACT_CLOSE_INFO:
                isInfoOpen = false;
                break;

            case ACT_CLOSE_SETTINGS:
                isSettingsOpen = false;
                draggingMusic = false;
                draggingEffects = false;
                break;

            case ACT_CLOSE_EXIT:
                isLogoutOpen = false;
                break;

            case ACT_YES_EXIT:
                isLogoutOpen = false;
                break;

            case ACT_CLOSE_MARKET:
                isMarketOpen=false;
                break;

            case ACT_CLOSE_SCOREBOARD:
                isScoreboardOpen=false;
                break;
        }
    }

    // ALTRI METODI DI InputProcessor //
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }
    @Override public boolean scrolled(float v, float v1) {
        return false;
    }
}
