package sorgente;

import com.badlogic.gdx.Gdx;

import java.util.Properties;

public class VersionInfo {

    private static final String version;

    static {
        String temp = "unknown";

        try {
            Properties props = new Properties();
            props.load(Gdx.files.internal("version.properties").read());
            temp = props.getProperty("projectVersion", "unknown");
        } catch (Exception e) {
            System.out.println("Could not load version.properties");
        }

        version = temp;
    }

    public static String getVersion() {
        return version;
    }
}

