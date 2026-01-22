package sorgente.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPUBrain {

    // 0 = facile, 1 = medio, 2 = difficile (ora molto più "pro")
    private final int difficulty;
    // 1 = rosso, 2 = giallo
    private final int botToken;
    private final int modalita;

    // Profondità massima per il minimax in modalità difficile (TOP gravity)
    // Puoi aumentare/diminuire per cambiare "intelligenza" e tempo di calcolo
    private static final int MAX_DEPTH_HARD = 6;

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

    // ---------------- BEST MOVE (ora con MINIMAX per TOP) ----------------
    private int bestMove(int[][] board, int clickedRow, int clickedCol) {
        int gravity = GameUI.gravityStep;

        // Se gravità laterale, per ora usiamo la logica "smart" (già abbastanza forte)
        // perché la fisica è diversa e più complicata da simulare in minimax.
        if (gravity != 0) {
            return smartMove(board, clickedRow, clickedCol);
        }

        // TOP gravity: usiamo MINIMAX con alpha-beta e valutazione euristica
        return minimaxBestMoveTop(board);
    }

    // ---------------- MINIMAX PER TOP GRAVITY (difficile) ----------------

    // Restituisce la colonna migliore secondo minimax
    private int minimaxBestMoveTop(int[][] board) {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;

        int opponent = (botToken == 1) ? 2 : 1;

        // Ordine di preferenza colonne (centro prima)
        int[] preferredOrderCols = {3, 2, 4, 1, 5, 0, 6};

        for (int col : preferredOrderCols) {
            if (!isValidTopMove(board, col)) continue;

            int row = getNextOpenRowTop(board, col);
            if (row == -1) continue;

            // Simula la mossa del bot
            board[row][col] = botToken;

            // Se vince subito, è la scelta migliore
            if (checkWin(board, row, col, botToken)) {
                board[row][col] = 0;
                return col;
            }

            int score = minimax(board, MAX_DEPTH_HARD - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, botToken, opponent);

            // Undo
            board[row][col] = 0;

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }
        }

        // Se per qualche motivo non trova nulla, fallback random
        if (bestCol == -1) {
            bestCol = randomMove(board, -1, -1);
        }

        return bestCol;
    }

    // Funzione minimax con potatura alpha-beta
    private int minimax(int[][] board, int depth, boolean maximizingPlayer,
                        int alpha, int beta, int bot, int opponent) {

        // Controllo terminale: vittoria, sconfitta, pareggio o profondità zero
        Integer terminalScore = getTerminalScore(board, bot, opponent, depth);
        if (terminalScore != null) {
            return terminalScore;
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (int col : getValidTopColumns(board)) {
                int row = getNextOpenRowTop(board, col);
                if (row == -1) continue;

                board[row][col] = bot;
                int eval = minimax(board, depth - 1, false, alpha, beta, bot, opponent);
                board[row][col] = 0;

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // potatura
            }

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int col : getValidTopColumns(board)) {
                int row = getNextOpenRowTop(board, col);
                if (row == -1) continue;

                board[row][col] = opponent;
                int eval = minimax(board, depth - 1, true, alpha, beta, bot, opponent);
                board[row][col] = 0;

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // potatura
            }

            return minEval;
        }
    }

    // Ritorna un punteggio se lo stato è terminale, altrimenti null
    private Integer getTerminalScore(int[][] board, int bot, int opponent, int depth) {
        // Controlla se qualcuno ha vinto
        if (hasAnyWin(board, bot)) {
            // Più in alto nella profondità = vittoria più veloce = punteggio maggiore
            return 100000 + depth;
        }
        if (hasAnyWin(board, opponent)) {
            // Sconfitta: punteggio molto negativo
            return -100000 - depth;
        }

        // Pareggio: nessuna colonna valida
        if (getValidTopColumns(board).isEmpty()) {
            return 0;
        }

        // Non terminale
        if (depth <= 0) {
            // Valutazione euristica della posizione
            return evaluateBoard(board, bot, opponent);
        }

        return null;
    }

    // Controlla se il token dato ha una qualsiasi vittoria sulla board
    private boolean hasAnyWin(int[][] board, int token) {
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                if (board[r][c] == token) {
                    if (checkWin(board, r, c, token)) return true;
                }
            }
        }
        return false;
    }

    // Restituisce tutte le colonne valide (TOP gravity)
    private List<Integer> getValidTopColumns(int[][] board) {
        List<Integer> valid = new ArrayList<>();
        for (int c = 0; c < 7; c++) {
            if (isValidTopMove(board, c)) valid.add(c);
        }
        return valid;
    }

    // Una mossa è valida se la colonna non è piena
    private boolean isValidTopMove(int[][] board, int col) {
        return board[0][col] == 0;
    }

    // Restituisce la prossima riga libera in una colonna (TOP gravity)
    private int getNextOpenRowTop(int[][] board, int col) {
        for (int r = 5; r >= 0; r--) {
            if (board[r][col] == 0) return r;
        }
        return -1;
    }

    // ---------------- FUNZIONE DI VALUTAZIONE EURISTICA ----------------
    // Più il punteggio è alto, più la posizione è favorevole al bot
    private int evaluateBoard(int[][] board, int bot, int opponent) {
        int score = 0;

        // 1) Controllo del centro: avere pezzi nella colonna centrale è molto forte
        int centerCol = 3;
        int centerCount = 0;
        for (int r = 0; r < 6; r++) {
            if (board[r][centerCol] == bot) centerCount++;
        }
        score += centerCount * 6; // peso del centro

        // 2) Valuta tutte le "finestre" di 4 celle (orizzontali, verticali, diagonali)
        // e assegna punteggi in base a quante pedine del bot/opponent ci sono
        // (tipo: 3 in fila con 1 vuota = molto buono, ecc.)

        // Orizzontali
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7 - 3; c++) {
                int[] window = {board[r][c], board[r][c + 1], board[r][c + 2], board[r][c + 3]};
                score += evaluateWindow(window, bot, opponent);
            }
        }

        // Verticali
        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 6 - 3; r++) {
                int[] window = {board[r][c], board[r + 1][c], board[r + 2][c], board[r + 3][c]};
                score += evaluateWindow(window, bot, opponent);
            }
        }

        // Diagonali ↘
        for (int r = 0; r < 6 - 3; r++) {
            for (int c = 0; c < 7 - 3; c++) {
                int[] window = {
                    board[r][c],
                    board[r + 1][c + 1],
                    board[r + 2][c + 2],
                    board[r + 3][c + 3]
                };
                score += evaluateWindow(window, bot, opponent);
            }
        }

        // Diagonali ↙
        for (int r = 0; r < 6 - 3; r++) {
            for (int c = 3; c < 7; c++) {
                int[] window = {
                    board[r][c],
                    board[r + 1][c - 1],
                    board[r + 2][c - 2],
                    board[r + 3][c - 3]
                };
                score += evaluateWindow(window, bot, opponent);
            }
        }

        return score;
    }

    // Valuta una finestra di 4 celle
    private int evaluateWindow(int[] window, int bot, int opponent) {
        int score = 0;
        int botCount = 0;
        int oppCount = 0;
        int emptyCount = 0;

        for (int v : window) {
            if (v == bot) botCount++;
            else if (v == opponent) oppCount++;
            else if (v == 0) emptyCount++;
        }

        // Se la finestra è "pura" (solo bot e vuoti)
        if (botCount == 4) {
            score += 1000;
        } else if (botCount == 3 && emptyCount == 1) {
            score += 50;
        } else if (botCount == 2 && emptyCount == 2) {
            score += 10;
        }

        // Se la finestra è "pura" per l'avversario, penalizza
        if (oppCount == 3 && emptyCount == 1) {
            score -= 80; // penalità più forte per bloccare
        } else if (oppCount == 2 && emptyCount == 2) {
            score -= 15;
        }

        return score;
    }

    // ---------------- UTILS ----------------
    public boolean checkWin(int[][] board, int row, int col, int token) {

        // modalità HORIZONTAL: solo orizzontale
        if (GameUI.mod == 2) {
            return checkDirection(board, row, col, token, 0, 1);
        }

        // classic / gravity3 / speedy
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

