/*
Forza4 • class SessionLockService •
Gestisce il lock della sessione lato server sincronizzandolo con quello locale. Comunica con Firestore per
verificare se l’utente è già connesso altrove, aggiornare il lock remoto e garantire che esista una sola
sessione attiva
Developed by Drop Logic©. All rights reserved.
*/

package sorgente.UserData;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static sorgente.Authentication.AuthAlgorithms.checkInternetConnection;

public final class SessionLockService {

    private SessionLockService() {
        // Utility class: prevent instantiation
    }


    private static ScheduledExecutorService heartbeatExecutor;
    private static ScheduledExecutorService recoveryExecutor;

    private static final long HEARTBEAT_INTERVAL_MS = 5_000;  // 5 secondi => tempo di aggiornamento del timestamp
    private static final long RECOVERY_INTERVAL_MS = 2_500;   // 2.5 secondi
    private static String currentUsername;
    private static int cont_hb = 0;

    /**
     * Avvia un heartbeat periodico che aggiorna il lock dell'utente su Firestore.
     * Il metodo è pensato per essere richiamato al login o all'avvio della sessione.
     *
     * @param username username dell'utente per cui mantenere attivo il lock
     */
    public static void startHeartbeat(String username) {
        stopHeartbeat(); // sicurezza
        stopRecovery();

        currentUsername = username;

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(createDaemonThreadFactory());
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!checkInternetConnection()) {
                // internet assente
                //UIManager.isConnected = false;
                System.out.println("[DEBUG] Connessione assente");
            } else {
               // UIManager.isConnected = true;

                try {
                    // aggiorna lo stato del lock
                    LocalLockStore.setLockStatus(username, true);
                } catch (IOException e) {
                    System.out.println("Errore nel settaggio lock: " + e.getMessage());
                }

                cont_hb++;
                System.out.println("[DEBUG] Heartbeat #" + cont_hb + " inviato per utente " + username);
            }
        }, 0, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Rilascia il lock dell'utente (se presente) e ferma tutti i task schedulati.
     * Va richiamato tipicamente nel dispose() principale dell'applicazione.
     */
    public static void shutdownAll() {
        try {
            stopHeartbeat();
            stopRecovery();
            if (currentUsername != null) {
                LocalLockStore.setLockStatus(currentUsername, false);
                System.out.println("[DEBUG] Lock rilasciato per utente " + currentUsername);
            }
        } catch (IOException e) {
            System.out.println("Errore durante il rilascio del lock: " + e.getMessage());
        }
    }

    // metodo per interrompere l'heartbeat
    private static void stopHeartbeat() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdownNow();
            System.out.println("[DEBUG] Heartbeat fermato");
        }
    }

    // metodo per interrompere il rilascio del lock
    private static void stopRecovery() {
        if (recoveryExecutor != null && !recoveryExecutor.isShutdown()) {
            recoveryExecutor.shutdownNow();
            System.out.println("[DEBUG] Recovery fermato");
        }
    }

    private static ThreadFactory createDaemonThreadFactory() {
        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        };
    }
}
