package de.unhandledexceptions.codersclash.bot.game;

import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class TicTacToe {

    private final List<String> reactions = List.of(Reactions.ARROW_DOWN, Reactions.ARROW_UP, Reactions.SMALL_ARROW_LEFT, Reactions.SMALL_ARROW_RIGHT, Reactions.YES_EMOTE, Reactions.NO_EMOTE);
    private final String BLANK_FIELD = "◻";
    private final String SELECTED = "\uD83D\uDD37";
    private final String X = "\uD83C\uDDFD";
    private final String O = "\uD83D\uDD34";

    public void start(Message message, Member player1, Member player2) {
        boolean first = ThreadLocalRandom.current().nextBoolean();
        game(new EmbedBuilder(), new String[][]{
                {SELECTED, BLANK_FIELD, BLANK_FIELD},
                {BLANK_FIELD, BLANK_FIELD, BLANK_FIELD},
                {BLANK_FIELD, BLANK_FIELD, BLANK_FIELD}
        }, message, first ? player1 : player2, first ? player2 : player1, 0, 0);
    }

    private void game(EmbedBuilder builder, String[][] fields, Message message, Member current, Member next, int currentRow, int currentColumn) {
        builder.setDescription("");
        for (String[] row : fields) {
            for (String field : row) {
                builder.appendDescription(field);
            }
            builder.appendDescription("\n");
        }
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(current.getUser(), message, (emoji) -> {
            switch (emoji) {
                case Reactions.ARROW_DOWN:
                    if (currentRow == 2) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        fields[currentRow][currentColumn] = BLANK_FIELD;
                        fields[currentRow - 1][currentColumn] = SELECTED;
                        game(builder, fields, message, current, next, currentRow - 1, currentColumn);
                    }
                    break;
                case Reactions.ARROW_UP:
                    if (currentRow == 0) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        fields[currentRow][currentColumn] = BLANK_FIELD;
                        fields[currentRow + 1][currentColumn] = SELECTED;
                        game(builder, fields, message, current, next, currentRow + 1, currentColumn);
                    }
                    break;
                case Reactions.SMALL_ARROW_LEFT:
                    if (currentColumn == 0) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        fields[currentRow][currentColumn] = BLANK_FIELD;
                        fields[currentRow][currentColumn - 1] = SELECTED;
                        game(builder, fields, message, current, next, currentRow, currentColumn - 1);
                    }
                    break;
                case Reactions.SMALL_ARROW_RIGHT:
                    if (currentColumn == 2) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        fields[currentRow][currentColumn] = BLANK_FIELD;
                        fields[currentRow][currentColumn + 1] = SELECTED;
                        game(builder, fields, message, current, next, currentRow, currentColumn + 1);
                    }
                    break;
                case Reactions.YES_EMOTE:
                    if (fields[currentRow][currentColumn].equals(X) || fields[currentRow][currentColumn].equals(O)) {
                        game(builder, fields, message, current, next, currentRow, currentColumn);
                    } else {
                        // TODO
                        fields[currentRow][currentColumn]
                    }
                case Reactions.NO_EMOTE:
            }
        }, reactions);
    }
}
