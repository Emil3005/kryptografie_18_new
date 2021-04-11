package cryption.verify;

import config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VerifyJar

{


    public static boolean verified(String strJarFileName) {
        boolean isComponentAccepted;
        isComponentAccepted = false;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(Config.instance.jdkPath + "jarsigner", "-verify", Config.instance.jarPath + strJarFileName);
            Process processInstance = processBuilder.start();

            processInstance.waitFor();

            InputStream inputStream = processInstance.getInputStream();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            if ((line = bufferedReader.readLine()) != null) {
                do {
                    System.out.println(line);
                    if (line.contains("verified")) {
                        isComponentAccepted = true;
                    }
                } while ((line = bufferedReader.readLine()) != null);
            }

        } catch (IOException e) {
            Config.instance.textArea.info("IOException while verifying " + strJarFileName);
        } catch (InterruptedException e) {
            Config.instance.textArea.info("InterruptedException while verifying " + strJarFileName);
        }
        return !isComponentAccepted;
    }
}
