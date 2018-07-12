package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class SettingsCommand implements ICommand {

    private static final EmbedBuilder builder = new EmbedBuilder();

    private Database database;
    private CommandSettings settings;

    public SettingsCommand(Database database, CommandSettings settings) {
        this.database = database;
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 1) {
                channel.sendMessage(new EmbedBuilder().setDescription("Loading Main Menu...").build()).queue((msg) ->
                        menu(event.getAuthor(), msg, Layer.MAIN_MENU, Layer.MAIN_MENU));
            } else {
                Messages.sendMessage(channel, Messages.Type.INFO, "Wrong usage. Command info:\n" + this.info(member)).queue();
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    private void menu(User user, Message message, Layer current, Layer before) {
        message.clearReactions().queue((v) -> {
            editMessage(message, current);
            Reactions.newMenu(message, user, (emoji) -> (v2) -> {
                if (emoji.equals(Reactions.BACK_EMOTE)) {
                    menu(user, message, before, current);
                    return;
                } else if (emoji.equals(Reactions.M)) {
                    menu(user, message, Layer.MAIN_MENU, current);
                    return;
                }

                switch (current) {
                    case MAIN_MENU:
                        switch (emoji) {
                            case Reactions.STAR:
                                menu(user, message, Layer.XP_SYSTEM, current);
                                break;
                            case Reactions.EXCLAMATION:
                                menu(user, message, Layer.REPORTS, current);
                                break;
                            case Reactions.P:
                                menu(user, message, Layer.PREFIX, current);
                                break;
                            case Reactions.QUESTION_MARK:
                                menu(user, message, Layer.HELP, current);
                                break;
                        }
                        break;
                    case PREFIX:
                        if (emoji.equals(Reactions.Y)) {
                            menu(user, message, Layer.CHANGE_PREFIX, current);
                        }
                        break;
                    case CHANGE_PREFIX:
                        var channel = message.getTextChannel();
                        Reactions.newMessageWaiter(user, channel, (msg) -> {
                            channel.sendMessage("message gesendet").queue();
                            String prefix = msg.getContentRaw();
                            if (prefix.matches(CommandSettings.VALID_PREFIX) && prefix.length() <= 40) {
                                channel.sendMessage("prefix matcht").queue();
                                Reactions.newYesNoMenu("Would you like to set `" + msg.getContentRaw() + "` as your new prefix?",
                                        channel, user, (m) -> {
                                            m.delete().queue();
                                            settings.setCustomPrefix(channel.getGuild().getIdLong(), prefix);
                                            database.setPrefix(channel.getGuild().getIdLong(), prefix);
                                            Messages.sendMessage(channel, Messages.Type.SUCCESS, "Successfully set `" + prefix + "` as the new prefix!").queue(Messages::deleteAfterFiveSec);
                                            menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);

                                        }, (m) -> menu(user, message, Layer.MAIN_MENU, Layer.PREFIX));
                            } else {
                                channel.sendMessage("prefix matcht nicht").queue();
                                Messages.sendMessage(channel, Messages.Type.ERROR, "Your prefix `" + prefix + "` is not valid. Remember that a valid prefix cannot contain "
                                        + "any of these: `? * + ^ \\ $`\nAlso, it cannot be longer than 40 characters.").queue(Messages::deleteAfterFiveSec);
                                menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);
                            }
                        }, 20, (v3) -> {
                            channel.sendMessage("expired").queue();
                            Messages.sendMessage(message.getChannel(), Messages.Type.WARNING, "Your prefix change request expired.").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.PREFIX);
                        });
                        break;
                    case REPORTS:
                        int newValue;
                        for (newValue = 0; newValue < 12 && !emoji.equals(Reactions.getNumber(newValue)); newValue++) ;
                        if (newValue == 0) {
                            database.setReportsUntilBan(message.getGuild().getIdLong(), 11);
                            Messages.sendMessage(message.getChannel(), Messages.Type.SUCCESS,
                                    "Automatic ban for reports is now deactivated!").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.REPORTS);
                        } else if (newValue < 11) {
                            database.setReportsUntilBan(message.getGuild().getIdLong(), newValue);
                            Messages.sendMessage(message.getChannel(), Messages.Type.SUCCESS,
                                    String.format("Member are now being banned after `%d` reports!", newValue)).queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.REPORTS);
                        }
                        break;
                    case XP_SYSTEM:
                        if (emoji.equals(Reactions.Y)) {
                            database.setUseXpSystem(message.getGuild().getIdLong(), !database.xpSystemActivated(message.getGuild().getIdLong()));
                            Messages.sendMessage(message.getChannel(), Messages.Type.SUCCESS, "Changes were successful!").queue(Messages::deleteAfterFiveSec);
                            menu(user, message, Layer.MAIN_MENU, Layer.XP_SYSTEM);
                        }


                }
            },true);
        });
    }

    private void editMessage(Message message, Layer layer) {
        builder.clear().setColor(message.getGuild().getSelfMember().getColor());
        switch (layer) {
            case MAIN_MENU:
                message.addReaction(Reactions.STAR).queue();
                message.addReaction(Reactions.EXCLAMATION).queue();
                message.addReaction(Reactions.P).queue();
                message.addReaction(Reactions.QUESTION_MARK).queue();
                builder.setTitle("Main Menu")
                        .setDescription("This is the main menu. Select one of the options below!\n"
                                + Reactions.STAR + " Disable/Enable the XP-system.\n"
                                + Reactions.EXCLAMATION + " Configure report settings\n"
                                + Reactions.P + " Change the command prefix\n"
                                + Reactions.QUESTION_MARK + " Show help\n"
                                + Reactions.BACK_EMOTE + " Go back\n"
                                + Reactions.M + " Jump to main menu\n"
                                + Reactions.NO_EMOTE + " Exit");
                break;
            case XP_SYSTEM:
                message.addReaction(Reactions.Y).queue();
                builder.setTitle("XP System").setDescription((database.xpSystemActivated(message.getGuild().getIdLong())
                        ? "The xp system is currently activated for this guild. Would you like to deactivate it?"
                        : "The xp system is currently deactivated for this guild. Would you like to activate it?")
                        + "\n" + Reactions.Y + " Yes\n" + Reactions.BACK_EMOTE + " Go Back\n" + Reactions.NO_EMOTE + " Exit");
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
                                + Reactions.BACK_EMOTE + " navigates you one step back. Note that you can go back only one step!\n"
                                + Reactions.NO_EMOTE + " deletes this message and closes the dialogue.");
                break;
            case PREFIX:
                message.addReaction(Reactions.Y).queue();
                builder.setTitle("Prefix settings")
                        .setDescription("The bot's prefix on this guild is `" + Bot.getPrefix(message.getGuild().getIdLong())
                                + "`, the default prefix is `" + Bot.getPrefix(-1) + "`. Would you like to change this guild's prefix?\n"
                                + Reactions.Y + " Yes\n" + Reactions.BACK_EMOTE + " Go Back\n" + Reactions.NO_EMOTE + " Exit");
                break;
            case CHANGE_PREFIX:
                builder.setTitle("Change your prefix").setDescription("Please type in the new prefix now!");
                break;
            case GAME:
                builder.setTitle("Game channel").setDescription("");
        }
        message.editMessage(builder.build()).queue();
        message.addReaction(Reactions.BACK_EMOTE).queue();
        message.addReaction(Reactions.M).queue();
    }

    private enum Layer {
        MAIN_MENU,
        REPORTS,
        PREFIX,
        CHANGE_PREFIX,
        GAME,
        XP_SYSTEM,
        HELP,
        MAIL_CHANNEL;
    }

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
