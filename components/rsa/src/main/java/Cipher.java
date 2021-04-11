import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class Cipher {
    private BigInteger crypt(BigInteger message, Key key) {
        return message.modPow(key.getE(), key.getN());
    }

    public byte[] encrypt(String plainMessage, Key key, Logger logger) {
        byte[] bytes = plainMessage.getBytes(Charset.defaultCharset());
        logger.info("Performing encryption via m ^ e mod n");
        return crypt(new BigInteger(bytes), key).toByteArray();
    }

    public String decrypt(byte[] cipher, Key key, Logger logger) {
        byte[] msg = crypt(new BigInteger(cipher), key).toByteArray();
        logger.info("Performing decryption via m ^ d mod n");
        return new String(msg);
    }
}