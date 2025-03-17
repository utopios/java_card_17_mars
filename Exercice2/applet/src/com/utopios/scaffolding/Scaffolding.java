
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;

public class Scaffolding extends Applet {
    
    private static final byte INS_VERIFY_PIN  = (byte) 0x20;
    private static final byte INS_CHANGE_PIN  = (byte) 0x21;
    private static final byte INS_UNBLOCK_PIN = (byte) 0x55;

    private static final byte PIN_MAX_TRIES = 3;
    private static final byte PIN_MAX_SIZE  = 4;

    private static final byte PUK_MAX_TRIES = 5;
    private static final byte PUK_SIZE      = 8;
    
    private static final short SW_CARD_BLOCKED = (short) 0x6283;
    private static final short SW_PIN_BLOCKED  = (short) 0x6983;

    private OwnerPIN pin;

    private byte[] pukValue;
    private byte pukTriesRemaining;

    private boolean cardBlocked;
    
    protected Scaffolding() {
        pin = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_SIZE);
        byte[] defaultPin = { '1','2','3','4' };
        pin.update(defaultPin, (short) 0, (byte) defaultPin.length);

        pukValue = new byte[PUK_SIZE];
        byte[] defaultPuk = { '1','2','3','4','5','6','7','8' };
        Util.arrayCopyNonAtomic(defaultPuk, (short)0, pukValue, (short)0, (short)defaultPuk.length);
        pukTriesRemaining = PUK_MAX_TRIES;

        cardBlocked = false;
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        if (cardBlocked) {
            ISOException.throwIt(SW_CARD_BLOCKED);
        }

        switch(ins) {
            case INS_VERIFY_PIN:
                verifyPin(apdu);
                break;
            case INS_CHANGE_PIN:
                changePin(apdu);
                break;
            case INS_UNBLOCK_PIN:
                unblockPin(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
   }

   private void verifyPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];       
        short dataOffset = ISO7816.OFFSET_CDATA;

        if (lc == 0 || lc > PIN_MAX_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Tenter la vérification
        boolean pinOk = pin.check(buffer, dataOffset, lc);
        if (!pinOk) {
            // PIN incorrect => pin.check() décrémente déjà le compteur
            // Si le compteur d'essais PIN atteint 0, le PIN est bloqué
            if (pin.getTriesRemaining() == 0) {
                // PIN bloqué => on peut renvoyer un SW explicite
                ISOException.throwIt(SW_PIN_BLOCKED);
            } else {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }
        // Si on arrive ici, le PIN est vérifié et "authentifié"
    }

    private void changePin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];
        short dataOffset = ISO7816.OFFSET_CDATA;

        // Vérifier que la session est authentifiée (PIN validé)
        if (!pin.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        // Vérifier la longueur du nouveau PIN
        if (lc == 0 || lc > PIN_MAX_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Mettre à jour la valeur du PIN
        pin.update(buffer, dataOffset, lc);

        
        // nouvelle vérification la prochaine fois
        pin.reset();
    }

    private void unblockPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];
        short dataOffset = ISO7816.OFFSET_CDATA;

        // Vérifier la longueur de PUK reçue
        if (lc != PUK_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Vérifier si le compteur de tentatives PUK n'est pas déjà à 0
        if (pukTriesRemaining == 0) {
            // La carte est déjà bloquée à cause du PUK épuisé
            // On met "cardBlocked" à true pour bloquer toutes les opérations ultérieures
            cardBlocked = true;
            ISOException.throwIt(SW_CARD_BLOCKED);
        }

        // Comparer le PUK reçu au PUK stocké
        if (Util.arrayCompare(buffer, dataOffset, pukValue, (short)0, (short)PUK_SIZE) == 0) {
            // PUK correct => débloquer le PIN
            pin.resetAndUnblock();

            // Réinitialiser le PIN à une valeur par défaut (ex: "1234")
            byte[] newPin = { '1','2','3','4' };
            pin.update(newPin, (short) 0, (byte) newPin.length);

            // Restaurer le compteur de tentatives PUK
            pukTriesRemaining = PUK_MAX_TRIES;

        } else {
            // PUK incorrect => décrémenter et vérifier si on arrive à 0
            pukTriesRemaining--;
            if (pukTriesRemaining == 0) {
                // Bloquer la carte
                cardBlocked = true;
                ISOException.throwIt(SW_CARD_BLOCKED);
            // } else {
                // Sinon, on lève un SW "sécurité non satisfaite"
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }
    }
}
