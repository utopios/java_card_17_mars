
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.JCSystem;

public class Scaffolding extends Applet {
    

    
    private short counter;
    private short counter2;
    private byte[] tempBuffer

    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        counter = 0;
        tempBuffer = new byte[64];
        tempBuffer = JCSystem.makeTransientByteArray((short)64, JCSystem.CLEAR_ON_RESET);
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

    private void transactionOperation() {
        JCSystem.beginTransaction();

        try {
            counter++;
            counter2++;
            JCSystem.commitTransaction();
        }catch(Exception ex) {
            JCSystem.abortTransaction();
        }
    }

   
   @Override 
   public void process(APDU apdu) {
        
    }

    
    
}
