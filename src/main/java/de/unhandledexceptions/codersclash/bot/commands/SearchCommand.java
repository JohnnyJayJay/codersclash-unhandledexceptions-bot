package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.*;
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

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */

public class SearchCommand implements ICommand {

    public static final Pattern FIND_ID = Pattern.compile("\\(\\d+\\)");

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
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
                List<String> display = find(shardmanager, name, false);
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((m) -> {
                    ListDisplay.displayList(display, m, member.getUser(), 10, (v) -> m.delete().queue());
                }, Messages.defaultFailure(channel));
            } else if (args[0].equalsIgnoreCase("display") && args[1].matches("(?i)((guilds)|(users))")) {
                event.getMessage().delete().queue();
                builder.setTitle("Results").setColor(Type.SUCCESS.getColor()).setFooter(Type.SUCCESS.getFooter(), Type.SUCCESS.getFooterUrl());
                sendMessage(channel, Type.SUCCESS, "Loading results...").queue((msg) -> {
                    List<String> display = args[1].equalsIgnoreCase("guilds")
                            ? shardmanager.getGuildCache().stream().map((guild) -> format("`%s (%d)`", guild.getName(), guild.getIdLong())).collect(Collectors.toList())
                            : shardmanager.getUserCache().stream().map((user) -> format("`%#s (%d)`", user, user.getIdLong())).collect(Collectors.toList());
                    display.sort(String.CASE_INSENSITIVE_ORDER);
                    ListDisplay.displayList(display, msg, event.getAuthor(), display.size() < 50 ? 10 : 20, (v) -> msg.delete().queue());
                });
                } else {
                    wrongUsageMessage(channel, member, this);
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    public List<String> find(ShardManager shardmanager, String name, boolean searchUser) {
        List<String> ret = new ArrayList<>();
        name = name.toLowerCase();
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
            users.stream().map((user) -> format("%d: `%#s (%d)`", (users.indexOf(user) + 1), user, user.getIdLong())).forEach(ret::add);
        } else {
            List<Guild> guilds = new ArrayList<>();
            for (var jda : shardmanager.getShardCache()) {
                guilds.addAll(jda.getGuildsByName(name, true));
            }
            String finalName = name;
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().startsWith(finalName) && !guilds.contains(guild)).forEach(guilds::add);
            shardmanager.getGuildCache().stream().filter((guild) -> guild.getName().toLowerCase().contains(finalName) && !guilds.contains(guild)).forEach(guilds::add);
            guilds.stream().map((guild) -> format("%d: `%s (%d)` ", (guilds.indexOf(guild) + 1), guild.getName(), guild.getIdLong())).forEach(ret::add);
        }
        return ret;
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 2
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `5`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Let's you search for a specific user/guild.\nYou don't need to input the whole name.\nIt can also display all users/guilds.\n\n" +
                "**Usage**: `%s[search|looksfor|browse] [user|guild] <name>`\n\t\t\t  `%s[search|looksfor|browse] display [users|guilds]`\n\n**Permission level**: `2`", prefix, prefix);
    }
}
