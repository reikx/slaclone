package slaclone.command;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

public abstract class CommandBase{
    private String name;
    public CommandBase(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void onEvent(SlackMessagePosted event, SlackSession session);

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
