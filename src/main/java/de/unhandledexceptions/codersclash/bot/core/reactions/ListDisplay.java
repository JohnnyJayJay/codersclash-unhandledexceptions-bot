package de.unhandledexceptions.codersclash.bot.core.reactions;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Johnny_JayJay
 */

public class ListDisplay {

    public static final List<String> SELECTION_DISPLAY_REACTIONS = List.of(Reactions.YES_EMOTE, Reactions.ARROW_LEFT, Reactions.ARROW_UP,
            Reactions.ARROW_DOWN, Reactions.ARROW_RIGHT, Reactions.NO_EMOTE);
    public static final List<String> DISPLAY_REACTIONS = List.of(Reactions.ARROW_LEFT, Reactions.ARROW_RIGHT, Reactions.NO_EMOTE);
    public static final List<String> SCROLL_DISPLAY_REACTIONS = List.of(Reactions.ARROW_UP, Reactions.ARROW_DOWN, Reactions.YES_EMOTE, Reactions.NO_EMOTE);

    public static void displayList(List<String> list, Message message, User user, int interval) {
        displayList(list, message, user, interval, v -> {});
    }

    public static void displayList(List<String> list, Message message, User user, int interval, Consumer<Void> abort) {
        DISPLAY_REACTIONS.forEach((reaction) -> message.addReaction(reaction).queue());
        display(list, message, user, new EmbedBuilder(), interval, 0, list.size() < interval ? list.size() : interval, pages(list, interval), abort);
    }

    public static void displayListSelection(List<String> list, Message message, User user, int interval, Consumer<String> selected) {
        displayListSelection(list, message, user, interval, selected, v -> {});
    }

    public static void displayListSelection(List<String> list, Message message, User user, int interval, Consumer<String> selected, Consumer<Void> abort) {
        SELECTION_DISPLAY_REACTIONS.forEach((reaction) -> message.addReaction(reaction).queue());
        selectionDisplay(list, message, user, new EmbedBuilder(), interval, 0, list.size() < interval ? list.size() : interval, pages(list, interval), 0, selected, abort);
    }

    public static void displayScrollableListSelection(List<String> list, Message message, String title, Color color, User user, int firstIndex, Consumer<String> selected, Consumer<Void> abort) {
        SCROLL_DISPLAY_REACTIONS.forEach((reaction) -> message.addReaction(reaction).queue());
        scrollableDisplay(list, message, user, new EmbedBuilder().setTitle(title).setColor(color), firstIndex, selected, abort);
    }

    public static void displayScrollableListSelection(List<String> list, Message message, User user, Consumer<String> selected) {
        displayScrollableListSelection(list, message, user, 0, selected, v -> {});
    }

    public static void displayScrollableListSelection(List<String> list, Message message, User user, int firstIndex, Consumer<String> selected, Consumer<Void> abort) {
        displayScrollableListSelection(list, message, "Please choose: " + list.size(), Color.GREEN, user, firstIndex, selected, v -> {});
    }

    private static int pages(List list, int interval) {
        return (list.size() % interval == 0
                ? list.size() / interval
                : list.size() / interval + 1);
    }

    private static void scrollableDisplay(List<String> list, Message message, User user, EmbedBuilder builder, int current, Consumer<String> selected, Consumer<Void> abort) {
        builder.setDescription(Reactions.ARROW_UP + "\n```\n" + list.get(current) + "```" + Reactions.ARROW_DOWN);
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(user, message, (emoji) -> {
            switch (emoji) {
                case Reactions.ARROW_UP:
                    if (current == 0) {
                        scrollableDisplay(list, message, user, builder, list.size() - 1, selected, abort);
                    } else {
                        scrollableDisplay(list, message, user, builder, current - 1, selected, abort);
                    }
                    break;
                case Reactions.ARROW_DOWN:
                    if (current == list.size() - 1) {
                        scrollableDisplay(list, message, user, builder, 0, selected, abort);
                    } else {
                        scrollableDisplay(list, message, user, builder, current + 1, selected, abort);
                    }
                    break;
                case Reactions.YES_EMOTE:
                    selected.accept(list.get(current));
                    break;
                case Reactions.NO_EMOTE:
                    abort.accept(null);
                    break;
            }
        }, SCROLL_DISPLAY_REACTIONS, true);
    }

