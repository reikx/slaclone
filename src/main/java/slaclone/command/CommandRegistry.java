package slaclone.command;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import slaclone.sys.Logger;

import java.util.HashMap;

public class CommandRegistry {
    private static final HashMap<String,CommandBase> commandBases = new HashMap<>();
    public static void register(CommandBase commandBase) {
        commandBases.put(commandBase.getName(),commandBase);
    }

    public static void run(String command,SlackMessagePosted event, SlackSession session){
        for (String name:commandBases.keySet()){
            System.out.println(command);
            if(name.equals(command)){
                Logger.info("command run -> "+ name);
                commandBases.get(name).onEvent(event,session);
                break;
            }
        }
    }
}
