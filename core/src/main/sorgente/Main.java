/*
Forza4 • class Main •
Classe principale del progetto Forza4.
L'entrata del programma è in "lwjgl3/src/main/java/com/droplogic/lwjgl3/Lwjgl3Launcher.java"
Developed by Drop Logic©. All rights reserved.
*/

package sorgente;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {
    public SpriteBatch screen;

    @Override
    public void create() {
        screen = new SpriteBatch();

        // chiamata alla schermata di caricamento
        this.setScreen(new LoadingScreen(this, true));

        // limite a 60 fps
        Gdx.graphics.setForegroundFPS(60);
    }

    @Override
    public void dispose() {
        //SessionLockManager.shutdownAll(); // rilascia il lock --> serve a sbloccare la sessione utente e permettere un nuovo login
        screen.dispose(); // rimozione risorse
    }
}

