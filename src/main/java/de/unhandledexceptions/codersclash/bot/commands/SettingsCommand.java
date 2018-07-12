package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Regex;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class SettingsCommand implements ICommand {

    private static EmbedBuilder builder;

    private Database database;
    private CommandSettings settings;

    public SettingsCommand(Database database, CommandSettings settings) {
        this.database = database;
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                builder = new EmbedBuilder();
                channel.sendMessage(builder.setDescription("Loading Main Menu...").build()).queue((msg) ->
                        menu(event.getAuthor(), msg, Layer.MAIN_MENU, Layer.MAIN_MENU));
            } else {
                sendMessage(channel, Type.INFO, "Wrong usage. Command info:\n" + this.info(member)).queue();
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    private void menu(User user, Message message, Layer current, Layer before) {
        message.clearReactions().queue((v) -> {
            editMessage(message, current);
            Reactions.newMenu(message, user, (emoji) -> (v2) -> {
                if (emoji.equals(Reactions.BACK)) {
                    menu(user, message, before, current);
                    return;
                } else if (emoji.equals(Reactions.M)) {
                    menu(user, message, Layer.MAIN_MENU, current);
                    return;
                }

                switch (current) {
                    case MAIN_MENU:
                        switch (emoji) {
                            case Reactions.SPEECH_BUBBLE:
                                menu(user, message, Layer.CHANNEL_MANAGEMENT, current);
                                break;
                            case Reactions.FLOPPY:
                                menu(user, message, Layer.FEATURES, current);
                                break;
                            case Reactions.CONTROLLER:
                                menu(user, message, Layer.GAME, current);
                                break;
                            case Reactions.QUESTION_MARK:
                                menu(user, message, Layer.HELP, current);
                                break;
                        }
                        break;
                    case CHANNEL_MANAGEMENT:
                        switch (emoji) {
                            case Reactions.MAIL:
                                menu(user, message, Layer.MAIL_CHANNEL, current);
                                break;
                            case Reactions.REPEAT:
                                menu(user, message, Layer.AUTO_CHANNEL, current);
                                break;
                        }
                        break;
                    case FEATURES:
                        switch (emoji) {
                            case Reactions.STAR:
                                menu(user, message, Layer.XP_SYSTEM, current);
                                break;
                            case Reactions.EXCLAMATION_MARK:
                                menu(user, message, Layer.REPORTS, current);
                                break;
                            case Reactions.P:
                                menu(user, message, Layer.PREFIX, current);
                                break;
                        }
                        break;
                    case PREFIX:
                        if (emoji.equals(Reactions.Y)) {
                            message.clearReactions().queue();
                            builder.clear().setColor(message.getGuild().getSelfMember().getColor())
                                    .setTitle("Change your prefix").setDescription("Please type in the new prefix now!");
                            message.editMessage(builder.build()).queue();
                            var channel = message.getTextChannel();
                            Reactions.newMessageWaiter(user, channel, (msg) -> {
                                String prefix = msg.getContentRaw();
                                msg.delete().queue();
                                if (prefix.matches(CommandSettings.VALID_PREFIX) && prefix.length() <= 40) {
                                    Reactions.newYesNoMenu("Would you like to set `" + msg.getContentRaw() + "` as your new prefix?",
                                            channel, user, (m) -> {
                                                m.delete().queue();
                                                settings.setCustomPrefix(channel.getGuild().getIdLong(), prefix);
                                                database.setPrefix(channel.getGuild().getIdLong(), prefix);
                                                sendMessage(channel, Type.SUCCESS, "Successfully set `" + prefix + "` as the new prefix!").queue(Messages::deleteAfterFiveSec);
                                                menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);

                                            }, (m) -> menu(user, message, Layer.MAIN_MENU, Layer.PREFIX));
                                } else {
                                    sendMessage(channel, Type.ERROR, "Your prefix `" + prefix + "` is not valid. Remember that a valid prefix cannot contain "
                                            + "any of these: `? * + ^ \\ $`\nAlso, it cannot be longer than 40 characters.").queue(Messages::deleteAfterFiveSec);
                                    menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);
                                }
                            }, (s) -> true, 30, (v3) -> {
                                sendMessage(message.getChannel(), Type.WARNING, "Your prefix change request expired.").queue(Messages::deleteAfterFiveSec);
                                menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);
                            });
                        }
                        break;
                    case REPORTS:
                        int newValue;
                        for (newValue = 0; newValue < 12 && !emoji.equals(Reactions.getNumber(newValue)); newValue++) ;
                        if (newValue == 0) {
                            database.setReportsUntilBan(message.getGuild().getIdLong(), 11);
                            sendMessage(message.getChannel(), Type.SUCCESS,
                                    "Automatic ban for reports is now deactivated!").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.REPORTS);
                        } else if (newValue < 11) {
                            database.setReportsUntilBan(message.getGuild().getIdLong(), newValue);
                            sendMessage(message.getChannel(), Type.SUCCESS,
                                    String.format("Member are now being banned after `%d` reports!", newValue)).queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.REPORTS);
                        }
                        break;
                    case XP_SYSTEM:
                        if (emoji.equals(Reactions.Y)) {
                            database.setUseXpSystem(message.getGuild().getIdLong(), !database.xpSystemActivated(message.getGuild().getIdLong()));
                            sendMessage(message.getChannel(), Type.SUCCESS, "Changes were successful!").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.XP_SYSTEM);
                        }
                        break;
                    case GAME: // TODO
                        break;
                    case AUTO_CHANNEL: // TODO
                        break;
                    case MAIL_CHANNEL:
                        if (emoji.equals(Reactions.Y)) {
                            message.clearReactions().queue();
                            builder.clear().setColor(message.getGuild().getSelfMember().getColor())
                                    .setTitle("Change Mail Channel").setDescription("Mention the new Mail Channel now!");
                            message.editMessage(builder.build()).queue();
                            Reactions.newMessageWaiter(user, message.getChannel(), (msg) -> {
                                msg.delete().queue();
                                Reactions.newYesNoMenu("Do you want to set " + msg.getContentRaw() + " as the new mail channel?", message.getTextChannel(), user, (v3) -> {
                                    v3.delete().queue();
                                    database.setMailChannel(message.getGuild().getIdLong(), msg.getMentionedChannels().get(0).getIdLong());
                                    sendMessage(message.getChannel(), Type.SUCCESS, "Mail Channel successfully set to " + msg.getContentRaw()).queue(Messages::deleteAfterFiveSec);
                                    menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL);
                                }, (v3) -> menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL));
                            }, (string) -> string.matches(Regex.CHANNEL_MENTION), 30, (v3) -> {
                                sendMessage(message.getChannel(), Type.WARNING, "Your channel change request expired.").queue(Messages::deleteAfterFiveSec);
                                menu(user, message, Layer.MAIN_MENU, Layer.MAIL_CHANNEL);
                            });
                        } else if (emoji.equals(Reactions.CLOSED_INBOX)) {
                            Reactions.newYesNoMenu("Do you want to reset your mail channel and therefore deactivate the mail function?", message.getTextChannel(), user, (msg) -> {
                                msg.delete().queue();
                                database.setMailChannel(message.getGuild().getIdLong(), 0);
                                sendMessage(message.getChannel(), Type.SUCCESS, "Mail Channel successfully reset.").queue(Messages::deleteAfterFiveSec);
                                menu(user, message, Layer.MAIN_MENU, current);
                            }, (v3) -> menu(user, message, Layer.MAIN_MENU, current));
                        }
                        break;
                }
            },true);
        });
    }

    private void editMessage(Message message, Layer layer) {
        builder.clear().setColor(message.getGuild().getSelfMember().getColor());
        switch (layer) {
            case MAIN_MENU:
                message.addReaction(Reactions.SPEECH_BUBBLE).queue();
                message.addReaction(Reactions.FLOPPY).queue();
                message.addReaction(Reactions.CONTROLLER).queue();
                message.addReaction(Reactions.QUESTION_MARK).queue();
                builder.setTitle("Main Menu")
                        .setDescription("This is the main menu. Select one of the options below!\n"
                                + Reactions.SPEECH_BUBBLE + " Channel Settings\n"
                                + Reactions.FLOPPY + " Feature/Command Settings\n"
                                + Reactions.CONTROLLER + " Game Settings\n"
                                + Reactions.QUESTION_MARK + " Show help\n"
                                + Reactions.BACK + " Go back\n"
                                + Reactions.M + " Jump to main menu\n"
                                + Reactions.NO_EMOTE + " Exit");
                break;
            case CHANNEL_MANAGEMENT:
                message.addReaction(Reactions.MAIL).queue();
                message.addReaction(Reactions.REPEAT).queue();
                builder.setTitle("Channel Management")
                        .setDescription("Set your Mail Channel and your Autochannel here!\n"
                                + Reactions.MAIL + " Set Mail Channel\n"
                                + Reactions.REPEAT + " Set Autochannel");
                break;
            case FEATURES:
                message.addReaction(Reactions.STAR).queue();
                message.addReaction(Reactions.EXCLAMATION_MARK).queue();
                message.addReaction(Reactions.P).queue();
                builder.setTitle("Feature Management").setDescription("Settings for the bot's features.\n"
                        + Reactions.STAR + " Disable/Enable the XP-system.\n"
                        + Reactions.EXCLAMATION_MARK + " Configure report settings\n"
                        + Reactions.P + " Change the command prefix");
                break;
            case XP_SYSTEM:
                message.addReaction(Reactions.Y).queue();
                builder.setTitle("XP System").setDescription((database.xpSystemActivated(message.getGuild().getIdLong())
                        ? "The xp system is currently activated for this guild. Would you like to deactivate it?"
                        : "The xp system is currently deactivated for this guild. Would you like to activate it?")
                        + "\n" + Reactions.Y + " Yes\n" + Reactions.BACK + " Go Back\n" + Reactions.NO_EMOTE + " Exit");
                break;
            case REPORTS:
                for (int i = 0; i <= 10; i++)
                    message.addReaction(Reactions.getNumber(i)).queue();
                int currentValue = database.getReportsUntilBan(message.getGuild());
                builder.setTitle("Report System")
                        .setDescription(String.format("Currently, a member will be banned after `%d` reports. "
                                + "After how many reports should a member be banned?\n"
                                + "(%s means never)", currentValue, Reactions.getNumber(0)));
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
                message.addReaction(Reactions.Y).queue();
                builder.setTitle("Prefix settings")
                        .setDescription("The bot's prefix on this guild is `" + Bot.getPrefix(message.getGuild().getIdLong())
                                + "`, the default prefix is `" + Bot.getPrefix(-1) + "`. Would you like to change this guild's prefix?\n"
                                + Reactions.Y + " Yes\n" + Reactions.BACK + " Go Back\n" + Reactions.NO_EMOTE + " Exit");
                break;
            case GAME:
                builder.setTitle("Game channel").setDescription(""); // TODO
                break;
            case MAIL_CHANNEL:
                message.addReaction(Reactions.Y).queue();
                message.addReaction(Reactions.CLOSED_INBOX).queue();
                Long id = database.getMailChannel(message.getGuild());
                String currentChannel;
                if (id != null && message.getGuild().getTextChannelById(id) != null)
                    currentChannel = message.getGuild().getTextChannelById(id).getAsMention();
                else
                    currentChannel = "Not Set";
                builder.setTitle("Mail Channel/Inbox").setDescription("Set this guild's inbox channel here! Current channel: " + currentChannel
                        + "\nDo you want to change that?\n"
                        + Reactions.Y + " Yes\n"
                        + Reactions.CLOSED_INBOX + " Deactivate the mail function by deactivating current mail channel\n"
                        + Reactions.BACK + " Go Back\n"
                        + Reactions.NO_EMOTE + " Exit"); // TODO
                break;
        }
        message.editMessage(builder.build()).queue();
        message.addReaction(Reactions.BACK).queue();
        message.addReaction(Reactions.M).queue();
    }

    private enum Layer {
        MAIN_MENU,
        CHANNEL_MANAGEMENT,
        FEATURES,
        HELP,
        REPORTS,
        XP_SYSTEM,
        PREFIX,
        GAME,
        MAIL_CHANNEL,
        AUTO_CHANNEL
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
