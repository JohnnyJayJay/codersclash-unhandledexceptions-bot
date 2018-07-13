package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

public class SearchCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        // TODO Permission level
        if (args.length > 1) {
            var shardmanager = event.getJDA().asBot().getShardManager();
            var builder = new EmbedBuilder();
            if (args[0].equalsIgnoreCase("user")){
                sendMessage(channel, Type.DEFAULT, "Searching...").queue((msg) -> {
                    String name = event.getCommand().getJoinedArgs(1);
                    List<User> withDiscriminator = Collections.EMPTY_LIST;
                    List<User> users = new ArrayList<>();
                    if (name.matches(".+#\\d{4}")) {
                        String newName = name.replaceAll("#\\d{4,5}", "");
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
                    if (users.isEmpty()) {
                        shardmanager.getUserCache().stream().filter((user) -> user.getName().toLowerCase().startsWith(finalName.toLowerCase())).forEach(users::add);
                        if (users.isEmpty()) {
                            users.addAll(withDiscriminator);
                            shardmanager.getUserCache().stream().filter((user) -> user.getName().toLowerCase().contains(finalName.toLowerCase())).forEach(users::add);
                        }
                    }
                    msg.delete().queue();
                    if (users.isEmpty()) {
                        sendMessage(channel, Type.ERROR, "Unfortunately, I didn't find what you were looking for.", "No Results").queue(Messages::deleteAfterFiveSec);
                    } else {
                        builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                        sendMessage(channel, Type.SUCCESS, "Loading results...")
                                .queue((m) -> {
                                            List<String> display = users.stream().map((user) -> String.format("%#s (%d)", user, user.getIdLong())).collect(Collectors.toList());
                                            display(display, m, event.getAuthor(), builder, 0, display.size() >= 10 ? 10 : display.size());
                                        }, Messages.defaultFailure(channel));
                    }
                });

            } else if (args[0].equalsIgnoreCase("guild")) {
                String name = event.getCommand().getJoinedArgs(1);
                List<Guild> guilds = new ArrayList<>();
                for (var jda : shardmanager.getShardCache()) {
                    guilds.addAll(jda.getGuildsByName(name, true));
                }
                shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().contains(name)).forEach(guilds::add);
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...")
                        .queue((m) -> {
                            List<String> display = guilds.stream().map((guild) -> String.format("%s (%d)", guild.getName(), guild.getIdLong())).collect(Collectors.toList());
                            display(display, m, event.getAuthor(), builder, 0, display.size() >= 10 ? 10 : display.size());
                        }, Messages.defaultFailure(channel));
            } else if (args[0].equalsIgnoreCase("display") && args[1].matches("(?i)((guilds)|(users))")) {
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((msg) -> {
                    List<String> display = args[1].equalsIgnoreCase("guilds")
                            ? shardmanager.getGuildCache().stream().map((guild) -> String.format("%s (%d)", guild.getName(), guild.getIdLong())).collect(Collectors.toList())
                            : shardmanager.getUserCache().stream().map((user) -> String.format("%#s (%d)", user, user.getIdLong())).collect(Collectors.toList());
                    display(display, msg, event.getAuthor(), builder, 0, display.size() >= 10 ? 10 : display.size());
                });
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            wrongUsageMessage(channel, member, this);
        }
    }

    private void display(List<String> list, Message message, User user, EmbedBuilder builder, int from, int to) {
        builder.setDescription("**Results for your search:** " + list.size() + "\n");
        builder.appendDescription("```\n");
        for (int i = from; i < to; i++)
            builder.appendDescription((i + 1) + ": " + list.get(i) + "\n");
        builder.appendDescription("```");
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(user, message, (emoji) -> {
            if (emoji.equals(Reactions.ARROW_DOWN)) {
                if (to == list.size()) {
                    display(list, message, user, builder, from, to);
                } else if (to + 10 <= list.size()) {
                    display(list, message, user, builder, to, to + 10);
                } else {
                    display(list, message, user, builder, to, list.size());
                }
            } else if (emoji.equals(Reactions.ARROW_UP)) {
                if (from == 0) {
                    display(list, message, user, builder, from, to);
                } else if (from - 10 >= 0) {
                    display(list, message, user, builder, from - 10, from);
                } else {
                    display(list, message, user, builder, 0, from);
                }
            }
        }, List.of(Reactions.ARROW_UP, Reactions.ARROW_DOWN));
    }

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
