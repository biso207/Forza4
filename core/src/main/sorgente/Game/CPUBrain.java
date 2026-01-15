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

    // ENTRY POINT: restituisce SOLO la colonna
    public int chooseMove(int[][] board) {
        return switch (difficulty) {
            case 0 -> randomMove(board);
            case 1 -> smartMove(board);
            case 2 -> bestMove(board);
            default -> randomMove(board);
        };
    }

    // ---------------- RANDOM MOVE ----------------
    private int randomMove(int[][] board) {
        List<Integer> validCols = new ArrayList<>();

        for (int col = 0; col < 7; col++) {
            int[] landing = GameUI.getGravityLandingCellStatic(board, col);
            if (landing[0] != -1) validCols.add(col);
        }

        if (validCols.isEmpty()) return -1;

        return validCols.get(new Random().nextInt(validCols.size()));
    }


    // ---------------- SMART MOVE ----------------
    private int smartMove(int[][] board) {
        // 1) prova a vincere
        for (int col = 0; col < 7; col++) {
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

        // 2) prova a bloccare l’avversario
        int opponent = (botToken == 1) ? 2 : 1;
        for (int col = 0; col < 7; col++) {
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

        // 3) altrimenti mossa casuale
        return randomMove(board);
    }

    // ---------------- BEST MOVE ----------------
    private int bestMove(int[][] board) {
        int[] preferredOrder = {3, 2, 4, 1, 5, 0, 6};

        // 1) prova a vincere
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

        // 2) blocca l’avversario
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

        // 3) scegli la colonna libera più centrale
        for (int col : preferredOrder) {
            if (board[0][col] == 0) {
                return col;
            }
        }

        // 4) fallback
        return randomMove(board);
    }

    // ---------------- UTILS ----------------
    private int getLowestFreeRow(int[][] board, int col) {
        for (int row = 5; row >= 0; row--) {
            if (board[row][col] == 0) return row;
        }
        return -1;
    }

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
