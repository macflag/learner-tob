/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;
public enum OverlayPosition {
    TOP_LEFT("Top Left"), TOP_CENTER("Top Center"), TOP_RIGHT("Top Right"),
    CENTER("Center"), BOTTOM_LEFT("Bottom Left"), BOTTOM_RIGHT("Bottom Right");
    private final String d; OverlayPosition(String d){this.d=d;} @Override public String toString(){return d;}
}