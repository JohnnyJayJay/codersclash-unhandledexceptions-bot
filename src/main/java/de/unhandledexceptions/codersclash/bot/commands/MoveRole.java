package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class MoveRole implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (event.getCommand().getLabel().equalsIgnoreCase("moverole")) {
            if (event.getMessage().getMentionedRoles().isEmpty() || args.length < 2)
                return;

            var role = event.getMessage().getMentionedRoles().get(0);
            if (args[1].matches("\\d+")) {
                int pos = Integer.parseInt(args[1]);
                event.getGuild().getController().modifyRolePositions().selectPosition(role).moveTo(pos).queue();
            } else if (event.getMessage().getMentionedRoles().size() == 2) {
                var role2 = event.getMessage().getMentionedRoles().get(1);
                event.getGuild().getController().modifyRolePositions().selectPosition(role).moveTo(role2.getPosition()).queue();
            }
        } else if (args.length > 1 && !event.getGuild().getRolesByName(event.getCommand().getJoinedArgs(), false).isEmpty()) {
            var role = event.getGuild().getRolesByName(event.getCommand().getJoinedArgs(), false).get(0);
            role.getManager().setMentionable(true).queue();
        }
    }
}
