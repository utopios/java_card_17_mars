
package com.utopios.scaffolding;


import javacard.framework.*;
import javacard.security.*;

public class AccessControlApplet extends Applet {

    // APDU Instructions
    private static final byte INS_ENROLL = (byte) 0x40;
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_SIGN_SESSION = (byte) 0x30;

    private static final byte PIN_MAX_TRIES = 3;

    private byte[] userId;
    private byte[] pinHash;
    private byte tries;
    private boolean authenticated;

    private ECPrivateKey privateKey;
    private Signature ecdsaSignature;
    private byte[] sigBuffer;

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new AccessControlApplet();
    }

    protected AccessControlApplet() {
        userId = new byte[16]; // UID max 16 octets
        pinHash = new byte[32]; // SHA-256 hash
        tries = PIN_MAX_TRIES;
        authenticated = false;

        // Génération de paire de clés ECC
        KeyPair keyPair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_256);
        keyPair.genKeyPair();
        privateKey = (ECPrivateKey) keyPair.getPrivate();

        // Signature ECDSA avec SHA-256
        ecdsaSignature = Signature.getInstance(Signature.ALG_ECDSA_SHA_256, false);

        sigBuffer = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);

        register();
    }

    public void process(APDU apdu) throws ISOException {
        if (selectingApplet()) return;

        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        switch (ins) {
            case INS_ENROLL:
                enroll(apdu);
                break;
            case INS_VERIFY_PIN:
                verifyPin(apdu);
                break;
            case INS_SIGN_SESSION:
                signSession(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void enroll(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        if (len < 6) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        short uidLen = buf[ISO7816.OFFSET_CDATA];
        Util.arrayCopy(buf, (short)(ISO7816.OFFSET_CDATA + 1), userId, (short) 0, uidLen);

        short pinLen = (short)(len - uidLen - 1);
        byte[] pin = JCSystem.makeTransientByteArray(pinLen, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(buf, (short)(ISO7816.OFFSET_CDATA + 1 + uidLen), pin, (short) 0, pinLen);

        MessageDigest sha = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        sha.doFinal(pin, (short) 0, pinLen, pinHash, (short) 0);

        tries = PIN_MAX_TRIES;
        authenticated = false;
    }

    private void verifyPin(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        if (tries == 0) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

        byte[] hashTest = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_DESELECT);
        MessageDigest sha = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        sha.doFinal(buf, ISO7816.OFFSET_CDATA, len, hashTest, (short) 0);

        if (Util.arrayCompare(pinHash, (short) 0, hashTest, (short) 0, (short) 32) == 0) {
            authenticated = true;
            tries = PIN_MAX_TRIES;
        } else {
            tries--;
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    private void signSession(APDU apdu) {
        if (!authenticated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        byte[] buf = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        ecdsaSignature.init(privateKey, Signature.MODE_SIGN);
        short sigLen = ecdsaSignature.sign(buf, ISO7816.OFFSET_CDATA, len, sigBuffer, (short) 0);

        apdu.setOutgoing();
        apdu.setOutgoingLength(sigLen);
        apdu.sendBytesLong(sigBuffer, (short) 0, sigLen);
    }
}
