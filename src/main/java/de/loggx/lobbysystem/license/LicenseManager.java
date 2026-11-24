package de.loggx.lobbysystem.license;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.loggx.lobbysystem.LobbySystem;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LicenseManager {

    private final LobbySystem plugin;

    public LicenseManager(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public boolean validateLicense() {
        String key = plugin.getConfigManager().getConfig().get("license_key").getAsString();
        String serverUrl = plugin.getConfigManager().getConfig().get("license_server_url").getAsString();

        if (key == null || key.isEmpty() || key.equals("DEINE-LIZENZ-HIER")) {
            return false;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            JsonObject payload = new JsonObject();
            payload.addProperty("licenseKey", key);
            payload.addProperty("plugin", "LobbySystem");
            payload.addProperty("port", plugin.getServer().getPort());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject resJson = JsonParser.parseString(response.body()).getAsJsonObject();
                return resJson.has("valid") && resJson.get("valid").getAsBoolean();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Lizenz-Server nicht erreichbar: " + e.getMessage());
        }
        return false;
    }
}
