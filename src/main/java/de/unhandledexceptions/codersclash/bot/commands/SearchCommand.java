package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

public class SearchCommand implements ICommand {

    public final List<String> SELECTION_DISPLAY_REACTIONS = List.of(Reactions.ARROW_UP, Reactions.ARROW_DOWN,
            Reactions.DOUBLE_ARROW_UP, Reactions.DOUBLE_ARROW_DOWN, Reactions.YES_EMOTE, Reactions.NO_EMOTE);
    public final List<String> DISPLAY_REACTIONS = List.of(Reactions.DOUBLE_ARROW_UP, Reactions.DOUBLE_ARROW_DOWN, Reactions.NO_EMOTE);
    public final Pattern FIND_ID = Pattern.compile("\\(\\d+\\)");

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        // TODO Permission level
        if (args.length > 1) {
            var shardmanager = event.getJDA().asBot().getShardManager();
            var builder = new EmbedBuilder();
            if (args[0].equalsIgnoreCase("user")){
                sendMessage(channel, Type.DEFAULT, "Searching...").queue((msg) -> {
                    String name = event.getCommand().getJoinedArgs(1);
                    List<String> display = find(shardmanager, name.toLowerCase(), true);
                    msg.delete().queue();
                    if (display.isEmpty()) {
                        sendMessage(channel, Type.ERROR, "Unfortunately, I didn't find what you were looking for.", "No Results").queue(Messages::deleteAfterFiveSec);
                    } else {
                        builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                        sendMessage(channel, Type.SUCCESS, "Loading results...")
                                .queue((m) -> {
                                    DISPLAY_REACTIONS.forEach((reaction) -> m.addReaction(reaction).queue());
                                    display(display, m, event.getAuthor(), builder, 10, 0, display.size() >= 10 ? 10 : display.size(), pages(display, 10));
                                }, Messages.defaultFailure(channel));
                    }
                });

            } else if (args[0].equalsIgnoreCase("guild")) {
                String name = String.join(" ", Arrays.asList(args).subList(1, args.length));
                List<String> display = find(shardmanager, name.toLowerCase(), false);
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((m) -> {
                    DISPLAY_REACTIONS.forEach((reaction) -> m.addReaction(reaction).queue());
                    display(display, m, event.getAuthor(), builder, 10, 0, display.size() >= 10 ? 10 : display.size(), pages(display, 10));
                }, Messages.defaultFailure(channel));
            } else if (args[0].equalsIgnoreCase("display") && args[1].matches("(?i)((guilds)|(users))")) {
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((msg) -> {
                    List<String> display = args[1].equalsIgnoreCase("guilds")
                            ? shardmanager.getGuildCache().stream().map((guild) -> String.format("%s (%d)", guild.getName(), guild.getIdLong())).collect(Collectors.toList())
                            : shardmanager.getUserCache().stream().map((user) -> String.format("%#s (%d)", user, user.getIdLong())).collect(Collectors.toList());
                    SELECTION_DISPLAY_REACTIONS.forEach((reaction) -> msg.addReaction(reaction).queue());
                    selectionDisplay(display, msg, event.getAuthor(), builder, 15, 0, display.size() >= 15 ? 15 : display.size(), pages(display, 15), 0, (id) -> {});
                });
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            wrongUsageMessage(channel, member, this);
        }
    }

