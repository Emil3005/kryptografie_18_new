import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class RSACracker {
    private static RSACracker instance = new RSACracker();

    private BigInteger e = BigInteger.ZERO;
    private BigInteger n = BigInteger.ZERO;

    public Port port;

    private RSACracker() {
        port = new Port();
    }

    public static RSACracker getInstance() {
        return instance;
    }

    public class Port {
        public String version() {
            return null;
        }

        public String decrypt(String encryptedMessage, File publicKeyfile) {
            return decryptMessage(encryptedMessage, publicKeyfile);
        }
    }

    private String decryptMessage(String encryptedMessage, File publicKeyfile) {

        try {
            loadKeyFile(publicKeyfile);
        } catch (IOException ioException) {
            return null;
        }


        byte[] bytes = Base64.getDecoder().decode(encryptedMessage);

        try {
            BigInteger plain = execute(new BigInteger(bytes));
            if (plain == null)
                return null;
            byte[] plainBytes = plain.toByteArray();
            return new String(plainBytes);
        } catch (RSACrackingException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void loadKeyFile(File keyfile) throws IOException {
        Gson gson = new Gson();
        Reader reader = null;

        try {
            reader = Files.newBufferedReader(keyfile.toPath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        Keyfile parsed = gson.fromJson(reader, Keyfile.class);

        if (parsed.e == null || parsed.d == null || parsed.n == null) throw new IOException();

        e = parsed.e;
        n = parsed.n;
    }

    private BigInteger execute(BigInteger cipher) throws RSACrackingException {
        BigInteger p, q, d;
        List<BigInteger> factorList = factorize(n);

        if (factorList == null)
            return null;

        if (factorList.size() != 2) {
            throw new RSACrackingException("cannot determine factors p and q");
        }

        p = factorList.get(0);
        q = factorList.get(1);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        d = e.modInverse(phi);
        return cipher.modPow(d, n);
    }

    public List<BigInteger> factorize(BigInteger n) {
        BigInteger two = BigInteger.valueOf(2);
        List<BigInteger> factorList = new LinkedList<>();

        if (n.compareTo(two) < 0) {
            throw new IllegalArgumentException("must be greater than one");
        }

        while (n.mod(two).equals(BigInteger.ZERO)) {
            factorList.add(two);
            n = n.divide(two);
            if (Thread.currentThread().isInterrupted())
                return null;
        }

        if (n.compareTo(BigInteger.ONE) > 0) {
            BigInteger factor = BigInteger.valueOf(3);
            while (factor.multiply(factor).compareTo(n) <= 0) {
                if (n.mod(factor).equals(BigInteger.ZERO)) {
                    factorList.add(factor);
                    n = n.divide(factor);
                } else {
                    factor = factor.add(two);
                }
                if (Thread.currentThread().isInterrupted())
                    return null;
            }
            factorList.add(n);
        }

        return factorList;
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