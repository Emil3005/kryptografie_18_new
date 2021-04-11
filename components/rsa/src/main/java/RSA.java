import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class RSA {
    private static RSA instance = new RSA();
    public Port port;

    public RSA(){
        port = new Port();
    }

    public String encryptRSA(String plain, Key key, Logger logger){
        Cipher cipher = new Cipher();
        byte[] encryptedMessage = cipher.encrypt(plain, key, logger);
        return Base64.getEncoder().encodeToString(encryptedMessage);
    }

    public String decryptRSA(String encrypted, Key key, Logger logger){
        Cipher cipher = new Cipher();
        byte[] encryptedMessage = Base64.getDecoder().decode(encrypted);
        return cipher.decrypt(encryptedMessage, key, logger);
    }

    public static RSA getInstance() {
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

            Key key = new Key(parsed.n, parsed.e);
            logger.info("Parsing done public key: " + key.getE());

            logger.info("Encrypting message \"" + plain + "\"");
            return encryptRSA(plain, key, logger);
        }

        public String decrypt(File keyfile, String cipher, Logger logger) {

            Keyfile parsed = null;
            try {
                parsed = parseKeyfile(keyfile, logger);
            } catch (IOException e) {
                return null;
            }

            Key key = new Key(parsed.n, parsed.d);

            logger.info("Parsed keyfile private key: " + key.getE());

            logger.info("Decrypting message \"" + cipher + "\"");
            return decryptRSA(cipher, key, logger);
        }

        private Keyfile parseKeyfile(File keyfile, Logger logger) throws IOException {
            Gson gson = new Gson();
            Reader reader = null;
            try {
                logger.info("Loading keyfile" + keyfile);
                reader = Files.newBufferedReader(keyfile.toPath());
            } catch (Exception e) { }
            logger.info("Parsing keyfile");
            Keyfile parsed = gson.fromJson(reader, Keyfile.class);

            if (parsed.e == null || parsed.d == null || parsed.n == null) {
                logger.info("keyfile invalid");
                throw new IOException();
            }

            return parsed;
        }
    }

    public class Keyfile{
        BigInteger e;
        BigInteger d;
        BigInteger n;

        public Keyfile(){}

        public Keyfile(BigInteger e, BigInteger d, BigInteger n){
            this.e = e;
            this.d = d;
            this.n = n;
        }

    }
}
