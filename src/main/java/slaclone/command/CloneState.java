package slaclone.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import slaclone.Main;
import slaclone.Config;
import slaclone.sys.Logger;
import java.io.*;

import slaclone.util.HttpUtil;
import slaclone.util.SlaclonelUtil;



public class CloneState {
    private static SlackMessagePostedListener nowListener;
    private static final ObjectMapper MAPPER = new ObjectMapper();



    public static void updateListener(String channelName){
        SlackChannel channel = Main.getWatcherSession().findChannelByName(channelName);
        final SlackChannel clonerChannel = Main.getClonerSession().findChannelByName(Config.getConfig().get(Config.CLONER_CHANNEL).asText());
        if(channel == null){
            Main.getClonerSession().sendMessage(clonerChannel,"Couldn't find channel:" + channelName);
            return;
        }
        SlackTeam wTeam = Main.getWatcherSession().getTeam();
        if(nowListener != null){
            Main.getWatcherSession().removeMessagePostedListener(nowListener);
            Main.getClonerSession().sendMessage(clonerChannel,"stopped cloning -> #" + Config.getConfig().get(Config.WATCHER_CHANNEL).asText() + " at " + wTeam.getName() + "(" + wTeam.getDomain() + ")");
        }
        nowListener = (event1, session1) -> {
            if(!event1.getChannel().getId().equals(channel.getId()))return;
            if(event1.getSender().getId().equals(Main.getClonerSession().sessionPersona().getId()))return;
            JsonNode profile = SlaclonelUtil.getProfile(event1.getUser());
            SlackChatConfiguration configuration = null;
            String displayName = "";
            File renamed = null;
            if(profile != null) {
                displayName = profile.get("display_name").asText();
                if (displayName == null || displayName.equals("")) {
                    displayName = profile.get("real_name").asText();
                }
                String icon = profile.get("image_512").asText();
                configuration = SlackChatConfiguration.getConfiguration().withIcon(icon).withName(displayName);
            }
            String message = SlaclonelUtil.parseMessageSafe(event1.getMessageContent());

            System.out.println(event1);
            JsonNode node = null;
            try {
                node = MAPPER.readTree(event1.getJsonSource());
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] fileBinary = null;
            String fileName = null;
            if(node != null&&node.has("files")){
                String id = node.get("files").get(0).get("id").asText();
                fileName = node.get("files").get(0).get("name").asText();
                JsonNode fileProp = SlaclonelUtil.makeFilePublic(id);
                if(fileProp != null) {
                    String urlPublic = fileProp.get("file").get("permalink_public").asText();
                    String urlPrivate = fileProp.get("file").get("url_private").asText();
                    String s1s[] = urlPublic.split("/");
                    String uid = s1s[s1s.length - 1];
                    String s2s[] = uid.split("-");
                    String imageUrl = urlPrivate + "?pub_secret=" + s2s[s2s.length - 1];
                    File file = HttpUtil.getFile(imageUrl);
                    if(file != null){
                        renamed = new File(file.getParent() + "/" + fileName);
                    }
                    if(renamed != null&&file.renameTo(renamed)){
                        try {
                            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(renamed));
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            while (stream.read(buf,0,buf.length) > 0){
                                bos.write(buf);
                            }
                            fileBinary = bos.toByteArray();
                            stream.close();
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    SlaclonelUtil.makeFilePrivate(id);
                }
            }

            Main.getClonerSession().sendMessage(clonerChannel,new SlackPreparedMessage.Builder().withMessage(message).build(),configuration);
            if(fileBinary != null){
                Main.getClonerSession().sendFile(clonerChannel,fileBinary,fileName,fileName,"from:" + displayName);
                renamed.delete();
            }
        };

        Main.getWatcherSession().addMessagePostedListener(nowListener);
        Config.getConfig().put(Config.WATCHER_CHANNEL,channel.getName());
        Config.rewrite();
        Main.getClonerSession().sendMessage(clonerChannel,"started cloning -> #" + channel.getName() + " at " + wTeam.getName() + "(" + wTeam.getDomain() + ")");
    }

}

