package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Regex;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;

public class MentionListener extends ListenerAdapter {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm");
    private EmbedBuilder builder = new EmbedBuilder();
    private Config config;

    public MentionListener(Config config) {
        this.config = config;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().matches(Regex.MEMBER_MENTION) && event.getMessage().getMentionedMembers().size() == 1
                && event.getMessage().getMentionedUsers().get(0).getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            String prefix = Bot.getPrefix(event.getGuild().getIdLong());
            var shardManager = event.getJDA().asBot().getShardManager();
            var stringBuilder = new StringBuilder();
            long members = 0;
            for (Guild guild : shardManager.getGuildCache()) {
                members += guild.getMemberCache().size();
            }
            config.getBotOwners().forEach((id) -> stringBuilder.append(String.format("`%#s` ", shardManager.getUserById((long) id))));
            builder.clear().addField("Name", config.getBotName(), true)
                    .addField("Default Prefix", "`" + config.getPrefix() + "`", true)
                    .addField("This Guild's Prefix", "`" + prefix + "`", true)
                    .addField("Birth", event.getJDA().getSelfUser().getCreationTime().format(formatter), true)
                    .addField("Help Command", "`" + prefix + "[help|helpme|commands] <command>`", false)
                    .addField("Creators", stringBuilder.toString(), false)
                    .addField("Guilds", Long.toString(shardManager.getGuildCache().size()), false)
                    .addField("Members", Long.toString(members), true)
                    .setColor(event.getGuild().getSelfMember().getColor());
            Messages.sendMessage(event.getChannel(), Messages.Type.NO_TYPE, "Introducing... me!", "Hi!", false, builder).queue();
            //Messages.sendMessage(event.getChannel(), Messages.Type.NO_TYPE, "Introducing... me!", "Hi!", false, builder).queue(this::reactionsAdd);
        }
    }

    /*private void reactionsAdd(Message msg) {
        msg.addReaction("\uD83C\uDDF9").queue();
        msg.addReaction("\uD83C\uDDF7").queue();
        msg.addReaction("\uD83C\uDDFE").queue();
        msg.addReaction("\uD83C\uDDE8").queue();
        msg.addReaction("\uD83C\uDDE6").queue();
        msg.addReaction("\uD83C\uDDED").queue();
    }*/
}
