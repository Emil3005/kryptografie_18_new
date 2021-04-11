package cryption.loader;

import content.Content;

import java.io.File;
import java.io.InvalidObjectException;
import java.net.URL;
import java.net.URLClassLoader;

public class PortLoader {

    public static Object getPort (String jarPath, String className) throws InvalidObjectException {

        try {

            URL[] url = new URL[]{new File(jarPath).toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(url, Content.class.getClassLoader());
            Class<?> aClass = Class.forName(className, true, classLoader);
            Object instance = aClass.getMethod("getInstance").invoke(null);
            return aClass.getDeclaredField("port").get(instance);

        }
        catch (Exception e) {

            throw new InvalidObjectException("getting port from jar failed!");

        }
    }
}
