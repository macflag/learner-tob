/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Draws Maiden-room floor markers, all tuned to the room's maroon palette but
 * kept distinct from one another:
 *   - your role's "stand here" box  -> light red
 *   - Nylocas Matomenos true tiles  -> darker red (freeze roles only)
 *   - blood-spawn true tiles        -> medium grey (all roles)
 *
 * The standing box arrives in template world coords (static, needs instance
 * translation). NPC marks arrive as the server (true) SW tile plus the NPC's
 * size, so multi-tile mobs are centred on their full footprint rather than
 * highlighting just the south-west corner.
 */
public class TileMarkerOverlay extends Overlay
{
    // Where you stand: soft, muted dusty rose that sits in the room's palette.
    private static final Color STAND_STROKE = new Color(198, 158, 152);
    private static final Color STAND_FILL   = new Color(198, 158, 152, 32);
    // Nylos to freeze: darker, saturated red.
    private static final Color NYLO_STROKE  = new Color(150, 30, 30);
    private static final Color NYLO_FILL    = new Color(150, 30, 30, 70);
    // Blood spawns: warm medium grey.
    private static final Color BLOOD_STROKE = new Color(165, 158, 158);
    private static final Color BLOOD_FILL   = new Color(165, 158, 158, 55);
    // Bloat rendered tile (where the client draws him): light grey.
    private static final Color BLOAT_STROKE      = new Color(200, 200, 200);
    private static final Color BLOAT_FILL        = new Color(200, 200, 200, 40);
    // Bloat true tile (server position, one tick ahead of render): bright white.
    private static final Color BLOAT_TRUE_STROKE = new Color(255, 255, 255);
    private static final Color BLOAT_TRUE_FILL   = new Color(255, 255, 255, 25);

    private static final String LABEL = "Stay in this area";

    /** A live NPC marker: server (true) SW tile + footprint size in tiles. */
    public static final class Mark
    {
        final WorldPoint sw;
        final int size;

        public Mark(WorldPoint sw, int size)
        {
            this.sw = sw;
            this.size = Math.max(1, size);
        }
    }

    private final Client client;

    // {minX, maxX, minY, maxY} template world coords, plane 0. Null = none.
    private volatile int[] box;
    private volatile List<Mark> nyloTiles  = Collections.emptyList();
    private volatile List<Mark> bloodTiles = Collections.emptyList();

    // Bloat NPC reference (null = Bloat not in room). Read fresh every frame for smooth tile positions.
    private volatile NPC bloatNpc;

    @Inject
    TileMarkerOverlay(Client client)
    {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
    }

    /** Plugin pushes the active Maiden markers each tick (any may be null/empty). */
    void set(int[] box, List<Mark> nylos, List<Mark> bloods)
    {
        this.box = box;
        this.nyloTiles  = nylos  != null ? nylos  : Collections.emptyList();
        this.bloodTiles = bloods != null ? bloods : Collections.emptyList();
    }

    /** Plugin pushes the Bloat NPC reference each tick (null = not in room). */
    void setBloat(NPC npc)
    {
        this.bloatNpc = npc;
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        g.setStroke(new BasicStroke(2));

        int[] b = box;
        if (b != null)
        {
            for (int x = b[0]; x <= b[1]; x++)
                for (int y = b[2]; y <= b[3]; y++)
                    drawTemplateTile(g, new WorldPoint(x, y, 0), STAND_FILL, STAND_STROKE);
            drawLabel(g, b);
        }

        // Blood first, nylos on top (freezers care most about those).
        for (Mark m : bloodTiles)
            drawNpcTile(g, m, BLOOD_FILL, BLOOD_STROKE);
        for (Mark m : nyloTiles)
            drawNpcTile(g, m, NYLO_FILL, NYLO_STROKE);

        // Bloat: rendered tile (visual position) first, true tile (server position) on top.
        // When Bloat is turning/walking, the white true tile snaps ahead one tick so players
        // can read the new direction before the model animation catches up.
        NPC bloat = bloatNpc;
        if (bloat != null)
        {
            NPCComposition comp = bloat.getTransformedComposition();
            int bloatSize = comp != null ? comp.getSize() : 5;

            LocalPoint rendered = bloat.getLocalLocation();
            if (rendered != null)
                fillPoly(g, Perspective.getCanvasTileAreaPoly(client, rendered, bloatSize),
                        BLOAT_FILL, BLOAT_STROKE);

            WorldPoint trueWorld = WorldPoint.fromLocalInstance(client, bloat.getLocalLocation());
            LocalPoint trueSW = (trueWorld != null) ? LocalPoint.fromWorld(client, trueWorld) : null;
            if (trueSW != null)
            {
                int half = (bloatSize - 1) * Perspective.LOCAL_TILE_SIZE / 2;
                fillPoly(g, Perspective.getCanvasTileAreaPoly(client, trueSW.plus(half, half), bloatSize),
                        BLOAT_TRUE_FILL, BLOAT_TRUE_STROKE);
            }
        }

        return null;
    }

    /** Static template tile: translate into the current instance, then draw. */
    private void drawTemplateTile(Graphics2D g, WorldPoint template, Color fill, Color stroke)
    {
        for (WorldPoint dyn : WorldPoint.toLocalInstance(client, template))
        {
            LocalPoint lp = LocalPoint.fromWorld(client, dyn);
            if (lp == null)
                continue;
            fillPoly(g, Perspective.getCanvasTilePoly(client, lp), fill, stroke);
        }
    }

    /**
     * Live NPC true tile. getWorldLocation() is the SW tile of the footprint, so
     * we shift to the footprint centre and draw the full size x size area -- this
     * matches RuneLite's own true-tile highlight for multi-tile mobs.
     */
    private void drawNpcTile(Graphics2D g, Mark m, Color fill, Color stroke)
    {
        LocalPoint sw = LocalPoint.fromWorld(client, m.sw);
        if (sw == null)
            return;
        int half = (m.size - 1) * Perspective.LOCAL_TILE_SIZE / 2;
        LocalPoint center = sw.plus(half, half);
        fillPoly(g, Perspective.getCanvasTileAreaPoly(client, center, m.size), fill, stroke);
    }

    private void fillPoly(Graphics2D g, Polygon poly, Color fill, Color stroke)
    {
        if (poly == null)
            return;
        g.setColor(fill);
        g.fill(poly);
        g.setColor(stroke);
        g.draw(poly);
    }

    private void drawLabel(Graphics2D g, int[] b)
    {
        WorldPoint center = new WorldPoint((b[0] + b[1]) / 2, (b[2] + b[3]) / 2, 0);
        for (WorldPoint dyn : WorldPoint.toLocalInstance(client, center))
        {
            LocalPoint lp = LocalPoint.fromWorld(client, dyn);
            if (lp == null)
                continue;
            Point txt = Perspective.getCanvasTextLocation(client, g, lp, LABEL, 60);
            if (txt != null)
                OverlayUtil.renderTextLocation(g, txt, LABEL, STAND_STROKE);
            break;
        }
    }
}