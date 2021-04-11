

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class PortTest {
    @Test
    public void portTest(){
        String encrypted = "QbEoOdE=";
        File keyfile = new File("/home/liam/IdeaProjects/kryptografie/keyfiles/rsasmall.json");

        System.out.println(RSACracker.getInstance().port.decrypt(encrypted, keyfile));

    }
}
