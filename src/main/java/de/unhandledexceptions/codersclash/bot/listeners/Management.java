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

public class Management extends ListenerAdapter {

    private Bot bot;
    private Timer timer;

    public Management(Bot bot) {
        this.bot = bot;
        this.timer = new Timer();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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
                        channel.sendMessage("Alle Shards werden restartet...").complete();
                        timer.schedule(timerTaskOf((v) -> bot.restart()), 5000);
                    } else if (args[1].matches("\\d")) {
                        if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                            channel.sendMessage("Shard " + args[1] + " gibt es nicht!").queue();
                            channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                                    shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                        } else {
                            channel.sendMessage("Shard " + args[1] + " wird restartet").complete();
                            timer.schedule(timerTaskOf((v) -> bot.restart(Integer.parseInt(args[1])-1)), 5000);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("status")) {
                    if (args[1].equals("complete")) {
                        channel.sendMessage("Overall ping: " + shardManager.getAveragePing() + "ms").queue();
                        channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                                shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                    } else if (args[1].matches("\\d")) {
                        channel.sendMessage("Ping von Shard " + args[1] + ": " + shardManager.getShardById(Integer.parseInt(args[1])-1).getPing() + "ms").queue();
                    }
                } else if (args[0].equalsIgnoreCase("shutdown")) {
                    if (args[1].equals("complete")) {
                        channel.sendMessage("Bot wird heruntergefahren.").complete();
                        timer.schedule(timerTaskOf((v) -> bot.shutdown()), 5000);
                    } else if (args[1].matches("\\d")) {
                        if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                            channel.sendMessage("Shard " + args[1] + " gibt es nicht!").queue();
                            channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                                    shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                        } else {
                            channel.sendMessage("Shard " + args[1] + " wird heruntergefahren").complete();
                            timer.schedule(timerTaskOf((v) -> bot.shutdown(Integer.parseInt(args[1])-1)), 5000);
                        }
                    }
                }
            } else if (event.getMessage().getContentRaw().matches(prefix + "(?i)manage commandsettings ((activate)|(deactivate))")) {
                if (args[1].equalsIgnoreCase("deactivate") && bot.getCommandSettings().isActivated()) {
                    bot.getCommandSettings().deactivate();
                    event.getChannel().sendMessage("CommandSettings wurden deaktiviert.").queue();
                } else if (args[1].equalsIgnoreCase("activate") && !bot.getCommandSettings().isActivated()) {
                    bot.getCommandSettings().activate();
                    event.getChannel().sendMessage("CommandSettings wurden aktiviert.").queue();
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
