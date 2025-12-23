package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.SoundManager;

import java.util.Timer;
import java.util.TimerTask;

public class LobbyInput implements InputProcessor
{
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
    protected boolean market;
    protected boolean scoreboard;
    protected boolean daily;

    protected boolean settings;
    protected boolean information;
    protected boolean exit;
    protected boolean man;

    // Hover

    protected boolean isBtnSwitch;
    protected boolean isBtnNoExitHover;
    protected boolean isBtnYesExitHover;
    protected boolean isBtnCloseInfoHover;
    protected boolean isBtnCloseSettingsHover;
    protected boolean classicHover;
    protected boolean gravity4Hover;
    protected boolean horizontalHover;
    protected boolean speedyHover;
    protected boolean marketHover;
    protected boolean scoreboardHover;
    protected boolean dailyHover;
    protected boolean settingsHover;
    protected boolean informationHover;
    protected boolean exitHover;
    protected boolean accessibilityHover;

    //Per Aprire le finestre

    protected boolean isWindowOpenInfo;
    protected boolean isWindowOpenExit;
    protected boolean isWindowOpenSettings;

    private final Pixmap mouse;
    private final Cursor cursor;


    //Hitbox

    protected final Rectangle musicBarArea;
    protected final Rectangle effectsBarArea;
    final Rectangle switchL;
    final Rectangle switchD;
    Rectangle switchE;

    private final Rectangle[] classicStars = new Rectangle[3];
    private final Rectangle[] gravityStars = new Rectangle[3];
    private final Rectangle[] horizontalStars = new Rectangle[3];
    private final Rectangle[] speedyStars = new Rectangle[3];

    private final Rectangle btnCloseInfoArea;
    private final Rectangle btnCloseSettingsArea;

    private final Rectangle btn_no;
    private final Rectangle btn_yes;

    private final Rectangle classicArea;
    private final Rectangle gravity4Area;
    private final Rectangle horizontalArea;

    private final Rectangle speedyArea;
    private final Rectangle marketArea;
    private final Rectangle scoreboardArea;

    private final Rectangle dailyArea;
    private final Rectangle settingsArea;
    private final Rectangle informationArea;

    private final Rectangle exitArea;
    private final Rectangle manArea;


