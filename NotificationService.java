package src;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NotificationService {
    // Secrets will be fetched from GitHub Environment Variables for security
    private static final String BOT_TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");
    private static final String CHAT_ID = System.getenv("TELEGRAM_CHAT_ID");

    public static void sendPushNotification(String message) {
        if (BOT_TOKEN == null || CHAT_ID == null) {
            System.out.println("Telegram credentials are missing in Environment Variables.");
            return;
        }

        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Adding customized alert parameters
            // Note: To ring for 15 seconds, ensure your Telegram app notification settings are set to 'Long' or 'Urgent'
            String jsonInputString = "{\"chat_id\": \"" + CHAT_ID + "\", \"text\": \"" + message + "\", \"disable_notification\": false}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Notification sent successfully to phone.");
                trigger15SecondAlertSimulation();
            } else {
                System.out.println("Failed to send notification. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error executing notification service: " + e.getMessage());
        }
    }

    private static void trigger15SecondAlertSimulation() {
        System.out.println("Holding alert channel open for 15 seconds...");
        try {
            // Keeps the process running for 15 seconds to sync with the mobile push sound duration
            Thread.sleep(15000);
            System.out.println("Alert duration finished.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
