package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Regex;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.commands.SearchCommand.FIND_ID;
import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */
public class SettingsCommand implements ICommand {

    private List<String> zeroToTen = List.of("10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "0");

    private Database database;
    private CommandSettings settings;

    public SettingsCommand(Database database, CommandSettings settings) {
        this.database = database;
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
            return;
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            sendMessage(channel, Type.WARNING, "I can't open the settings dialogue because I don't have permission to manage messages in this channel!").queue();
            return;
        }
        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                var builder = new EmbedBuilder();
                event.getMessage().delete().queue();
                Main.otherThread(() -> channel.sendMessage(builder.setDescription("Loading Main Menu...").build()).queue((msg) ->
                        menu(event.getAuthor(), msg, Layer.MAIN_MENU, Layer.MAIN_MENU, builder), defaultFailure(channel)));
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    private void menu(User user, Message message, Layer current, Layer before, EmbedBuilder builder) {
        message.clearReactions().queue((v) -> {
            editMessage(message, current, builder);
            Reactions.newMenu(user, message, (emoji) -> {
                if (emoji.equals(Reactions.BACK)) {
                    menu(user, message, before, current, builder);
                    return;
                } else if (emoji.equals(Reactions.M)) {
                    menu(user, message, Layer.MAIN_MENU, current, builder);
                    return;
                } else if (emoji.equals(Reactions.NO_EMOTE)) {
                    message.delete().queue();
                    return;
                }

                TextChannel textChannel = message.getTextChannel();
                Guild guild = message.getGuild();
                switch (current) {
                    case MAIN_MENU:
                        switch (emoji) {
                            case Reactions.SPEECH_BUBBLE:
                                menu(user, message, Layer.CHANNEL_MANAGEMENT, current, builder);
                                break;
                            case Reactions.GEAR:
                                menu(user, message, Layer.FEATURES, current, builder);
                                break;
                            case Reactions.QUESTION_MARK:
                                menu(user, message, Layer.HELP, current, builder);
                                break;
                        }
                        break;
                    case CHANNEL_MANAGEMENT:
                        switch (emoji) {
                            case Reactions.MAIL:
                                menu(user, message, Layer.MAIL_CHANNEL, current, builder);
                                break;
                            case Reactions.REPEAT:
                                menu(user, message, Layer.AUTO_CHANNEL, current, builder);
                                break;
                        }
                        break;
                    case FEATURES:
                        switch (emoji) {
                            case Reactions.STAR:
                                menu(user, message, Layer.XP_SYSTEM, current, builder);
                                break;
                            case Reactions.EXCLAMATION_MARK:
                                menu(user, message, Layer.REPORTS, current, builder);
                                break;
                            case Reactions.P:
                                menu(user, message, Layer.PREFIX, current, builder);
                                break;
                        }
                        break;
                    case PREFIX:
                        if (emoji.equals(Reactions.Y)) {
                            message.clearReactions().queue();
                            builder.setTitle("Change your prefix").setDescription("Please type in the new prefix now!");
                            message.editMessage(builder.build()).queue();
                            Reactions.newMessageWaiter(user, textChannel, 30, (msg) -> {
                                String prefix = msg.getContentRaw();
                                msg.delete().queue();
                                if (prefix.matches(CommandSettings.VALID_PREFIX) && prefix.length() <= 40) {
                                    Reactions.newYesNoMenu(user, textChannel, "Would you like to set `" + msg.getContentRaw() + "` as your new prefix?", (m) -> {
                                        m.delete().queue();
                                        settings.setCustomPrefix(textChannel.getGuild().getIdLong(), prefix);
                                        database.setPrefix(textChannel.getGuild().getIdLong(), prefix);
                                        sendMessage(textChannel, Type.SUCCESS, "Successfully set `" + prefix + "` as the new prefix!").queue(Messages::deleteAfterFiveSec);
                                        menu(user, message, Layer.MAIN_MENU, Layer.PREFIX, builder);
                                    }, (reaction) -> menu(user, message, Layer.MAIN_MENU, Layer.PREFIX, builder));
                                } else {
                                    sendMessage(textChannel, Type.ERROR, "Your prefix `" + prefix + "` is not valid. Remember that a valid prefix cannot contain "
                                            + "any of these: `? * + ^ \\ $`\nAlso, it cannot be longer than 40 characters.").queue(Messages::deleteAfterFiveSec);
                                    menu(user, message, Layer.MAIN_MENU, Layer.PREFIX, builder);
                                }
                            }, (v3) -> {
                                sendMessage(textChannel, Type.WARNING, "Your prefix change request expired.").queue(Messages::deleteAfterFiveSec);
                                menu(user, message, Layer.MAIN_MENU, Layer.PREFIX, builder);
                            });
                        }
                        break;
                    case REPORTS:
                        if (emoji.equals(Reactions.Y)) {
                            message.clearReactions().queue((voit) -> {
                                int currentValue = database.getReportsUntilBan(guild);
                                ListDisplay.displayScrollableListSelection(zeroToTen, message, "When should a member be banned? (0 means never)",
                                        guild.getSelfMember().getColor(), user, currentValue == 11 ? 10 : 10 - currentValue, (selected) -> {
                                            int newValue = Integer.parseInt(selected);
                                            database.setReportsUntilBan(guild.getIdLong(), newValue == 0 ? 11 : newValue);
                                            sendMessage(textChannel, Type.SUCCESS, "Successfully changed reports until ban to `" + (newValue == 0 ? "NEVER" : newValue) + "`!").queue(Messages::deleteAfterFiveSec);
                                            menu(user, message, Layer.MAIN_MENU, Layer.FEATURES, builder);
                                        }, (aVoid -> menu(user, message, Layer.MAIN_MENU, Layer.FEATURES, builder)));
                            });
                        }
                        break;
                    case XP_SYSTEM:
                        if (emoji.equals(Reactions.Y)) {
                            database.setUseXpSystem(guild.getIdLong(), !database.xpSystemActivated(guild.getIdLong()));
                            sendMessage(textChannel, Type.SUCCESS, "Changes were successful!").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.XP_SYSTEM, builder);
                        }
                        break;
                    case AUTO_CHANNEL:
                        switch (emoji) {
                            case Reactions.PENCIL:
                                message.clearReactions().queue();
                                builder.setTitle("Change AutoChannel").setDescription("Enter the new AutoChannel now!");
                                message.editMessage(builder.build()).queue();
                                Reactions.newMessageWaiter(user, textChannel, 30, (msg) -> {
                                    msg.delete().queue();
                                    Reactions.newYesNoMenu(user, message.getTextChannel(), "Do you want to set" + Reactions.SPEAKER+  "`" + msg.getContentRaw() + "` as the new AutoChannel?", (m) -> {
                                        m.delete().queue();
                                        database.setAutoChannel(guild.getIdLong(), guild.getVoiceChannelsByName(msg.getContentRaw(), false).get(0).getIdLong());
                                        VoiceChannel voiceChannel = guild.getVoiceChannelById(database.getAutoChannel(guild));
                                        sendMessage(textChannel, Type.SUCCESS, format("AutoChannel successfully set to\n" + Reactions.SPEAKER + "`%s (%s)`", voiceChannel.getName(), voiceChannel.getId())).queue(Messages::deleteAfterFiveSec);
                                        menu(user, message, Layer.MAIN_MENU, Layer.AUTO_CHANNEL, builder);
                                    }, (m) -> menu(user, message, Layer.MAIN_MENU, Layer.AUTO_CHANNEL, builder));
                                }, (v3) -> {
                                    sendMessage(textChannel, Type.WARNING, "Your channel change request expired.").queue(Messages::deleteAfterFiveSec);
                                    menu(user, message, Layer.MAIN_MENU, Layer.AUTO_CHANNEL, builder);
                                });
                                break;
                            case Reactions.CLIPBOARD:
                                List<String> channels = guild.getVoiceChannelCache().stream().map((voiceChannel -> format(Reactions.SPEAKER + "`%s (%s)`", voiceChannel.getName(), voiceChannel.getId()))).collect(Collectors.toList());
                                message.clearReactions().queue((aVoid) -> {
                                    ListDisplay.displayListSelection(channels, message, user, channels.size() > 50 ? 20 : 10, (selected) -> {
                                        var matcher = FIND_ID.matcher(selected);
                                        matcher.find();
                                        var channel = guild.getVoiceChannelById(matcher.group().replaceAll("[\\(\\)]", ""));
                                        database.setAutoChannel(guild.getIdLong(), channel.getIdLong());
                                        sendMessage(textChannel, Type.SUCCESS, "AutoChannel successfully set to\n" + selected).queue(Messages::deleteAfterFiveSec);
                                        menu(user, message, Layer.MAIN_MENU, Layer.AUTO_CHANNEL, builder);
                                    }, (anotherVoid) -> menu(user, message, Layer.MAIN_MENU, Layer.AUTO_CHANNEL, builder));
                                }, defaultFailure(textChannel));
                                break;
                            case Reactions.BOT:
                                sendMessage(textChannel, Type.DEFAULT, "Creating the channel...").queue((msg) -> {
                                    if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                                        guild.getController().createVoiceChannel("Join to create Channel").queue((channel) -> {
                                            msg.delete().queue();
                                            database.setAutoChannel(guild.getIdLong(), channel.getIdLong());
                                            sendMessage(textChannel, Type.SUCCESS, "Success! Your new AutoChannel is" + Reactions.SPEAKER + "`"
                                                    + (channel).getName() + "`").queue(Messages::deleteAfterFiveSec);
                                            menu(user, message, Layer.MAIN_MENU, current, builder);
                                        }, (t) -> {
                                            msg.delete().queue();
                                            defaultFailure(textChannel).accept(t);
                                            menu(user, message, Layer.MAIN_MENU, current, builder);
                                        });
                                    } else {
                                        msg.delete().queue();
                                        sendMessage(textChannel, Type.ERROR, "Woops. It seems like I don't have permission to do that!").queue(Messages::deleteAfterFiveSec);
                                        message.editMessage(builder.setTitle("Please be patient").setDescription("Sending you back to main menu...").build())
                                                .queue((m) -> menu(user, message, Layer.MAIN_MENU, current, builder), (t) -> menu(user, message, Layer.MAIN_MENU, current, builder));
                                    }
                                });
                                break;
                            case Reactions.PUT_LITTER_IN_ITS_PLACE:
                                Reactions.newYesNoMenu(user, textChannel, "Do you want to reset your AutoChannel and therefore deactivate the AutoChannel function?", (msg) -> {
                                    if (database.getAutoChannel(guild) == 0 || database.getAutoChannel(guild) == null) {
                                        sendMessage(textChannel, Type.WARNING, "AutoChannel is not set.").queue(Messages::deleteAfterFiveSec);
                                    } else {
                                        Reactions.newYesNoMenu(user, textChannel, "Do you want to delete the AutoChannel too?", (yes) -> {
                                            guild.getVoiceChannelById(database.getAutoChannel(guild)).delete().queue();
                                            yes.delete().queue();
                                            database.setAutoChannel(guild.getIdLong(), 0);
                                            sendMessage(textChannel, Type.SUCCESS, "AutoChannel successfully reset and deleted.").queue(Messages::deleteAfterFiveSec);
                                        }, (no) -> {
                                            database.setAutoChannel(guild.getIdLong(), 0);
                                            sendMessage(textChannel, Type.SUCCESS, "AutoChannel successfully reset.").queue(Messages::deleteAfterFiveSec);
                                        });
                                    }
                                    msg.delete().queue();
                                    menu(user, message, Layer.MAIN_MENU, current, builder);
                                }, (v3) -> menu(user, message, Layer.MAIN_MENU, current, builder));
                        }
                        break;
                    case MAIL_CHANNEL:
                        switch (emoji) {
                            case Reactions.PENCIL:
                                message.clearReactions().queue();
                                builder.setTitle("Change Mail Channel").setDescription("Mention the new Mail Channel now!");
                                message.editMessage(builder.build()).queue();
                                Reactions.newMessageWaiter(user, textChannel, 30, (msg) -> msg.getContentRaw().matches(Regex.CHANNEL_MENTION), (msg) -> {
                                    msg.delete().queue();
                                    Reactions.newYesNoMenu(user, message.getTextChannel(), "Do you want to set " + msg.getContentRaw() + " as the new mail channel?", (m) -> {
                                        m.delete().queue();
                                        database.setMailChannel(guild.getIdLong(), msg.getMentionedChannels().get(0).getIdLong());
                                        sendMessage(textChannel, Type.SUCCESS, "Mail Channel successfully set to " + msg.getContentRaw()).queue(Messages::deleteAfterFiveSec);
                                        menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL, builder);
                                    }, (m) -> menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL, builder));
                                }, (v3) -> {
                                    sendMessage(textChannel, Type.WARNING, "Your channel change request expired.").queue(Messages::deleteAfterFiveSec);
                                    menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL, builder);
                                });
                                break;
                            case Reactions.CLIPBOARD:
                                List<String> channels = guild.getTextChannelCache().stream().map(TextChannel::getAsMention).collect(Collectors.toList());
                                message.clearReactions().queue((aVoid) -> {
                                    ListDisplay.displayListSelection(channels, message, user, channels.size() > 50 ? 20 : 10, (selected) -> {
                                        var channel = guild.getTextChannelById(selected.replaceAll("[<>#]", ""));
                                        database.setMailChannel(guild.getIdLong(), channel.getIdLong());
                                        sendMessage(textChannel, Type.SUCCESS, "Mail Channel successfully set to " + selected).queue(Messages::deleteAfterFiveSec);
                                        menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL, builder);
                                    }, (anotherVoid) -> menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL, builder));
                                }, defaultFailure(textChannel));
                                break;
                            case Reactions.BOT:
                                sendMessage(textChannel, Type.DEFAULT, "Creating the channel...").queue((msg) -> {
                                    if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                                        guild.getController().createTextChannel("inbox").queue((channel) -> {
                                            msg.delete().queue();
                                            database.setMailChannel(guild.getIdLong(), channel.getIdLong());
                                            sendMessage(textChannel, Type.SUCCESS, "Success! Your new mail channel is "
                                                    + ((TextChannel) channel).getAsMention()).queue(Messages::deleteAfterFiveSec);
                                            menu(user, message, Layer.MAIN_MENU, current, builder);
                                        }, (t) -> {
                                            msg.delete().queue();
                                            defaultFailure(textChannel).accept(t);
                                            menu(user, message, Layer.MAIN_MENU, current, builder);
                                        });
                                    } else {
                                        msg.delete().queue();
                                        sendMessage(textChannel, Type.ERROR, "Woops. It seems like I don't have permission to do that!").queue(Messages::deleteAfterFiveSec);
                                        message.editMessage(builder.setTitle("Please be patient").setDescription("Sending you back to main menu...").build())
                                                .queue((m) -> menu(user, message, Layer.MAIN_MENU, current, builder), (t) -> menu(user, message, Layer.MAIN_MENU, current, builder));
                                    }
                                });
                                break;
                            case Reactions.CLOSED_INBOX:
                                Reactions.newYesNoMenu(user, textChannel, "Do you want to reset your Mail Channel and therefore deactivate the mail function?", (msg) -> {
                                    if (database.getMailChannel(guild) == 0 || database.getMailChannel(guild) == null) {
                                        sendMessage(textChannel, Type.WARNING, "Mail Channel is not set.").queue(Messages::deleteAfterFiveSec);
                                    } else {
                                        Reactions.newYesNoMenu(user, textChannel, "Do you want to delete the mail channel too?", (yes) -> {
                                            guild.getTextChannelById(database.getMailChannel(guild)).delete().queue();
                                            yes.delete().queue();
                                            database.setMailChannel(guild.getIdLong(), 0);
                                            sendMessage(textChannel, Type.SUCCESS, "Mail Channel successfully reset and deleted.").queue(Messages::deleteAfterFiveSec);
                                        }, (no) -> {
                                            database.setMailChannel(guild.getIdLong(), 0);
                                            sendMessage(textChannel, Type.SUCCESS, "Mail Channel successfully reset.").queue(Messages::deleteAfterFiveSec);
                                        });
                                    }
                                    msg.delete().queue();
                                    menu(user, message, Layer.MAIN_MENU, current, builder);
                                }, (v3) -> menu(user, message, Layer.MAIN_MENU, current, builder));
                        }
                        break;
                }
            }, current.EMOJIS);
        });
    }

    private void editMessage(Message message, Layer layer, EmbedBuilder builder) {
        boolean helpMessage = layer == Layer.HELP;
        builder.clear().setColor(message.getGuild().getSelfMember().getColor());
        switch (layer) {
            case MAIN_MENU:
                builder.setTitle("Main Menu")
                        .setDescription("This is the main menu. Select one of the options below!\n"
                                + Reactions.SPEECH_BUBBLE + " Channel Settings\n"
                                + Reactions.GEAR + " Feature/Command Settings\n"
                                + Reactions.QUESTION_MARK + " Show help\n");
                break;
            case CHANNEL_MANAGEMENT:
                builder.setTitle("Channel Management")
                        .setDescription("Manage your Mail Channel and your AutoChannel here!\n"
                                + Reactions.MAIL + " Manage Mail Channel\n"
                                + Reactions.REPEAT + " Manage AutoChannel\n");
                break;
            case FEATURES:
                builder.setTitle("Feature Management").setDescription("Settings for the bot's features.\n"
                        + Reactions.STAR + " Disable/Enable the XP-System.\n"
                        + Reactions.EXCLAMATION_MARK + " Configure report settings\n"
                        + Reactions.P + " Change the command prefix\n");
                break;
            case XP_SYSTEM:
                var jda = message.getJDA().asBot().getShardManager();
                var activated = jda.getEmotesByName("activated", false).get(0).getAsMention();
                var deactivated = jda.getEmotesByName("deactivated", false).get(0).getAsMention();
                builder.setTitle("XP System").setDescription((database.xpSystemActivated(message.getGuild().getIdLong())
                        ? format("The XP-System is currently %s for this guild.\n Would you like to turn it %s?", activated, deactivated)
                        : format("The XP-System is currently %s for this guild.\n Would you like to turn it %s?", deactivated, activated))
                        + "\n" + Reactions.Y + " Yes\n");
                break;
            case REPORTS:
                int currentValue = database.getReportsUntilBan(message.getGuild());
                builder.setTitle("Report System");
                builder.appendDescription(currentValue == 11
                        ? "Currently, members will not be banned for reports."
                        : "Currently, a member will be banned after `" + currentValue + "` reports.");
                builder.appendDescription("\nWould you like to change that?\n"
                        + Reactions.Y + " Yes, let me change that.\n");
                break;
            case HELP:
                builder.setTitle("Help")
                        .setDescription("This is the settings dialogue for this bot. You can navigate through it by using "
                                + "the given reactions. Most of the time, it should be self-explaining, but here are some things that might come in handy:\n"
                                + Reactions.M + " always sends you back to the main menu.\n"
                                + Reactions.BACK + " navigates you one step back. Note that you can go back only one step!\n"
                                + Reactions.NO_EMOTE + " deletes this message and closes the dialogue.");
                break;
            case PREFIX:
                builder.setTitle("Prefix settings")
                        .setDescription("The bot's prefix on this guild is `" + Bot.getPrefix(message.getGuild().getIdLong())
                                + "`, the default prefix is `" + Bot.getPrefix(-1) + "`. Would you like to change this guild's prefix?\n"
                                + Reactions.Y + " Yes, let me set a new prefix\n");
                break;
            case AUTO_CHANNEL:
                Long idAuto = database.getAutoChannel(message.getGuild());
                String currentAutoChannel;
                if (idAuto != null && message.getGuild().getVoiceChannelById(idAuto) != null)
                    currentAutoChannel = "`" + message.getGuild().getVoiceChannelById(idAuto).getName() + "`";
                else
                    currentAutoChannel = "Not set";
                builder.setTitle("AutoChannel").setDescription("Manage this guild's AutoChannel here!\nCurrent channel: " + Reactions.SPEAKER + "`" + currentAutoChannel + "`"
                        + "\nDo you want to change that?\n"
                        + Reactions.PENCIL + " Yes, let me set a new channel\n"
                        + Reactions.CLIPBOARD + " Yes, let me select a new channel\n"
                        + Reactions.BOT + " Yes, create one for me\n"
                        + Reactions.PUT_LITTER_IN_ITS_PLACE+ " Deactivate the AutoChannel by deactivating current AutoChannel\n");
                break;
            case MAIL_CHANNEL:
                Long idMail = database.getMailChannel(message.getGuild());
                String currentMailChannel;
                if (idMail != null && message.getGuild().getTextChannelById(idMail) != null)
                    currentMailChannel = message.getGuild().getTextChannelById(idMail).getAsMention();
                else
                    currentMailChannel = "Not set";
                builder.setTitle("Mail Channel/Inbox").setDescription("Manage this guild's inbox channel here!\nCurrent channel: " + currentMailChannel
                        + "\nDo you want to change that?\n"
                        + Reactions.PENCIL + " Yes, let me set a new channel\n"
                        + Reactions.CLIPBOARD + " Yes, let me select a new channel\n"
                        + Reactions.BOT + " Yes, create one for me\n"
                        + Reactions.CLOSED_INBOX + " Deactivate the mail function by deactivating current mail channel\n");
                break;
        }
        if (!helpMessage) {
            builder.appendDescription("-----------------------\n" + Reactions.BACK + " Go Back\n"
                    + Reactions.M + " Main Menu\n"
                    + Reactions.NO_EMOTE + " Exit");
        }
        layer.EMOJIS.forEach((emoji) -> message.addReaction(emoji).queue());
        message.editMessage(builder.build()).queue();
    }

    private enum Layer {
        MAIN_MENU(Reactions.SPEECH_BUBBLE, Reactions.GEAR, Reactions.QUESTION_MARK),
        CHANNEL_MANAGEMENT(Reactions.MAIL, Reactions.REPEAT),
        FEATURES(Reactions.STAR, Reactions.EXCLAMATION_MARK, Reactions.P),
        HELP(),
        REPORTS(Reactions.Y),
        XP_SYSTEM(Reactions.Y),
        PREFIX(Reactions.Y),
        MAIL_CHANNEL(Reactions.PENCIL, Reactions.CLIPBOARD, Reactions.BOT, Reactions.CLOSED_INBOX),
        AUTO_CHANNEL(Reactions.PENCIL, Reactions.CLIPBOARD, Reactions.BOT, Reactions.PUT_LITTER_IN_ITS_PLACE);

        public final List<String> EMOJIS;

        Layer(String... emojis) {
            this.EMOJIS = new ArrayList<>();
            this.EMOJIS.addAll(Arrays.asList(emojis));
            this.EMOJIS.add(Reactions.BACK);
            this.EMOJIS.add(Reactions.M);
            this.EMOJIS.add(Reactions.NO_EMOTE);
        }

    }

    @Override
    public String info(Member member) {
        int permLevel = Permissions.getPermissionLevel(member);
        String ret = permLevel < 5
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `5`\nYour permission " +
                "level: `" + permLevel + "`"
                : "**Description**: Opens the settings dialogue for this bot.\n\n**Usage**: `" + Bot.getPrefix(member.getGuild().getIdLong())
                + "[settings|control]`\n\n**Permission level**: `5`";
        return ret;
    }
}