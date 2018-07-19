package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

/**
 * @author Johnny_JayJay
 */

public class SearchCommand implements ICommand {

    public final Pattern FIND_ID = Pattern.compile("\\(\\d+\\)");

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if(Permissions.getPermissionLevel(member) >= 1) {
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
                                    ListDisplay.displayList(display, m, member.getUser(), 10, (v) -> m.delete().queue());
                                }, Messages.defaultFailure(channel));
                    }
                });

            } else if (args[0].equalsIgnoreCase("guild")) {
                String name = String.join(" ", Arrays.asList(args).subList(1, args.length));
                List<String> display = find(shardmanager, name.toLowerCase(), false);
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((m) -> {
                    ListDisplay.displayList(display, m, member.getUser(), 10, (v) -> m.delete().queue());
                }, Messages.defaultFailure(channel));
            } else if (args[0].equalsIgnoreCase("display") && args[1].matches("(?i)((guilds)|(users))")) {
                event.getMessage().delete().queue();
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((msg) -> {
                    List<String> display = args[1].equalsIgnoreCase("guilds")
                            ? shardmanager.getGuildCache().stream().map((guild) -> String.format("`%s (%d)`", guild.getName(), guild.getIdLong())).collect(Collectors.toList())
                            : shardmanager.getUserCache().stream().map((user) -> String.format("`%#s (%d)`", user, user.getIdLong())).collect(Collectors.toList());
                    display.sort(String.CASE_INSENSITIVE_ORDER);
                    ListDisplay.displayList(display, msg, event.getAuthor(), display.size() < 50 ? 10 : 20, (v) -> msg.delete().queue());
                });
                } else {
                    wrongUsageMessage(event.getMessage(), channel, member, this);
                }
            } else {
                wrongUsageMessage(event.getMessage(), channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
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
            users.stream().map((user) -> String.format("%d: `%#s (%d)`", (users.indexOf(user) + 1), user, user.getIdLong())).forEach(ret::add);
        } else {
            List<Guild> guilds = new ArrayList<>();
            for (var jda : shardmanager.getShardCache()) {
                guilds.addAll(jda.getGuildsByName(name, true));
            }
            String finalName = name;
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().startsWith(finalName) && !guilds.contains(guild)).forEach(guilds::add);
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().contains(finalName) && !guilds.contains(guild)).forEach(guilds::add);
            guilds.stream().map((guild) -> String.format("%d: `%s (%d)` ", (guilds.indexOf(guild) + 1), guild.getName(), guild.getIdLong())).forEach(ret::add);
        }
        return ret;
    }
    // TODO
    @Override
    public String info(Member member) {
        return "TODO";
    }
}
