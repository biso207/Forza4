/*
Forza4 • class GlobalProgressManager •
Controlla lo stato di caricamento globale, usato nelle schermate di caricamento
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.dbManagement;

public class GlobalProgressManager {
    private static ProgressListener listener;
    public static boolean isInitialLoading = true;

    public static void setListener(ProgressListener l) {
        listener = l;
    }

    public static void notifyProgress(int progress) {
        if (listener != null) {
            listener.onProgress(progress);
        }
    }
}
