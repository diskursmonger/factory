package factory;

import factory.factory.Factory;
import factory.ui.gui.MainWindow;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Application {
    private static final Logger logger;

    static {
        tryInitLogger();

        logger = Logger.getLogger(Application.class.getSimpleName());
    }

    public static void main(String[] args) {
        final Factory factory;

        try {
            factory = new Factory();
            logger.fine("Created a factory instance");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create a factory instance: {0}", e.getMessage());
            return;
        }

        logger.fine("Creating main window");
        var mainWindow = new MainWindow(factory);

        logger.fine("Running main window");
        mainWindow.run();
    }

    private static void tryInitLogger() {
        try {
            var config = Application.class.getResourceAsStream("/factory/logging.properties");
            if (config == null) {
                throw new IOException("Cannot load " + "\"logging.properties\"");
            }
            LogManager.getLogManager().readConfiguration(config);
        } catch (IOException e) {
            System.out.println("Could not setup logger configuration: " + e.toString());
        }
    }
}