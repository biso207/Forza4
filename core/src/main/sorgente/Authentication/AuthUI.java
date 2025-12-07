/*
Forza4 • class AuthUI •
Gestisce la grafica delle schermate di autenticazione (Login / Sign Up / Password Reset)
Developed by Drop Logic©. All rights reserved.
*/

package sorgente.Authentication;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Costruisce e gestisce tutta la UI delle schermate di autenticazione.
 * Usa un unico Stage con Skin personalizzata e mostra gli elementi
 * necessari in base alla AuthPage corrente.
 *
 * Non contiene logica di autenticazione: espone solo i widget e gli eventi
 * per permettere al "controller" o alla Screen di collegare la logica.
 */
// import codici e librerie
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sorgente.*;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.dbManagement.LoadingData.GlobalProgressManager;

import java.awt.*;
import java.io.IOException;

public class AuthUI extends ScreenAdapter implements ResourceLoader {
    // variabile di riferimento al gioco
    private final Main game;
    // screen di gioco
    private final SpriteBatch screen;

    // istanza classe algoritmi
    private final AuthAlgorithms alg;

    // immagini
    private Texture img1, img2, img3, showPS, coverPS, redBtn,
        redBtnHover, redBtnClicked, noInternet, btnBack, btnBackClicked, btnLogin, btnLoginClicked,
        btnSignup, btnSignupClicked, btnResetPSW, btnResetPSWClicked;

    // costruttore
    public AuthUI(Main game) {
        this.game = game;
        this.screen = game.screen;

        // istanza classe degli algoritmi
        alg = new AuthAlgorithms();

        // caricamento font e immagini
        Fonts.load();
        this.loadImages();
    }

    // ******************* //
    // CARICAMENTO RISORSE //
    // ******************* //

    // metodo per caricare e creare i font
    @Override
    public void loadFont() {}

    // metodo per caricare le immagini delle pagine di Accesso e Registrazione
    @Override
    public void loadImages() {
        // sfondi
        img1 = new Texture("login_signup_screens/login.png");
        img2 = new Texture("login_signup_screens/signup.png");
        img3 = new Texture("login_signup_screens/psw_reset.png");

        // -- ICONE --
        // icona mostra/nascondi password
        showPS     = new Texture("ui/icons/showPSW.png");
        coverPS    = new Texture("ui/icons/coverPSW.png");
        noInternet = new Texture("ui/icons/no_internet.png");

        // -- BUTTONS
        btnBack            = new Texture("ui/buttons/btn_back.png");
        btnBackClicked     = new Texture("ui/buttons/btn_back_clicked.png");
        btnLogin           = new Texture("ui/buttons/btn_login.png");
        btnLoginClicked    = new Texture("ui/buttons/btn_login_clicked.png");
        btnSignup          = new Texture("ui/buttons/btn_signup.png");
        btnSignupClicked   = new Texture("ui/buttons/btn_signup_clicked.png");
        redBtn             = new Texture("ui/buttons/red_btn.png");
        redBtnHover        = new Texture("ui/buttons/red_btn_hover.png");
        redBtnClicked      = new Texture("ui/buttons/red_btn_clicked.png");
        btnResetPSW        = new Texture("ui/buttons/btn_reset_password.png");
        btnResetPSWClicked = new Texture("ui/buttons/btn_reset_password_clicked.png");

        alg.enteringNickname = true; // digitazione nickname attivata
    }

    // ********************************* //
    // METODI DELLA CLASSE ScreenAdapter //
    // ********************************* //
    // metodo per aggiornare lo schermo
    @Override public void render(float delta) {
        // attivazione controllo input
        Gdx.input.setInputProcessor(alg);

        screen.begin();

        switch (alg.state) {
            case 0:
                screen.draw(img1, 0, 0);
                if (alg.error) Fonts.draw(screen, "Password wrong",420,72, Fonts.medium20);
                if (alg.error1) Fonts.draw(screen, "Nickname not found",402,72, Fonts.medium20);
                if (alg.error3) Fonts.draw(screen, "Your session is already open",361,72, Fonts.medium20);
                break;
            case 1:
                screen.draw(img2, 0, 0);
                if (alg.error) Fonts.draw(screen, "Nickname already in use",388,72, Fonts.medium20);
                if (alg.error4) Fonts.draw(screen, "Nickname not valid",388,72, Fonts.medium20);
                break;
            case 2:
                // salvataggio password in remoto
                try {
                    FirestoreUserRepository.setPassword(AuthAlgorithms.nickname, AuthAlgorithms.password);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // schermata di caricamento per upload/download dati
                LoadingScreen loadingScreen = new LoadingScreen(game, false);
                game.setScreen(loadingScreen); // creazione di un nuovo screen

                // setting oggetto del listener per la barra di caricamento
                GlobalProgressManager.setListener(loadingScreen);

                this.dispose(); // rilascio risorse
                break;
            default:
                break;
        }

        // messaggio "internet assente"
        if (alg.error2) {
            screen.draw(noInternet, 364, 50);
            Fonts.draw(screen, "No Internet Connection",406,72, Fonts.medium20);
        }

        // icona mostra/nascondi password
        if (alg.showPS) screen.draw(showPS, 690,288);
        else screen.draw(coverPS, 690,288);

        // pulsante PLAY e cambio pagina
        if (alg.isHover1) screen.draw(redBtnHover, 422, 185);
        if (alg.isHover2) {
            if (alg.state==1) screen.draw(btnResetPSW, 428, 99);
            else screen.draw(btnResetPSW, 428, 99);
        }

        // nickname
        Fonts.draw(screen, String.valueOf(alg.nicknameInput), 272, 412, Fonts.bold25);
        // password che può essere visibile o meno, l'utente deve solo cliccare l'icona a dx
        if (!alg.showPS) Fonts.draw(screen, "•".repeat(alg.passwordInput.length()), 272, 310, Fonts.bold25);
        else Fonts.draw(screen, String.valueOf(alg.passwordInput), 272, 316, Fonts.bold25);

        // crediti
        Fonts.draw(screen, "BIGA Games", 53, 45, Fonts.medium20); // firma al gioco
        Fonts.draw(screen, "October 2025", 838, 45, Fonts.medium20); // versione di gioco

        screen.end();
    }

    // spegnimento controllo input
    @Override public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    // rilascio delle risorse
    @Override public void dispose() {
        img1.dispose();
        img2.dispose();
        showPS.dispose();
        coverPS.dispose();
        redBtnClicked.dispose();
        redBtn.dispose();
        redBtnHover.dispose();
        noInternet.dispose();

        alg.dispose();
    }
}
