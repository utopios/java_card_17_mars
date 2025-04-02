import com.oracle.javacard.ams.script.APDUScript;
import com.oracle.javacard.ams.script.ScriptFailedException;
import com.oracle.javacard.ams.script.Scriptable;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class TestScript extends APDUScript {

    private List<CommandAPDU> commands = new LinkedList<>();
    private List<ResponseAPDU> responses = new LinkedList<>();
    private int index = 0;

    public List<ResponseAPDU> run(CardChannel channel) throws ScriptFailedException {
        return super.run(channel, c -> lookupIndex(c), r -> !isExpected(r));
    }

    @Override
    public TestScript append(Scriptable<CardChannel, CommandAPDU, ResponseAPDU> other) {
        super.append(other);
        return this;
    }

    public TestScript append(CommandAPDU apdu, ResponseAPDU expected) {
        super.append(apdu);
        this.commands.add(apdu);
        this.responses.add(expected);
        return this;
    }

    public TestScript append(CommandAPDU apdu) {
        super.append(apdu);
        return this;
    }

    private CommandAPDU lookupIndex(CommandAPDU apdu) {
        print(apdu);
        this.index = IntStream.range(0, this.commands.size())
                .filter(i -> apdu == this.commands.get(i))
                .findFirst()
                .orElse(-1);
        return apdu;
    }


    private boolean isExpected(ResponseAPDU response) {

        ResponseAPDU expected = (index < 0)? response : this.responses.get(index);
        if (!response.equals(expected)) {
            System.out.println("Received: ");
            print(response);
            System.out.println("Expected: ");
            print(expected);
            return false;
        }
        checkSW(expected.getSW(), response);
        checkArrayEquals(expected.getBytes(), response.getBytes());
        print(response);
        return true;
    }

    private void checkArrayEquals(byte[] expected, byte[] actual) throws ScriptFailedException {
        if (!Arrays.equals(expected, actual)) {
            throw new ScriptFailedException(
                    String.format(
                            "Échec de la comparaison de tableaux !%nAttendu: %s%nReçu   : %s",
                            Arrays.toString(expected),
                            Arrays.toString(actual)
                    )
            );
        }
    }
    private void checkSW(int expectedSW, ResponseAPDU actual) throws ScriptFailedException {
        if (actual.getSW() != expectedSW) {
            throw new ScriptFailedException(
                    String.format(
                            "SW incorrect ! (attendu 0x%04X, reçu 0x%04X)",
                            expectedSW, actual.getSW()
                    )
            );
        }
    }

    private static void print(CommandAPDU apdu) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X%02X%02X%02X %02X[", apdu.getCLA(), apdu.getINS(), apdu.getP1(), apdu.getP2(), apdu.getNc()));
        for (byte b : apdu.getData()) {
            sb.append(String.format("%02X", b));
        }
        sb.append("]");
        System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-C] %2$s %n", System.currentTimeMillis(), sb.toString());
    }

    private static void print(ResponseAPDU apdu) {
        byte[] bytes = apdu.getData();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-R] [%2$s] SW:%3$04X %n", System.currentTimeMillis(), sb.toString(), apdu.getSW());
    }
}
