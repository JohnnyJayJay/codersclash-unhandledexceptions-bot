package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public class Management implements ICommand {

    private Bot bot;

    public Management(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (Bot.getBotOwners().contains(member.getUser().getIdLong()) && args.length == 2) {
            var shardManager = bot.getAPI();
            if (args[0].equalsIgnoreCase("restart")) {
                if (args[1].equalsIgnoreCase("complete")) {
                    channel.sendMessage("Alle Shards werden restartet...").complete();
                    bot.restart();
                } else if (args[1].matches("\\d")) {
                    if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                        channel.sendMessage("Shard " + args[1] + " gibt es nicht!").queue();
                        channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                                shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                    } else {
                        channel.sendMessage("Shard " + args[1] + " wird restartet").complete();
                        bot.restart(Integer.parseInt(args[1]));
                    }
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args[1].equalsIgnoreCase("general")) {
                    channel.sendMessage("Overall ping: " + shardManager.getAveragePing() + "ms").queue();
                    channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                            shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                } else if (args[1].matches("\\d")) {
                    channel.sendMessage("Ping von Shard " + args[1] + ": " + shardManager.getShardById(Integer.parseInt(args[1])).getPing() + "ms").queue();
                }
            } else if (args[0].equalsIgnoreCase("shutdown")) {
                if (args[1].equalsIgnoreCase("complete")) {
                    channel.sendMessage("Bot wird heruntergefahren.").complete();
                    bot.shutdown();
                } else if (args[1].matches("\\d")) {
                    if (shardManager.getShardsTotal() <= Short.parseShort(args[1])) {
                        channel.sendMessage("Shard " + args[1] + " gibt es nicht!").queue();
                        channel.sendMessage("Gesamtzahl Shards: " + shardManager.getShardsTotal() + "\nAktive Shards: " +
                                shardManager.getShardsRunning() + "\nInaktive Shards: " + shardManager.getShardsQueued()).queue();
                    } else {
                        channel.sendMessage("Shard " + args[1] + " wird heruntergefahren").complete();
                        bot.shutdown(Integer.parseInt(args[1]));
                    }
                }
            }
        }
    }

    @Override
    public String info(Member member) {
        return "Sorry, but you are not allowed to see that.";
    }
}
