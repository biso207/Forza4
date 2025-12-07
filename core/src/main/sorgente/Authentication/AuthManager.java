/*
Forza4 • class AuthManager •
Gestisce la grafica dell'autenticazione usando AuthUI
e fa da ponte tra la logica (AuthAlgorithms, servizi, ecc.)
e la UI.
Developed by Drop logic©. All rights reserved.
*/

package sorgente.Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import sorgente.Main;

/**
 * AuthManager è lo Screen di autenticazione.
 * - Istanzia e contiene {@link AuthUI}, che gestisce tutta la parte grafica.
 * - Istanzia e contiene {@link AuthAlgorithms}, che gestisce la logica di login / signup.
 * - È il punto di collegamento tra input utente, logica e cambio schermata.
 */
public class AuthManager extends ScreenAdapter {

    private final Main game;
    private final SpriteBatch batch;

    private final AuthUI authUI;
    private final AuthAlgorithms authAlgorithms;

    // pagina corrente gestita a livello di Screen (AuthUI viene tenuta in sync)
    private AuthPage currentPage = AuthPage.LOGIN;

    public AuthManager(Main game) {
        this.game = game;

        // Se in Main hai già uno SpriteBatch condiviso, puoi passarlo qui
        // e rimuovere il new SpriteBatch():
        this.batch = new SpriteBatch();

        this.authAlgorithms = new AuthAlgorithms();
        this.authUI = new AuthUI(game);

        // pagina iniziale: Login
        this.currentPage = AuthPage.LOGIN;
    }

    // ====================== //
    //   METODI DELLO SCREEN  //
    // ====================== //

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        authUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        authUI.resize(width, height);
    }

    @Override
    public void show() {
        // se in futuro vuoi fare setup extra quando lo screen viene mostrato,
        // puoi farlo qui.
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        authUI.dispose();
        authAlgorithms.dispose();
        batch.dispose();
    }

    // Getter opzionale se vuoi accedere ad AuthUI da fuori.
    public AuthUI getAuthUI() {
        return authUI;
    }

    public Main getGame() {
        return game;
    }
}
