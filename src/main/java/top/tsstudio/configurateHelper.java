package top.tsstudio;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;

public class configurateHelper {
    public int tcpPort = 38383;
    public String networkID = "00";
    public Map<String, String> scenes = new HashMap<>();
    public String obsAddress = "localhost";
    public int obsPort = 4455;
    public boolean obsPasswordEnabled = false;
    public String obsPassword = "";
    public int obsTimeout = 5;

    public JsonElement readJsonFromFile(String filepath) {
        JsonElement jsonElement = null;
        try {
            FileReader reader = new FileReader(filepath);
            jsonElement = JsonParser.parseReader(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonElement;
    }

    public configurateHelper(String filepath) {
        //read from file
        JsonElement jElement = this.readJsonFromFile(filepath);
        if (jElement != null && jElement.isJsonObject()) {
            JsonObject jsonObject = jElement.getAsJsonObject();
            JsonObject tallyServerObject = jsonObject.getAsJsonObject("tally-server");
            if (tallyServerObject != null) {
                this.tcpPort = tallyServerObject.get("port").getAsInt();
                this.networkID = tallyServerObject.get("network-id").getAsString();
                JsonObject scenesObject = tallyServerObject.getAsJsonObject("scenes");
                if (scenesObject != null) {
                    for (Map.Entry<String, JsonElement> entry : scenesObject.entrySet()) {
                        this.scenes.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
            JsonObject uStreamServer = jsonObject.getAsJsonObject("upstream-server");
            if (uStreamServer != null) {
                if (uStreamServer.has("type") && uStreamServer.get("type").getAsString().equals("obs-websocket")) {
                    this.obsAddress = uStreamServer.get("host").getAsString();
                    this.obsPort = uStreamServer.get("port").getAsInt();
                    if (uStreamServer.has("password")) {
                        this.obsPasswordEnabled = true;
                        this.obsPassword = uStreamServer.get("password").getAsString();
                    }
                    this.obsTimeout = uStreamServer.get("timeout").getAsInt();
                }
            }

        }

    }

    public configurateHelper() {
        //default values with nothing
    }

}
