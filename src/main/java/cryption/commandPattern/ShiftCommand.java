package cryption.commandPattern;

import config.Config;
import cryption.PortLoader;
import cryption.VerifyJar;

import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ShiftCommand implements Callable<String>, ICommand{
    String message;

    public ShiftCommand(String message) {
        this.message = message;
    }

    @Override
    public String call() {
        String result = null;
        String jarName = "shift_cracker.jar";
        if (VerifyJar.verified(jarName)) {
            Config.instance.textArea.info("jar could not be verified - not loading corrupted jar");
        } else {
            try {
                Object port = PortLoader.getPort(Config.instance.jarPath + jarName, "ShiftCracker");
                Method method = port.getClass().getDeclaredMethod("decrypt", String.class);
                result = method.invoke(port, message).toString();
            } catch (InvalidObjectException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | SecurityException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
