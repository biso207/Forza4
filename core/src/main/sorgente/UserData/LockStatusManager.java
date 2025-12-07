package sorgente.UserData;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LockStatusManager {

    private static final String DATABASE_URL = "https://firestore.googleapis.com/v1/projects/astroinvasioncloud/databases/(default)/documents/";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5_000; // 5 secondi
    private static final long refreshInterval = DEFAULT_REFRESH_INTERVAL_MS;

    // metodo per recuperare l'ultimo timestamp
    private static long getLastTimestamp(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + CloudStorageManager.getAccessToken())
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


    // metodo per controllare che la sessione utente sia ancora attiva
    public static boolean isSessionExpired(String username) throws IOException {
        // tempo corrente
        long currentTime = System.currentTimeMillis();
        // recupero dell'ultimo timestamp
        long lastTimestamp = getLastTimestamp(username);

        return (currentTime - lastTimestamp) > (2 * refreshInterval);
    }

    // metodo per recuperare il valore di locked dell'utente
    public static boolean isUserLocked(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + CloudStorageManager.getAccessToken())
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

    // metodo per aggiornare lo stato di lock dell'utente
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
            .header("Authorization", "Bearer " + CloudStorageManager.getAccessToken())
            .patch(body)
            .build();

        Response response = client.newCall(request).execute();
        response.close();
    }
}




