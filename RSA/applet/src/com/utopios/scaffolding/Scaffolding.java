
package com.utopios.scaffolding;

import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.*;

public class Scaffolding extends Applet {
    
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private Cipher cipher;
    private byte[] buffer;
    
    protected Scaffolding() {
        
        KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_1024);
        keyPair.genKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
        buffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
    @Override 
    public void process(APDU apdu) {
        if (selectingApplet()) return;
        byte[] apduBuffer = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        cipher.init(privateKey, Cipher.MODE_ENCRYPT);
        short len = cipher.doFinal(apduBuffer, ISO7816.OFFSET_CDATA, dataLen, buffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(len);
        apdu.sendBytesLong(buffer, (short)0, len);
    }

    
    
}
