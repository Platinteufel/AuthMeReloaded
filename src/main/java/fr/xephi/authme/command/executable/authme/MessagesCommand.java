package fr.xephi.authme.command.executable.authme;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.command.ExecutableCommand;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.properties.PluginSettings;
import fr.xephi.authme.util.MessageUpdater;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

/**
 * Messages command, updates the user's messages file with any missing files
 * from the provided file in the JAR.
 */
public class MessagesCommand implements ExecutableCommand {

    @Inject
    private Settings settings;

    @Override
    public void executeCommand(CommandSender sender, List<String> arguments) {
        String jarFilePath = "/messages/messages_" + settings.getProperty(PluginSettings.MESSAGES_LANGUAGE) + ".yml";
        try {
            new MessageUpdater(settings.getMessagesFile(),
                jarFilePath,
                settings.getDefaultMessagesFile())
            .executeCopy(sender);
        } catch (Exception e) {
            sender.sendMessage("Could not update messages: " + e.getMessage());
            ConsoleLogger.logException("Could not update messages:", e);
        }
    }
}
