package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.game.TicTacToe;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class TicTacToeCommand implements ICommand {

    private TicTacToe game;

    public TicTacToeCommand(TicTacToe game) {
        this.game = game;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (args.length > 0 && !mentionedMembers.isEmpty()) {
            var target = mentionedMembers.get(0);
            Reactions.newYesNoMenu(target.getUser(), channel, "Do you want to play against " + member.getAsMention() + ", " + target.getAsMention() + "?", (msg) -> {
                Messages.sendMessage(channel, Messages.Type.NO_TYPE, "Starting game...").queue((msg2) -> game.start(msg2, member, target));
            }, true);
        } else {
            Messages.wrongUsageMessage(channel, member, this);
        }
    }
}