    public List<String> find(ShardManager shardmanager, String name, boolean searchUser) {
        List<String> ret = new ArrayList<>();
        if (searchUser) {
            List<User> withDiscriminator = Collections.EMPTY_LIST;
            List<User> users = new ArrayList<>();
            if (name.matches(".*#\\d{4}")) {
                String newName = name.replaceAll("#\\d{4}", "");
                String discriminator = name.replace(newName + "#", "");
                name = newName;
                String finalName = name;
                shardmanager.getUserCache().stream().filter((user) -> user.getName().equalsIgnoreCase(finalName)).filter((user) -> user.getDiscriminator().equals(discriminator)).forEach(users::add);
                withDiscriminator = shardmanager.getUserCache().stream().filter((user) -> user.getDiscriminator().equals(discriminator)).collect(Collectors.toList());
            }
            String finalName = name;
            for (var jda : shardmanager.getShardCache()) {
                users.addAll(jda.getUsersByName(finalName, true));
            }
            shardmanager.getUserCache().stream().filter((user) -> user.getName().toLowerCase().startsWith(finalName) && !user.getName().equalsIgnoreCase(finalName)).forEach(users::add);
            if (users.isEmpty()) {
                shardmanager.getUserCache().stream().filter((user) -> user.getName().toLowerCase().contains(finalName)).forEach(users::add);
                users.addAll(withDiscriminator);
            }
            users.stream().map((user) -> String.format("%#s (%d)", user, user.getIdLong())).forEach(ret::add);
        } else {
            List<Guild> guilds = new ArrayList<>();
            for (var jda : shardmanager.getShardCache()) {
                guilds.addAll(jda.getGuildsByName(name, true));
            }
            String finalName = name;
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().startsWith(finalName) && !guild.getName().equalsIgnoreCase(finalName)).forEach(guilds::add);
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().contains(finalName) && !guild.getName().equalsIgnoreCase(finalName)).forEach(guilds::add);
            guilds.stream().map((guild) -> String.format("%s (%d)", guild.getName(), guild.getIdLong())).forEach(ret::add);
        }
        return ret;
    }

    public int pages(List list, int interval) {
        return (list.size() % interval == 0
                ? list.size() / interval
                : list.size() / interval + 1);
    }

    public void selectionDisplay(List<String> list, Message message, User user, EmbedBuilder builder, int interval, int from, int to, int pages, int current, Consumer<String> selected) {
        builder.setDescription("**Results for your search: " + list.size() + "**\n"
                + "");
        for (int i = from; i < to; i++)
            builder.appendDescription((i + 1) + ": `" + list.get(i) + "` " + (i == current ? Reactions.ARROW_LEFT : "") + "\n");
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
                            selectionDisplay(list, message, user, builder, interval, from, to, pages, current, selected);
                        } else if (from - interval >= 0) {
                            selectionDisplay(list, message, user, builder, interval, from - interval, from, pages, current - 1, selected);
                        } else {
                            selectionDisplay(list, message, user, builder, interval, 0, from, pages, current - 1, selected);
                        }
                    } else {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, current - 1, selected);
                    }
                    break;
                case Reactions.ARROW_DOWN:
                    if (current == to - 1) {
                        if (to == list.size()) {
                            selectionDisplay(list, message, user, builder, interval, from, to, pages, current, selected);
                        } else if (to + interval <= list.size()) {
                            selectionDisplay(list, message, user, builder, interval, to, to + interval, pages, current + 1, selected);
                        } else {
                            selectionDisplay(list, message, user, builder, interval, to, list.size(), pages, current + 1, selected);
                        }
                    } else {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, current + 1, selected);
                    }
                    break;
                case Reactions.DOUBLE_ARROW_UP:
                    if (from == 0) {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, 0, selected);
                    } else if (from - interval >= 0) {
                        selectionDisplay(list, message, user, builder, interval, from - interval, from, pages, current - interval, selected);
                    } else {
                        selectionDisplay(list, message, user, builder, interval, 0, from, pages, (current - interval < 0 ? 0 : current - interval), selected);
                    }
                    break;
                case Reactions.DOUBLE_ARROW_DOWN:
                    if (to == list.size()) {
                        selectionDisplay(list, message, user, builder, interval, from, to, pages, list.size() - 1, selected);
                    } else if (to + interval <= list.size()) {
                        selectionDisplay(list, message, user, builder, interval, to, to + interval, pages, current + interval, selected);
                    } else {
                        selectionDisplay(list, message, user, builder, interval, to, list.size(), pages, (current + interval >= list.size() ? list.size() - 1 : current + interval), selected);
                    }
                    break;
                case Reactions.YES_EMOTE:
                    selected.accept(list.get(current));
                    break;
            }
        }, SELECTION_DISPLAY_REACTIONS);
    }

    private void display(List<String> list, Message message, User user, EmbedBuilder builder, int interval, int from, int to, int pages) {
        builder.setDescription("**Results for your search: " + list.size() + "**\n");
        builder.appendDescription("```\n");
        for (int i = from; i < to; i++)
            builder.appendDescription((i + 1) + ": `" + list.get(i) + "`\n");
        builder.appendDescription("```");
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
            if (emoji.equals(Reactions.DOUBLE_ARROW_DOWN)) {
                if (to == list.size()) {
                    display(list, message, user, builder, interval, from, to, pages);
                } else if (to + interval <= list.size()) {
                    display(list, message, user, builder, interval, to, to + interval, pages);
                } else {
                    display(list, message, user, builder, interval, to, list.size(), pages);
                }
            } else if (emoji.equals(Reactions.DOUBLE_ARROW_UP)) {
                if (from == 0) {
                    display(list, message, user, builder, interval, from, to, pages);
                } else if (from - interval >= 0) {
                    display(list, message, user, builder, interval, from - interval, from, pages);
                } else {
                    display(list, message, user, builder, interval, 0, from, pages);
                }
            }
        }, DISPLAY_REACTIONS);
    }



    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
