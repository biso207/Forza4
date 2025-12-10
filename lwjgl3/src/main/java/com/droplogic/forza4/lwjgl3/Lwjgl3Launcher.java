package com.droplogic.forza4.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import sorgente.Main;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("Forza4");

        // Risoluzione finestra iniziale 16:9
        configuration.setWindowedMode(1000, 700);

        configuration.setResizable(false);

        configuration.setWindowIcon("forza4_icon.png"); // icona del gioco

        new Lwjgl3Application(new Main(), configuration);
    }
}

