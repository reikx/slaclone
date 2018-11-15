package slaclone;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import slaclone.command.CloneCommand;
import slaclone.command.CloneState;
import slaclone.command.CommandRegistry;

import java.io.IOException;

public class Main {
    private static SlackSession clonerSession;
    private static SlackSession watcherSession;
    private static boolean isInitialized = false;
    public static void main(String args[]){
        clonerSession = SlackSessionFactory.createWebSocketSlackSession(Config.getConfig().get(Config.CLONER_TOKEN).asText());

        clonerSession.addMessagePostedListener(((event, session) -> {
            if(!event.getChannel().getName().equals(Config.getConfig().get(Config.CLONER_CHANNEL).asText()))return;
            if(!event.getMessageContent().startsWith("!"))return;
            String command = event.getMessageContent().substring(1,event.getMessageContent().indexOf(' '));
            command = command.length() == 0 ? event.getMessageContent() : command;
            CommandRegistry.run(command,event,session);
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SlackChannel channel = clonerSession.findChannelByName(Config.getConfig().get(Config.CLONER_CHANNEL).asText());
            if(channel == null)return;
            clonerSession.sendMessage(channel,"Clone system was stopped,see ya again!");
        }));

        clonerSession.addSlackConnectedListener(((event, session) -> {
            if(watcherSession.isConnected())init();
        }));

        watcherSession = SlackSessionFactory.createWebSocketSlackSession(Config.getConfig().get(Config.WATCHER_TOKEN).asText());

        watcherSession.addSlackConnectedListener(((event, session) -> {
            if(clonerSession.isConnected())init();
        }));

        new Thread(() -> {
            try {
                clonerSession.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        new Thread(() -> {
            try {
                watcherSession.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        CommandRegistry.register(new CloneCommand());
    }

    public static SlackSession getClonerSession() {
        return clonerSession;
    }

    private synchronized static void init(){
        if(isInitialized)return;
        SlackChannel channel = clonerSession.findChannelByName(Config.getConfig().get(Config.CLONER_CHANNEL).asText());
        if(channel == null)return;
        clonerSession.sendMessage(channel,"Clone system working now!");
        CloneState.updateListener(Config.getConfig().get(Config.WATCHER_CHANNEL).asText());
        isInitialized = true;
    }

    public static SlackSession getWatcherSession() {
        return watcherSession;
    }
}
