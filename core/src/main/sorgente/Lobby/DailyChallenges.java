/*
Forza4 • class DailyChallenges •
Crea e gestisce le missioni per il "Daily"
Developed by Drop Logic©. All rights reserved.
*/

/*
  Il 'Daily' è una modalità di gioco nella quale bisogna completare una missione e aspettare 24h
  per ricevere la missione successiva. In totale esistono 13 missioni che si ripetono uguali con un incremento
  di difficoltà di volta in volta. Questo sistema di difficoltà crescente e moderato e una missione ogni 24h, garantiscono
  un gioco lungo e quasi "eterno". Le missioni si sbloccano ogni mezzanotte, ma solo dopo aver completato quella
  in cui ci si trova.
  L'utente potrebbe rimanere inceppato nella stessa missione per giorni, al suo completamente, dovrà comunque
  aspettare la mezzanotte successiva per ricevere la successiva missione. Rispetto ad altri utenti si troverà più indietro
  avendo molte missioni da recuperare.
  Questo sistema obbliga l'utente a giocare in maniera costante e giornalmente.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import sorgente.UserData.UserProgressService;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class DailyChallenges {
    // array con i template delle missioni
    public final String[] TEMPLATES = {
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

    // 'fattore difficoltà missione' che parte da 2 e incrementa di 1 ogni 26 missioni (2 giri da 13) //
    public int N = 2; // questo numero incrementa in base alla missione in cui si trova l'utente
    // numero di crediti al completamento della missione
    public int credits = 2; // di base 2 poi aumenta come il 'fattore difficoltà missione'

    // costruttore
    public DailyChallenges() {}

    // metodo per creare e restituire la missione
    public String getMission() {
        int numMission = (int) UserProgressService.getProgress("num_mission");
        int safeNumMission = Math.max(1, numMission); // le missioni partono da 1

        // 1) quale template usare (0..12)
        int templateIndex = (safeNumMission - 1) % TEMPLATES.length; // -1 perché le missioni partono da 1
        String template = TEMPLATES[templateIndex];

        // 2) fattore difficoltà: parte da 2 e aumenta ogni giro completi
        int step = TEMPLATES.length; // 13 missioni e poi incremento
        N = 2 + (safeNumMission / step);

        // crediti al completamento della missione
        credits = N;

        // returna la missione creata nel metodo a parte
        return createMission(template, safeNumMission);
    }

    // metodo per creare la stringa con la missione
    private String createMission(String template, int safeNumMission) {
        String modeStr = "";
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

        // 4) costruisce la missione sostituendo i placeholder
        String mission = template.replace("{N}", String.valueOf(N));
        if (template.contains("{MODE}")) mission = mission.replace("{MODE}", modeStr);

        return mission;
    }

    // metodo per ottenere il numero di crediti al completamento della missione
    public String prize() { return "+" + credits; }

    // metodo per aggiornare i progressi della missione corrente
    public static Map<String, Object> buildDailyProgress(
        int missionIndex,
        int targetN,
        String mode,      // "" se non serve
        int difficulty    // 0 se non serve
    )
    {
        Map<String, Object> p = new HashMap<>();

        p.put("missionIndex", missionIndex);
        p.put("target", targetN);
        p.put("current", 0);
        p.put("mode", mode == null ? "" : mode);
        p.put("difficulty", difficulty);
        p.put("completed", false);
        p.put("updatedAtMs", System.currentTimeMillis());

        // key + meta
        Map<String, Object> meta = new HashMap<>();

        switch (missionIndex) {
            case 0 -> p.put("key", "PLAY_MATCHES");

            case 1 -> { p.put("key", "USE_BOOST"); meta.put("boost", "TokenCracker"); }
            case 2 -> p.put("key", "WIN_IN_MODE");
            case 3 -> { p.put("key", "USE_BOOST"); meta.put("boost", "Peek"); }

            case 4 -> { p.put("key", "WIN_STREAK_IN_MODE"); meta.put("streak", 0); }

            case 5 -> { p.put("key", "USE_BOOST"); meta.put("boost", "Undo"); }

            case 6 -> { p.put("key", "WIN_NO_BOOSTS"); meta.put("cleanWins", 0); }

            case 7 -> { p.put("key", "USE_BOOST"); meta.put("boost", "Freezer"); }

            case 8 -> { p.put("key", "EARN_POINTS"); meta.put("totalEarned", 0); }

            case 9 -> { p.put("key", "USE_BOOST"); meta.put("boost", "RowBreaker"); }

            case 10 -> { p.put("key", "WIN_AT_DIFFICULTY"); p.put("difficulty", 3); }

            case 11 -> { p.put("key", "USE_BOOST"); meta.put("boost", "Precision"); }

            case 12 -> { p.put("key", "EARN_CREDITS"); meta.put("totalEarned", 0); }

            default -> p.put("key", "UNKNOWN");
        }

        p.put("meta", meta);
        return p;
    }

    // metodo per calcolare la prossima mezzanotte
    public Instant nextMidnightFromNow() {
        ZoneId zone = ZoneId.of("Europe/Rome");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return nextMidnight.toInstant();
    }

    // metodo per creare il countdown per lo sblocco della missione successiva
    public String formatCountdown(long remainingMs) {
        long sec = remainingMs / 1000;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("Next in %02dh %02dm %02ds", h, m, sec);
    }
}
