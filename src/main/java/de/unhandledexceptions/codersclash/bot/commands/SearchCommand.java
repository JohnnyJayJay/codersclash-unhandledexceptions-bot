package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

public class SearchCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        // TODO !search [user|guild] <name>
        // TODO Permission level festlegen
        if (args.length > 1) {
            var shardmanager = event.getJDA().asBot().getShardManager();
            var builder = new EmbedBuilder();
            String name = event.getCommand().getJoinedArgs(1);
            if (args[0].equalsIgnoreCase("user")){
                sendMessage(channel, Type.DEFAULT, "Searching...").queue((msg) -> {
                    List<User> users = new ArrayList<>();
                    if (name.matches(".+#\\d{4}")) {
                        String nameOnly = name.replaceAll("#\\d{4,5}", "");
                        String discriminator = name.replaceFirst(nameOnly + "#", "");
                        shardmanager.getUserCache().stream().filter((user) -> user.getName().equalsIgnoreCase(nameOnly)).filter((user) -> user.getDiscriminator().equals(discriminator)).forEach(users::add);
                        if (users.isEmpty()) {
                            shardmanager.getUserCache().stream().filter((user) -> user.getName().equalsIgnoreCase(nameOnly)).forEach(users::add);
                            shardmanager.getUserCache().stream().filter((user) -> user.getDiscriminator().equals(discriminator)).forEach(users::add);
                        }
                    }
                    for (var jda : shardmanager.getShards()) {
                        users.addAll(jda.getUsersByName(name, true));
                    }
                    msg.delete().queue();
                    if (users.isEmpty()) {
                        sendMessage(channel, Type.ERROR, "Unfortunately, I didn't find what you were looking for.", "No Results").queue(Messages::deleteAfterFiveSec);
                    } else {
                        builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                        sendMessage(channel, Type.SUCCESS, "Loading results...")
                                .queue((m) -> {
                                            m.addReaction(Reactions.ARROW_UP).queue();
                                            m.addReaction(Reactions.ARROW_DOWN).queue();
                                            List<String> list = users.stream().map((user) -> String.format("%#s (%d)", user, user.getIdLong())).collect(Collectors.toList());
                                            display(list, m, event.getAuthor(), builder, 0, list.size() >= 10 ? 10 : list.size());
                                        }, Messages.defaultFailure(channel));
                    }
                });

            } else if (args[0].equalsIgnoreCase("guild")) {
                // TODO
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            wrongUsageMessage(channel, member, this);
        }
    }

    private void display(List<String> list, Message message, User user, EmbedBuilder builder, int from, int to) {
        builder.setDescription("**Results of your search:**\n");
        builder.appendDescription("```\n");
        for (int i = from; i < to; i++)
            builder.appendDescription((i + 1) + ": " + list.get(i) + "\n");
        builder.appendDescription("```");
        message.editMessage(builder.build()).queue();
        Reactions.newMenu(message, user, (emoji) -> (v) -> {
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
        }, Collections.EMPTY_LIST, true, 120);
    }

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
