package handler;

import config.Config;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggingHandler {
    Handler handler;
    Logger logger;
    Boolean enabled;

    public LoggingHandler() {
        logger = java.util.logging.Logger.getLogger("file");
        enabled = false;
    }

    public void createLogfile(String algorithmType, String direction) {
        if (!enabled) {
            return;
        }
        if (handler != null) {
            logger.removeHandler(handler);
        }
        try {
            long date = new Date().getTime() / 1000;
            handler = new FileHandler(String.format("log/" + direction + "_" + algorithmType + "_" + date + ".txt"));
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);
        } catch (IOException e) {
            Config.instance.textArea.info("It seems an Error occurred during logFile creation");
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void switchHandler() {
        if (!enabled) {
            enabled = true;
            Config.instance.textArea.info("Logging is enabled");
        } else {
            enabled = false;
            if (handler != null) {
                logger.removeHandler(handler);
            }
            Config.instance.textArea.info("Logging is disabled");
        }
    }
}
