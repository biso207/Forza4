/*
Forza4 • interface LoadCallback •
Classe principale del progetto Forza4.
Contiene i metodi per la notifica di progresso e completamento del caricamento dei dati da Firestore
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.dbManagement;

public interface LoadCallback {
    void onProgress(int progress); // esempio: 0-100
    void onComplete(boolean success, String result);
}
