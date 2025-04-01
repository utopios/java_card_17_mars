/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 *
 */

package com.oracle.jcclassic.samples.arrayview.MyShareable;

import javacard.framework.Shareable;

public interface SecureCounterShareable extends Shareable {
    short getCounter();
    void resetCounter();
}
