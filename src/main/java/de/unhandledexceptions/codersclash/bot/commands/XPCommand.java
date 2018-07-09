package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class XPCommand extends ListenerAdapter implements ICommand {

    private final Map<String, String> urls = new HashMap<>() {{
        put("full1", "http://www.baggerstation.de/testseite/bots/full1.png");
        put("full2", "http://www.baggerstation.de/testseite/bots/full2.png");
        put("full3", "http://www.baggerstation.de/testseite/bots/full3.png");
        put("empty1", "http://www.baggerstation.de/testseite/bots/empty1.png");
        put("empty2", "http://www.baggerstation.de/testseite/bots/empty2.png");
        put("empty3", "http://www.baggerstation.de/testseite/bots/empty3.png");
    }};

    private CommandSettings settings;
    private Database database;

    public XPCommand(CommandSettings settings, Database database) {
        this.settings = settings;
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        // TODO Überprüfen, ob der Guild das XP system aktiviert hat. wenn nicht -> return
        database.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        long xp = database.getGuildXp(member);
        long maxxp = database.getGuildLvl(member)*4;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .addField("Guild", "Level: "+database.getGuildLvl(member)+
                        "\nXp: "+database.getGuildXp(member)+"/"+database.getGuildLvl(member)*4+
                        "\n"+getProgressBar(database.getGuildXp(member), database.getGuildLvl(member) * 4, member)
                        ,true)
                .addField("User", "Level: "+database.getUserLvl(member.getUser())+
                "\nXp: "+database.getUserXp(member.getUser())+"/"+database.getUserLvl(member.getUser())*4+
                "\n"+getProgressBar(database.getUserXp(member.getUser()), database.getUserLvl(member.getUser()) * 4, member)
                        , true);
        Messages.sendMessage(textChannel, Messages.Type.DEFAULT, "Take a look at your xp status:", "Level information", true, embedBuilder).queue();
    }

    @Override
    public String info(Member member) {
        return String.format("**Description**: Gives you information about your Level.\n\n**Usage**: `%s[xp|lvl|level]`\n\n**Permission level**: `0`",
                settings.getPrefix(member.getGuild().getIdLong()));
    }

    @Override
    public void onGenericGuildMessage(GenericGuildMessageEvent origevent) {
        // TODO Überprüfen, ob der Guild das XP system aktiviert hat. wenn nicht -> return
        if (origevent instanceof GuildMessageReactionAddEvent) {
            GuildMessageReactionAddEvent event = (GuildMessageReactionAddEvent) origevent;
            database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getUser().getIdLong());
            event.getReaction().getTextChannel()
                    .getMessageById(event.getReaction().getMessageId()).queue(
                            (msg) -> database.addXp(msg.getMember(), 1)
            );
        } else if (origevent instanceof GuildMessageReactionRemoveEvent) {
            GuildMessageReactionRemoveEvent event = (GuildMessageReactionRemoveEvent) origevent;
            database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getUser().getIdLong());
            event.getReaction().getTextChannel().getMessageById(event.getReaction().getMessageId()).queue(
                    (msg) -> database.removeXp(event.getMember(), 1)
            );
        } else if (origevent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) origevent;
            database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
            if (!event.getAuthor().isBot()) {
                if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
                    try {
                        for (String name : urls.keySet()) {
                            if (event.getMember().getGuild().getEmotesByName(name, true).size() == 0) {
                                event.getMember().getGuild().getController().createEmote(name, Icon.from(new URL(urls.get(name)).openStream())).queue();
                            }
                        }
                    } catch (IOException e) {
                        Logging.getLogger().error("An Exception occurred while creating/parsing emotes:", e);
                    }
                }
                int length = event.getMessage().getContentRaw().length();
                int result;
                if (length > 10) {
                    result = ThreadLocalRandom.current().nextInt(length - 10) + 10;
                } else result = ThreadLocalRandom.current().nextInt(length);
                database.addXp(event.getMember(), result);
            }
        }
        origevent.getChannel().getMessageById(origevent.getMessageId()).queue(
                (msg) -> this.checkLvl(msg.getMember())
        );
    }

    private String getProgressBar(long xp, long maxxp, Member member) {
        Emote[] emotes = new Emote[8];
        emotes[0] = member.getGuild().getEmotesByName("empty1", true).get(0);
        emotes[1] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[2] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[3] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[4] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[5] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[6] = member.getGuild().getEmotesByName("empty2", true).get(0);
        emotes[7] = member.getGuild().getEmotesByName("empty3", true).get(0);
        if (maxxp/8<=xp) {
            emotes[0] = member.getGuild().getEmotesByName("full1", true).get(0);
            if (maxxp/8*2<=xp) {
                emotes[1] = member.getGuild().getEmotesByName("full2", true).get(0);
                if (maxxp / 8 * 3 <= xp) {
                    emotes[2] = member.getGuild().getEmotesByName("full2", true).get(0);
                    if (maxxp / 8 * 4 <= xp) {
                        emotes[3] = member.getGuild().getEmotesByName("full2", true).get(0);
                        if (maxxp / 8 * 5 <= xp) {
                            emotes[4] = member.getGuild().getEmotesByName("full2", true).get(0);
                            if (maxxp / 8 * 6 <= xp) {
                                emotes[5] = member.getGuild().getEmotesByName("full2", true).get(0);
                                if (maxxp / 8 * 7 <= xp) {
                                    emotes[6] = member.getGuild().getEmotesByName("full2", true).get(0);
                                    if (maxxp == xp) {
                                        emotes[7] = member.getGuild().getEmotesByName("full3", true).get(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Emote emote:emotes) {
            stringBuilder.append(emote.getAsMention());
        }
        stringBuilder.append("\n\n");
        // FIXME was ist das ? xD
        //database."SELECT * FROM Customers ORDER BY CustomerID;"
        return stringBuilder.toString();
    }

    private void checkLvl(Member member) {
        if (database.getUserXp(member.getUser())>=(database.getUserLvl(member.getUser()))*4) {
            database.addUserLvl(member.getUser());
        }
        if (database.getGuildXp(member)>=(database.getGuildLvl(member)*4)) {
            database.addGuildLvl(member);
        }
    }
}
