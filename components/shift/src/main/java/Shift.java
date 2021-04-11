import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Shift {
    private static Shift instance = new Shift();
    public Port port;

    public Shift(){
        port = new Port();
    }

    public String encryptRSA(String plain, int key, Logger logger){
        CaesarCipher cipher = new CaesarCipher(key);
        return cipher.encrypt(plain, logger);
    }

    public String decryptRSA(String encrypted, int key, Logger logger){
        CaesarCipher cipher = new CaesarCipher(key);
        return cipher.decrypt(encrypted, logger);
    }

    public static Shift getInstance() {
        return instance;
    }

    public class Port {
        public String version() {
            return null;
        }

        public String encrypt(File keyfile, String plain, Logger logger) {

            Keyfile parsed = null;
            try {
                parsed = parseKeyfile(keyfile, logger);
            } catch (IOException e) {
                return null;
            }

            logger.info("Parsed keyfile key: " + parsed.key);

            logger.info("Encrypting message \"" + plain + "\"");
            return encryptRSA(plain, parsed.key, logger);
        }

        public String decrypt(File keyfile, String cipher, Logger logger) {
            Keyfile parsed = null;
            try {
                parsed = parseKeyfile(keyfile, logger);
            } catch (IOException e) {
                return null;
            }

            logger.info("Parsed keyfile key: " + parsed.key);

            logger.info("Decrypting message \"" + cipher + "\"");
            return decryptRSA(cipher, parsed.key, logger);
        }

        private Keyfile parseKeyfile(File keyfile, Logger logger) throws IOException {
            Gson gson = new Gson();
            Reader reader = null;
            try {
                logger.info("Loading keyfile");
                reader = Files.newBufferedReader(keyfile.toPath());
            } catch (Exception e) { }
            logger.info("Parsing keyfile" + keyfile);
            Keyfile parsed = gson.fromJson(reader, Keyfile.class);
            if (parsed.key == 0) {
                logger.info("keyfile invalid");
                throw new IOException();
            }
            return parsed;
        }
    }

    public class Keyfile{
        int key;

        public Keyfile(){}

        public Keyfile(int key){
            this.key = key;
        }

    }

}
