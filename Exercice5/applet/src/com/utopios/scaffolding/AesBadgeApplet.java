
package com.utopios.scaffolding;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class AesBadgeApplet extends Applet {
    
    private AESKey aesKey;
    private Cipher cipher;
    private byte[] buffer;
    private byte[] userId;

    
    protected AesBadgeApplet() {
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        byte[] keydata = new byte[16];
        RandomData random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        random.generateData(keydata, (short)0, (short) 16);
        aesKey.setKey(keydata, (short) 0);
        cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        buffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        userId = new byte[] {
            'U', 'S', 'E', 'R', '_', '0', '0', '1', 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new AesBadgeApplet();
    }

   
    @Override 
    public void process(APDU apdu) {
        if (selectingApplet()) return;
        byte[] apduBuffer = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        cipher.init(aesKey, Cipher.MODE_ENCRYPT);
        short len = cipher.doFinal(userId, (short)0, (short)16, buffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(len);
        apdu.sendBytesLong(buffer, (short)0, len);
    }
}
