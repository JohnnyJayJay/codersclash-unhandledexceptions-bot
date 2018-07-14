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

public class ScoreBoardCommand implements ICommand {

    Database database;

    public ScoreBoardCommand(Database database) {
        this.database = database;
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
            builder.append("**ScoreBoard**\n```\n");
            var list = database.getScoreBoard(table, order);
            int i =0;
            int i2 = 10;
            for (ScoreBoardUser user : list) {
                if (i2>i) {
                    if (shardManager.getUserById(user.getUserid()).isBot()) {
                        i2++;
                    } else {
                        if (type.equals("member")&&!member.getGuild().getId().equals(user.getGuildid())) {
                            i2++;
                        }
                        builder.append(shardManager.getUserById(user.getUserid()).getName() + "#" +
                                shardManager.getUserById(user.getUserid()).getDiscriminator() + "   " +
                                "\tLevel: " + user.getLvl() + "   " + "\tXP: " + user.getXp() + "\n");
                    }
                    i++;
                }
            }
            builder.append("```");
            for (int i3 =0; list.size()>i3; i3++) {
                var user = list.get(i3);
                if (user.getUserid().equals(member.getUser().getId())) {
                    if (!builder.toString().contains(":arrow_right: **Your place**\n")) {
                        builder.append(":arrow_right: **Your place**\n" + String.valueOf(i3+1));
                    }
                }
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
