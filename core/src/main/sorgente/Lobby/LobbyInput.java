package sorgente.Lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sorgente.SoundManager;

public class LobbyInput implements InputProcessor
{
    private static final Log log = LogFactory.getLog(LobbyInput.class);

    //Clicked

    protected boolean btnCloseExit;
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

    private final Rectangle btnCloseInfoArea;
    private final Rectangle btnCloseSettingsArea;


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
        classicArea     = new Rectangle(37, 160, 190, 250);
        gravity4Area    = new Rectangle(270, 160, 180, 250);
        horizontalArea  = new Rectangle(500, 160, 180, 250);
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

        // riproduzione del click
        if (click) SoundManager.playClickButton(50);

        return click;
    }


    private void resetHover()
    {
        classicHover = gravity4Hover = horizontalHover = speedyHover = false;
        marketHover = scoreboardHover = dailyHover = false;
        settingsHover = informationHover = exitHover = accessibilityHover = false;
        isBtnCloseInfoHover = false;
        isBtnCloseSettingsHover = false;
    }



    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        resetHover();

        if(isWindowOpenInfo || isWindowOpenSettings)
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

            return false;
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
            horizontalHover = true;
            return true;
        }

        if (speedyArea.contains(screenX, screenY))
        {
            speedyHover = true;
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



    private void setFalse()
    {
        classic = gravity4 = horizontal = speedy = false;
        market = scoreboard = daily = false;
        settings = information = exit = man = false;
        btnCloseInfo=false;
        btnCloseSettings=false;
    }

    private boolean checkHitboxes(int x, int y)
    {
        setFalse();

        if(isWindowOpenInfo || isWindowOpenSettings)
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

          return false;
        }

        if (classicArea.contains(x, y))
        {
            classic = true;
            log.info("Classic cliccato!");
            return true;
        }

        if (gravity4Area.contains(x, y))
        {
            gravity4 = true;
            log.info("Gravity4 cliccato!");
            return true;
        }

        if (horizontalArea.contains(x, y))
        {
            horizontal = true;
            log.info("Horizontal cliccato!");
            return true;
        }

        if (speedyArea.contains(x, y))
        {
            speedy = true;
            log.info("Speedy cliccato!");
            return true;
        }

        if (marketArea.contains(x, y))
        {
            market = true;
            log.info("Market cliccato!");
            return true;
        }

        if (dailyArea.contains(x, y))
        {
            daily = true;
            log.info("Daily cliccato!");
            return true;
        }

        if (scoreboardArea.contains(x, y))
        {
            scoreboard = true;
            log.info("Scoreboard cliccato!");
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

    @Override public boolean keyDown(int i) { return false; }
    @Override public boolean keyUp(int i) { return false; }
    @Override public boolean keyTyped(char c) { return false; }
    @Override public boolean touchUp(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int i, int i1, int i2) {return false;}
    @Override public boolean scrolled(float v, float v1) { return false; }
}

