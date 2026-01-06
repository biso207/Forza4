/*
Forza4 • interface ProgressListener •
Classe principale del progetto Forza4.
Contiene i metodi per la notifica di progresso del caricamento dei dati da Firestore
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.dbManagement;

public interface ProgressListener {
    void onProgress(int progress);
}
