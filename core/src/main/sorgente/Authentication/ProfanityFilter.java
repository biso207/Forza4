package sorgente.Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfanityFilter {
    private static final List<String> LANGS = List.of("it", "en", "fr", "de", "es");
    private static final List<String> blacklist = new ArrayList<>();
    private static Pattern badPattern;

    /** Carica blacklist da file <lang>.txt (una parola per riga), rimuove commenti/vuote - by ChatGPT */
    public static void loadBlacklists() {
        for (String lang : LANGS) {
            FileHandle file = Gdx.files.internal("badwords/" + lang + ".txt");

            String content = file.readString("UTF-8");
            String[] lines = content.split("\\r?\\n");

            for (String w : lines) {
                w = w.strip().toLowerCase();
                if (w.isBlank() || w.startsWith("#")) continue;
                blacklist.add(Pattern.quote(w));
            }
        }

        buildPattern();
    }

    /** Costruisce regex che cattura qualsiasi parola vietata - by ChatGPT */
    private static void buildPattern() {
        String joined = String.join("|", blacklist);
        badPattern = Pattern.compile("(?i)\\b(" + joined + ")\\b");
    }

    /** Normalizza “furbetto” nickname sostituendo simboli e numeri - by ChatGPT */
    public static String normalize(String s) {
        // Converte tutto in minuscolo
        s = s.toLowerCase();

        // Sostituzioni personalizzate dei "furbetti"
        Map<Character, Character> substitutions = Map.of(
            '4', 'a',
            '@', 'a',
            '3', 'e',
            '€', 'e',
            '1', 'i',
            '!', 'i',
            '0', 'o',
            '5', 's',
            '$', 's'
        );

        StringBuilder normalized = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (substitutions.containsKey(c)) {
                normalized.append(substitutions.get(c));
            } else if (Character.isLetter(c)) {
                normalized.append(c);
            }
            // Altri caratteri vengono ignorati
        }

        return normalized.toString();
    }


    /** Controlla se il nickname è pulito - by ChatGPT */
    public static boolean isValidNickname(String nickname) {
        String norm = normalize(nickname);
        Matcher m = badPattern.matcher(norm);
        return !m.find();
    }

}



