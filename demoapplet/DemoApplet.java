import javacard.framework.ISO7816;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.Util;
import javacard.framework.ISOException;

public class DemoApplet extends Applet {

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new DemoApplet();
    }

    protected DemoApplet() {
        //Initialisation de la configuration de l'applet
    }

    @Override
    public void process(APDU apdu) {
        //Traitement des commandes APDU
        byte[] buffer = apdu.getBuffer();
        if(selectingApplet()) {
            return;
        }
        switch(buffer[ISO7816.OFFSET_INS]) {
            
            case 0x10:
                handle10(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void handle10(APDU apdu) {
       byte[] buffer = apdu.getBuffer();
       byte[] hello = {(byte)'h',(byte)'e',(byte)'l',(byte)'l',(byte)'o'};
       Util.arrayCopyNonAtomic(hello, (short)0, buffer, (short)0, (short) hello.length);
       apdu.setOutgoingAndSend((short)0, (short) hello.length);
    
    }
}