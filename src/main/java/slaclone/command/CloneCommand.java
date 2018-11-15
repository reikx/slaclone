package slaclone.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import slaclone.Main;
import slaclone.util.SlaclonelUtil;

import java.util.Collection;

public class CloneCommand extends CommandBase{
    public CloneCommand() {
        super("clone");
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        String args[] = event.getMessageContent().split(" ");
        if(args.length == 2){
            String channelName = args[1];
            CloneState.updateListener(channelName);
        }
    }
}
