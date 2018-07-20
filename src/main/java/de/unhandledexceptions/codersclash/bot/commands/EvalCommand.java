package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.entities.Vote;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Optional;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 17.07.2018
 */

public class EvalCommand implements ICommand {

    private final Config config;
    private final ShardManager shardManager;
    private final VoteCommand voteCommand;

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("Nashorn");

    public EvalCommand(Config config, ShardManager shardManager, VoteCommand voteCommand)
    {
        this.voteCommand = voteCommand;
        this.config = config;
        this.shardManager = shardManager;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {

        Long userId = member.getUser().getIdLong();
        if(config.getBotOwners().stream().noneMatch(userId::equals))
        {
            sendMessage(channel, Type.ERROR, "You don't have the permission to eval something!").queue();
            return;
        }

        if (event.getMessage().getContentRaw().contains("getToken"))
        {
            sendMessage(channel, Type.ERROR, "Token publishing is forbidden").queue();
            return;
        }

        try
        {
            SCRIPT_ENGINE.eval("var imports = new JavaImporter(" +

                    "Packages.net.dv8tion.jda.core," +
                    "java.lang," +
                    "java.util," +
                    "java.time" +
                    ");");
        } catch (ScriptException e)
        {
            e.printStackTrace();
        }

        SCRIPT_ENGINE.put("channel", channel);
        SCRIPT_ENGINE.put("author", member.getUser());
        SCRIPT_ENGINE.put("member", member);
        SCRIPT_ENGINE.put("guild", event.getGuild());
        SCRIPT_ENGINE.put("shardManager", shardManager);
        SCRIPT_ENGINE.put("votes", voteCommand.getVotes());
        SCRIPT_ENGINE.put("jda", event.getJDA());

        try
        {
            Object evaluation = SCRIPT_ENGINE.eval("{with (imports) {" + String.join(" ", args).replaceAll("#", "().") + "}};");

            if (evaluation != null)
            {
                new MessageBuilder().appendCodeBlock(evaluation.toString(), "Java").buildAll(MessageBuilder.SplitPolicy.NEWLINE, MessageBuilder.SplitPolicy.SPACE, MessageBuilder.SplitPolicy.ANYWHERE).forEach(msg -> channel.sendMessage(msg).queue());
            }
        } catch (ScriptException e)
        {
            channel.sendMessage(new EmbedBuilder().setTitle("An exception was thrown").setDescription(e.toString()).build()).queue();
        }
    }


}
