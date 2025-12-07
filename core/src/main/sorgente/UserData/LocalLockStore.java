/*
Forza4 • class LocalLockStore •
Gestisce lo stato di blocco dell'utente lato client, salvandolo su un file locale e fornendo
metodi per verificare, attivare e rimuovere il lock della sessione
Developed by Drop Logic©. All rights reserved.
*/

package sorgente.UserData;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class LocalLockStore {

    private LocalLockStore() {
        // Utility class: prevent instantiation
    }


    private static final String DATABASE_URL = "https://firestore.googleapis.com/v1/projects/astroinvasioncloud/databases/(default)/documents/";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5_000; // 5 secondi
    private static final long refreshInterval = DEFAULT_REFRESH_INTERVAL_MS;

    // metodo per recuperare l'ultimo timestamp
    private static long getLastTimestamp(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + FirestoreUserRepository.getAccessToken())
            .get()
            .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();

        Map<String, Object> responseMap = new Gson().fromJson(body, Map.class);
        Map<String, Object> fields = (Map<String, Object>) responseMap.get("fields");

        if (fields == null || !fields.containsKey("lock")) {
            return System.currentTimeMillis(); // Return current time if no lock field exists
        }

        try {
            Map<String, Object> lockMap = (Map<String, Object>) fields.get("lock");
            Map<String, Object> mapValue = (Map<String, Object>) lockMap.get("mapValue");
            Map<String, Object> lockFields = (Map<String, Object>) mapValue.get("fields");
            Map<String, Object> timestampMap = (Map<String, Object>) lockFields.get("timestamp");
            String timestampStr = (String) timestampMap.get("integerValue");
            return Long.parseLong(timestampStr);
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis(); // Return current time in case of any error
        }
    }


    /**
     * Verifica se la sessione dell'utente è scaduta confrontando
     * il timestamp salvato su Firestore con l'orario corrente.
     *
     * @param username username dell'utente
     * @return true se la sessione è considerata scaduta, false altrimenti
     * @throws IOException in caso di problemi di comunicazione con Firestore
     */
    public static boolean isSessionExpired(String username) throws IOException {
        // tempo corrente
        long currentTime = System.currentTimeMillis();
        // recupero dell'ultimo timestamp
        long lastTimestamp = getLastTimestamp(username);

        return (currentTime - lastTimestamp) > (2 * refreshInterval);
    }

    /**
     * Ritorna il valore del flag di lock memorizzato per l'utente su Firestore.
     *
     * @param username username dell'utente
     * @return true se l'utente risulta bloccato, false altrimenti
     * @throws IOException in caso di problemi di comunicazione con Firestore
     */
    public static boolean isUserLocked(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + FirestoreUserRepository.getAccessToken())
            .get()
            .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();

        Map<String, Object> responseMap = new Gson().fromJson(body, Map.class);
        Map<String, Object> fields = (Map<String, Object>) responseMap.get("fields");

        if (fields == null || !fields.containsKey("lock")) {
            return false; // non c'è il campo lock, quindi non bloccato
        }

        try {
            Map<String, Object> lockMap = (Map<String, Object>) fields.get("lock");
            Map<String, Object> mapValue = (Map<String, Object>) lockMap.get("mapValue");
            Map<String, Object> lockFields = (Map<String, Object>) mapValue.get("fields");

            Map<String, Object> lockedMap = (Map<String, Object>) lockFields.get("locked");
            boolean locked = (boolean) lockedMap.get("booleanValue");

            return locked; // ritorna semplicemente il valore booleano

        } catch (Exception e) {
            e.printStackTrace();
            return false; // per sicurezza, in caso di errore
        }
    }

    /**
     * Aggiorna lo stato di lock dell'utente su Firestore impostando anche
     * il timestamp dell'ultima modifica.
     *
     * @param username username dell'utente
     * @param locked   nuovo stato di lock da salvare
     * @throws IOException in caso di problemi di comunicazione con Firestore
     */
    public static void setLockStatus(String username, boolean locked) throws IOException {
        String url = DATABASE_URL + "astroData/" + username + "?updateMask.fieldPaths=lock";

        long timestamp = System.currentTimeMillis();

        // Costruisci solo i campi da aggiornare, senza toccare altri dati del documento
        Map<String, Object> lockFields = new HashMap<>();
        lockFields.put("locked", Map.of("booleanValue", locked));
        lockFields.put("timestamp", Map.of("integerValue", Long.toString(timestamp)));

        Map<String, Object> lockMap = new HashMap<>();
        lockMap.put("mapValue", Map.of("fields", lockFields));

        Map<String, Object> fields = new HashMap<>();
        fields.put("lock", lockMap);

        Map<String, Object> document = new HashMap<>();
        document.put("fields", fields);

        String json = new Gson().toJson(document);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + FirestoreUserRepository.getAccessToken())
            .patch(body)
            .build();

        Response response = client.newCall(request).execute();
        response.close();
    }
}




