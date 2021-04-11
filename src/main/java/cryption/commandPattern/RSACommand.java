package cryption.commandPattern;

import config.Config;
import cryption.PortLoader;
import cryption.VerifyJar;

import java.io.File;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class RSACommand implements Callable<String>, ICommand {
    String strKeyFile;
    String message;
    File keyFile;

    public RSACommand(String strKeyFile, String message) {
        this.strKeyFile = strKeyFile;
        this.message = message;
    }

    @Override
    public String call() {
        String result = null;
        keyFile = new File(Config.instance.keyFiles + strKeyFile);
        if (keyFile.exists()) {
            String jarName = "rsa_cracker.jar";
            if (VerifyJar.verified(jarName)) {
                Config.instance.textArea.info("jar could not be verified - not loading corrupted jar");
            } else {
                try {
                    Object port = PortLoader.getPort(Config.instance.jarPath + jarName, "RSACracker");
                    Method method = port.getClass().getDeclaredMethod("decrypt", String.class, File.class);
                    var answer = method.invoke(port, message, keyFile);
                    if (answer == null) {
                        Config.instance.textArea.info("Problems Cracking Message probably invalid keyfile");
                    } else {
                        result = answer.toString();
                    }
                } catch (InvalidObjectException e) {
                    Config.instance.textArea.info("Keyfile could not be Processed");
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Config.instance.textArea.info("Keyfile " + strKeyFile + " not existing");
        }
        return result;
    }
}
