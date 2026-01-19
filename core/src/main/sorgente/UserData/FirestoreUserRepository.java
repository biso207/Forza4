/*
Forza4 • class FirestoreUserRepository •
Gestisce la lettura/scrittura dei dati utente sul Firestore Database
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.UserData;

// import classi e librerie
import com.badlogic.gdx.Gdx;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import okhttp3.*;
import org.mindrot.jbcrypt.BCrypt;
import sorgente.dbManagement.LoadCallback;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreUserRepository {

    // dati del database per la connessione
    private static final String PROJECT_ID = "forza4-adf22"; // nome database
    private static final String DATABASE_URL = "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/";

    // costruttore
    public FirestoreUserRepository() {}

    // metodo per creare una nuova richiesta al db e creare un field al documento utente
    public static void createRequest(String type, String nameField, Object valueField, String username) throws IOException {
        // URL con updateMask per aggiornare solo il campo "date"
        String url = DATABASE_URL + "users/" + username + "?updateMask.fieldPaths=" + nameField;

        Map<String, Object> pswField = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> document = new HashMap<>();

        pswField.put(type, valueField);
        fields.put(nameField, pswField);
        document.put("fields", fields);

        Gson gson = new Gson();
        String json = gson.toJson(document);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .patch(body)
            .build();

        Response response = client.newCall(request).execute();
        response.close();
    }

    // metodo per creare una richiesta per i getter
    public static Map createGetterRequest(String nameField, String username) throws IOException {
        String url = DATABASE_URL + "users/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .get()
            .build();

        Response response = client.newCall(request).execute();

        assert response.body() != null;
        String body = response.body().string();
        response.close();

        Map responseMap = new Gson().fromJson(body, Map.class);
        Map fields = (Map) responseMap.get("fields");
        return (Map) fields.get(nameField);
    }

    // metodo per recuperare il token che permette la comunicazione client-server
    protected static String getAccessToken() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(Gdx.files.internal("private_key_db.json").read())
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    // metodo per controllare dell'esistenza del nickname sul server
    public static boolean checkUsernameExists(String username) throws IOException {
        String url = DATABASE_URL + "users/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .get()
            .build();

        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        response.close();

        return responseCode == 200;
    }

    // PUNTI UTENTE //
    // metodo per salvare i punti utente
    public static void setUserPoints(String username, int points) throws IOException {
        String url = DATABASE_URL + "users/" + username + "?updateMask.fieldPaths=points";

        Map<String, Object> pointsField = new HashMap<>();
        pointsField.put("integerValue", Integer.toString(points));

        Map<String, Object> fields = new HashMap<>();
        fields.put("points", pointsField);

        Map<String, Object> document = new HashMap<>();
        document.put("fields", fields);

        Gson gson = new Gson();
        String json = gson.toJson(document);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .patch(body)
            .build();

        Response response = client.newCall(request).execute();
        response.close();
    }

    public static Map<String, Integer> loadAllUserPoints() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        String nextPageToken = null;
        OkHttpClient client = new OkHttpClient();

        do {
            // base URL con pageSize 1000
            String url = DATABASE_URL + "users?pageSize=1000";

            // se c'è un token, aggiungilo (pagina successiva)
            if (nextPageToken != null) {
                url += "&pageToken=" + nextPageToken;
            }

            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + getAccessToken())
                .get()
                .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            response.close();

            Map<String, Object> responseMap = new Gson().fromJson(body, Map.class);

            // se non ci sono documenti, finiamo
            if (!responseMap.containsKey("documents")) break;

            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseMap.get("documents");

            // --- Parsing dei dati della pagina corrente ---
            for (Map<String, Object> doc : documents) {
                String namePath = (String) doc.get("name");
                String[] parts = namePath.split("/");
                String username = parts[parts.length - 1];

                Map<String, Object> fields = (Map<String, Object>) doc.get("fields");
                if (fields != null && fields.containsKey("points")) {
                    Map<String, Object> pointsMap = (Map<String, Object>) fields.get("points");
                    Object valueObj = pointsMap.get("integerValue");

                    if (valueObj != null) {
                        try {
                            int points = Integer.parseInt(valueObj.toString());
                            map.put(username, points);
                        } catch (NumberFormatException ignore) {
                            System.err.println("Valore non valido per l'utente: " + username);
                        }
                    }
                }
            }

            // se esiste una prossima pagina, ripeti
            nextPageToken = (String) responseMap.get("nextPageToken");

        } while (nextPageToken != null);

        System.out.println("Caricati " + map.size() + " utenti da Firestore.");
        return map;
    }


    // PASSWORD //
    // metodo per salvare la password utente in cloud
    public static void setPassword(String username, String password) throws IOException {
        // hash della password
        password = BCrypt.hashpw(password, BCrypt.gensalt());
        createRequest("stringValue", "psw", password, username);
    }

    // metodo per recuperare la password utente
    public static String getPassword(String username) throws IOException {
        return (String) createGetterRequest("psw", username).get("stringValue");
    }


    // DATA REGISTRAZIONE //
    // metodo per salvare la data di registrazione utente
    public static void setSignupDate(String username, String date) throws IOException {
        // hash della data
        date = BCrypt.hashpw(date, BCrypt.gensalt());
        createRequest("stringValue", "date", date, username);
    }
    // metodo per recuperare la data di registrazione utente
    public static String getSignupDate(String username) throws IOException {
        return (String) createGetterRequest("date", username).get("stringValue");
    }


    // DATI //
    // salva il file .dat => esegue tutto con un thread separato dal thread main di gioco
    public static void uploadDatAsync(String username, String datBase64, LoadCallback callback) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProgress(10);

                // URL con updateMask per aggiornare solo il campo "dat"
                String url = DATABASE_URL + "users/" + username + "?updateMask.fieldPaths=dat";

                Map<String, Object> fields = new HashMap<>();
                Map<String, Object> dataField = new HashMap<>();
                dataField.put("stringValue", datBase64);
                fields.put("dat", dataField);

                Map<String, Object> document = new HashMap<>();
                document.put("fields", fields);

                Gson gson = new Gson();
                String json = gson.toJson(document);

                if (callback != null) callback.onProgress(30);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .patch(body)
                    .build();

                Response response = client.newCall(request).execute();
                response.close();

                if (callback != null) {
                    callback.onProgress(100);
                    callback.onComplete(true, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) callback.onComplete(false, e.getMessage());
            }
        }).start();
    }

    // legge il file .dat => esegue tutto con un thread separato dal thread main di gioco
    public static void downloadDatAsync(String username, LoadCallback callback) {
        new Thread(() -> {
            try {
                callback.onProgress(10);

                String url = DATABASE_URL + "users/" + username;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .get()
                    .build();

                callback.onProgress(30);

                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String body = response.body().string();
                response.close();

                callback.onProgress(60);

                Map responseMap = new Gson().fromJson(body, Map.class);
                Map fields = (Map) responseMap.get("fields");
                Map datField = (Map) fields.get("dat");
                String datBase64 = (String) datField.get("stringValue");

                callback.onProgress(100);
                callback.onComplete(true, datBase64);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onComplete(false, e.getMessage());
            }
        }).start();
    }

    // Getter MINIMAL: legge daily_progress.current e daily_progress.target dal documento utente.
    // Se il campo non esiste (utente nuovo), ritorna {0, 1}.
    public static int[] getDailyProgressNumbers(String username) throws IOException {
        try {
            Map dailyProgress = createGetterRequest("daily_progress", username);
            if (dailyProgress == null) return new int[]{0, 1};

            Map mapValue = (Map) dailyProgress.get("mapValue");
            if (mapValue == null) return new int[]{0, 1};

            Map fields = (Map) mapValue.get("fields");
            if (fields == null) return new int[]{0, 1};

            int current = 0;
            int target = 1;

            Map currentMap = (Map) fields.get("current");
            if (currentMap != null && currentMap.get("integerValue") != null) {
                current = Integer.parseInt(currentMap.get("integerValue").toString());
            }

            Map targetMap = (Map) fields.get("target");
            if (targetMap != null && targetMap.get("integerValue") != null) {
                target = Integer.parseInt(targetMap.get("integerValue").toString());
            }

            if (target <= 0) target = 1;
            if (current < 0) current = 0;
            if (current > target) current = target;

            return new int[]{current, target};

        } catch (Exception ignore) {
            return new int[]{0, 1};
        }
    }

    // metodo per salvare la prossima mezzanotte dopo il completamente dell'ultima missione
    public static void setNextDailyTime(String username, String time) throws IOException {
        createRequest("timestamp", "daily_next_unlock_at", time, username);
    }

    // metodo per settare i contatori/progresso per le missioni
    public static void setDailyProgress(String username, Map<String, Object> countProgresses) throws IOException {
        createRequest("mapValue", "daily_progress", countProgresses, username);
    }


    // ELIMINAZIONE PROFILO //
    // metodo per eliminare definitivamente un profilo utente
    public static void deleteUserProfile(String username) throws IOException {
        String url = DATABASE_URL + "users/" + username;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .delete()
            .build();

        Response response = client.newCall(request).execute();
        response.close();
    }
}

