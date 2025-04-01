
package com.utopios.scaffolding;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class Scaffolding extends Applet {
    
    private MessageDigest digest;
    private byte[] hashBuffer;
    
    protected Scaffolding() {
        digest = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        hashBuffer = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_DESELECT);
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
    @Override 
    public void process(APDU apdu) {
        if (selectingApplet()) return;
        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        digest.doFinal(buf, ISO7816.OFFSET_CDATA, len, hashBuffer, (short) 0);
        apdu.setOutgoing();
        apdu.setOutgoingLength((short) 32);
        apdu.sendBytesLong(hashBuffer, (short) 0, (short) 32);
    }
}
