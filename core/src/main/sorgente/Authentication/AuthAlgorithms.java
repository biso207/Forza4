package sorgente.Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;
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
    protected boolean enteringNickname, enteringPassword, enteringDay, enteringMonth, enteringYear;

    // variabili per recuperare nick e psw utente
    public static String nickname, password;

    // variabili per comporre le stringhe digitate di nick e psw
    protected final StringBuilder nicknameInput, passwordInput, dayInput, monthInput, yearInput, resetDayInput,
        resetMonthInput, resetYearInput, resetPasswordInput;

    // 0 = month, 1 = day, 2 = year, 3 = new password
    protected int resetFieldIndex = 0;

    // selezione testi (per Ctrl+A, evidenzia tutto)
    protected boolean nicknameSelected = false;
    protected boolean passwordSelected = false;

    // variabile per nascondere/mostrare la password e cambiare stile pulsanti
    protected boolean showPS=false, btnRedHover=false, btnResetPSWHover=false,
        gotoSignupHover=false, gotoLoginHover=false, gobackHover=false;

    // variabili click pulsanti
    protected boolean btnRedClicked=false, btnResetPSWClicked=false,
        gotoSignupClicked=false, gotoLoginClicked=false, gobackClicked=false;

    // variabili per i delay tra le schermate
    protected float clickedTimer = 0f;
    protected float screenChangeDelay = 0f;
    protected boolean pendingScreenChange = false;
    protected int pendingNextState = -1;

    // delay per l'esecuzione dei processi di autenticazione (login/signup)
    protected boolean pendingAuthProcess = false;
    protected float authDelay = 0f;


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
        this.enteringDay = false;
        this.enteringMonth = false;
        this.enteringYear = false;

        // dichiarazione dei stringBuilder
        nicknameInput = new StringBuilder();
        passwordInput = new StringBuilder();
        dayInput = new StringBuilder();
        monthInput = new StringBuilder();
        yearInput = new StringBuilder();
        resetDayInput  = new StringBuilder();
        resetMonthInput  = new StringBuilder();
        resetYearInput  = new StringBuilder();
        resetPasswordInput = new StringBuilder();

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
     * Ritorna true se il nickname è attualmente selezionato (Ctrl+A).
     */
    public boolean isNicknameSelected() {
        return nicknameSelected;
    }

    /**
     * Ritorna true se la password è attualmente selezionata (Ctrl+A).
     */
    public boolean isPasswordSelected() {
        return passwordSelected;
    }

    /**
     * Pianifica un cambio di schermata con un leggero ritardo, per mostrare l'animazione di click.
     */
    public void scheduleScreenChange(int nextState, float delaySeconds) {
        pendingScreenChange = true;
        pendingNextState = nextState;
        screenChangeDelay = delaySeconds;
    }


    /**
     * Pianifica l'esecuzione ritardata del processo di autenticazione (login/signup),
     * usato per mostrare l'animazione di click del pulsante PLAY prima di cambiare schermata.
     */
    public void scheduleAuthProcess(float delaySeconds) {
        pendingAuthProcess = true;
        authDelay = delaySeconds;
    }

    /**
     * Resetta tutti i flag di click dei pulsanti.
     */
    public void resetClickFlags() {
        btnRedClicked = false;
        btnResetPSWClicked = false;
        gotoSignupClicked = false;
        gotoLoginClicked = false;
        gobackClicked = false;
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

    public String getResetMonth()   { return resetMonthInput.toString(); }
    public String getResetDay()     { return resetDayInput.toString(); }
    public String getResetYear()    { return resetYearInput.toString(); }
    public String getResetNewPassword() { return resetPasswordInput.toString(); }

    // 0 = month, 1 = day, 2 = year, 3 = password
    public int getResetActiveField() { return resetFieldIndex; }

    public void resetFieldsNewPSW() {
        resetMonthInput.setLength(0);
        resetDayInput.setLength(0);
        resetYearInput.setLength(0);
        resetPasswordInput.setLength(0);
        resetFieldIndex = 0;
    }


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
        // reset selezioni (Ctrl+A)
        nicknameSelected = false;
        passwordSelected = false;

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
        nickname = sanitizeNickname(nicknameInput.toString());

        try {
            if (!FirestoreUserRepository.checkUsernameExists(nickname)) {

                // 1) password in chiaro
                password = passwordInput.toString();

                // 2) salva password su Firestore (hashata)
                FirestoreUserRepository.setPassword(nickname, password);

                // 4) data di registrazione
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String date = LocalDate.now().format(formatter);

                // 5) salva la data su Firestore
                FirestoreUserRepository.setSignupDate(nickname, date);

                // 6) crea i progressi utente
                createUserProgresses();

                // 7) set lock, start heartbeat, ecc.
                LocalLockStore.setLockStatus(nickname, true);
                SessionLockService.startHeartbeat(nickname);
                UserProgressService.loadProgresses();
                state = 3;
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

            // password errata => libera subito il lock
            if (!BCrypt.checkpw(String.valueOf(passwordInput), hashedPsw)) {
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
    public void createUserProgresses() {
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

    // metodo per validare l'input di nickname e password
    private boolean isValidInput() {
        return !nicknameInput.isEmpty() && !passwordInput.isEmpty();
    }

    // metodo per valida l'input del reset password
    private boolean isValidResetInput() {
        return resetMonthInput.length()  == 2 &&
            resetDayInput.length()    == 2 &&
            resetYearInput.length()   == 4 &&
            !resetPasswordInput.isEmpty();
    }

    // metodo per i controlli durante la digitazione del reset password
    private boolean handleResetKeyTyped(char character) {
        // suono digitazione
        SoundManager.playDigitSound(50);

        // ENTER -> se non siamo ancora sul campo password, avanza di campo
        if (character == '\n' || character == '\r') {
            if (resetFieldIndex < 3) {
                resetFieldIndex++;
            } else {
                // siamo nel campo password -> tenta reset
                if (isValidResetInput()) {
                    scheduleAuthProcess(0.20f); // stesso delay del click
                }
            }
            return true;
        }

        // BACKSPACE: cancella solo nel campo corrente (senza cambiare campo)
        if (character == '\b') {
            switch (resetFieldIndex) {
                case 0:
                    if (!resetMonthInput.isEmpty())
                        resetMonthInput.deleteCharAt(resetMonthInput.length() - 1);
                    break;
                case 1:
                    if (!resetDayInput.isEmpty())
                        resetDayInput.deleteCharAt(resetDayInput.length() - 1);
                    break;
                case 2:
                    if (!resetYearInput.isEmpty())
                        resetYearInput.deleteCharAt(resetYearInput.length() - 1);
                    break;
                case 3:
                    if (!resetPasswordInput.isEmpty())
                        resetPasswordInput.deleteCharAt(resetPasswordInput.length() - 1);
                    break;
            }
            return true;
        }

        // cifre numeriche -> per i campi data
        if (character >= '0' && character <= '9') {
            switch (resetFieldIndex) {
                case 0: // day DD
                    if (resetDayInput.length() < 2) {
                        resetDayInput.append(character);
                        if (resetDayInput.length() == 2) resetFieldIndex = 1; // auto-avanza a MONTH
                    }
                    break;
                case 1: // month MM
                    if (resetMonthInput.length() < 2) {
                        resetMonthInput.append(character);
                        if (resetMonthInput.length() == 2) resetFieldIndex = 2; // auto-avanza a YEAR
                    }
                    break;
                case 2: // year YYYY
                    if (resetYearInput.length() < 4) {
                        resetYearInput.append(character);
                        if (resetYearInput.length() == 4) resetFieldIndex = 3; // auto-avanza a PASSWORD
                    }
                    break;
                case 3: // password: numeri ammessi come normali caratteri
                    if (resetPasswordInput.length() < 32) { // limite a piacere
                        resetPasswordInput.append(character);
                    }
                    break;
            }
            return true;
        }

        // caratteri stampabili per la sola password
        if (character >= 32 && character < 127) {
            if (resetFieldIndex == 3 && resetPasswordInput.length() < 32) {
                resetPasswordInput.append(character);
            }
            return true;
        }

        return true;
    }

    // metodo per il reset della password
    private void processPasswordReset() {
        try {
            // compone la data in formato dd/MM/yyyy (uguale al DB)
            String day   = resetDayInput.toString();
            String month = resetMonthInput.toString();
            String year  = resetYearInput.toString();

            String dateUser = day + "/" + month + "/" + year;

            // recupera data dal DB
            String dateDb = FirestoreUserRepository.getSignupDate(nickname);

            if (dateDb == null || !dateDb.equals(dateUser)) {
                resetErrors(); // reset errori
                resetFieldsNewPSW(); // pulizia campi
                error = true; // in AuthUI è scritto come "Incorrect ID Creation Date"
                return;
            }

            // aggiornamento password su Firestore
            String newPasswordPlain = resetPasswordInput.toString();
            FirestoreUserRepository.setPassword(nickname, newPasswordPlain);

            // PASSWORD OK, DATA OK -> passiamo a LOBBY (state 3)
            state = 3;
        } catch (Exception e) {
            e.printStackTrace();
            resetErrors();
        }
    }

    // chiamato dopo il delay di auth (login/signup/reset)
    public void executeAuthProcess() {
        // LOGIN / SIGNUP
        if (state == 0 || state == 1) {
            processLoginOrSignup();
            return;
        }

        // PASSWORD RESET
        if (state == 2) {
            // 1) check internet
            if (!checkInternetConnection()) {
                resetErrors();
                error2 = true;
                return;
            }

            // 2) check campi (MM/DD/YYYY + nuova password)
            if (!isValidResetInput()) {
                // campi incompleti, non andare avanti
                return;
            }

            // 3) check data + reset password + passaggio a lobby
            processPasswordReset();
        }
    }

    // metodo per rilevare il click da tastiera
    @Override public boolean keyTyped(char character) {
        // riproduzione suono digitazione
        SoundManager.playDigitSound(50); // suono del click

        // scelta del campo da modificare
        StringBuilder currentInput = enteringNickname ? nicknameInput : passwordInput;
        boolean isNicknameField = enteringNickname;
        boolean currentSelected = isNicknameField ? nicknameSelected : passwordSelected;

        // se il testo è selezionato (Ctrl+A), gestiamo prima la cancellazione/sostituzione
        if (currentSelected) {
            // BACKSPACE: cancella tutto il testo selezionato
            if (character == '\b') {
                currentInput.setLength(0);
                if (isNicknameField) nicknameSelected = false; else passwordSelected = false;
                return true;
            }

            // carattere stampabile: sostituisce completamente il testo selezionato
            if (character >= 32 && character < 127) {
                currentInput.setLength(0);
                currentInput.append(character);
                if (isNicknameField) nicknameSelected = false; else passwordSelected = false;
                return true;
            }

            // ENTER: rimuove solo lo stato di selezione e prosegue con la logica esistente
            if (character == '\n' || character == '\r') {
                if (isNicknameField) nicknameSelected = false; else passwordSelected = false;
                // non facciamo return qui: lasciamo che venga gestito sotto
            }
        }

        // digitazione data di registrazione SOLO nella pagina 2
        if (state==2) {
            return handleResetKeyTyped(character);
        }

        // ENTER termina la digitazione o avvia il processo di autenticazione
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
                    error2 = true; // stampa errore
                }
                // processi di autenticazione
                else processLoginOrSignup();
            }
        }
        // BACKSPACE per cancellare un carattere singolo
        else if (character == '\b' && !currentInput.isEmpty()) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
        // controllo digitazione caratteri validi
        else if (character >= 32 && character < 127 && currentInput.length() <= 10) {
            currentInput.append(character);
        }
        return true;
    }

    // metodo per controllare i click del mouse
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println(screenX + " " + screenY);
        // reset degli hover per mostrare bene il resto
        btnRedHover = btnResetPSWHover = gotoLoginHover = gotoSignupHover = gobackHover = false;

        // cambio pagina - accesso <-> registrazione (pulsante in alto a destra)
        if ((state==0||state==1) && (screenX >= 894 && screenX <= 944) && (screenY >= 48 && screenY <= 98)) {
            SoundManager.playClickButton(50); // suono del click
            resetTexts(); // reset campi editabili
            resetErrors(); // reset di qualunque errore

            // determinazione della pagina di destinazione
            int targetState = (state == 0) ? 1 : 0;
            if (targetState == 1) gotoSignupClicked = true; // da login a signup
            else gotoLoginClicked = true;  // da signup a login

            clickedTimer = 0.15f;
            scheduleScreenChange(targetState, 0.20f);
        }

        // pulsante back => da psw reset a login (in alto a sinistra)
        if (state == 2 && (screenX >= 38 && screenX <= 88) && (screenY >= 48 && screenY <= 98)) {
            resetErrors(); // reset di qualunque errore
            SoundManager.playClickButton(50); // suono del click
            gobackClicked = true;
            clickedTimer = 0.15f;
            scheduleScreenChange(0, 0.20f);
        }

        // reset psw (da login a schermata reset)
        if (state == 0 && !nicknameInput.isEmpty() &&
            (screenX >= 378 && screenX <= 604) && (screenY >= 541 && screenY <= 583)) {
            SoundManager.playClickButton(50); // suono del click
            btnResetPSWClicked = true;
            clickedTimer = 0.15f;

            // check esistenza utente
            try {
                if (!FirestoreUserRepository.checkUsernameExists(sanitizeNickname(nicknameInput.toString()))) {
                    resetErrors(); // reset di qualunque errore
                    error1 = true; // in AuthUI è mostrato come "Nickname not found"
                } else {
                    // setting del nome utente
                    nickname = sanitizeNickname(nicknameInput.toString());
                    // passaggio alla schermata di reset password
                    scheduleScreenChange(2, 0.20f);
                }

                // pulizia
                resetTexts(); // reset campi editabili
                resetFieldsNewPSW(); // campi nella pagina di reset password
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // click per procedere avanti (pulsante rosso)
        if ((screenX >= 417 && screenX <= 567) && (screenY >= 436 && screenY <= 487)) {
            SoundManager.playClickButton(50); // suono del click
            btnRedClicked = true;
            clickedTimer = 0.15f;

            // LOGIN / SIGNUP (state 0 o 1)
            if (state == 0 || state == 1) {
                if (!isValidInput()) {
                    return true; // campi vuoti, non facciamo nulla
                }

                if (!checkInternetConnection()) {
                    resetErrors();
                    error2 = true;
                } else {
                    // delay prima di eseguire login/signup
                    scheduleAuthProcess(0.20f);
                }
            }

            // PASSWORD RESET (state == 2)
            else if (state == 2) {
                if (!isValidResetInput()) {
                    // dati incompleti, non andare avanti
                    return true;
                }

                // delay prima di eseguire il reset
                scheduleAuthProcess(0.20f);
            }

            return true; // importantissimo: abbiamo gestito il click, non proseguire oltre
        }


        // click per nascondere/mostrare la password (icona occhio)
        if ((screenX >= 689 && screenX <= 716) && (screenY >= 352 && screenY <= 372)) {
            SoundManager.playClickButton(50); // suono del click
            showPS = !showPS;
        }

        // click per attivare la digitazione del nickname
        if (!enteringNickname &&
            (screenX >= 251 && screenX <= 732) && (screenY >= 243 && screenY <= 283)) {
            SoundManager.playClickButton(50); // suono del click
            enteringNickname = true;
            enteringPassword = false;
            // quando cambiamo campo di input, nessun testo deve restare selezionato
            nicknameSelected = false;
            passwordSelected = false;
        }

        // click per attivare la digitazione della password
        if (!enteringPassword &&
            (screenX >= 251 && screenX <= 732) && (screenY >= 340 && screenY <= 380)) {
            SoundManager.playClickButton(50); // suono del click
            enteringNickname = false;
            enteringPassword = true;
            // cambio campo: azzeriamo eventuali selezioni attive
            nicknameSelected = false;
            passwordSelected = false;
        }
        return true;
    }

    @Override public boolean mouseMoved(int screenX, int screenY) {
        // finché si muove fuori dai pulsanti rimangono spenti, con le grafiche di base
        btnRedHover = btnResetPSWHover = gotoLoginHover = gotoSignupHover = gobackHover = false;

        // icona mouse
        if ((screenX >= 0 && screenX <= 1000) && (screenY >= 0 && screenY <= 700)) {
            Gdx.graphics.setCursor(cursor);
        }

        // pulsante avanti rosso
        if ((isValidInput() || isValidResetInput()) &&
            (screenX >= 417 && screenX <= 567) && (screenY >= 436 && screenY <= 487)) {
            btnRedHover = true;
        }

        // pulsante in alto a dx
        if ((state==0||state==1) && (screenX >= 894 && screenX <= 944) && (screenY >= 48 && screenY <= 98)) {
            if (state == 0) gotoSignupHover = true;
            else gotoLoginHover = true;
        }

        // pulsante in alto a sx
        if (state == 2 &&
            (screenX >= 38 && screenX <= 88) && (screenY >= 48 && screenY <= 98)) {
            gobackHover = true;
        }

        // pulsante reset psw
        if (state == 0 && !nicknameInput.isEmpty() &&
            (screenX >= 378 && screenX <= 604) && (screenY >= 541 && screenY <= 583)) {
            btnResetPSWHover = true;
        }

        return true;
    }

    // altri metodi
    @Override public boolean keyDown(int keycode) {
        // Ctrl + A per selezionare tutto il testo del campo attivo
        if (keycode == Input.Keys.A &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {

            if (enteringNickname && !nicknameInput.isEmpty()) {
                nicknameSelected = true;
                passwordSelected = false;
            } else if (enteringPassword && !passwordInput.isEmpty()) {
                passwordSelected = true;
                nicknameSelected = false;
            }
            return true;
        }

        return false;
    }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