    private static void selectionDisplay(List<String> list, Message message, User user, EmbedBuilder builder, int interval,
                                         int from, int to, int pages, int current, Consumer<String> selected, Consumer<Void> abort) {
        builder.setDescription("");
        for (int i = from; i < to; i++)
            builder.appendDescription(list.get(i) + (i == current ? Reactions.ARROW_LEFT : "") + "\n");
        int currentPage;
        if (to < list.size()) {
            currentPage = (list.size() % interval == 0
                    ? (list.size() + to) / interval
                    : (list.size() + to) / interval + 1) - pages;
        } else
            currentPage = pages;

        builder.setFooter("Page " + currentPage + " of " + pages, null);
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(user, message, (emoji) -> {
            switch (emoji) {
                case Reactions.ARROW_UP:
                    if (current == from) {
                        if (from == 0) {
                            selectionDisplay(list, message, user, builder, interval, from, to, pages, current, selected, abort);
                        } else if (from - interval >= 0) {
                            selectionDisplay(list, message, user, builder, interval, from - interval, from, pages, current - 1, selected, abort);
                        } else {
                            selectionDisplay(list, message, user, builder, interval, 0, from, pages, current - 1, selected, abort);
                        }
                    } else {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, current - 1, selected, abort);
                    }
                    break;
                case Reactions.ARROW_DOWN:
                    if (current == to - 1) {
                        if (to == list.size()) {
                            selectionDisplay(list, message, user, builder, interval, from, to, pages, current, selected, abort);
                        } else if (to + interval <= list.size()) {
                            selectionDisplay(list, message, user, builder, interval, to, to + interval, pages, current + 1, selected, abort);
                        } else {
                            selectionDisplay(list, message, user, builder, interval, to, list.size(), pages, current + 1, selected, abort);
                        }
                    } else {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, current + 1, selected, abort);
                    }
                    break;
                case Reactions.ARROW_LEFT:
                    if (from == 0) {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, 0, selected, abort);
                    } else if (from - interval >= 0) {
                        selectionDisplay(list, message, user, builder, interval, from - interval, from, pages, current - interval, selected, abort);
                    } else {
                        selectionDisplay(list, message, user, builder, interval, 0, from, pages, (current - interval < 0 ? 0 : current - interval), selected, abort);
                    }
                    break;
                case Reactions.ARROW_RIGHT:
                    if (to == list.size()) {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, list.size() - 1, selected, abort);
                    } else if (to + interval <= list.size()) {
                        selectionDisplay(list, message, user, builder, interval, to, to + interval, pages, current + interval, selected, abort);
                    } else {
                        selectionDisplay(list, message, user, builder, interval, to, list.size(), pages, (current + interval >= list.size() ? list.size() - 1 : current + interval), selected, abort);
                    }
                    break;
                case Reactions.YES_EMOTE:
                    selected.accept(list.get(current));
                    break;
                case Reactions.NO_EMOTE:
                    abort.accept(null);
                    break;
            }
        }, SELECTION_DISPLAY_REACTIONS, true);
    }

    private static void display(List<String> list, Message message, User user, EmbedBuilder builder, int interval, int from, int to, int pages, Consumer<Void> abort) {
        builder.setDescription("");
        for (int i = from; i < to; i++)
            builder.appendDescription(list.get(i) + "\n");
        int currentPage;
        if (to < list.size()) {
            currentPage = (list.size() % interval == 0
                    ? (list.size() + to) / interval
                    : (list.size() + to) / interval + 1) - pages;
        } else
            currentPage = pages;

        builder.setFooter("Page " + currentPage + " of " + pages, null);
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(user, message, (emoji) -> {
            if (emoji.equals(Reactions.ARROW_RIGHT)) {
                if (to == list.size()) {
                    display(list, message, user, builder, interval, from, to, pages, abort);
                } else if (to + interval <= list.size()) {
                    display(list, message, user, builder, interval, to, to + interval, pages, abort);
                } else {
                    display(list, message, user, builder, interval, to, list.size(), pages, abort);
                }
            } else if (emoji.equals(Reactions.ARROW_LEFT)) {
                if (from == 0) {
                    display(list, message, user, builder, interval, from, to, pages, abort);
                } else if (from - interval >= 0) {
                    display(list, message, user, builder, interval, from - interval, from, pages, abort);
                } else {
                    display(list, message, user, builder, interval, 0, from, pages, abort);
                }
            } else if (emoji.equals(Reactions.NO_EMOTE)) {
                abort.accept(null);
            }
        }, DISPLAY_REACTIONS, true);
    }

}
