package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Hax
 * @time 20:32 21.07.2018
 * @project codersclashunhandledexceptionsbot
 * @package de.unhandledexceptions.codersclash.bot.core
 * @class CommandSettingsHandler
 **/

public class CommandSettingsHandler {

    private CommandSettings commandSettings;
    private HashMap<ICommand, String> hashMap = new HashMap<>(); // Command, Alias aka label
    private ArrayList<ICommand> commands = new ArrayList<>(); // All commands
    private HashMap<String, ICommand> hashMap2 = new HashMap<>(); // labels, Command
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

    public HashMap<String, ICommand> getCommandfromLabel() {
        return hashMap2;
    }

    public HashMap<ICommand, String> getLabelFromCommand() {
        return hashMap;
    }

    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

}
