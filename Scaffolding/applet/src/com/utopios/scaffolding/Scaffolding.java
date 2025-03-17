
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class Scaffolding extends Applet {
    
    
    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if(selectingApplet()) {
            return;
        }
        switch(buffer[ISO7816.OFFSET_INS]) {
            
            case 0x10:
                handle10(apdu);
                break;
            case 0x30:
                short dataLength = apdu.setIncomingAndReceive();
                byte[] data = new byte[dataLength];
                Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, data, (short)0, dataLength);
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
