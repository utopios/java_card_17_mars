
package com.utopios.scaffolding;

import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.*;

public class RsaMailboxApplet extends Applet {
    

    private static final byte INS_GET_PUBLIC_KEY = (byte) 0x21;
    private static final byte INS_DECRYPT_MESSAGE = (byte) 0x20;

    private RSAPublicKey pubKey;
    private RSAPrivateKey privKey;
    
    private Cipher rsaCipher;
    private byte[] tempBuffer;

    protected RsaMailboxApplet() {
        
        KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_1024);
        keyPair.genKeyPair();
        pubKey = (RSAPublicKey) keyPair.getPublic();
        privKey = (RSAPrivateKey) keyPair.getPrivate();

        rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
        tempBuffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new RsaMailboxApplet();
    }

   
    @Override 
    public void process(APDU apdu) {
        if (selectingApplet()) return;
       
        byte[] buffer = apdu.getBuffer();
        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_GET_PUBLIC_KEY:
                sendPublicKey(apdu);
                break;
            case INS_DECRYPT_MESSAGE:
                decryptMessage(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                break;
        }
    }

    private void sendPublicKey(APDU apdu) {
        short offset = 0;
        short lenMod = pubKey.getModulus(tempBuffer, offset);
        offset += lenMod;
        short lenExp = pubKey.getExponent(tempBuffer, offset);
        offset += lenExp;

        apdu.setOutgoing();
        apdu.setOutgoingLength(offset);
        apdu.sendBytesLong(tempBuffer, (short) 0, offset);
    } 


    private void decryptMessage(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        rsaCipher.init(privKey, Cipher.MODE_DECRYPT);
        short outLen = rsaCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, tempBuffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(outLen);
        apdu.sendBytesLong(tempBuffer, (short) 0, outLen);
    }

    private void encryptMessage(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        rsaCipher.init(privKey, Cipher.MODE_ENCRYPT);
        short outLen = rsaCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, tempBuffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(outLen);
        apdu.sendBytesLong(tempBuffer, (short) 0, outLen);
    }

    
    
}
