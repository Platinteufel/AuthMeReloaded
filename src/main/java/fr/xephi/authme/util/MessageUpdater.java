package fr.xephi.authme.util;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.output.MessageKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Updates a user's messages file with messages from the JAR files.
 */
public class MessageUpdater {

    private final File userFile;
    private final FileConfiguration userConfiguration;
    private final FileConfiguration localJarConfiguration;
    private final FileConfiguration defaultJarConfiguration;

    public MessageUpdater(File userFile, String jarFile, String jarDefaultsFile) throws Exception {
        if (!userFile.exists()) {
            throw new Exception("Local messages file does not exist");
        }
        this.userFile = userFile;
        this.userConfiguration = YamlConfiguration.loadConfiguration(userFile);

        localJarConfiguration = loadJarFileOrSendError(jarFile);
        defaultJarConfiguration = jarFile.equals(jarDefaultsFile)
            ? null
            : loadJarFileOrSendError(jarDefaultsFile);
        if (localJarConfiguration == null && defaultJarConfiguration == null) {
            throw new Exception("Could not load any JAR messages file to copy from");
        }
    }

    public void executeCopy(CommandSender sender) {
        // Set all missing messages from jar to userConfiguration
        boolean hasMissingMessages = false;
        for (MessageKey entry : MessageKey.values()) {
            final String key = entry.getKey();
            if (!userConfiguration.contains(key)) {
                String jarMessage = getMessageFromJar(key);
                if (jarMessage != null) {
                    hasMissingMessages = true;
                    userConfiguration.set(key, jarMessage);
                }
            }
        }

        if (!hasMissingMessages) {
            sender.sendMessage("No new messages to add");
            return;
        }

        // Save user configuration file
        try {
            userConfiguration.save(userFile);
            sender.sendMessage("Message file updated with new messages");
        } catch (IOException e) {
            sender.sendMessage("Could not save to messages file");
            ConsoleLogger.logException("Could not save new messages to file:", e);
        }
    }

    private String getMessageFromJar(String key) {
        String message = (localJarConfiguration == null ? null : localJarConfiguration.getString(key));
        if (message != null) {
            return message;
        }
        return (defaultJarConfiguration == null ? null : defaultJarConfiguration.getString(key));
    }

    private static FileConfiguration loadJarFileOrSendError(String jarPath) {
        InputStream stream = MessageUpdater.class.getResourceAsStream(jarPath);
        if (stream == null) {
            ConsoleLogger.info("Could not load '" + jarPath + "' from JAR file");
            return null;
        }
        InputStreamReader isr = new InputStreamReader(stream);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(isr);
        close(isr);
        close(stream);
        return configuration;
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
}
