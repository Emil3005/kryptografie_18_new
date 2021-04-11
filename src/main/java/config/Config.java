package config;

import handler.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

public enum Config {
    instance;

    public final String jarPath = "libs/";

    public final String keyFiles = "keyfiles/";

    public final String jdkPath = "C:\\Program Files\\Java\\jdk-15.0.2\\bin\\";

    public final String logDir =  "log";

    public final java.util.logging.Logger textArea = java.util.logging.Logger.getLogger("textarea");

    public final Map<String, String> intrudedChannels = new HashMap<>();

    public final LoggingHandler loggingHandler = new LoggingHandler();


}
