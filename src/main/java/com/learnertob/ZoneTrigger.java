/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.coords.WorldPoint;

/**
 * A reusable rectangular world-zone proximity trigger.
 *
 * A zone is defined by inclusive world bounds (minX..maxX, minY..maxY) on a
 * single plane, a display title, and a supplier that produces the list of
 * problems to show when the player enters the zone. The trigger tracks
 * inside/outside state so it fires once on ENTRY (not every tick) and re-fires
 * if the player leaves and comes back.
 *
 * This is the shared building block for the raid-start door check and every
 * Phase 3 per-room gate: each one is just a new ZoneTrigger with different
 * bounds and a different checks supplier.
 */
public class ZoneTrigger
{
    private final String title;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int plane;
    private final Supplier<List<String>> checks;

    private boolean inside = false;

    public ZoneTrigger(String title, int minX, int maxX, int minY, int maxY,
                       int plane, Supplier<List<String>> checks)
    {
        this.title  = title;
        this.minX   = Math.min(minX, maxX);
        this.maxX   = Math.max(minX, maxX);
        this.minY   = Math.min(minY, maxY);
        this.maxY   = Math.max(minY, maxY);
        this.plane  = plane;
        this.checks = checks;
    }

    /** True if the given world point is inside this zone (bounds inclusive). */
    public boolean contains(WorldPoint p)
    {
        return p != null
                && p.getPlane() == plane
                && p.getX() >= minX && p.getX() <= maxX
                && p.getY() >= minY && p.getY() <= maxY;
    }

    /**
     * Updates the inside/outside state from the player's current position and
     * returns true only on the tick the player crosses from outside to inside.
     */
    public boolean entered(WorldPoint p)
    {
        boolean now = contains(p);
        boolean justEntered = now && !inside;
        inside = now;
        return justEntered;
    }

    /** Clears the inside flag so the next entry fires fresh (e.g. after a region load). */
    public void reset()
    {
        inside = false;
    }

    public String getTitle()
    {
        return title;
    }

    /** Runs the attached checks and returns the list of problems (empty = all good). */
    public List<String> runChecks()
    {
        return checks.get();
    }
}