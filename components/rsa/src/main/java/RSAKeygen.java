import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAKeygen {
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger n;
    private final BigInteger t;
    private final BigInteger e;
    private final BigInteger d;
    private final Key publicKey;
    private final Key privateKey;

    public RSAKeygen(int keyLength) {
        SecureRandom randomGenerator = new SecureRandom();

        p = new BigInteger(keyLength, 100, randomGenerator).nextProbablePrime();
        q = new BigInteger(keyLength, 100, randomGenerator).nextProbablePrime();

        n = p.multiply(q);
        t = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = getCoPrime(t);
        d = e.modInverse(t);

        publicKey = new Key(n, e);

        privateKey = new Key(n, d);
    }

    public boolean isCoPrime(BigInteger c, BigInteger n) {
        BigInteger one = new BigInteger("1");
        return c.gcd(n).equals(one);
    }

    public BigInteger getCoPrime(BigInteger n) {
        BigInteger result = new BigInteger(n.toString());
        BigInteger one = new BigInteger("1");
        BigInteger two = new BigInteger("2");
        result = result.subtract(two);

        while (result.intValue() > 1) {
            if (result.gcd(n).equals(one))
                break;
            result = result.subtract(one);
        }

        return result;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getT() {
        return t;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getD() {
        return d;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public Key getPrivateKey() {
        return privateKey;
    }
}