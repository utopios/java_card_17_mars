import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.oracle.javacard.ams.AMSession;
import com.oracle.javacard.ams.config.CAPFile;

import javax.smartcardio.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Properties;


public class Terminal {
    private List<CAPElement> capElements;
    private AMService ams;
    private Properties props;
    private AMSession deploy;
    private AMSession undeploy;
    private CardTerminal cardTerminal;
    private TestScript testScript;
    private String said;

    public Terminal(String said,List<CAPElement> capElements, String[] args, TestScript testScript) throws NoSuchProviderException, IOException {
        this.said  = said;
        ams = AMServiceFactory.getInstance("GP2.2");
        props.load(new FileInputStream(getArg(args, "props")));
        ams.setProperties(props);
        this.capElements = capElements;
        this.testScript = testScript;
    }

    public void init() throws IOException, NoSuchAlgorithmException, CardException, NoSuchProviderException {
        deploy = ams.openSession(said);
        for(CAPElement capElement : capElements) {
            deploy = deploy.load(capElement.getAid(), capElement.getCapFile().getBytes());
        }

        for(CAPElement capElement : capElements) {
            if(capElement.isToInstall())
                deploy = deploy.install(capElement.getPackageAid(),capElement.getAid(), capElement.getClassAid());
        }
        deploy.close();
        testScript.append(deploy);

        cardTerminal = getTerminal("socket", "127.0.0.1", "9025"); // or getTerminal("pcsc");


    }

    public void run() throws CardException {
        // Wait some seconds to allow connections
        if (cardTerminal.waitForCardPresent(10000)) {
            System.out.println("Connection to simulator established: "+ cardTerminal.getName());
            Card card = cardTerminal.connect("*");
            System.out.println(getFormattedATR(card.getATR().getBytes()));

            List<ResponseAPDU> responses = testScript.run(card.getBasicChannel());
            card.disconnect(true);

            System.out.println("Responses count: " + responses.size());
        }
        else {
            System.out.println("Connection to simulator failed");
        }
    }




    private static CardTerminal getTerminal(String... connectionParams) throws NoSuchAlgorithmException, NoSuchProviderException, CardException {

        TerminalFactory tf;
        String connectivityType = connectionParams[0];
        if (connectivityType.equals("socket")) {
            String ipaddr = connectionParams[1];
            String port = connectionParams[2];
            tf = TerminalFactory.getInstance("SocketCardTerminalFactoryType",
                    List.of(new InetSocketAddress(ipaddr, Integer.parseInt(port))),
                    "SocketCardTerminalProvider");
        } else {
            tf = TerminalFactory.getDefault();
        }
        return tf.terminals().list().get(0);
    }

    private static String getArg(String[] args, String argName) throws IllegalArgumentException {

        String value = null;

        for (String param : args) {
            if (param.startsWith("-" + argName + "=")) {
                value = param.substring(param.indexOf('=') + 1);
            }
        }

        if(value == null || (value.length() == 0)) {
            throw new IllegalArgumentException("Mandatory argument [" + argName + "] is missing");
        }
        return value;
    }

    private static String getFormattedATR(byte[] ATR) {
        StringBuilder sb = new StringBuilder();
        for (byte b : ATR) {
            sb.append(String.format("%02X ", b));
        }
        return String.format("ATR: [%s]", sb.toString().trim());
    }

    public void append(CommandAPDU apdu, ResponseAPDU expected) {
        testScript.append(apdu, expected);
    }

    public void append(CommandAPDU apdu) {
        testScript.append(apdu);
    }

    public static CommandAPDU parseCommandAPDU(String input) {
        String[] parts = input.trim().split(" ");
        byte[] commandBytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            commandBytes[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return new CommandAPDU(commandBytes);
    }


}
