/*
Forza4 • class DailyChallenges •
crea e gestisce le missioni per il "daily"
developed by Drop Logic©. all rights reserved.
*/

/*
  Il 'daily' è una modalità di gioco nella quale bisogna completare una missione e aspettare 24h
  per ricevere la missione successiva. In totale esistono 13 missioni che si ripetono uguali con un incremento
  di difficoltà di volta in volta. Questo sistema di difficoltà crescente e moderato e una missione ogni 24h, garantiscono
  un gioco lungo e quasi "eterno". Le missioni si sbloccano ogni mezzanotte, ma solo dopo aver completato quella
  in cui ci si trova.
  L'utente potrebbe rimanere inceppato nella stessa missione per giorni, al suo completamento, dovrà comunque
  aspettare la mezzanotte successiva per ricevere la successiva missione. Rispetto ad altri utenti si troverà più indietro
  avendo molte missioni da recuperare.
  Questo sistema obbliga l'utente a giocare in maniera costante e giornalmente.
*/

package sorgente.Lobby;

import sorgente.UserData.UserProgressService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DailyChallenges {

    // array con i template delle missioni
    public static final String[] TEMPLATES = {
        "Play {N} matches.",
        "Use {N} Token Cracker.",
        "Win {N} matches in {MODE} mode.",
        "Use {N} Peek.",
        "Win {N} consecutive matches in {MODE} mode.",
        "Use {N} Undo.",
        "Win {N} matches without using any boosts (any game mode).",
        "Use {N} Freezer.",
        "Earn {N} points.",
        "Use {N} Row Breaker.",
        "Win {N} matches at difficulty 3.",
        "Use {N} Precision.",
        "Earn {N} credits."
    };

    // numero totale template (13)
    private static final int t = 13;

    // costruttore
    public DailyChallenges() {}

    // metodo per creare e restituire la missione corrente
    public static String getMission() {
        int numMission = ((Number) UserProgressService.getProgress("num_mission")).intValue();
        int safe = Math.max(1, numMission);

        // 1) quale template usare (0..12)
        int templateIndex = (safe - 1) % TEMPLATES.length;
        String template = TEMPLATES[templateIndex];

        // 2) fattore difficoltà (nota: qui rispetta la tua scelta attuale)
        int n = targetN(safe);

        // 3) costruisce la missione sostituendo i placeholder
        return createMission(template, safe, n);
    }

    // metodo per ottenere il premio della daily (es: "+3")
    public static int prize() {
        int safe = Math.max(1, ((Number) UserProgressService.getProgress("num_mission")).intValue());
        return targetN(safe);
    }

    // metodo per calcolare n in base alla missione (lasciato come tua scelta)
    public static int targetN(int safeNumMission) {
        return 2 + (safeNumMission / t);
    }

    // metodo per creare la stringa con la missione
    private static String createMission(String template, int safeNumMission, int n) {
        String modeStr = "";

        // calcola la modalità solo se serve
        if (template.contains("{MODE}")) {
            int groupIndex = (safeNumMission - 1) / TEMPLATES.length; // 0 per 1..13, 1 per 14..26, ecc.
            int gameModeIndex = groupIndex % 4; // 0,1,2,3,0,1,2,3...

            modeStr = switch (gameModeIndex) {
                case 0 -> "Classic";
                case 1 -> "Gravity4";
                case 2 -> "Horizontal";
                case 3 -> "Speedy";
                default -> "{error}";
            };
        }

        // sostituisce i placeholder
        String mission = template.replace("{N}", String.valueOf(n));
        if (template.contains("{MODE}")) mission = mission.replace("{MODE}", modeStr);

        return mission;
    }

    // metodo per calcolare la prossima mezzanotte (europe/rome)
    public static Instant nextMidnightFromNow() {
        ZoneId zone = ZoneId.of("Europe/Rome");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return nextMidnight.toInstant();
    }

    // metodo per creare il countdown per lo sblocco della missione successiva
    public static String formatCountdown(long remainingMs) {
        if (remainingMs <= 0) return "daily available";

        long sec = remainingMs / 1000;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;

        return String.format("Unlock in %2dh %2dm %2ds", h, m, s);
    }

    // aggiornamento progresso daily a fine partita (solo missioni "match-based")
    public static void updateDaily(
        boolean won,
        boolean usedAnyBoostThisMatch,
        int mod,
        int pointsEarned,
        int creditsEarned,
        int gameDifficulty
    ) {
        // se daily già completata => stop
        if ((boolean) UserProgressService.getProgress("is_daily_completed")) return;

        int safe = Math.max(1, ((Number) UserProgressService.getProgress("num_mission")).intValue());
        int idx = (safe - 1) % t;
        int n = targetN(safe);

        int progress = ((Number) UserProgressService.getProgress("daily_progress")).intValue();
        int streak   = ((Number) UserProgressService.getProgress("daily_streak")).intValue();

        String curMode = modeName(mod);
        String reqMode = requiredModeForMission(safe);

        switch (idx) {
            case 0 -> progress++; // play {n} matches

            case 2 -> { // win {n} matches in {mode}
                if (won && curMode.equals(reqMode)) progress++;
            }

            case 4 -> { // win {n} consecutive matches in {mode}
                if (curMode.equals(reqMode)) {
                    streak = won ? (streak + 1) : 0;
                    progress = streak;
                }
            }

            case 6 -> { // win {n} matches without boosts
                if (won && !usedAnyBoostThisMatch) progress++;
            }

            case 8 -> { // earn {n} points
                if (pointsEarned > 0) progress += pointsEarned;
            }

            case 10 -> { // win {n} matches at difficulty 3 (in gioco le difficoltà vanno da 0 a 3)
                if (won && gameDifficulty == 2) progress++;
            }

            case 12 -> { // earn {n} credits
                if (creditsEarned > 0) progress += creditsEarned;
            }

            // todo: aggiungere le missioni boost-based
            default -> {
                // le missioni boost-based non si aggiornano qui
                return;
            }
        }

        // clamp
        if (progress < 0) progress = 0;
        if (progress > n) progress = n;

        // completamento
        if (progress >= n) {
            progress = n;
            UserProgressService.setProgress("is_daily_completed", true);
            UserProgressService.setProgress("is_daily_reward_claimed", false);
            // qui non setto next_unlock: lo setti quando fai claim, come volevi
        }

        // salva progressi
        UserProgressService.setProgress("daily_progress", progress);
        UserProgressService.setProgress("daily_streak", streak);
    }

    // aggiornamento daily quando usi un boost (solo missioni boost-based)
    // boostKey è la chiave che usi nei progressi, es: "num_peek", "num_undo", ecc.
    public static void updateDailyOnBoostUse(String boostKey) {
        // se daily già completata => stop
        if ((boolean) UserProgressService.getProgress("is_daily_completed")) return;

        int safe = Math.max(1, ((Number) UserProgressService.getProgress("num_mission")).intValue());
        int idx = (safe - 1) % t;

        // mappa semplice: quale chiave boost serve per la missione corrente
        String requiredBoostKey = switch (idx) {
            case 1 -> "num_token_cracker";
            case 3 -> "num_peek";
            case 5 -> "num_undo";
            case 7 -> "num_freezer";
            case 9 -> "num_row_breaker";
            case 11 -> "num_precision";
            default -> null;
        };

        if (requiredBoostKey == null) return;
        if (!requiredBoostKey.equals(boostKey)) return;

        int n = targetN(safe);
        int progress = ((Number) UserProgressService.getProgress("daily_progress")).intValue() + 1;

        if (progress > n) progress = n;

        if (progress >= n) {
            UserProgressService.setProgress("is_daily_completed", true);
            UserProgressService.setProgress("is_daily_reward_claimed", false);
        }

        UserProgressService.setProgress("daily_progress", progress);
    }

    // converte l'indice mod in nome modalità
    private static String modeName(int mod) {
        return switch (mod) {
            case 0 -> "Classic";
            case 1 -> "Gravity4";
            case 2 -> "Horizontal";
            case 3 -> "Speedy";
            default -> "";
        };
    }

    // recupera la modalità richiesta dalla missione corrente (solo per template con {mode})
    private static String requiredModeForMission(int safeNumMission) {
        int groupIndex = (safeNumMission - 1) / t; // cambia ogni 13 missioni
        int gameModeIndex = groupIndex % 4;

        return switch (gameModeIndex) {
            case 0 -> "Classic";
            case 1 -> "Gravity4";
            case 2 -> "Horizontal";
            case 3 -> "Speedy";
            default -> "Classic";
        };
    }

    // metodo per il reset della missione
    public static void checkDailyMidnightUnlock() {
        boolean claimed = (boolean) UserProgressService.getProgress("daily_reward_claimed");
        boolean completed = (boolean) UserProgressService.getProgress("is_daily_completed");
        long unlockAt = ((Number) UserProgressService.getProgress("daily_next_unlock_at")).longValue();

        // se non sei in cooldown, non fare nulla
        if (!completed || !claimed || unlockAt <= 0) return;

        // se non è ancora mezzanotte, stop
        if (System.currentTimeMillis() < unlockAt) return;

        // reset daily per nuova missione
        UserProgressService.setProgress("daily_progress", 0);
        UserProgressService.setProgress("daily_streak", 0);
        UserProgressService.setProgress("is_daily_completed", false);
        UserProgressService.setProgress("daily_reward_claimed", false);
        UserProgressService.setProgress("daily_next_unlock_at", 0L);
    }

}
