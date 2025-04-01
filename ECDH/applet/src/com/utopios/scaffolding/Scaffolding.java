
package com.utopios.scaffolding;

import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.*;

public class Scaffolding extends Applet {
    
    private ECPrivateKey privateKey;
    private byte[] sharedSecret;

    private KeyAgreement ecdh;
    
    protected Scaffolding() {
        
        KeyPair keyPair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_256);
        keyPair.genKeyPair();
        privateKey = (ECPrivateKey) keyPair.getPrivate();

        ecdh = KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH, false);
        sharedSecret = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_DESELECT);
        
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

        //Clé Alice
        ECPublicKey clientPub = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
        clientPub.setW(buf, ISO7816.OFFSET_CDATA, len);

        ecdh.init(privateKey);
        short secretLen = ecdh.generateSecret(clientPub, sharedSecret, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(secretLen);
        //Uniquement pour l'exemple, le sharedSecret est utilisé pour crypt ou décrypt
        //On envoit la clé public de l'applet
        apdu.sendBytesLong(sharedSecret, (short) 0, secretLen);
    }

    
    
}
