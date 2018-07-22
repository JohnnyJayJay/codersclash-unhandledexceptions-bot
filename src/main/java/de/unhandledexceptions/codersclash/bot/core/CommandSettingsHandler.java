package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hax
 * @time 20:32 21.07.2018
 * @project codersclashunhandledexceptionsbot
 * @package de.unhandledexceptions.codersclash.bot.core
 * @class CommandSettingsHandler
 **/

public class CommandSettingsHandler {

    private CommandSettings commandSettings;
    private Map<ICommand, String> hashMap = new HashMap<>(); // Command, Alias aka label
    private List<ICommand> commands = new ArrayList<>(); // All commands
    private Map<String, ICommand> hashMap2 = new HashMap<>(); // labels, Command
    public CommandSettingsHandler(CommandSettings commandSettings) {
        this.commandSettings = commandSettings;
    }

    public CommandSettingsHandler put(@Nonnull ICommand executor, @Nonnull String... labels) {
        for (String label: labels) {
            if (!hashMap.containsKey(executor)) {
                hashMap.put(executor, label);
                commands.add(executor);
            }
            hashMap2.put(label, executor);
            commandSettings.put(executor, labels);
        }
        return this;
    }

    public Map<String, ICommand> getCommandfromLabel() {
        return hashMap2;
    }

    public Map<ICommand, String> getLabelFromCommand() {
        return hashMap;
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

}
