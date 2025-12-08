/*
Forza4 • class AuthUI •
Gestisce la grafica delle schermate di autenticazione (Login / Sign Up / Password Reset)
Developed by Drop Logic©. All rights reserved.
*/

package sorgente.Authentication;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

    // supporto per il calcolo della larghezza del testo e cursore lampeggiante
    private final GlyphLayout layout = new GlyphLayout();
    private float cursorTimer = 0f;
    private boolean cursorVisible = true;

    // immagini
    private Texture img1, img2, img3, showPS, coverPS, btnRed,
        btnRedHover, btnRedClicked, noInternet, btnBack, btnBackClicked, btnLogin, btnLoginClicked,
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
        btnRed             = new Texture("ui/buttons/red_btn.png");
        btnRedHover        = new Texture("ui/buttons/red_btn_hover.png");
        btnRedClicked      = new Texture("ui/buttons/red_btn_clicked.png");
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

        // aggiornamento lampeggio cursore (circa due volte al secondo)
        cursorTimer += delta;
        if (cursorTimer >= 0.5f) {
            cursorVisible = !cursorVisible;
            cursorTimer = 0f;
        }

        // TIMER PER ANIMAZIONE CLICK
        if (alg.clickedTimer > 0) {
            alg.clickedTimer -= delta;
            if (alg.clickedTimer <= 0) {
                alg.resetClickFlags(); // spegne tutti i pulsanti "clicked"
            }
        }

        // esecuzione ritardata del processo di autenticazione (login/signup)
        if (alg.pendingAuthProcess) {
            alg.authDelay -= delta;
            if (alg.authDelay <= 0f) {
                alg.pendingAuthProcess = false;
                // qui eseguiamo l'algoritmo di login/signup vero e proprio.
                alg.processLoginOrSignup();
            }
        }

        // ritardo del cambio schermata (per transizioni login/signup/reset)
        if (alg.pendingScreenChange) {
            alg.screenChangeDelay -= delta;
            if (alg.screenChangeDelay <= 0) {
                alg.state = alg.pendingNextState;
                alg.pendingScreenChange = false;
            }
        }

        screen.begin();

        switch (alg.state) {
            case 0:
                screen.draw(img1, 0, 0);
                if (alg.error) Fonts.draw(screen, "Incorrect Password",415,63, Fonts.bold20);
                if (alg.error1) Fonts.draw(screen, "Nickname not found",412,63, Fonts.bold20);
                if (alg.error3) Fonts.draw(screen, "Your session is already open",368,63, Fonts.bold20);
                break;
            case 1:
                screen.draw(img2, 0, 0);
                if (alg.error) Fonts.draw(screen, "Nickname already in use",393,63, Fonts.bold20);
                if (alg.error4) Fonts.draw(screen, "Nickname not valid",415,63, Fonts.bold20);
                break;
            case 2:
                screen.draw(img3, 0, 0);
                if (alg.error) Fonts.draw(screen, "Incorrect ID Creation Date",388,72, Fonts.bold20);
                break;
            case 3:
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
            screen.draw(noInternet, 374, 40);
            Fonts.draw(screen, "No Internet Connection",416,63, Fonts.bold20);
        }

        // icona mostra/nascondi password
        if (alg.showPS) screen.draw(showPS, 695,323);
        else screen.draw(coverPS, 695,323);

        // -- HOVER -- //
        if (alg.btnRedHover) screen.draw(btnRedHover, 424, 209); // pulsante rosso avanti
        if (alg.btnResetPSWHover) screen.draw(btnResetPSW, 386, 113); // pulsante passaggio a psw reset
        if (alg.gotoLoginHover) screen.draw(btnLogin, 902, 598); // pulsante go-to-login
        if (alg.gotoSignupHover) screen.draw(btnSignup, 902, 598); // pulsante go-to-signup
        if (alg.gobackHover) screen.draw(btnBack, 46, 598); // pulsante go-back

        // -- CLICKED -- //
        if (alg.btnRedClicked) screen.draw(btnRedClicked, 424, 209); // pulsante rosso avanti
        if (alg.btnResetPSWClicked) screen.draw(btnResetPSWClicked, 386, 113);  // pulsante passaggio a psw reset
        if (alg.gotoLoginClicked) screen.draw(btnLoginClicked, 902, 598); // pulsante go-to-login
        if (alg.gotoSignupClicked) screen.draw(btnSignupClicked, 902, 598); // pulsante go-to-signup
        if (alg.gobackClicked) screen.draw(btnBackClicked, 46, 598); // pulsante go-back

        // -- TESTI -- //
        if (alg.state == 0 || alg.state == 1) Fonts.draw(screen, "PLAY",450,251, Fonts.bold40);
        else Fonts.draw(screen, "SAVE",445,251, Fonts.bold40);

        // nickname (con supporto selezione + cursore lampeggiante)
        String nicknameText = String.valueOf(alg.nicknameInput);

        // se il testo del nickname è selezionato (Ctrl+A), cambiamo colore per evidenziare
        if (alg.isNicknameSelected()) Fonts.bold25.setColor(com.badlogic.gdx.graphics.Color.SKY);
        else Fonts.bold25.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        Fonts.bold25.draw(screen, nicknameText, 272, 445);

        // cursore del nickname alla fine del testo
        if (alg.enteringNickname && !alg.isNicknameSelected() && cursorVisible) {
            layout.setText(Fonts.bold25, nicknameText);
            float cursorX = 272 + layout.width + 2f;
            Fonts.bold25.draw(screen, "|", cursorX, 445);
        }

        // reset colore per non influenzare altri testi
        Fonts.bold25.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        // password (mostrata o coperta con pallini) + selezione e cursore
        String passwordText;
        if (!alg.showPS) passwordText = alg.passwordInput.length() > 0 ? "•".repeat(alg.passwordInput.length()) : "";
        else passwordText = String.valueOf(alg.passwordInput);

        float passwordY = 345f;

        if (alg.isPasswordSelected()) Fonts.bold25.setColor(com.badlogic.gdx.graphics.Color.SKY);
        else Fonts.bold25.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        Fonts.bold25.draw(screen, passwordText, 272, passwordY);

        if (alg.enteringPassword && !alg.isPasswordSelected() && cursorVisible) {
            layout.setText(Fonts.bold25, passwordText);
            float cursorX = 272 + layout.width + 2f;
            Fonts.bold25.draw(screen, "|", cursorX, passwordY);
        }

        // reset colore dopo il rendering della password
        Fonts.bold25.setColor(Color.WHITE);

        // -- CREDITI GIOCO -- //
        Fonts.draw(screen, "Drop Logic", 49, 63, Fonts.medium20); // firma al gioco
        Fonts.draw(screen, "Beta", 912, 63, Fonts.medium20); // versione di gioco

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
        img3.dispose();
        showPS.dispose();
        coverPS.dispose();
        btnRedClicked.dispose();
        btnRed.dispose();
        btnRedHover.dispose();
        noInternet.dispose();
        btnBack.dispose();
        btnBackClicked.dispose();
        btnLogin.dispose();
        btnLoginClicked.dispose();
        btnSignup.dispose();
        btnSignupClicked.dispose();
        btnResetPSW.dispose();
        btnResetPSWClicked.dispose();

        alg.dispose();
    }
}
