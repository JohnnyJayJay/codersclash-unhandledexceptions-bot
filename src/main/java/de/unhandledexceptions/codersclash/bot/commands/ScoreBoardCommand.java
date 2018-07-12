package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.ScoreBoardUser;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;

public class ScoreBoardCommand implements ICommand {

    Database database;

    public ScoreBoardCommand(Database database) {
        this.database= database;
    }

    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        String[] strings1 = "user,member".split(",");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        ShardManager shardManager = member.getJDA().asBot().getShardManager();
        for (String type:strings1) {
            String table = "";
            String order = "";
            if (type.equals("user")) {
                table = "discord_user";
                order = "user_lvl";
            } else if (type.equals("member")) {
                table = "discord_member";
                order = "member_lvl";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("**ScoreBoard**\n");
            var list = database.getScoreBoard(table, order);
            for (int i =0; 10>i; i++) {
                var user = list.get(i);
                var name = shardManager.getUserById(user.getUserid()).getName();
                System.out.println(name);
                builder.append(name+"   "+"Level: "+user.getLvl()+"   "+"XP: "+user.getXp()+"\n");
            }
            embedBuilder.addField(((type.equals("member")) ? "Guild" : "User"), builder.toString(), true);
        }
        Messages.sendMessage(textChannel, Messages.Type.INFO, "ScoreBoard", "Scoreboardtitle", true, embedBuilder).queue();
    }

    @Override
    public String info(Member member) {
        return null;
    }
}
