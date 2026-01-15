package sorgente.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPUBrain {

    // 0 = facile, 1 = medio, 2 = difficile
    private final int difficulty;
    // 1 = rosso, 2 = giallo
    private final int botToken;
    private final int modalita;

    public CPUBrain(int difficulty, int botToken, int mod) {
        this.difficulty = difficulty;
        this.botToken = botToken;
        this.modalita = mod;
    }

    // ---------------- ENTRY POINTS ----------------
    // Compatibilità: versione senza row/col (fallback)
    public int chooseMove(int[][] board) {
        // fallback: chiama la versione estesa con valori di default
        return chooseMove(board, -1, -1);
    }

    // Nuova versione: il chiamante può passare riga e colonna (es. GameUI)
    // clickedRow/clickedCol sono "preferenze" o coordinate del click simulato
    public int chooseMove(int[][] board, int clickedRow, int clickedCol) {
        return switch (difficulty) {
            case 0 -> randomMove(board, clickedRow, clickedCol);
            case 1 -> smartMove(board, clickedRow, clickedCol);
            case 2 -> bestMove(board, clickedRow, clickedCol);
            default -> randomMove(board, clickedRow, clickedCol);
        };
    }

    // ---------------- HELPERS PER LANDING ----------------
    // Ritorna {row, col} di atterraggio secondo la gravità, partendo da una "colonna richiesta"
    // modifica landingForColumn per ricevere preferredRow
    private int[] landingForColumn(int[][] board, int requestedCol, int preferredRow) {
        return GameUI.getGravityLandingCellStatic(board, requestedCol, preferredRow);
    }

    // Per gravità laterale: dato un indice di riga, trova la prima cella libera in quella riga
    // scansionando le colonne nella direzione della gravità. Restituisce {row, col} o {-1,-1}.
    private int[] landingForRow(int[][] board, int requestedRow) {
        int gravity = GameUI.gravityStep;

        if (requestedRow < 0 || requestedRow > 5) return new int[]{-1, -1};

        // RIGHT -> scansiona colonne da destra verso sinistra (preferisce colonne più a destra)
        if (gravity == 1) {
            for (int c = 6; c >= 0; c--) {
                if (board[requestedRow][c] == 0) return new int[]{requestedRow, c};
            }
            return new int[]{-1, -1};
        }

        // LEFT -> scansiona colonne da sinistra verso destra (preferisce colonne più a sinistra)
        if (gravity == 2) {
            for (int c = 0; c < 7; c++) {
                if (board[requestedRow][c] == 0) return new int[]{requestedRow, c};
            }
            return new int[]{-1, -1};
        }

        // Se non è gravità laterale, non usare questa funzione
        return new int[]{-1, -1};
    }

    // Per gravità laterale: trova la prima cella libera scorrendo righe dal basso verso l'alto,
    // e per ogni riga cerca la prima cella libera nella direzione della gravità.
    private int[] landingForAnyRowBottomUp(int[][] board) {
        int gravity = GameUI.gravityStep;
        if (gravity != 1 && gravity != 2) return new int[]{-1, -1};

        for (int r = 5; r >= 0; r--) {
            int[] l = landingForRow(board, r);
            if (l[0] != -1) return l;
        }
        return new int[]{-1, -1};
    }

    // Data una "richiesta" (requestedIndex) e la gravità, ritorna la cella di atterraggio:
    // - se gravity == TOP -> requestedIndex è una colonna
    // - se gravity == RIGHT/LEFT -> requestedIndex è una riga
    // Nota: aggiunto preferredRow per coerenza (non sempre necessario)
    private int[] landingForRequestedIndex(int[][] board, int requestedIndex, int preferredRow) {
        int gravity = GameUI.gravityStep;
        if (gravity == 0) {
            if (requestedIndex < 0 || requestedIndex > 6) return new int[]{-1, -1};
            return landingForColumn(board, requestedIndex, preferredRow);
        } else {
            if (requestedIndex < 0 || requestedIndex > 5) return new int[]{-1, -1};
            return landingForRow(board, requestedIndex);
        }
    }

    // Converte l'indice richiesto (colonna o riga) nella colonna effettiva da restituire al chiamante
    private int columnFromLanding(int[] landing) {
        if (landing == null || landing[0] == -1) return -1;
        return landing[1];
    }

    // ---------------- RANDOM MOVE ----------------
    private int randomMove(int[][] board, int clickedRow, int clickedCol) {
        List<Integer> validCols = new ArrayList<>();
        int gravity = GameUI.gravityStep;

        if (gravity == 0) {
            // TOP: itera colonne
            for (int col = 0; col < 7; col++) {
                int[] landing = landingForColumn(board, col, clickedRow);
                if (landing[0] != -1) validCols.add(col);
            }
        } else {
            // RIGHT/LEFT: itera righe (dal basso verso l'alto) e aggiungi la colonna effettiva
            for (int r = 5; r >= 0; r--) {
                int[] landing = landingForRow(board, r);
                if (landing[0] != -1) {
                    int retCol = landing[1];
                    if (!validCols.contains(retCol)) validCols.add(retCol);
                }
            }
        }

        if (validCols.isEmpty()) return -1;
        return validCols.get(new Random().nextInt(validCols.size()));
    }

    // ---------------- SMART MOVE ----------------
    private int smartMove(int[][] board, int clickedRow, int clickedCol) {
        int gravity = GameUI.gravityStep;

        // 1) prova a vincere
        if (gravity == 0) {
            // TOP: prova ogni colonna
            for (int col = 0; col < 7; col++) {
                int[] landing = landingForColumn(board, col, clickedRow);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = botToken;
                    boolean win = checkWin(board, row, landingCol, botToken);
                    board[row][landingCol] = 0;
                    if (win) return landingCol;
                }
            }
        } else {
            // RIGHT/LEFT: prova ogni riga (preferendo clickedRow se valido)
            List<Integer> rowsOrder = new ArrayList<>();
            if (clickedRow >= 0 && clickedRow <= 5) rowsOrder.add(clickedRow);
            for (int r = 5; r >= 0; r--) if (r != clickedRow) rowsOrder.add(r);

            for (int r : rowsOrder) {
                int[] landing = landingForRow(board, r);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = botToken;
                    boolean win = checkWin(board, row, landingCol, botToken);
                    board[row][landingCol] = 0;
                    if (win) return landingCol;
                }
            }
        }

        // 2) prova a bloccare l’avversario
        int opponent = (botToken == 1) ? 2 : 1;
        if (gravity == 0) {
            for (int col = 0; col < 7; col++) {
                int[] landing = landingForColumn(board, col, clickedRow);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = opponent;
                    boolean oppWins = checkWin(board, row, landingCol, opponent);
                    board[row][landingCol] = 0;
                    if (oppWins) return landingCol;
                }
            }
        } else {
            List<Integer> rowsOrder = new ArrayList<>();
            if (clickedRow >= 0 && clickedRow <= 5) rowsOrder.add(clickedRow);
            for (int r = 5; r >= 0; r--) if (r != clickedRow) rowsOrder.add(r);

            for (int r : rowsOrder) {
                int[] landing = landingForRow(board, r);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = opponent;
                    boolean oppWins = checkWin(board, row, landingCol, opponent);
                    board[row][landingCol] = 0;
                    if (oppWins) return landingCol;
                }
            }
        }

        // 3) fallback casuale
        return randomMove(board, clickedRow, clickedCol);
    }

    // ---------------- BEST MOVE ----------------
    private int bestMove(int[][] board, int clickedRow, int clickedCol) {
        int[] preferredOrderCols = {3, 2, 4, 1, 5, 0, 6};

        int gravity = GameUI.gravityStep;

        // 1) prova a vincere (ordine preferito)
        if (gravity == 0) {
            for (int col : preferredOrderCols) {
                int[] landing = landingForColumn(board, col, clickedRow);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = botToken;
                    boolean win = checkWin(board, row, landingCol, botToken);
                    board[row][landingCol] = 0;
                    if (win) return landingCol;
                }
            }
        } else {
            // per gravità laterale, interpretiamo preferredOrderCols come "preferenze di colonna"
            // ma cerchiamo righe che permettano quelle colonne; più semplice: proviamo righe dal basso
            List<Integer> rowsOrder = new ArrayList<>();
            if (clickedRow >= 0 && clickedRow <= 5) rowsOrder.add(clickedRow);
            for (int r = 5; r >= 0; r--) if (r != clickedRow) rowsOrder.add(r);

            for (int r : rowsOrder) {
                int[] landing = landingForRow(board, r);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = botToken;
                    boolean win = checkWin(board, row, landingCol, botToken);
                    board[row][landingCol] = 0;
                    if (win) return landingCol;
                }
            }
        }

        // 2) blocca l’avversario
        int opponent = (botToken == 1) ? 2 : 1;
        if (gravity == 0) {
            for (int col : preferredOrderCols) {
                int[] landing = landingForColumn(board, col, clickedRow);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = opponent;
                    boolean oppWins = checkWin(board, row, landingCol, opponent);
                    board[row][landingCol] = 0;
                    if (oppWins) return landingCol;
                }
            }
        } else {
            List<Integer> rowsOrder = new ArrayList<>();
            if (clickedRow >= 0 && clickedRow <= 5) rowsOrder.add(clickedRow);
            for (int r = 5; r >= 0; r--) if (r != clickedRow) rowsOrder.add(r);

            for (int r : rowsOrder) {
                int[] landing = landingForRow(board, r);
                int row = landing[0];
                int landingCol = landing[1];
                if (row != -1) {
                    board[row][landingCol] = opponent;
                    boolean oppWins = checkWin(board, row, landingCol, opponent);
                    board[row][landingCol] = 0;
                    if (oppWins) return landingCol;
                }
            }
        }

        // 3) scegli la mossa "più centrale" (fallback)
        if (gravity == 0) {
            for (int col : preferredOrderCols) {
                int[] landing = landingForColumn(board, col, clickedRow);
                if (landing[0] != -1) return landing[1];
            }
        } else {
            for (int r = 5; r >= 0; r--) {
                int[] landing = landingForRow(board, r);
                if (landing[0] != -1) return landing[1];
            }
        }

        // 4) fallback
        return randomMove(board, clickedRow, clickedCol);
    }

    // ---------------- UTILS ----------------
    public boolean checkWin(int[][] board, int row, int col, int token) {

        // modalità HORIZONTAL: solo orizzontale
        if (GameUI.mod == 2) {
            return checkDirection(board, row, col, token, 0, 1);
        }

        // classic / gravity4 / speedy
        return checkDirection(board, row, col, token, 1, 0)   // verticale
            || checkDirection(board, row, col, token, 0, 1)   // orizzontale
            || checkDirection(board, row, col, token, 1, 1)   // diagonale ↘
            || checkDirection(board, row, col, token, 1, -1); // diagonale ↙
    }

    private boolean checkDirection(int[][] board, int row, int col, int token, int dr, int dc) {
        int count = 1;
        count += countTokens(board, row, col, token, dr, dc);
        count += countTokens(board, row, col, token, -dr, -dc);
        return count >= 4;
    }

    private int countTokens(int[][] board, int row, int col, int token, int dr, int dc) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;

        while (r >= 0 && r < 6 && c >= 0 && c < 7 && board[r][c] == token) {
            count++;
            r += dr;
            c += dc;
        }

        return count;
    }
}
