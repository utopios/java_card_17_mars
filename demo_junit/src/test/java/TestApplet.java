import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;


public class TestApplet {

    private Terminal terminal;
    private TestScript testScript;

    @BeforeEach
    public void init() {
        testScript = new TestScript();
    }

    @Test
    public void TestCase1() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, CardException {
        CAPElement capElement = new CAPElement("aid:a00000006203010C01","aid:a00000006203010C01", "aid:a00000006203010C0101","",true);
        String [] args = {""};
        terminal = new Terminal("aid:A000000151000000", Arrays.asList(capElement), args, testScript);
        terminal.init();
        terminal.append(Terminal.parseCommandAPDU(""));
        terminal.run();
        //Récupère la ou les réponses
        //testScript.getResponses().get()
        //Assert avec junit
    }
}
