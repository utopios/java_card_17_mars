
package com.utopios.scaffolding;

import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.*;

public class Scaffolding extends Applet {
    private Signature ecdsa;
    private ECPrivateKey privateKey;
    private byte[] buffer;
    
    protected Scaffolding() {
        
        KeyPair keyPair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_256);
        keyPair.genKeyPair();
        privateKey = (ECPrivateKey) keyPair.getPrivate();

        ecdsa = Signature.getInstance(Signature.ALG_ECDSA_SHA_256, false);
        buffer = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        
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

        ecdsa.init(privateKey, Signature.MODE_SIGN);
        short sigLen = ecdsa.sign(buf, ISO7816.OFFSET_CDATA, len, buffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(sigLen);
        apdu.sendBytesLong(buffer, (short) 0, sigLen);
    }

    
    
}
