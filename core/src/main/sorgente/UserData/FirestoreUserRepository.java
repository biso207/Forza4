/*
Forza4 • class FirestoreUserRepository •
Gestisce la lettura/scrittura dei dati utente sul Firestore Database
Developed by Drop Logic©. All rights reserved.
*/

package sorgente.UserData;

import com.badlogic.gdx.Gdx;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import okhttp3.*;
import org.mindrot.jbcrypt.BCrypt;
import sorgente.dbManagement.LoadingData.LoadCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreUserRepository
{
    // mappa per i punti degli utenti
    public static Map<String, Integer> userPointsMap = new HashMap<>();

    // dati del database per la connessione
    private static final String PROJECT_ID = "astroinvasioncloud"; // nome database
    private static final String DATABASE_URL = "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/";

    // costruttore
    public FirestoreUserRepository() {}

    // metodo per recuperare il token che permette la comunicazione client-server
    protected static String getAccessToken() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(Gdx.files.internal("private_key_db.json").read())
            .createScoped("https://www.googleapis.com/auth/cloud-platform");
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    // metodo per controllare dell'esistenza del nickname sul server
    public static boolean checkUsernameExists(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

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
        String url = DATABASE_URL + "astroData/" + username + "?updateMask.fieldPaths=points";

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

    // metodo per caricare i punti di tutti gli utenti
    public static void loadAllUserPoints() throws IOException {
        String url = DATABASE_URL + "astroData?pageSize=1000";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + getAccessToken())
            .get()
            .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();

        Map<String, Object> responseMap = new Gson().fromJson(body, Map.class);

        if (!responseMap.containsKey("documents")) {
            System.out.println("Nessun documento trovato!");
            return;
        }

        List<Map<String, Object>> documents = (List<Map<String, Object>>) responseMap.get("documents");
        userPointsMap.clear();

        for (Map<String, Object> doc : documents) {
            String namePath = (String) doc.get("name");
            String[] parts = namePath.split("/");
            String username = parts[parts.length - 1];

            Map<String, Object> fields = (Map<String, Object>) doc.get("fields");

            if (fields.containsKey("points")) {
                Map<String, Object> pointsMap = (Map<String, Object>) fields.get("points");
                Object valueObj = pointsMap.get("integerValue");

                if (valueObj != null) {
                    try {
                        int points = Integer.parseInt(valueObj.toString());
                        userPointsMap.put(username, points);
                    } catch (NumberFormatException e) {
                        System.err.println("Valore non valido per 'points' dell'utente " + username);
                    }
                }
            }
        }
    }

    // PASSWORD //
    // metodo per recuperare la password utente
    public static String getPassword(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

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
        Map pswField = (Map) fields.get("psw");
        return (String) pswField.get("stringValue");
    }

    // metodo per salvare la password utente in cloud
    public static void setPassword(String username, String password) throws IOException {
        // URL con updateMask per aggiornare solo il campo "psw"
        String url = DATABASE_URL + "astroData/" + username + "?updateMask.fieldPaths=psw";

        // hash della password
        password = BCrypt.hashpw(password, BCrypt.gensalt());

        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> pswField = new HashMap<>();
        pswField.put("stringValue", password);
        fields.put("psw", pswField);

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

    // DATI //
    // salva il file .dat => esegue tutto con un thread separato dal thread main di gioco
    public static void uploadDatAsync(String username, String datBase64, LoadCallback callback) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProgress(10);

                // URL con updateMask per aggiornare solo il campo "dat"
                String url = DATABASE_URL + "astroData/" + username + "?updateMask.fieldPaths=dat";

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

                String url = DATABASE_URL + "astroData/" + username;

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

    // ELIMINAZIONE PROFILO //
    // metodo per eliminare definitivamente un profilo utente
    public static void deleteUserProfile(String username) throws IOException {
        String url = DATABASE_URL + "astroData/" + username;

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

