import java.util.logging.Logger;

public class CaesarCipher {
    private final int key;

    public CaesarCipher(int key) {
        this.key = key;
    }

    public String encrypt(String plainText, Logger logger) {
        StringBuilder stringBuilder = new StringBuilder();

        logger.info("Shifting plain text by " + key + " to encrypt");
        for (int i = 0; i < plainText.length(); i++) {
            char character = (char) (plainText.codePointAt(i) + key);
            stringBuilder.append(character);
        }

        return stringBuilder.toString();
    }

    public String decrypt(String cipherText, Logger logger) {
        StringBuilder stringBuilder = new StringBuilder();

        logger.info("Shifting cipher backwards by " + key + " to decrypt");
        for (int i = 0; i < cipherText.length(); i++) {
            char character = (char) (cipherText.codePointAt(i) - key);
            stringBuilder.append(character);
        }

        return stringBuilder.toString();
    }
}
