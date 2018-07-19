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
    private final List<String> chooseGameList = List.of(Reactions.getNumber(3), Reactions.getNumber(5), Reactions.getNumber(9), Reactions.NO_EMOTE);

    public TicTacToeCommand(TicTacToe game) {
        this.game = game;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (args.length > 0 && !mentionedMembers.isEmpty()) {
            var target = mentionedMembers.get(0);
            Messages.sendMessage(channel, Messages.Type.QUESTION, "How big should the game be?").queue((msg) -> {
                chooseGameList.forEach((reaction) -> msg.addReaction(reaction).queue());
                Reactions.newMenu(member.getUser(), msg, (emoji) -> {
                    msg.delete().queue();
                    if (emoji.equals(Reactions.getNumber(3))) {
                        waitForResponse(channel, member, target, 3);
                    } else if (emoji.equals(Reactions.getNumber(5))) {
                        waitForResponse(channel, member, target, 5);
                    } else if (emoji.equals(Reactions.getNumber(9))) {
                        waitForResponse(channel, member, target, 9);
                    } else if (emoji.equals(Reactions.NO_EMOTE)) {
                        msg.delete().queue();
                    }
                }, chooseGameList);
            });

        } else {
            Messages.wrongUsageMessage(event.getMessage(), channel, member, this);
        }
    }

    private void waitForResponse(TextChannel channel, Member player1, Member player2, int size) {
        Reactions.newYesNoMenu(player2.getUser(), channel, "Do you want to play against " + player1.getAsMention() + ", " + player2.getAsMention() + "?", (msg) -> {
            Messages.sendMessage(channel, Messages.Type.NO_TYPE, "Starting game...").queue((msg2) -> game.start(msg2, player1, player2, size));
        }, true);
    }
}
