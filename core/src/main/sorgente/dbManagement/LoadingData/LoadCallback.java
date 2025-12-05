package sorgente.dbManagement.LoadingData;

public interface LoadCallback {
    void onProgress(int progress); // esempio: 0-100
    void onComplete(boolean success, String result);
}
