package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static java.lang.String.format;

public class Management extends ListenerAdapter {

    private Bot bot;
    private Timer timer;

    public Management(Bot bot) {
        this.bot = bot;
        this.timer = new Timer();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        var jda = event.getJDA().asBot().getShardManager();
        var activated = jda.getEmotesByName("activated", false).get(0).getAsMention();
        var deactivated = jda.getEmotesByName("deactivated", false).get(0).getAsMention();
        String prefix = Bot.getPrefix(event.getGuild().getIdLong());
        String regex = prefix + "(?i)manage (((status)|(restart)|(shutdown)) ((complete)|(\\d)))";
        String regexWrongUsage = prefix + "(?i)manage.*";
        if (Bot.getBotOwners().contains(event.getAuthor().getIdLong())) {
            var argsList = Arrays.asList(event.getMessage().getContentRaw().replaceFirst(Bot.getPrefix(event.getGuild().getIdLong()), "")
                    .split("\\s+"));
            String[] args = argsList.subList(1, argsList.size()).toArray(new String[argsList.size()]);
            if (event.getMessage().getContentRaw().matches(regex)) {
                var channel = event.getChannel();
                var shardManager = bot.getAPI();
                if (args[0].equalsIgnoreCase("restart")) {
                    if (args[1].equalsIgnoreCase("complete")) {
                        sendMessage(channel, Type.WARNING,":repeat: Restarting all Shards...").complete();
                        timer.schedule(timerTaskOf((v) -> bot.restart()), 5000);
                    } else if (args[1].matches("\\d")) {
                        if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                            sendMessage(channel, Type.ERROR,"Shard " + args[1] + " doesn't exist!").queue();
                            sendMessage(channel, Type.INFO, format("Shards total: `%s`\nShards %s: `%s`\nShards %s: `%s`" ,
                                    shardManager.getShardsTotal(), activated, shardManager.getShardsRunning(), deactivated, shardManager.getShardsQueued())).queue();
                        } else {
                            sendMessage(channel, Type.WARNING, ":repeat: Restarting Shard " + args[1]).complete();
                            timer.schedule(timerTaskOf((v) -> bot.restart(Integer.parseInt(args[1])-1)), 5000);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("status")) {
                    if (args[1].equals("complete")) {
                        sendMessage(channel, Type.INFO, format("Average ping: `%s`\nShards total: `%s`\nShards %s: `%s`\nShards %s: `%s`" ,
                                shardManager.getAveragePing(), shardManager.getShardsTotal(), activated, shardManager.getShardsRunning(), deactivated, shardManager.getShardsQueued())).queue();
                    } else if (args[1].matches("\\d")) {
                        sendMessage(channel, Type.INFO,"Ping from Shard " + args[1] + ": " + shardManager.getShardById(Integer.parseInt(args[1])-1).getPing() + "ms").queue();
                    }
                } else if (args[0].equalsIgnoreCase("shutdown")) {
                    if (args[1].equals("complete")) {
                        sendMessage(channel, Type.SHUTDOWN,":rotating_light: **Shutting down Bot**.").complete();
                        timer.schedule(timerTaskOf((v) -> bot.shutdown()), 5000);
                    } else if (args[1].matches("\\d")) {
                        if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                            sendMessage(channel, Type.ERROR,"Shard " + args[1] + " doesn't exist!").queue();
                            sendMessage(channel, Type.INFO, format("Shards total: `%s`\nShards %s: `%s`\nShards %s: `%s`" ,
                                    shardManager.getShardsTotal(), activated, shardManager.getShardsRunning(), deactivated, shardManager.getShardsQueued())).queue();
                        } else {
                            sendMessage(channel, Type.SHUTDOWN,":rotating_light: Shutting down Shard `" + args[1] + "`").complete();
                            timer.schedule(timerTaskOf((v) -> bot.shutdown(Integer.parseInt(args[1])-1)), 5000);
                        }
                    }
                }
            } else if (event.getMessage().getContentRaw().matches(prefix + "(?i)manage (commandsettings|cs) ((activate|on)|(deactivate|off))")) {
                if (args[1].equalsIgnoreCase("deactivate") || args[1].equalsIgnoreCase("off")) {
                    if (!bot.getCommandSettings().isActivated()) {
                        sendMessage(event.getChannel(), Type.SUCCESS, "`CommandSettings` already " + deactivated).queue();
                    } else {
                        bot.getCommandSettings().deactivate();
                        sendMessage(event.getChannel(), Type.SUCCESS,"`CommandSettings` successfully turned " + deactivated).queue();
                    }

                } else if (args[1].equalsIgnoreCase("activate") || args[1].equalsIgnoreCase("on")) {
                     if (bot.getCommandSettings().isActivated()) {
                         sendMessage(event.getChannel(), Type.SUCCESS, "`CommandSettings` already " + activated).queue();
                     } else {
                         bot.getCommandSettings().activate();
                         sendMessage(event.getChannel(), Type.SUCCESS, "`CommandSettings` successfully turned" + activated).queue();
                     }
                }
            } else if (event.getMessage().getContentRaw().matches(regexWrongUsage))
                sendMessage(event.getChannel(), Type.WARNING, "Wrong Usage.").queue((msg) -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
        } else if (event.getMessage().getContentRaw().matches(regex))
            sendMessage(event.getChannel(), Type.ERROR, "Nothing to see here. **Bot Owners only.** " + event.getMember().getAsMention()).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
    }

    private TimerTask timerTaskOf(Consumer<Void> consumer) {
        return new TimerTask() {
            @Override
            public void run() {
                consumer.accept(null);
            }
        };
    }
}
