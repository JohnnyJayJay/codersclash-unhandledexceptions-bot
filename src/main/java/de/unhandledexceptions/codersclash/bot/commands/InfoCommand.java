package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
//TODO ganzer command
public class InfoCommand implements ICommand {
    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        var shardManager = commandEvent.getJDA().asBot().getShardManager();
        EmbedBuilder builder = new EmbedBuilder();
        for (JDA jda : shardManager.getShards()) {
            builder.addField("Shard "+jda.getShardInfo().getShardId(), "Guilds: "+ jda.getGuilds().size()+"\nUser: "+jda.getUsers().size(), true);
        }
        sendMessage(textChannel, Messages.Type.INFO, "Here are the Shardinformation from the Bot.", "Informations", true, builder).queue();
    }

    @Override
    public String info(Member member) {
        return null;
    }
}
