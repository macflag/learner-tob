package com.learnertob;

public enum Role
{
    SOUTH_FREEZE("SFRZ"),
    NORTH_FREEZE("NFRZ"),
    RANGED("RDPS"),
    MELEE("MDPS");

    private final String displayName;

    Role(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}