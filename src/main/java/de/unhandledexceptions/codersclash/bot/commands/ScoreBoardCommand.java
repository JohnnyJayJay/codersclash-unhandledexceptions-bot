package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static java.lang.String.format;

public class ScoreBoardCommand implements ICommand {

    Database database;
    CommandSettings settings;

    public ScoreBoardCommand(Database database, CommandSettings settings) {
        this.database = database;
        this.settings = settings;
    }
    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_WRITE))
            return;
        if (Permissions.getPermissionLevel(member) >= 1) {
            String[] strings1 = "member,user".split(",");
            EmbedBuilder embedBuilder = new EmbedBuilder();
            ShardManager shardManager = member.getJDA().asBot().getShardManager();
            for (String type : strings1) {
                String table = "";
                String order = "";
                if ("user".equals(type)) {
                    table = "discord_user";
                    order = "user_lvl";
                } else if ("member".equals(type)) {
                    table = "discord_member";
                    order = "member_lvl";
                }
                StringBuilder builder = new StringBuilder();
                builder.append("Scoreboard\n```\n");
                var list = database.getScoreBoard(table, order);
                int i = 0;
                int i2 = 10;
                for (ScoreBoardUser user : list) {
                    if (i2 > i) {
                        if (shardManager.getUserById(user.getUserid()).isBot()) {
                            i2++;
                        } else {
                            if ("member".equals(type) && !member.getGuild().getId().equals(user.getGuildid())) {
                                i2++;
                            } else
                                builder.append(shardManager.getUserById(user.getUserid()).getName() + "#" +
                                        shardManager.getUserById(user.getUserid()).getDiscriminator() + "   " +
                                        "\tLevel: " + user.getLvl() + "   " + "\tXP: " + user.getXp() + "\n");
                        }
                        i++;
                    }
                }
                builder.append("```");
                boolean run = true;
                for (int i3 = 0; list.size() > i3 && run; i3++) {
                    var user = list.get(i3);
                    if (user.getUserid().equals(member.getUser().getId()) && !builder.toString().contains(":arrow_right: **Your place**\n")) {
                            run = false;
                            builder.append(":arrow_right: **Your place**\n" + String.valueOf(i3 + 1));
                    }
                }
                embedBuilder.addField(((type.equals("member")) ? "**Server**" : "**Global**"), builder.toString(), true);
            }sendMessage(textChannel, Messages.Type.INFO, "for `" + commandEvent.getGuild().getName() + "`", "Scoreboard", false, embedBuilder).queue();
        } else {
            noPermissionsMessage(textChannel, member);
        }
    }
    @Override
    public String info(Member member) {
        return format("**Description**: Gives you information about your score and the best scores.\n\n**Usage**: `%s[scoreboard|sb]`\n\n**Permission level**: `1`",
                settings.getPrefix(member.getGuild().getIdLong()));
    }

    public static class ScoreBoardUser {

        private String userid;
        private String guildid;
        private long xp;
        private long lvl;

        public ScoreBoardUser(String userid, String guildid, long member_xp, long member_lvl) {
            this.userid = userid;
            this.guildid = guildid;
            this.xp = member_xp;
            this.lvl = member_lvl;
        }

        public String getUserid() {
            return userid;
        }

        public String getGuildid() {
            return guildid;
        }

        public long getXp() {
            return xp;
        }

        public long getLvl() {
            return lvl;
        }
    }
}
