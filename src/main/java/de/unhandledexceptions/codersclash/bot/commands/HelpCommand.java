package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.CommandSettingsHandler;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static java.lang.String.format;
import static java.lang.String.join;

/**
 * @author Hax
 * @time 20:26 21.07.2018
 * @project codersclashunhandledexceptionsbot
 * @package de.unhandledexceptions.codersclash.bot.commands
 * @class HelpCommand
 **/

public class HelpCommand implements ICommand {

    CommandSettingsHandler commandSettingsHandler;

    public HelpCommand(CommandSettingsHandler commandSettingsHandler) {
        this.commandSettingsHandler = commandSettingsHandler;
    }

    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        EmbedBuilder builder = new EmbedBuilder();
        if (strings.length==0) {
            String helpLabels = format("[%s]", join("|", commandSettingsHandler.getCommandSettings().getHelpLabelSet()));
            String prefix = commandSettingsHandler.getCommandSettings().getPrefix(member.getGuild().getIdLong());

            StringBuilder labels = new StringBuilder();
            for (int i = 0; commandSettingsHandler.getCommands().size() > i; i++) {
                labels.append(commandSettingsHandler.getCommandSettings().getPrefix(commandEvent.getGuild().getIdLong()) + commandSettingsHandler.getLabelFromCommand().get(commandSettingsHandler.getCommands().get(i)) + (commandSettingsHandler.getCommands().size() - 1 != i ? ", " : ""));
            }
            builder.setColor(commandEvent.getGuild().getSelfMember().getColor())
                    .addField("Commands", format("```\n%s```", join(format(", %s", prefix), labels.toString())), true);
            sendMessage(textChannel, Messages.Type.NO_TYPE,
                    format("To learn more about a specific command, just call\n`%s[help|helpme|commands] <label>`.\nThe following commands are currently available:", prefix), "Help", false, builder).queue();
        } else if (strings.length == 1 && commandSettingsHandler.getCommandfromLabel().containsKey(strings[0])) {
            builder.setColor(commandEvent.getGuild().getSelfMember().getColor())
                    .appendDescription(format("**Command Info for:** `%s`\n\n", strings[0]))
                    .appendDescription(commandSettingsHandler.getCommandfromLabel().get(strings[0]).info(member));
            sendMessage(textChannel, Messages.Type.NO_TYPE, "", "Help", false, builder).queue();
        }
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        String ret = format("**Description**: Provides you with help.\n\n" +
                "**Usage**: `%s[help|helpme|command]`\n\n**Permission level**: `0`", prefix);
        return ret;
    }
}
