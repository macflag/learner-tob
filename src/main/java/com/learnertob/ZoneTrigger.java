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
    /** Optional constraint on which edge the player must cross to trigger. */
    public enum EntrySide { ANY, WEST, EAST, NORTH, SOUTH }

    private final String title;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int plane;
    private final Supplier<List<String>> checks;
    private final EntrySide entrySide;

    private boolean inside = false;
    private WorldPoint last = null;

    public ZoneTrigger(String title, int minX, int maxX, int minY, int maxY,
                       int plane, Supplier<List<String>> checks)
    {
        this(title, minX, maxX, minY, maxY, plane, checks, EntrySide.ANY);
    }

    public ZoneTrigger(String title, int minX, int maxX, int minY, int maxY,
                       int plane, Supplier<List<String>> checks, EntrySide entrySide)
    {
        this.title     = title;
        this.minX      = Math.min(minX, maxX);
        this.maxX      = Math.max(minX, maxX);
        this.minY      = Math.min(minY, maxY);
        this.maxY      = Math.max(minY, maxY);
        this.plane     = plane;
        this.checks    = checks;
        this.entrySide = entrySide;
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
        boolean fire = justEntered && crossedRequiredEdge(p);
        inside = now;
        last = p;
        return fire;
    }

    /**
     * Confirms the player WALKED into the zone across the required edge. A
     * teleport (e.g. arriving back in the lobby after leaving the raid) is a
     * large position jump and is rejected, so it won't re-fire the popup.
     */
    private boolean crossedRequiredEdge(WorldPoint p)
    {
        if (entrySide == EntrySide.ANY)
            return true;
        if (last == null || last.getPlane() != p.getPlane())
            return false;

        int dx = Math.abs(p.getX() - last.getX());
        int dy = Math.abs(p.getY() - last.getY());
        if (dx > 2 || dy > 2)
            return false; // not an adjacent step → teleport, ignore

        switch (entrySide)
        {
            case WEST:  return last.getX() <  minX; // came from the left, moving east
            case EAST:  return last.getX() >  maxX;
            case SOUTH: return last.getY() <  minY;
            case NORTH: return last.getY() >  maxY;
            default:    return true;
        }
    }

    /** Clears the inside flag so the next entry fires fresh (e.g. after a region load). */
    public void reset()
    {
        inside = false;
        last = null;
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