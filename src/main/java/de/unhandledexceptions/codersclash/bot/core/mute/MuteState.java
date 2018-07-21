package de.unhandledexceptions.codersclash.bot.core.mute;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public interface MuteState {

    void muteGuild(Member member);

    void mute(User user);

    void unMuteGuild();

    void unMuteUser(User user);

    boolean isGuildMuted();

    boolean isUserMuted(User user);
}
