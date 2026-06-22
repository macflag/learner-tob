package com.learnertob;
public enum OverlayFontSize {
    SMALL("Small"), MEDIUM("Medium"), LARGE("Large");
    private final String d; OverlayFontSize(String d){this.d=d;} @Override public String toString(){return d;}
}