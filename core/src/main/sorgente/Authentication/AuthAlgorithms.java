package sorgente.Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import org.mindrot.jbcrypt.BCrypt;
import sorgente.ProfanityFilter;
import sorgente.SoundManager;
import sorgente.UserData.FirestoreUserRepository;
import sorgente.UserData.UserProgressService;
import sorgente.UserData.LocalLockStore;
import sorgente.UserData.SessionLockService;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AuthAlgorithms implements InputProcessor {
    // variabili di controllo digitazione
    protected boolean enteringNickname, enteringPassword;
    // variabili per recuperare nick e psw utente
    public static String nickname, password;
    // variabili per comporre le stringhe digitate di nick e psw
    protected final StringBuilder nicknameInput, passwordInput;

    // variabile per nascondere/mostrare la password e cambiare stile pulsanti
    protected boolean showPS=false, btnRedHover=false, btnResetPSWHover=false,
    gotoSignupHover=false, gotoLoginHover=false, gobackHover=false;

    // variabili per gli errori durante le autenticazioni
    protected boolean error = false; // "Password wrong"/"Nickname already in use"
    protected boolean error1 = false; // "Nickname not found"
    protected boolean error2 = false; // "No Internet Connection"
    protected boolean error3 = false; // "Your session is already open on another device"
    protected boolean error4 = false; // "Nickname not valid"


    /* pagina di riferimento
        0 = LogIn
        1 = SignUp
    */
    protected int state;

    // mouse
    private final Pixmap mouse; // immagini
    private final Cursor cursor; // oggetto cursore

    // costruttore
    public AuthAlgorithms() {
        // attivazione area di digitazione
        this.enteringNickname = true;
        this.enteringPassword = false;

        // dichiarazione dei stringBuilder
        nicknameInput = new StringBuilder();
        passwordInput = new StringBuilder();

        mouse = new Pixmap(Gdx.files.internal("ui/icons/cursor.png"));

        cursor = Gdx.graphics.newCursor(mouse, 0, 0);

        // apertura schermata login
        state = 0;

        // caricamento lista di parole vietate per il nickname
        ProfanityFilter.loadBlacklists();
    }

    // metodo per il rilascio delle risorse
    public void dispose() {
        mouse.dispose();
    }

    // *********************** //
    // METODI DI SUPPORTO UI  //
    // *********************** //

    /**
     * Imposta le credenziali inserite dall'utente a partire dalle stringhe
     * provenienti dalla nuova UI (AuthUI). In questo modo possiamo riusare
     * tutta la logica esistente basata su nicknameInput e passwordInput
     * anche senza più l'InputProcessor personalizzato.
     */
    public void setCredentials(String nicknameText, String passwordText) {
        nicknameInput.setLength(0);
        passwordInput.setLength(0);

        if (nicknameText != null) {
            nicknameInput.append(nicknameText);
        }
        if (passwordText != null) {
            passwordInput.append(passwordText);
        }
    }

    /**
     * Imposta la modalità corrente su LogIn.
     *  state = 0 => login
     */
    public void setLoginMode() {
        this.state = 0;
    }

    /**
     * Imposta la modalità corrente su SignUp.
     *  state = 1 => registrazione
     */
    public void setSignUpMode() {
        this.state = 1;
    }

    /**
     * Restituisce lo stato corrente dell'algoritmo (0=login, 1=signup, 2=lobby).
     */
    public int getState() {
        return state;
    }

    /**
     * Ritorna true se è presente qualunque errore di autenticazione.
     */
    public boolean hasAnyError() {
        return error || error1 || error2 || error3 || error4;
    }

    public boolean isPasswordOrNicknameConflictError() { return error; }
    public boolean isNicknameNotFoundError()          { return error1; }
    public boolean isNoConnectionError()              { return error2; }
    public boolean isSessionAlreadyOpenError()        { return error3; }
    public boolean isInvalidNicknameError()           { return error4; }


    // ************************** //
    // PROCESSI DI AUTENTICAZIONE //
    // ************************** //

    // metodo per resettare gli errori
    public void resetErrors() {
        error=error1=error2=error3=error4=false;
    }

    // metodo per pulire il nickname da caratteri non adatti alle cartelle
    public static String sanitizeNickname(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // metodo per verifica che il PC sia connesso ad internet, in caso negativo ogni operazione è bloccata
    public static boolean checkInternetConnection() {
        // esegue un ping su google.com, in caso di risposta positiva vuol dire che il PC è connesso a internet
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return address.isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }

    // metodo per resettare i testi dei campi digitati
    public void resetTexts() {
        // reset lunghezza
        if (state==0) { // caso login
            if (error) { // error solo password errata
                passwordInput.setLength(0);
                // reset campi digitabili
                enteringNickname = false;
                enteringPassword = true;
                return;
            }

            // qualunque altro errore //
            // reset lunghezza testi
            nicknameInput.setLength(0);
            passwordInput.setLength(0);
            // reset campi digitabili
            enteringNickname = true;
            enteringPassword = false;
        }

        // caso signup - ogni testo e campo da resettare //
        nicknameInput.setLength(0);
        passwordInput.setLength(0);
        // campi digitabili
        enteringNickname = true;
        enteringPassword = false;
    }


    // metodo per direzione all'algoritmo di registrazione o accesso
    public void processLoginOrSignup() {
        if (state == 0) {
            LogInAlg(); // algoritmo di accesso
        } else {
            SignUpAlg(); // algoritmo di registrazione
        }
        resetTexts();
    }

    // algoritmo di registrazione
    public void SignUpAlg() {
        // recupero nickname digitato con pulizia da caratteri non adatti
        nickname = sanitizeNickname(nicknameInput.toString());

        try {
            // nickname invalido, contiene parole invalide
            //if (!ProfanityFilter.isValidNickname(nickname)) { error4=true; return; } // todo: migliorarlo perché non funziona

            // controllo presenza utente
            if (!FirestoreUserRepository.checkUsernameExists(nickname)) {

                // assegnazione della psw digitata
                password = passwordInput.toString();

                // creazione file utente
                createFiles();

                // blocco del lock
                LocalLockStore.setLockStatus(nickname, true);

                SessionLockService.startHeartbeat(nickname); // inizio del refresh del timestamp
                UserProgressService.loadProgresses(); // caricamento progressi utente
                state = 3; // passaggio alla lobby
            }
            else if (!nickname.isEmpty() && !passwordInput.isEmpty()) {
                error = true;
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // algoritmo di accesso
    public void LogInAlg() {
        nickname = sanitizeNickname(nicknameInput.toString());

        try {
            // nickname non trovato
            if (!FirestoreUserRepository.checkUsernameExists(nickname)) { resetErrors(); error1 = true; return; }

            // sessione scaduta => rilascio del lock
            if (LocalLockStore.isSessionExpired(nickname)) { LocalLockStore.setLockStatus(nickname, false); }

            // stato del lock => "true"=>impossibile accedere/"false"=>l'utente entra
            if (LocalLockStore.isUserLocked(nickname)) { resetErrors(); error3 = true; return; }

            // blocca subito la sessione
            LocalLockStore.setLockStatus(nickname, true);

            // recupero password utente dal server
            String hashedPsw = FirestoreUserRepository.getPassword(nickname);

            /// once all the users will have the password hashed, we can leave only the hash control in the
            /// password-check behind. now we use an "&&" statement to permit access for both types of passwords,
            ///  hashed or not. the previous version of the game was using a non-hash method to save the passwords.

            // password errata => libera subito il lock
            if (!hashedPsw.contentEquals(passwordInput) && !BCrypt.checkpw(String.valueOf(passwordInput), hashedPsw)) {
                resetErrors(); error = true;
                LocalLockStore.setLockStatus(nickname, false);
                return;
            }

            // password corretta => procede con la lobby
            password = passwordInput.toString();

            SessionLockService.startHeartbeat(nickname); // inizio del refresh del timestamp
            UserProgressService.loadProgresses(); // caricamento progressi utente
            state = 3; // passaggio alla lobby
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    // metodo per creare i file per i progressi utente
    public void createFiles() {
        // data di registrazione utente
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // formato
        String date = LocalDate.now().format(formatter); // recupero giorno creazione profilo

        // hash della password
        password = BCrypt.hashpw(password, BCrypt.gensalt());

        // setting dati del nuovo utente
        UserProgressService.setProgress("nickname", nickname); // nickname
        UserProgressService.setProgress("password", password); // password
        UserProgressService.setProgress("date", date); // data di registrazione

        // init progressi del nuovo utente
        UserProgressService.setProgress("avatar", 0);
        UserProgressService.setProgress("credits", 0);
        UserProgressService.setProgress("credits_missions", 0);
        UserProgressService.setProgress("total_credits", 0);
        UserProgressService.setProgress("completed_mission", false);
        UserProgressService.setProgress("diff_classic_game", 1);
        UserProgressService.setProgress("diff_space_battle", 1);
        UserProgressService.setProgress("mission_id", 1);
        UserProgressService.setProgress("level", 1);
        UserProgressService.setProgress("movement_type", 1);
        UserProgressService.setProgress("shot_type", 1);
        UserProgressService.setProgress("spacecraft_CG", 0);
        UserProgressService.setProgress("spacecraft_SB", 0);
        UserProgressService.setProgress("spacecraft_SJ", 0);
        UserProgressService.setProgress("num_double_points", 1);
        UserProgressService.setProgress("num_gold_heart", 1);
        UserProgressService.setProgress("num_shield", 1);
        UserProgressService.setProgress("num_super_laser", 1);
        UserProgressService.setProgress("num_mission", 1);
        UserProgressService.setProgress("wins_SB_missions", 0);
        UserProgressService.setProgress("num_aliens_hit", 0);
        UserProgressService.setProgress("num_aliens_hit_missions", 0);
        UserProgressService.setProgress("matches_CG", 0);
        UserProgressService.setProgress("matches_SB", 0);
        UserProgressService.setProgress("won_SB", 0);
        UserProgressService.setProgress("win_streak_SB", 0);
        UserProgressService.setProgress("points", 0);
        UserProgressService.setProgress("points_missions", 0);
        UserProgressService.setProgress("state_product_5", false);
        UserProgressService.setProgress("state_product_6", false);
        UserProgressService.setProgress("level_bought", false);
        UserProgressService.setProgress("sound_volume", 0.5);
        UserProgressService.setProgress("music_volume", 0.5);
        UserProgressService.setProgress("alpha_fragments", 0);
        UserProgressService.setProgress("show_warning", true);

        // salvataggio punti di base in remoto nel loro apposito campo
        try { FirestoreUserRepository.setUserPoints(AuthAlgorithms.nickname, 0); }
        catch (Exception e) { System.out.println(e.getMessage()); }

        // salvataggio su file dei progressi e dati utente iniziali
        UserProgressService.saveProgresses();
    }

    // ************************************** //
    // METODI DELL'INTERFACCIA InputProcessor //
    // ************************************** //

    // metodo per validare gli input
    private boolean isValidInput() {
        return !nicknameInput.isEmpty() && !passwordInput.isEmpty();
    }

    // metodo per rilevare il click da tastiera
    @Override public boolean keyTyped(char character) {
        // riproduzione suono digitazione
        SoundManager.playDigitSound(50); // suono del click

        // scelta del campo da modificare
        StringBuilder currentInput = enteringNickname ? nicknameInput : passwordInput;

        // ENTER terminare la digitazione
        if ((character == '\n' || character == '\r')) {
            // passaggio alla digitazione della password
            if (enteringNickname) {
                enteringPassword = true;
                enteringNickname = false;
            }
            // controllo validità campi digitati
            else if (isValidInput()) {
                // connessione assente
                if (!checkInternetConnection()) {
                    resetErrors(); // reset di qualunque errore
                    resetTexts(); // reset lunghezza campi digitati
                    error2=true; // stampa errore
                }
                // processi di autenticazione
                else processLoginOrSignup();
            }
        }
        // BACKSPACE per cancellare un carattere
        else if (character == '\b' && !currentInput.isEmpty()) currentInput.deleteCharAt(currentInput.length() - 1);
            // controllo digitazione caratteri validi
        else if (character >= 32 && character < 127 && currentInput.length() <= 10) currentInput.append(character);
        return true;
    }

    // metodo per controllare i click del mouse
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println(screenX + " " + screenY);

        // cambio pagina - accesso => registrazione
        if ((screenX >= 894 && screenX <= 944) && (screenY >= 48 && screenY <= 98)) {
            resetTexts(); // reset campi editabili
            resetErrors(); // reset di qualunque errore
            SoundManager.playClickButton(50); // suono del click

            // cambio pagina
            if (state==0) state = 1; // da login a signup
            else state=0; // da signup a login
        }

        // pulsante back => da psw reset a login
        if (state==2 && (screenX >= 38 && screenX <= 88) && (screenY >= 48 && screenY <= 98)) state=0;

        // reset psw
        if (state==0 && !nicknameInput.isEmpty() && (screenX >= 378 && screenX <= 604) && (screenY >= 541 && screenY <= 583)) {
            // check esistenza utente
            try {
                if (!FirestoreUserRepository.checkUsernameExists(nickname)) { resetErrors(); error1 = true;} // nickname not found
                else state=2; // passaggio al reset password
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // click per procedere avanti
        if (isValidInput() && (screenX >= 417 && screenX <= 567) && (screenY >= 436 && screenY <= 487)) {
            SoundManager.playClickButton(50); // suono del click

            // controllo internet e passaggio algoritmi di autenticazione
            if (!checkInternetConnection()) { resetErrors(); error2=true; }
            // todo: aggiungere l'opzione di aggiornamento password
            else { processLoginOrSignup(); }
        }

        // click per nascondere/mostrare la password
        if ((screenX >= 689 && screenX <= 716) && (screenY >= 352 && screenY <= 372)) {
            SoundManager.playClickButton(50); // suono del click
            showPS = !showPS;
        }

        // click per attivare la digitazione del nickname
        if (!enteringNickname && ((screenX>=251 && screenX<=732) && (screenY>=243 && screenY<=283))) {
            SoundManager.playClickButton(50); // suono del click
            enteringNickname=true;
            enteringPassword=false;
        }

        // click per attivare la digitazione della password
        if (!enteringPassword && ((screenX>=251 && screenX<=732) && (screenY>=340 && screenY<=380))) {
            SoundManager.playClickButton(50); // suono del click
            enteringNickname=false;
            enteringPassword=true;
        }
        return true;
    }

    // cambio icona mouse al passaggio sugli elementi
    @Override public boolean mouseMoved(int screenX, int screenY) {
        // finché si muove fuori dai pulsanti rimangono spenti, con le grafiche di base
        btnRedHover=btnResetPSWHover=gotoLoginHover=gotoSignupHover=gobackHover=false;

        // icona mouse
        if ((screenX >= 0 && screenX <= 1000) && (screenY >= 0 && screenY <= 700)) {
            Gdx.graphics.setCursor(cursor);
        }

        // pulsante avanti rosso
        if (isValidInput() && (screenX >= 417 && screenX <= 567) && (screenY >= 436 && screenY <= 487)) {
            btnRedHover=true;
        }

        // pulsante in alto a dx
        if ((screenX >= 894 && screenX <= 944) && (screenY >= 48 && screenY <= 98)) {
            if (state==0) gotoSignupHover=true;
            else gotoLoginHover=false;
        }

        // pulsante in alto a sx
        if (state==2 && (screenX >= 38 && screenX <= 88) && (screenY >= 48 && screenY <= 98)) gobackHover=true;

        // pulsante reset psw
        if (state==0 && !nicknameInput.isEmpty() && (screenX >= 378 && screenX <= 604) && (screenY >= 541 && screenY <= 583)) {
            btnResetPSWHover=true;
        }

        return true;
    }

    // altri metodi
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
