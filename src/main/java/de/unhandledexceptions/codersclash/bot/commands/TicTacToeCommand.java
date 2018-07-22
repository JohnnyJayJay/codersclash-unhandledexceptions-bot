package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.game.TicTacToe;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */
public class TicTacToeCommand implements ICommand {

    private TicTacToe game;
    private final List<String> chooseGameList = List.of(Reactions.getNumber(3), Reactions.getNumber(5), Reactions.getNumber(9), Reactions.NO_EMOTE);

    public TicTacToeCommand(TicTacToe game) {
        this.game = game;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE))
            return;

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (args.length > 0 && !mentionedMembers.isEmpty()) {
            var target = mentionedMembers.get(0);
            Main.otherThread(() -> Messages.sendMessage(channel, Messages.Type.QUESTION, "How big should the game be?").queue((msg) -> {
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
            }));
        } else {
            Messages.wrongUsageMessage(channel, member, this);
        }
    }

    private void waitForResponse(TextChannel channel, Member player1, Member player2, int size) {
        Reactions.newYesNoMenu(player2.getUser(), channel, "Do you want to play against " + player1.getAsMention() + ", " + player2.getAsMention() + "?", (msg) -> {
            Messages.sendMessage(channel, Messages.Type.NO_TYPE, "Starting game...").queue((msg2) -> game.start(msg2, player1, player2, size));
        }, true);
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        String ret = format("**Description**: Starts a game of Tic-Tac-Toe.\n\n" +
                "**Usage**: `%s[ttt|tictactoe] @Member`\n\n**Permission level**: `0`", prefix, prefix);
        return ret;
    }
}