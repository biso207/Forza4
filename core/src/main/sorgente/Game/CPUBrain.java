/*
Forza4 • class CPUBrain •
Questa classe è il cervello della CPU. Diventa più 'smart' in base alla difficoltà e viene controllato anche se
una mossa utente o CPU provoca la vittoria di una partita
Developed by Drop Logic©. All rights reserved.
*/

// package di appartenenza
package sorgente.Game;

// import classi e librerie
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPUBrain {

    // 🔢 Livello di difficoltà del bot:
    // 0 = facile (mossa casuale)
    // 1 = medio (prova a vincere o bloccare)
    // 2 = difficile (strategia avanzata, da implementare)

    private final int difficulty;

    // 🎯 Valore della pedina del bot:
    // 1 = rosso, 2 = giallo (deve corrispondere a boardState)

    private final int botToken;
    private final int modalita;

    // 🧱 Costruttore: inizializza il bot con difficoltà e colore
    public CPUBrain(int difficulty, int botToken, int mod) {
        this.difficulty = difficulty;
        this.botToken = botToken;

        modalita=mod;

    }

    // 🧠 Metodo principale: decide in quale colonna giocare
    // Riceve lo stato attuale della griglia (6x7) e restituisce l’indice della colonna scelta
    public int chooseMove(int[][] board) {
        return switch (difficulty) {
            case 0 -> randomMove(board); // livello facile: mossa casuale
            case 1 -> smartMove(board);  // livello medio: cerca di vincere o bloccare
            case 2 -> bestMove(board);   // livello difficile: da implementare
            default -> randomMove(board); // fallback: se il livello è sconosciuto, gioca a caso
        };
    }

    // 🎲 Mossa casuale: sceglie una colonna libera a caso
    private int randomMove(int[][] board) {
        List<Integer> validCols = new ArrayList<>();

        // Scansiona tutte le colonne
        for (int col = 0; col < 7; col++)
        {
            // Se la prima riga è vuota, la colonna è giocabile
            if (board[0][col] == 0)
            {
                validCols.add(col);
            }
        }

        // Sceglie una colonna a caso tra quelle disponibili
        return validCols.get(new Random().nextInt(validCols.size()));
    }

    // 🧠 Mossa intelligente: prova a vincere o bloccare l’avversario
    private int smartMove(int[][] board) {
        // 1️⃣ Prova a vincere: simula ogni colonna e verifica se può chiudere la partita
        for (int col = 0; col < 7; col++)
        {
            int row = getLowestFreeRow(board, col);

            if (row != -1)
            {
                board[row][col] = botToken; // simula la mossa

                if (checkWin(board, row, col, botToken))
                {
                    board[row][col] = 0; // annulla la simulazione
                    return col; // gioca qui per vincere
                }

                board[row][col] = 0; // reset
            }
        }

        // 2️⃣ Prova a bloccare l’avversario: simula le sue mosse e blocca se necessario
        int opponent = (botToken == 1) ? 2 : 1;
        for (int col = 0; col < 7; col++)
        {
            int row = getLowestFreeRow(board, col);
            if (row != -1)
            {
                board[row][col] = opponent; // simula la mossa dell’avversario
                if (checkWin(board, row, col, opponent)) {
                    board[row][col] = 0; // reset
                    return col; // blocca l’avversario
                }
                board[row][col] = 0; // reset
            }
        }

        // 3️⃣ Nessuna minaccia o vittoria immediata: gioca casualmente
        return randomMove(board);
    }

    // 🧠 Mossa avanzata: da implementare (es. minimax, valutazione euristica)
    private int bestMove(int[][] board) {
        // Ordine di preferenza: centro → lati
        int[] preferredOrder = {3, 2, 4, 1, 5, 0, 6};

        // 1️⃣ Prova a vincere subito
        for (int col : preferredOrder) {
            int row = getLowestFreeRow(board, col);
            if (row != -1) {
                board[row][col] = botToken;
                if (checkWin(board, row, col, botToken)) {
                    board[row][col] = 0;
                    return col;
                }
                board[row][col] = 0;
            }
        }

        // 2️⃣ Blocca l’avversario se sta per vincere
        int opponent = (botToken == 1) ? 2 : 1;
        for (int col : preferredOrder) {
            int row = getLowestFreeRow(board, col);
            if (row != -1) {
                board[row][col] = opponent;
                if (checkWin(board, row, col, opponent)) {
                    board[row][col] = 0;
                    return col;
                }
                board[row][col] = 0;
            }
        }

        // 3️⃣ Nessuna urgenza: scegli la colonna libera più centrale possibile
        for (int col : preferredOrder) {
            if (board[0][col] == 0) {
                return col;
            }
        }

        // 4️⃣ Fallback: mossa casuale
        return randomMove(board);
    }


    // 🔽 Trova la prima riga libera in una colonna (dal basso verso l’alto)
    private int getLowestFreeRow(int[][] board, int col) {
        for (int row = 5; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1; // colonna piena
    }

    // ✅ Controlla se una mossa ha generato una vittoria
    public boolean checkWin(int[][] board, int row, int col, int token) {

        if(GameUI.mod == 2)
        {
           return checkDirection(board, row, col, token, 0, 1);
        }

        // Controlla tutte le direzioni: verticale, orizzontale, diagonale /
        return checkDirection(board, row, col, token, 1, 0)   // verticale ↓↑
            || checkDirection(board, row, col, token, 0, 1)   // orizzontale ←→
            || checkDirection(board, row, col, token, 1, 1)   // diagonale ↘↖
            || checkDirection(board, row, col, token, 1, -1); // diagonale ↙↗
    }

    // 🔁 Conta quanti gettoni consecutivi ci sono in una direzione
    private boolean checkDirection(int[][] board, int row, int col, int token, int dr, int dc) {
        int count = 1; // include la pedina appena giocata

        // Conta nella direzione positiva (es. destra, giù, diagonale)
        count += countTokens(board, row, col, token, dr, dc);

        // Conta nella direzione opposta (es. sinistra, su, diagonale inversa)
        count += countTokens(board, row, col, token, -dr, -dc);

        return count >= 4; // 4 o più pedine consecutive = vittoria
    }

    // 🔢 Conta quanti gettoni uguali ci sono in una direzione specifica
    private int countTokens(int[][] board, int row, int col, int token, int dr, int dc) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;

        // Continua finché resta nella griglia e trova pedine uguali
        while (r >= 0 && r < 6 && c >= 0 && c < 7 && board[r][c] == token) {
            count++;
            r += dr;
            c += dc;
        }

        return count;
    }
}
