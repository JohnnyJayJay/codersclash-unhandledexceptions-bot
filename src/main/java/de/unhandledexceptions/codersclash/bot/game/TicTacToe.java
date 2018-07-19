package de.unhandledexceptions.codersclash.bot.game;

import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class TicTacToe {

    private final List<String> REACTIONS = List.of(Reactions.ARROW_UP, Reactions.ARROW_DOWN, Reactions.SMALL_ARROW_LEFT, Reactions.SMALL_ARROW_RIGHT, Reactions.YES_EMOTE, Reactions.NO_EMOTE);
    private final String BLANK_FIELD = "◻";
    private final String SELECTED = "\uD83D\uDD37";
    private final String X = "\uD83C\uDDFD";
    private final String O = "\uD83D\uDD34";

    public void start(Message message, Member player1, Member player2) {
        REACTIONS.forEach((reaction) -> message.addReaction(reaction).queue());
        boolean first = ThreadLocalRandom.current().nextBoolean();
        var current = new SimpleEntry<Member, String>(first ? player1 : player2, first ? X : O);
        var next = new SimpleEntry<Member, String>(first ? player2 : player1, first ? O : X);
        String[][] fields = new String[3][3]; // TODO dynamisch user entscheiden lassen
        for (String[] row : fields)
            Arrays.fill(row, BLANK_FIELD);
        game(new EmbedBuilder().setTitle(current.getKey().getEffectiveName() + " " + current.getValue() + " vs. " + next.getKey().getEffectiveName() + " " + next.getValue()), fields, message, current, next, 0, 0);
    }

    private void game(EmbedBuilder builder, String[][] fields, Message message, SimpleEntry<Member, String> current, SimpleEntry<Member, String> next, int currentRow, int currentColumn) {
        builder.setDescription(current.getKey().getAsMention() + ", it's your turn!\n");
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                builder.appendDescription(i == currentRow && j == currentColumn ? SELECTED : fields[i][j]);
            }
            builder.appendDescription("\n");
        }
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(current.getKey().getUser(), message, (emoji) -> {
            switch (emoji) {
                case Reactions.ARROW_DOWN:
                    game(builder, fields, message, current, next, currentRow == 2 ? currentRow : currentRow + 1, currentColumn);
                    break;
                case Reactions.ARROW_UP:
                    game(builder, fields, message, current, next, currentRow == 0 ? currentRow : currentRow - 1, currentColumn);
                    break;
                case Reactions.SMALL_ARROW_LEFT:
                    game(builder, fields, message, current, next, currentRow, currentColumn == 0 ? currentColumn : currentColumn - 1);
                    break;
                case Reactions.SMALL_ARROW_RIGHT:
                    game(builder, fields, message, current, next, currentRow, currentColumn == 2 ? currentColumn : currentColumn + 1);
                    break;
                case Reactions.YES_EMOTE:
                    if (fields[currentRow][currentColumn].equals(X) || fields[currentRow][currentColumn].equals(O)) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        // TODO
                        fields[currentRow][currentColumn] = current.getValue();
                        boolean won = false;
                        for (String[] row : fields) {
                            if (Arrays.stream(row).allMatch(current.getValue()::equals)) {
                                won = true;
                            }
                        }
                        if (!won) {
                            boolean diagonal = (fields[0][0].equals(current.getValue()) && fields[1][1].equals(current.getValue()) && fields[2][2].equals(current.getValue())) ||
                                    (fields[0][2].equals(current.getValue()) && fields[1][1].equals(current.getValue()) && fields[2][0].equals(current.getValue()));
                            if (diagonal)
                                won = true;
                        }
                        if (won) {
                            message.editMessage(builder.clear().setDescription(current.getKey().getEffectiveName() + " gewinnt!").setColor(current.getKey().getColor()).setFooter("Yay", null).build()).queue();
                        } else if (Arrays.stream(fields).allMatch((row) -> Arrays.stream(row).noneMatch(BLANK_FIELD::equals))) {
                            message.editMessage(builder.clear().setDescription("Unentschieden!").setFooter("Nay", null).build()).queue();
                        } else {
                            game(builder, fields, message, next, current, 0, 0);
                        }
                    }
                    break;
                case Reactions.NO_EMOTE:
                    Messages.sendMessage(message.getChannel(), Messages.Type.INFO, "Member `" + current.getKey().getEffectiveName() + "` cancelled the game.").queue((msg) -> message.delete().queue());
                    break;
            }
        }, REACTIONS);
    }

    private boolean 
}
