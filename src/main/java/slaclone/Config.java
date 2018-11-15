package slaclone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;

public class Config {
    private static ObjectNode configJson;
    public static final String WATCHER_TOKEN = "watcher-token";
    public static String WATCHER_CHANNEL = "watcher-channel";
    public static final String CLONER_TOKEN = "cloner-token";
    public static String CLONER_CHANNEL = "cloner-channel";
    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            configJson = (ObjectNode)mapper.readTree(new File("./config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rewrite(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("./config.json"),configJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ObjectNode getConfig() {
        return configJson;
    }
}
