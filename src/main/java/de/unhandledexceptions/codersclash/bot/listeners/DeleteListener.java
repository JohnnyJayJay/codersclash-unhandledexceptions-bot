package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Database;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class DeleteListener extends ListenerAdapter {

    private Database database;

    public DeleteListener(Database database) {
        this.database = database;
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        database.deleteGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        database.deleteMember(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        database.deleteMember(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        database.createGuildIfNotExists(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }
}
