package sorgente;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Fonts {

    // ---- Charset latino ----
    public static final String CHARSET_LATIN =
        FreeTypeFontGenerator.DEFAULT_CHARS +
            "àèéìòùÀÈÉÌÒÙ" +
            "áéíóúüñ¿¡" +
            "çâêîôûäëïöüÿÇÄÖÜßœŒ" +
            "•●◦·";

    // ---- Charset cirillico ----
    public static final String CHARSET_CYRILLIC =
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
            "•●◦·";

    // ---- FONT LATINI ----
    public static BitmapFont light20;
    public static BitmapFont medium20;
    public static BitmapFont bold25, bold32, bold40, bold60;

    // ---- FONT CIRILLICO ----
    public static BitmapFont cyrillicLight20, cyrillicMedium20, cyrillicBold25, cyrillicBold32, cyrillicBold40,
    cyrillicBold60;

    public static void load() {

        // ---------- GENERATORI LATINI ----------
        FreeTypeFontGenerator genLight =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/instagram_sans/InstagramSans-Light.ttf"));

        FreeTypeFontGenerator genMedium =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/instagram_sans/InstagramSans-Medium.ttf"));

        FreeTypeFontGenerator genBold =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/instagram_sans/InstagramSans-Bold.ttf"));


        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.characters = CHARSET_LATIN;

        // --- LIGHT ---
        p.size = 20; light20 = genLight.generateFont(p);

        // --- MEDIUM ---
        p.size = 20; medium20 = genMedium.generateFont(p);

        // --- BOLD ---
        p.size = 25; bold25 = genBold.generateFont(p);
        p.size = 32; bold32 = genBold.generateFont(p);
        p.size = 40; bold40 = genBold.generateFont(p);
        p.size = 60; bold60 = genBold.generateFont(p);

        // dispose generatori
        genLight.dispose();
        genMedium.dispose();
        genBold.dispose();


        // ---------- GENERATORE CIRILLICO ----------
        FreeTypeFontGenerator genCyrLight =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/inter/Inter-Light.ttf"));

        FreeTypeFontGenerator genCyrMedium =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/inter/Inter-Medium.ttf"));

        FreeTypeFontGenerator genCyrBold =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/inter/Inter-Bold.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter pc = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pc.characters = CHARSET_CYRILLIC;

        // --- LIGHT ---
        pc.size = 20; cyrillicLight20 = genCyrLight.generateFont(pc);

        // --- MEDIUM ---
        pc.size = 20; cyrillicMedium20 = genCyrMedium.generateFont(pc);

        // --- BOLD ---
        pc.size = 25; cyrillicBold25 = genCyrBold.generateFont(pc);
        pc.size = 32; cyrillicBold32 = genCyrBold.generateFont(pc);
        pc.size = 40; cyrillicBold40 = genCyrBold.generateFont(pc);
        pc.size = 60; cyrillicBold60 = genCyrBold.generateFont(pc);

        // dispose generatori
        genCyrLight.dispose();
        genCyrMedium.dispose();
        genCyrBold.dispose();

    }


    // ============================================================
    //                  DRAW MULTILINGUA
    // ============================================================

    public static void draw(SpriteBatch batch, String text, float x, float y, BitmapFont font) {

        if (text == null || font == null) return;

        // Altrimenti usa il font latino passato
        font.draw(batch, text, x, y);
    }


    private static boolean containsCyrillic(String s) {
        for (char c : s.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.CYRILLIC)
                return true;
        }
        return false;
    }
}
