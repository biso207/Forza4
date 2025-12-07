package sorgente.LogInSignUp;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NotificaAvvio
{
    public void sendMessage()
    {
        String token = "8105519085:AAEcmzhYSLOmn0qSASe8YCD_UfYi_eDsTM8"; // token del bot
        String chatId = "5191176873"; // chat_id

        try
        {
            String message = AuthAlgorithms.nickname + " ha avviato il gioco"; // messaggio
            String urlString = "https://api.telegram.org/bot" + token + "/sendMessage" +
                "?chat_id=" + chatId +
                "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.getResponseCode(); // fondamentale per il ricevimento del messaggio
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