    public LobbyInput()
    {

        isWindowOpenInfo =false;
        isWindowOpenExit =false;
        isWindowOpenSettings=false;

        classic = gravity4 = horizontal = speedy = false;
        market = scoreboard = daily = false;
        settings = information = exit = man = false;

        // Hitbox principali


        switchD=new Rectangle(407,464,30,30);
        switchL=new Rectangle(505,464,30,30);


        switchE=switchL;

        // Barra volume musica
        musicBarArea = new Rectangle(316, 375, 370, 40); // x, y, width, height // Barra volume effetti
        effectsBarArea = new Rectangle(316, 425, 370, 40);

        classicStars[0]= new Rectangle( 129, 375, 20, 20);
        classicStars[1]= new Rectangle(145,375,20,20);

        gravityStars[0]= new Rectangle(372, 376, 20, 20);
        gravityStars[1]=new Rectangle(403,376,20,20);

        horizontalStars[0] = new Rectangle(612, 375, 20, 20);
        horizontalStars[1] = new Rectangle(642, 375, 20, 20);

       speedyStars[0] = new Rectangle( 852, 375, 20, 20);
       speedyStars[1] = new Rectangle(878,375,20,20);


        classicArea     = new Rectangle(37, 160, 190, 250);
        gravity4Area    = new Rectangle(270, 160, 190, 250);
        horizontalArea  = new Rectangle(500, 160, 190, 250);
        speedyArea      = new Rectangle(730, 160, 190, 250);

        // Hitbox secondarie
        marketArea      = new Rectangle(37, 400, 301, 200);
        dailyArea       = new Rectangle(660, 400, 301, 200);
        scoreboardArea  = new Rectangle(365, 400, 240, 220);

        // Bottoni
        exitArea        = new Rectangle(390, 615, 30, 30);
        informationArea = new Rectangle(450, 615, 30, 30);
        settingsArea    = new Rectangle(510, 615, 30, 30);
        manArea         = new Rectangle(578, 615, 30, 30);

        btnCloseInfoArea = new Rectangle(708,236,40,40);
        btnCloseSettingsArea = new Rectangle(705,245,40,40);

        btn_no= new Rectangle(503,408,141,52);
        btn_yes= new Rectangle(341,408,141,52);

        // Cursor personalizzato
        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));
        cursor = Gdx.graphics.newCursor(mouse, 0, 0);
        Gdx.graphics.setCursor(cursor);
    }

    public void dispose()
    {
        mouse.dispose();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        System.out.println(screenX+" "+screenY);

        boolean click = checkHitboxes(screenX, screenY);

        if (click)
        {
            SoundManager.playClickButton(500000000);
        }

        return click;
    }



    private void resetHover()
    {
        classicHover = gravity4Hover = horizontalHover = speedyHover = false;
        marketHover = scoreboardHover = dailyHover = false;
        settingsHover = informationHover = exitHover = accessibilityHover = false;
        isBtnCloseInfoHover = false;
        isBtnCloseSettingsHover = false;
        isBtnYesExitHover=false;
        isBtnNoExitHover=false;

        starHover[0]=false;
        starHover[1]=false;
        starHover[2]=false;
        starHover[3]=false;
        starHover[4]=false;
        starHover[5]=false;
        starHover[6]=false;
        starHover[7]=false;

    }


    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        resetHover();


        if(isWindowOpenInfo || isWindowOpenSettings || isWindowOpenExit)
        {

            if(btnCloseInfoArea.contains(screenX,screenY))
            {
                isBtnCloseInfoHover = true;
                return true;
            }

            if(btnCloseSettingsArea.contains(screenX,screenY))
            {
                isBtnCloseSettingsHover = true;
                return true;
            }

            if(btn_no.contains(screenX,screenY))
            {
                isBtnNoExitHover=true;
                return  true;
            }

            if(btn_yes.contains(screenX,screenY))
            {
                isBtnYesExitHover=true;
                return  true;
            }

            return false;
        }


        if (classicStars[0].contains(screenX, screenY))
        {
            log.info("Classic: cliccata stella 1");

            if (!starClicked[0])
                starHover[0] = true;

            return true;
        }
        else if (classicStars[1].contains(screenX, screenY))
        {
            log.info("Classic: cliccata stella 2");

            if (!starClicked[1])
                starHover[1] = true;

            return true;
        }


        if (gravityStars[0].contains(screenX, screenY))
        {
            log.info("Gravity4: cliccata stella 1");

            if (!starClicked[2])
                starHover[2] = true;

            return true;
        }
        else if (gravityStars[1].contains(screenX, screenY))
        {
            log.info("Gravity4: cliccata stella 2");

            if (!starClicked[3])
                starHover[3] = true;

            return true;
        }


        if (horizontalStars[0].contains(screenX, screenY))
        {
            log.info("Horizontal: cliccata stella 1");

            if (!starClicked[4])
                starHover[4] = true;

            return true;
        }
        else if (horizontalStars[1].contains(screenX, screenY))
        {
            log.info("Horizontal: cliccata stella 2");

            if (!starClicked[5])
                starHover[5] = true;

            return true;
        }


        if (speedyStars[0].contains(screenX, screenY))
        {
            log.info("Speedy: cliccata stella 1");

            if (!starClicked[6])
                starHover[6] = true;

            return true;
        }
        else if (speedyStars[1].contains(screenX, screenY))
        {
            log.info("Speedy: cliccata stella 2");

            if (!starClicked[7])
                starHover[7] = true;

            return true;
        }


        if (classicArea.contains(screenX, screenY))
        {
            classicHover = true;

            return true;
        }

        if (gravity4Area.contains(screenX, screenY))
        {
            gravity4Hover = true;
            return true;
        }

        if (horizontalArea.contains(screenX, screenY))
        {

            horizontalHover= true;
            return true;
        }

        if (speedyArea.contains(screenX, screenY))
        {
            speedyHover= true;
            return true;
        }

        if (marketArea.contains(screenX, screenY))
        {
            marketHover = true;
            return true;
        }

        if (dailyArea.contains(screenX, screenY))
        {
            dailyHover = true;
            return true;
        }

        if (scoreboardArea.contains(screenX, screenY))
        {
            scoreboardHover = true;
            return true;
        }

        if (exitArea.contains(screenX, screenY))
        {
            exitHover = true;
            return true;
        }

        if (informationArea.contains(screenX, screenY))
        {
            informationHover = true;
            return true;
        }

        if (manArea.contains(screenX, screenY))
        {
            accessibilityHover = true;
            return true;
        }

        if (settingsArea.contains(screenX, screenY))
        {
            settingsHover = true;
            return true;
        }

        return false;
    }

    private void updateMusicVolumeFromX(int x)
    {
        float relative = (x - musicBarArea.x) / musicBarArea.width;
        relative = MathUtils.clamp(relative, 0f, 1f);

        AudioSettings.setMusicVolume(relative);
    }

    private void updateEffectsVolumeFromX(int x)
    {
        float relative = (x - effectsBarArea.x) / effectsBarArea.width;
        relative = MathUtils.clamp(relative, 0f, 1f);

        AudioSettings.setEffectsVolume(relative);
    }




    private void setFalse()
    {
        classic = gravity4 = horizontal = speedy = false;
        market = scoreboard = daily = false;
        settings = information = exit = man = false;
        btnCloseInfo=false;
        btnCloseSettings=false;
        btnYesExit=false;
        btnNoExit=false;

        starClicked[0]=false;
        starClicked[1]=false;
        starClicked[2]=false;
        starClicked[3]=false;
        starClicked[4]=false;
        starClicked[5]=false;
        starClicked[6]=false;
        starClicked[7]=false;
    }



    private boolean checkHitboxes(int x, int y)
    {

        setFalse();


        if(isWindowOpenInfo || isWindowOpenSettings || isWindowOpenExit)
        {

                if(btnCloseInfoArea.contains(x,y))
                {
                    btnCloseInfo=true;
                    isWindowOpenInfo=false;

                    return true;
                }

                if(btnCloseSettingsArea.contains(x,y))
                {
                    btnCloseSettings=true;
                    isWindowOpenSettings=false;
                    return true;
                }


            if(isWindowOpenExit)
            {

                if (btn_no.contains(x, y))
                {
                    log.info("NOO");
                    btnNoExit = true;
                    isWindowOpenExit = false;
                    return true;
                }

                if (btn_yes.contains(x, y)) {
                    log.info("YEEEE");
                    btnYesExit = true;
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


            return false;
        }


        if (classicStars[0].contains(x, y))
        {
            // Se la stella 1 NON è cliccata → cliccala
            if (!starClicked[0])
            {
                starClicked[0] = true;
                difficolta[0] = 1;
                log.info("Classic: stella 1 cliccata");
            }

            return true;
        }

        if (classicStars[1].contains(x, y))
        {
            // Se la stella 2 NON è cliccata → cliccala
            if (!starClicked[1])
            {
                starClicked[1] = true;
                starClicked[0]=true;

                difficolta[0] = 2;
                log.info("Classic: stella 2 cliccata");
            }
            else
            {

                log.info("Classic: reset difficoltà");

                difficolta[0] = 0;
                starClicked[0] = false;
                starClicked[1] = false;
            }

            return true;
        }


        if (gravityStars[0].contains(x, y))
        {
            if (!starClicked[2])
            {
                starClicked[2] = true;
                difficolta[1] = 1;
                log.info("Gravity4: stella 1 cliccata");
            }
            return true;
        }

        if (gravityStars[1].contains(x, y))
        {
            if (!starClicked[3])
            {
                starClicked[3] = true;
                starClicked[2] =true;

                difficolta[1] = 2;
                log.info("Gravity4: stella 2 cliccata");
            }
            else
            {
                // RESET
                log.info("Gravity4: reset difficoltà");
                difficolta[1] = 0;
                starClicked[2] = false;
                starClicked[3] = false;
            }
            return true;
        }



        if (horizontalStars[0].contains(x, y))
        {
            if (!starClicked[4])
            {
                starClicked[4] = true;
                difficolta[2] = 1;
                log.info("Horizontal: stella 1 cliccata");
            }
            return true;
        }

        if (horizontalStars[1].contains(x, y))
        {
            if (!starClicked[5])
            {
                starClicked[4]=true;
                starClicked[5] = true;

                difficolta[2] = 2;
                log.info("Horizontal: stella 2 cliccata");
            }
            else
            {
                // RESET
                log.info("Horizontal: reset difficoltà");
                difficolta[2] = 0;
                starClicked[4] = false;
                starClicked[5] = false;
            }
            return true;
        }

        if (speedyStars[0].contains(x, y))
        {
            if (!starClicked[6])
            {
                starClicked[6] = true;
                difficolta[3] = 1;
                log.info("Horizontal: stella 1 cliccata");
            }
            return true;
        }

        if (speedyStars[1].contains(x, y))
        {
            if (!starClicked[7])
            {
                starClicked[7] = true;
                starClicked[6]=true;

                difficolta[3] = 2;
                log.info("Horizontal: stella 2 cliccata");
            }
            else
            {
                // RESET
                log.info("Horizontal: reset difficoltà");
                difficolta[3] = 0;
                starClicked[6] = false;
                starClicked[7] = false;
            }
            return true;
        }


        if (classicArea.contains(x, y))
        {

            classic = true;
            log.info("Classic cliccato!");
            new Timer().schedule(new TimerTask() { @Override public void run() { classic = false; } }, 100);

            return true;
        }

        if (gravity4Area.contains(x, y))
        {
            gravity4 = true;
            log.info("Gravity cliccato!");

            new Timer().schedule(new TimerTask() { @Override public void run() { gravity4 = false; } }, 100);

            return true;
        }

        if (horizontalArea.contains(x, y))
        {
            horizontal= true;
            log.info("Horizontal  cliccato!");

            new Timer().schedule(new TimerTask() { @Override public void run() { horizontal = false; } }, 100);

            return true;
        }

        if (speedyArea.contains(x, y))
        {

            speedy = true;
            log.info("Speedy cliccato!");
            new Timer().schedule(new TimerTask() { @Override public void run() { speedy = false; } }, 100);

            return true;
        }

        if (marketArea.contains(x, y))
        {
            market = true;
            log.info("Card cliccato!");
            new Timer().schedule(new TimerTask() { @Override public void run() { market = false; } }, 100);

            return true;
        }

        if (dailyArea.contains(x, y))
        {
            daily = true;
            log.info("Daily cliccato!");
            new Timer().schedule(new TimerTask() { @Override public void run() { daily = false; } }, 100);

            return true;
        }

        if (scoreboardArea.contains(x, y))
        {
            scoreboard = true;
            log.info("Scoreboard cliccato!");
            new Timer().schedule(new TimerTask() { @Override public void run() { scoreboard = false; } }, 100);
            return true;
        }

        if (exitArea.contains(x, y))
        {
            exit = true;
            isWindowOpenExit =true;
            // IMPORTANTE! LA POSIZIONE DEVE ESSERE ESATTAMENTE IL CENTRO DELLO SCHERMO (1000/2-widthImg/2, 700/2-heightImg/2)
            log.info("Exit cliccato!"); // todo: stampare da LobbyUI la grafica logout.png ATTENZIONE al dark e no
            return true;
        }

        if (informationArea.contains(x, y))
        {
            information = true;
            isWindowOpenInfo =true;
            // IMPORTANTE! LA POSIZIONE DEVE ESSERE ESATTAMENTE IL CENTRO DELLO SCHERMO
            log.info("Information cliccato!"); // todo: stampare da LobbyUI la grafica software_infos.png
            return true;
        }

        if (manArea.contains(x, y))
        {
            man = true;
            log.info("Man cliccato!");
            return true;
        }

        if (settingsArea.contains(x, y))
        {
            settings = true;
            isWindowOpenSettings=true;
            log.info("Settings cliccato!");
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        draggingMusic = false;
        draggingEffects = false;
        return false;
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        if (draggingMusic)
        {
            updateMusicVolumeFromX(screenX);
            return true;
        }

        if (draggingEffects)
        {
            updateEffectsVolumeFromX(screenX);
            return true;
        }

        return false;
    }


    @Override public boolean keyDown(int i) { return false; }
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }

    @Override public boolean scrolled(float v, float v1) { return false; }
}

