package slaclone.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import slaclone.Main;
import slaclone.Config;

import java.io.IOException;

public class SlaclonelUtil{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static JsonNode getProfile(SlackUser user){
        String resp = HttpUtil.get("https://slack.com/api/users.info?token=" + Config.getConfig().get(Config.WATCHER_TOKEN).asText() + "&user=" + user.getId());
        System.out.println(resp);

        if(resp == null)return null;
        try {
            JsonNode node = objectMapper.readTree(resp);
            if(node == null || !node.get("ok").asBoolean())return null;
            return node.get("user").get("profile");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonNode makeFilePublic(String fileId){
        System.out.println(Config.getConfig().get(Config.WATCHER_TOKEN).asText());
        String resp = HttpUtil.post(null,"https://slack.com/api/files.sharedPublicURL?" + ("token=" + Config.getConfig().get(Config.WATCHER_TOKEN).asText() + "&file=" + fileId),"application/json");
        if(resp == null)return null;
        try {
            JsonNode node = objectMapper.readTree(resp);
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonNode makeFilePrivate(String fileId){
        String resp = HttpUtil.post("token=" + Config.getConfig().get(Config.WATCHER_TOKEN).asText() + "&file=" + fileId,"https://slack.com/api/files.sharedPublicURL","application/json");
        if(resp == null)return null;
        try {
            JsonNode node = objectMapper.readTree(resp);
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUserDisplayName(SlackUser user){
        if(user == null)return null;
        JsonNode profile = getProfile(user);
        if(profile != null) {
            String disp = profile.get("display_name").asText();
            if (disp == null || disp.equals("")) {
                disp = profile.get("real_name").asText();
            }
            return disp;
        }
        return null;
    }

    public static String parseMessageSafe(String message){
        while (message.contains("<@")){
            int i = message.indexOf("<@");
            String id = message.substring(i + 2,i + 11);
            String disp = SlaclonelUtil.getUserDisplayName(Main.getWatcherSession().findUserById(id));
            if(disp != null) {
                message = message.replace("<@" + id + ">","@" + disp);
            }
            else{
                message = message.replace("<@" + id + ">",id);
            }
        }
        while (message.contains("<!here>")){
            message = message.replace("<!here>","@here");
        }
        while (message.contains("<!everyone>")){
            message = message.replace("<!everyone>","@here");
        }
        while (message.contains("<!channel>")){
            message = message.replace("<!channel>","@channel");
        }
        return message;
    }

}
