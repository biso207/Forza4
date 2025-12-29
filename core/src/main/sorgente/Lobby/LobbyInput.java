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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LobbyInput implements InputProcessor {
    private static final Log log = LogFactory.getLog(LobbyInput.class);
    private AudioSettings audio= new AudioSettings();
    private int numero=0;

    //Clicked

    protected int[] difficolta=new int[8];
    protected boolean[] starHover=new boolean[8];
    protected boolean[] starClicked=new boolean[8];

    protected boolean draggingMusic = false;
    protected boolean draggingEffects = false;


    protected boolean btnNoExit;
    protected boolean btnYesExit;
    protected boolean btnCloseSettings;
    protected boolean btnCloseInfo;

    protected boolean classic;
    protected boolean gravity4;
    protected boolean horizontal;
    protected boolean speedy;
    protected boolean scoreboard;
    protected boolean daily;

    protected boolean settings;
    protected boolean information;
    protected boolean exit;

    // Hover
    protected boolean isBtnMarket;
    protected boolean isBtnMarketClicked;
    protected boolean isBtnSwitch;
    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;
    protected boolean isBtnCloseInfoHover;
    protected boolean isBtnCloseSettingsHover;
    protected boolean isBtnCloseMarket;
    protected boolean isBtnCloseMarketHover;
    protected boolean classicHover;
    protected boolean gravity4Hover;
    protected boolean horizontalHover;
    protected boolean speedyHover;
    protected boolean marketHover;
    protected boolean scoreboardHover;
    protected boolean settingsHover;
    protected boolean informationHover;
    protected boolean exitHover;

    //Per Aprire le finestre
    protected boolean isWindowOpenInfo, isWindowOpenExit, isWindowOpenSettings, isWindowOpenScoreboard,
        isWindowOpenMarket;

    private final Pixmap mouse;
    private final Cursor cursor;

    // variabili per i delay tra le schermate
    protected float clickedTimer = 0f; // durata dell'icona "clicked"
    protected float screenChangeDelay = 0f;
    protected boolean pendingScreenChange = false;
    protected int pendingNextState = -1;

    // azione pronta da eseguire fuori (solo per cambio screen / exit)
    private int readyAction = -1;

    private boolean inputEnabled = true;

    //Hitbox
    protected final Rectangle musicBarArea;
    protected final Rectangle effectsBarArea;
    protected Rectangle switchL;
    protected final Rectangle switchD;
    protected Rectangle switchE;

    private final Rectangle classicStar1, gravityStar1, horizontalStar1, speedyStar1;
    private final Rectangle[] classicStars = new Rectangle[3];
    private final Rectangle[] gravityStars = new Rectangle[3];
    private final Rectangle[] horizontalStars = new Rectangle[3];
    private final Rectangle[] speedyStars = new Rectangle[3];

    private final Rectangle btnCloseInfoArea;
    private final Rectangle btnCloseSettingsArea;
    private final Rectangle btnCloseMarketArea;

    private final Rectangle btn_no;
    private final Rectangle btn_yes;

    private final Rectangle classicArea;
    private final Rectangle gravity4Area;
    private final Rectangle horizontalArea;

    private final Rectangle speedyArea;
    private final Rectangle scoreboardArea;

    private final Rectangle settingsArea;
    private final Rectangle informationArea;

    private final Rectangle exitArea;

    private final Rectangle marketButton;

    // azioni click sulle schermate
    public static final int ACT_CLOSE_INFO = 1;
    public static final int ACT_CLOSE_SETTINGS = 2;
    public static final int ACT_CLOSE_MARKET = 3;


    public static final int ACT_START_CLASSIC = 10;
    public static final int ACT_START_GRAVITY4 = 11;
    public static final int ACT_START_HORIZONTAL = 12;
    public static final int ACT_START_SPEEDY = 13;
    public static final int ACT_OPEN_SCOREBOARD = 30;
    public static final int ACT_OPEN_MARKET = 31;
    public static final int ACT_CLOSE_EXIT = 32;

    // costruttore
    public LobbyInput() {
        isWindowOpenInfo =false;
        isWindowOpenExit =false;
        isWindowOpenSettings=false;


        classic = gravity4 = horizontal = speedy = scoreboard = daily = false;
        settings = information = exit = false;

        isBtnMarket=false;

        // Hitbox principali
        switchD=new Rectangle(407,324,30,30);
        switchL=new Rectangle(535,324,30,30);
        switchE=switchL;

        // Barra volume musica
        musicBarArea = new Rectangle(316, 375, 370, 40); // x, y, width, height // Barra volume effetti
        effectsBarArea = new Rectangle(316, 425, 370, 40);

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
        exitArea        = new Rectangle(430, 615, 30, 30);
        informationArea = new Rectangle(481, 615, 30, 30);
        settingsArea    = new Rectangle(540, 615, 30, 30);

        btnCloseInfoArea = new Rectangle(686,217,40,40);
        btnCloseSettingsArea = new Rectangle(686,245,40,40);
        btnCloseMarketArea= new Rectangle(826,219,40,40);

        btn_no= new Rectangle(503,408,150,50);
        btn_yes= new Rectangle(341,408,150,50);

        marketButton= new Rectangle(831,64,50,50);

        // Cursor personalizzato
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);
    }

    private void resetClickedFlags() {
        btnCloseInfo = false;
        btnCloseSettings = false;
        btnNoExit = false;
        btnYesExit = false;

        // se vuoi includere anche questi:
        isBtnMarketClicked = false;
        classic = false;
        gravity4 = false;
        horizontal = false;
        speedy = false;
        daily = false;
        scoreboard = false;
    }

    // la UI chiama questo e se c'è un'azione pronta la consuma
    public int consumeReadyAction() {
        int a = readyAction;
        readyAction = -1;
        return a;
    }

    public void dispose()
    {
        mouse.dispose();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {

        //if (!inputEnabled) return false;
        System.out.println(screenX+" "+screenY);

        boolean click = checkHitboxes(screenX, screenY);

        if (click)
        {
           // SoundManager.playClickButton();
        }

        return click;
    }



    private void resetHover()
    {
        classicHover = gravity4Hover = horizontalHover = speedyHover = false;
        marketHover = scoreboardHover = false;
        settingsHover = informationHover = exitHover = false;
        isBtnCloseInfoHover = false;
        isBtnCloseSettingsHover = false;
        isBtnCloseMarketHover=false;
        isBtnYesExitHover=false;
        isBtnNoExitHover=false;

        isBtnMarket=false;

        starHover[0]=false;
        starHover[1]=false;
        starHover[2]=false;
        starHover[3]=false;
        starHover[4]=false;
        starHover[5]=false;
        starHover[6]=false;
        starHover[7]=false;
    }

    // "ascolta" il movimento del mouse
    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        if (!inputEnabled) return false;
        resetHover();


        // sets hover states for open window buttons
        if(isWindowOpenInfo || isWindowOpenSettings || isWindowOpenExit || isWindowOpenScoreboard || isWindowOpenMarket) {

            if(btnCloseInfoArea.contains(screenX,screenY)) {
                isBtnCloseInfoHover = true;
                return true;
            }

            if(btnCloseSettingsArea.contains(screenX,screenY)) {
                isBtnCloseSettingsHover = true;
                return true;
            }

            if(btn_no.contains(screenX,screenY)) {
                isBtnNoExitHover=true;
                return  true;
            }

            if(btn_yes.contains(screenX,screenY)) {
                isBtnYesExitHover=true;
                return  true;
            }

            if(btnCloseMarketArea.contains(screenX,screenY))
            {
              isBtnCloseMarketHover=true;
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
            isBtnMarket=true;
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
            scoreboardHover = true;
            return true;
        }

        if (exitArea.contains(screenX, screenY)) { // schermata logout
            exitHover = true;
            return true;
        }

        if (informationArea.contains(screenX, screenY)) { // schermata crediti di gioco
            informationHover = true;
            return true;
        }

        if (settingsArea.contains(screenX, screenY)) { // schermata impostazioni
            settingsHover = true;
            return true;
        }

        return false;
    }

    // cambio volume musica di gioco
    private void updateMusicVolumeFromX(int x) {
        float relative = (x - musicBarArea.x) / musicBarArea.width;
        relative = MathUtils.clamp(relative, 0f, 1f);

        AudioSettings.setMusicVolume(relative);
    }

    // cambio volume audio di gioco
    private void updateEffectsVolumeFromX(int x) {
        float relative = (x - effectsBarArea.x) / effectsBarArea.width;
        relative = MathUtils.clamp(relative, 0f, 1f);

        AudioSettings.setEffectsVolume(relative);
    }




    private void setFalse() {
        classic = gravity4 = horizontal = speedy = scoreboard = daily = false;
        settings = information = exit = false;
        btnCloseInfo=false;
        btnCloseSettings=false;
        btnYesExit=false;
        btnNoExit=false;
        isBtnCloseMarket=false;
    }


    // metodo per il controllo dei click
    private boolean checkHitboxes(int x, int y) {

        setFalse();


        if(isWindowOpenInfo || isWindowOpenSettings || isWindowOpenExit || isWindowOpenScoreboard || isWindowOpenMarket)
        {
            if (isWindowOpenInfo && btnCloseInfoArea.contains(x, y)) {
                btnCloseInfo = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_INFO, 0.20f);
                return true;
            }

            if (isWindowOpenSettings && btnCloseSettingsArea.contains(x, y)) {
                btnCloseSettings = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);
                scheduleScreenChange(ACT_CLOSE_SETTINGS, 0.20f);
                return true;
            }

            if (isWindowOpenExit) {
                if (btn_no.contains(x, y)) {
                    btnNoExit = true;
                    clickedTimer = 0.15f;
                    setInputEnabled(false);
                    scheduleScreenChange(ACT_CLOSE_EXIT, 0.20f);
                    return true;
                }

                if (btn_yes.contains(x, y)) {
                    btnYesExit = true;
                    return true; // IMPORTANTISSIMO: prima non tornavi true
                }
            }

            if (isWindowOpenSettings)
            {

                // CLICK SULLA BARRA MUSICA
                if (musicBarArea.contains(x, y))
                {
                    log.info("sono dentro");
                    draggingMusic = true;
                    updateMusicVolumeFromX(x);
                    return true;
                }

                // CLICK SULLA BARRA EFFETTI
                if (effectsBarArea.contains(x, y))
                {
                    log.info("sono dentro1");
                    draggingEffects = true;
                    updateEffectsVolumeFromX(x);
                    return true;
                }

                if(switchE.contains(x,y))
                {
                    log.info("sono switch");
                    if(switchE == switchD)
                    {
                        switchE=switchL;
                        isBtnSwitch=false;
                    }
                    else
                    {
                        isBtnSwitch=true;
                        switchE=switchD;
                    }

                    return true;
                }
            }

            if(isWindowOpenMarket && btnCloseMarketArea.contains(x,y))
            {
                btnCloseInfo = true;
                clickedTimer = 0.15f;
                setInputEnabled(false);

                isBtnCloseMarket=true;
                scheduleScreenChange(ACT_CLOSE_MARKET, 0.20f);
                return true;

            }

            return false;
        }

        // controllo click stelle difficoltà 1 per disattivare le altre
        if (classicStar1.contains(x, y)) { starClicked[0]=false; starClicked[1]=false; return true; }
        if (gravityStar1.contains(x, y)) { starClicked[2]=false; starClicked[3]=false; return true; }
        if (horizontalStar1.contains(x, y)) { starClicked[4]=false; starClicked[5]=false; return true; }
        if (speedyStar1.contains(x, y)) { starClicked[6]=false; starClicked[7]=false; return true; }

        // -- Difficoltà Game Mode "Classic" --
        // stella difficoltà 2
        if (classicStars[0].contains(x, y)) {
            boolean hadSecond = starClicked[1];
            if (hadSecond) starClicked[1] = false;          // se era attiva la 2, spegni anche lei

            starClicked[0] = !hadSecond && !starClicked[0]; // se la 2 era ON -> spegne tutto, altrimenti toggle 1
            difficolta[0] = starClicked[0] ? 1 : 0;

            return true;
        }
        // stella difficoltà 3
        if (classicStars[1].contains(x, y)) {
            starClicked[1] = !starClicked[1];               // toggle stella 2
            starClicked[0] = true;                          // se tocchi la 2, la 1 deve essere ON
            difficolta[0] = starClicked[1] ? 2 : 1;          // ON -> 2, OFF -> 1

            return true;
        }

        // -- Difficoltà Game Mode "Gravity4" --
        // stella difficoltà 2
        if (gravityStars[0].contains(x, y)) {
            boolean hadSecond = starClicked[3];
            if (hadSecond) starClicked[3] = false;

            starClicked[2] = !hadSecond && !starClicked[2];
            difficolta[1] = starClicked[2] ? 1 : 0;

            return true;
        }
        // stella difficoltà 3
        if (gravityStars[1].contains(x, y)) {
            starClicked[3] = !starClicked[3];
            starClicked[2] = true;
            difficolta[1] = starClicked[3] ? 2 : 1;

            return true;
        }

        // -- Difficoltà Game Mode "Horizontal" --
        // stella difficoltà 1
        if (horizontalStars[0].contains(x, y)) {
            boolean hadSecond = starClicked[5];
            if (hadSecond) starClicked[5] = false;

            starClicked[4] = !hadSecond && !starClicked[4];
            difficolta[2] = starClicked[4] ? 1 : 0;

            return true;
        }
        // stella difficoltà 2
        if (horizontalStars[1].contains(x, y)) {
            starClicked[5] = !starClicked[5];
            starClicked[4] = true;
            difficolta[2] = starClicked[5] ? 2 : 1;

            return true;
        }

        // -- Difficoltà Game Mode "Speedy" --
        // stella difficoltà 1
        if (speedyStars[0].contains(x, y)) {
            boolean hadSecond = starClicked[7];
            if (hadSecond) starClicked[7] = false;

            starClicked[6] = !hadSecond && !starClicked[6];
            difficolta[3] = starClicked[6] ? 1 : 0;

            return true;
        }
        // stella difficoltà 2
        if (speedyStars[1].contains(x, y)) {
            starClicked[7] = !starClicked[7];
            starClicked[6] = true;
            difficolta[3] = starClicked[7] ? 2 : 1;

            return true;
        }

        // click pulsante modalità "classic"
        if (classicArea.contains(x, y)) {
            classic = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_CLASSIC, 0.20f);
            return true;
        }

        // click pulsante modalità "gravity4"
        if (gravity4Area.contains(x, y)) {
            gravity4 = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_GRAVITY4, 0.20f);
            return true;
        }

        // click pulsante modalità "horizontal"
        if (horizontalArea.contains(x, y)) {
            horizontal = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_HORIZONTAL, 0.20f);
            return true;
        }

        // click pulsante modalità "speedy"
        if (speedyArea.contains(x, y)) {
            speedy = true;
            clickedTimer = 0.15f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_START_SPEEDY, 0.20f);
            return true;
        }

        if(marketButton.contains(x,y)) {
            isBtnMarketClicked = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_MARKET, 0.20f);
            return true;
        }

        // click pulsante classifica
        if (scoreboardArea.contains(x, y)) {
            scoreboard = true;
            clickedTimer = 0.10f;
            setInputEnabled(false);
            scheduleScreenChange(ACT_OPEN_SCOREBOARD, 0.20f);
            return true;
        }

        // -- COMMAND BAR --
        if (exitArea.contains(x, y)) {
            exit = true;
            isWindowOpenExit =true;
            // IMPORTANTE! LA POSIZIONE DEVE ESSERE ESATTAMENTE IL CENTRO DELLO SCHERMO (1000/2-widthImg/2, 700/2-heightImg/2)
            log.info("Exit cliccato!"); // todo: stampare da LobbyUI la grafica logout.png ATTENZIONE al dark e no
            return true;
        }

        if (informationArea.contains(x, y)) {
            information = true;
            isWindowOpenInfo =true;
            // IMPORTANTE! LA POSIZIONE DEVE ESSERE ESATTAMENTE IL CENTRO DELLO SCHERMO
            log.info("Information cliccato!"); // todo: stampare da LobbyUI la grafica software_infos.png
            return true;
        }

        if (settingsArea.contains(x, y)) {
            settings = true;
            isWindowOpenSettings=true;
            log.info("Settings cliccato!");
            return true;
        }

        return false;
    }

    // controllo rilascio del mouse
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {

        if (!inputEnabled) return false;
        draggingMusic = false;
        draggingEffects = false;
        return false;
    }


    // controllo drag del mouse
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!inputEnabled) return false;
        if (draggingMusic) {
            updateMusicVolumeFromX(screenX);
            return true;
        }

        if (draggingEffects) {
            updateEffectsVolumeFromX(screenX);
            return true;
        }

        return false;
    }

    // controllo click tasto tastiera
    @Override public boolean keyDown(int keycode)
    {
        if (!inputEnabled) return false;

        // ESC chiude le finestre aperte (come click sulla X)
        if (keycode == Input.Keys.ESCAPE)
        {
            // pagina crediti di gioco
            if (isWindowOpenInfo) {
                isWindowOpenInfo = false;
                btnCloseInfo = false;
                isBtnCloseInfoHover = false;
                return true;
            }

            // pagina impostazioni di gioco
            if (isWindowOpenSettings) {
                isWindowOpenSettings = false;
                btnCloseSettings = false;
                isBtnCloseSettingsHover = false;
                draggingMusic = false;
                draggingEffects = false;
                return true;
            }

            // pagina per il logout
            if (isWindowOpenExit) {
                isWindowOpenExit = false;
                btnYesExit = false;
                btnNoExit = false;
                isBtnYesExitHover = false;
                isBtnNoExitHover = false;
                return true;
            }

            if (isWindowOpenMarket) {
                isWindowOpenMarket = false;
                isBtnMarketClicked = false;
                return true;
            }

            if (isWindowOpenScoreboard) {
                isWindowOpenScoreboard = false;
                return true;
            }
        }

        return false;
    }
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) {
        if (!inputEnabled) return false;
        return false;
    }

    @Override public boolean scrolled(float v, float v1) {
        if (!inputEnabled) return false;
        return false;
    }

    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        if (!enabled) resetHover();
    }

    public boolean isInputEnabled() { return inputEnabled; }


    public void scheduleScreenChange(int nextState, float delaySeconds) {
        pendingScreenChange = true;
        pendingNextState = nextState;
        screenChangeDelay = delaySeconds;
    }

    public void update(float delta) {

        // timer per spegnere "clicked"
        if (clickedTimer > 0f) {
            clickedTimer -= delta;
            if (clickedTimer <= 0f) {
                resetClickedFlags();
            }
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

    private void executePendingAction(int act) {
        // qui stai ancora nella lobby -> riaccendi input
        setInputEnabled(true);

        switch (act) {
            case ACT_OPEN_MARKET:
                isWindowOpenMarket = true;
                break;

            case ACT_OPEN_SCOREBOARD:
                isWindowOpenScoreboard = true;
                break;

            case ACT_CLOSE_INFO:
                isWindowOpenInfo = false;
                btnCloseInfo = false;
                isBtnCloseInfoHover = false;
                break;

            case ACT_CLOSE_SETTINGS:
                isWindowOpenSettings = false;
                btnCloseSettings = false;
                isBtnCloseSettingsHover = false;
                draggingMusic = false;
                draggingEffects = false;
                break;

            case ACT_CLOSE_EXIT:
                isWindowOpenExit = false;
                btnNoExit = false;
                isBtnNoExitHover = false;
                break;

            case ACT_CLOSE_MARKET:
                isWindowOpenMarket=false;
                isBtnMarketClicked=false;
                isBtnMarket=false;



        }
    }
}
