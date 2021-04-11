package handler;

import config.Config;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggingHandler {
    Handler handler;
    Logger logger;
    Boolean enabled;

    public LoggingHandler(){
        logger = java.util.logging.Logger.getLogger("file");
        enabled = false;
    }



    public void switchHandlerForLogging(){
        if (!enabled) {
            enabled = true;
            Config.instance.textArea.info("Logging has been enabled");
        } else {
            enabled = false;
            if (handler != null){
                logger.removeHandler(handler);
            }
            Config.instance.textArea.info("Logging has been disabled");
        }
    }

    public void createLogfile(String algorithmType, String direction) {
        if (!enabled) {
            return;
        }
        if (handler != null) {
            logger.removeHandler(handler);
        }
        try {
            handler = new FileHandler(String.format("log/%s_%s_%d.txt", direction, algorithmType, new Date().getTime() / 1000));
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);
        } catch (IOException e) {
            Config.instance.textArea.info("Error occurred during logFile creation");
        }
    }

    public Logger getLogger(){
        return logger;
    }
}
