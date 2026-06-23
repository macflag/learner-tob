/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;
public enum OverlayDismiss {
    CLICK("Click to close"),
    SECONDS_3("3 seconds"),
    SECONDS_5("5 seconds"),
    SECONDS_10("10 seconds");
    private final String d; OverlayDismiss(String d){this.d=d;} @Override public String toString(){return d;}
}