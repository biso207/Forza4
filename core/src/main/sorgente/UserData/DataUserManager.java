package sorgente.UserData;

import org.json.JSONObject;
import sorgente.LogInSignUp.AuthAlgorithms;
import sorgente.LogInSignUp.LoadingData.GlobalProgressManager;
import sorgente.LogInSignUp.LoadingData.LoadCallback;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DataUserManager implements LoadCallback
{
    private static final Map<String, Object> progressi = new HashMap<>();; // hashmap per i dati

    // carica i progressi utente (decodifica Base64 + parsing JSON)
    public static void loadProgresses() throws IOException {
        // caricamento da cloud in remoto
        CloudStorageManager.downloadDatAsync(AuthAlgorithms.nickname, new LoadCallback() {
            @Override
            public void onProgress(int progress) {
                // aggiorna la barra di caricamento
                GlobalProgressManager.notifyProgress(progress);
            }

            @Override
            public void onComplete(boolean success, String result) {
                if (success) {
                    // decrypt della stringa passata
                    byte[] decodedBytes = Base64.getDecoder().decode(result);
                    String jsonText = new String(decodedBytes);

                    // salvataggio dati nella mappa dei progressi utente
                    JSONObject json = new JSONObject(jsonText);
                    for (String key : json.keySet()) {
                        progressi.put(key, json.get(key));
                    }
                } else {
                    System.out.println("Errore nel download dei progressi utente: " + result);
                }
            }
        });
    }

    // salva i progressi sul server remoto (JSON → Base64 → scrittura)
    public static void saveProgresses() {
        JSONObject json = new JSONObject(progressi);
        String encoded = Base64.getEncoder().encodeToString(json.toString(4).getBytes());

        // salvataggio dati utente in cloud remoto
        CloudStorageManager.uploadDatAsync(AuthAlgorithms.nickname, encoded, new LoadCallback() {
            @Override
            public void onProgress(int progress) {
                // aggiorna la barra di caricamento
                if (GlobalProgressManager.isInitialLoading) {
                    GlobalProgressManager.notifyProgress(progress);
                }
            }

            @Override
            public void onComplete(boolean success, String result) {}
        });
    }

    // recupera un progresso specifico
    public static Object getProgress(String nome) {
        return progressi.getOrDefault(nome, null);
    }

    // aggiorna un valore e salva tutto
    public static void setProgress(String nome, Object valore) {
        progressi.put(nome, valore);
        saveProgresses();
    }

    // metodo per resettare i progressi al logout => evita sovrascritture
    public static void resetProgress() {
        progressi.clear();
    }

    // ************************************ //
    // METODI DELL'INTERFACCIA LoadCallback //
    // ************************************ //
    // da lasciare vuoti per non creare errori
    @Override
    public void onProgress(int progress) {}
    @Override
    public void onComplete(boolean success, String result) {}
}

