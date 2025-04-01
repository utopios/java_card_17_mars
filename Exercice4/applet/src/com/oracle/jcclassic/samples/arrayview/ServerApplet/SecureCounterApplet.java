/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 *
 */

package com.oracle.jcclassic.samples.arrayview.ServerApplet;

import com.oracle.jcclassic.samples.arrayview.MyShareable.SecureCounterShareable;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.MultiSelectable;
import javacard.framework.Shareable;
import javacard.framework.Util;

public class SecureCounterApplet extends Applet implements SecureCounterShareable {
    private short counter;
    private static final byte[] MONITOR_AID = { (byte) 0xA0, 0x00, 0x00, 0x00, (byte) 0x62, 0x03, 0x01, 0x0C, 0x0D, 0x01 };
    private AID authrizedAID;

    protected SecureCounterApplet() {
        counter = 0;
        authrizedAID = new AID(MONITOR_AID, (short)0, (byte)MONITOR_AID.length);
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new SecureCounterApplet();
    }
    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        if(clientAID == null || !clientAID.equals(authrizedAID)) {
            return null;
        }
        return this;
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;
        counter++;
    }

    public short getCounter() {
        counter += 1;
        return counter;
    }

    public void resetCounter() {
        counter = 0;
    }
}
