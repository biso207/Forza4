/*
Forza4 - class Main -
Classe principale del progetto Forza4.
L'entrata del programma è in "lwjgl3/src/main/java/com/droplogic/lwjgl3/Lwjgl3Launcher.java"
Developed by Drop Logic©. All rights reserved.
*/

package sorgente;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}
