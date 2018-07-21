package de.unhandledexceptions.codersclash.bot.core.reactions;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

/**
 * @author Hax
 * @time 21:37 21.07.2018
 * @project codersclashunhandledexceptionsbot
 * @package de.unhandledexceptions.codersclash.bot.core.reactions
 * @class TestClass
 **/

public class TestClass implements ICommand {

    public long getReactionsLong(User user, Message message) {
        long reactionslong = 0;
        List<MessageReaction> reactions =  message.getReactions();
        for (MessageReaction reaction : reactions) {
            List<User> users = reaction.getUsers().complete();
            for (User selecteduser : users) {
                if (user.equals(selecteduser)) {
                    reactionslong++;
                }
            }
        }
        return reactionslong;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Message message = channel.getMessageById(args[0]).complete();
        channel.sendMessage(String.valueOf(getReactionsLong(event.getMessage().getMentionedUsers().get(0), message))).queue();
    }
}
