package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

// TODO Fortschritt verlangsamen
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
        if (!database.xpSystemActivated(commandEvent.getGuild().getIdLong()))
            return;

        long xp = database.getGuildXp(member);
        long maxxp = database.getGuildLvl(member) * 4;
        if (commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
            try {
                for (String name : urls.keySet()) {
                    if (commandEvent.getJDA().getEmotesByName(name, true).size() == 0) {
                        commandEvent.getGuild().getController().createEmote(name, Icon.from(new URL(urls.get(name)).openStream())).queue();
                    }
                }
            } catch (IOException e) {
                Logging.getLogger().error("An Exception occurred while creating/parsing emotes:", e);
                return;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .addField("Guild", "Level: " + database.getGuildLvl(member) +
                                    "\nXp: " + database.getGuildXp(member) + "/" + database.getGuildLvl(member) * 4 +
                                    "\n" + getProgressBar(database.getGuildXp(member), database.getGuildLvl(member) * 4, member)
                            ,true)
                    .addField("User", "Level: "+database.getUserLvl(member.getUser())+
                                    "\nXp: "+database.getUserXp(member.getUser())+"/"+database.getUserLvl(member.getUser())*4+
                                    "\n"+getProgressBar(database.getUserXp(member.getUser()), database.getUserLvl(member.getUser()) * 4, member)
                            , true);
            sendMessage(textChannel, Messages.Type.DEFAULT, "Take a look at your xp status:", "Level information", true, embedBuilder).queue();
        }
    }

    @Override
    public String info(Member member) {
        return String.format("**Description**: Gives you information about your level.\n\n**Usage**: `%s[xp|lvl|level]`\n\n**Permission level**: `0`",
                settings.getPrefix(member.getGuild().getIdLong()));
    }

    @Override
    public void onGenericGuildMessage(GenericGuildMessageEvent origevent) {
        if (origevent instanceof GuildMessageDeleteEvent || !database.xpSystemActivated(origevent.getGuild().getIdLong()))
            return;

        if (origevent instanceof GuildMessageReactionAddEvent) {
            GuildMessageReactionAddEvent event = (GuildMessageReactionAddEvent) origevent;
            event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> database.addXp(msg.getMember(), 1));
        } else if (origevent instanceof GuildMessageReactionRemoveEvent) {
            GuildMessageReactionRemoveEvent event = (GuildMessageReactionRemoveEvent) origevent;
            event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> database.removeXp(msg.getMember(), 1));
        } else if (origevent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) origevent;
            if (event.getMessage().getType() != MessageType.DEFAULT || event.getAuthor().isBot())
                return;

            int length = event.getMessage().getContentRaw().length();
            int result;
            if (length > 0) {
                if (length > 10)
                    result = ThreadLocalRandom.current().nextInt(length - 10) + 10;
                else
                    result = ThreadLocalRandom.current().nextInt(length);

                database.addXp(event.getMember(), result);
            }
        }
        origevent.getChannel().getMessageById(origevent.getMessageId()).queue((msg) -> {
            if (msg.getMember() != null)
                this.checkLvl(msg.getMember());
        });

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
