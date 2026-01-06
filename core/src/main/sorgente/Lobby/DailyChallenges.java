/*
Forza4 • class DailyChallenges •
Crea e gestisce le missioni per il "Daily"
Developed by Drop Logic©. All rights reserved.
*/

/*
  Il 'Daily' è una modalità di gioco nella quale bisogna completare una missione e aspettare 24h
  per ricevere la missione successiva. In totale esistono 13 missioni che si ripetono uguali con un incremento
  di difficoltà di volta in volta. Questo sistema di difficoltà crescente e moderato e una missione ogni 24h, garantiscono
  un gioco lungo e quasi "eterno". Le missioni non si sbloccano ogni 24h ore, ma si sbloccano al completamento di quella
  in cui ci si trova.
  L'utente potrebbe rimanere inceppato nella stessa missione per giorni, al suo completamente, dovrà comunque
  aspettare 24h per ricevere la successiva. Rispetto ad altri si troverà più indietro avendo molte missioni da recuperare.
  Questo sistema obbliga l'utente a giocare in maniera costante e giornalmente.
*/

// package di appartenenza
package sorgente.Lobby;

// import classi e librerie
import org.jetbrains.annotations.NotNull;
import sorgente.UserData.UserProgressService;

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
        "Use {N} Row Braker.",
        "Win {N} matches at difficulty 3.",
        "Use {N} Precision.",
        "Earn {N} credits."
    };
    private String mission;

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

        // 2) fattore difficoltà: parte da 2 e aumenta ogni 2 giri completi
        int step = TEMPLATES.length * 2; // 26 (2 giri da 13)
        N = 2 + ((safeNumMission - 1) / step); // -1 per non aumentare "troppo presto"

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

        // 4) costruisci la missione sostituendo i placeholder
        String mission = template.replace("{N}", String.valueOf(N));

        if (template.contains("{MODE}")) {
            mission = mission.replace("{MODE}", modeStr);
        }
        return mission;
    }


    // metodo per ottenere il numero di crediti al completamento della missione
    public String getCredits() { return "+" + credits; }

}
