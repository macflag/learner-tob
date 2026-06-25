/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Gear check result popup built on RuneLite's {@link OverlayPanel}.
 *
 * Dismissal is per-popup, chosen by the caller via {@link DismissMode}:
 *   CLICK  — only a click INSIDE the popup closes it (no timer). Used by the
 *            manual gear check and the raid-start door check, so spam-clicking
 *            to run/eat never closes it.
 *   TIMED  — auto-dismisses after a set number of seconds; a click inside still
 *            closes it early. Used for in-room prompts.
 *
 * A click inside the popup always closes it, in either mode — the plugin's mouse
 * listener calls {@link #containsPoint(Point)} and only dismisses on a hit, so
 * clicks elsewhere on screen pass straight through to the game.
 *
 * Urgent popups can be shown {@code flashing}, which pulses the header and
 * background to draw the eye (gated by the "Flashing alerts" config toggle).
 */
public class GearCheckOverlay extends OverlayPanel
{
    public enum DismissMode { CLICK, TIMED, COMPLY }

    private static final long FLASH_PERIOD_MS = 400;
    // Dark cockpit: a clean result is neutral, never green.
    private static final Color NEUTRAL = new Color(150, 150, 150);

    private final LearnerTobConfig config;

    private String title = null;
    private List<String> lines = null;
    private boolean isPassed = false;
    private boolean isLots = false;
    private long shownAt = 0;

    private DismissMode dismissMode = DismissMode.CLICK;
    private int timedSeconds = 3;
    private boolean flashing = false;

    @Inject
    public GearCheckOverlay(LearnerTobConfig config)
    {
        this.config = config;
        setPosition(OverlayPosition.TOP_CENTER);
    }

    /** Shows a popup using the user's global dismiss setting (manual gear check). */
    public void showResult(String resultTitle, List<String> issues)
    {
        showResult(resultTitle, issues, DismissMode.CLICK, 3, false);
    }

    /**
     * Shows a popup with an explicit dismiss behaviour.
     *
     * @param mode    CLICK (click-inside only) or TIMED (auto-dismiss)
     * @param seconds auto-dismiss seconds when mode is TIMED
     * @param flash   pulse the popup to draw attention (urgent callouts)
     */
    public void showResult(String resultTitle, List<String> issues,
                           DismissMode mode, int seconds, boolean flash)
    {
        this.title = resultTitle;
        this.isPassed = issues.isEmpty();
        this.isLots = issues.size() > 3;
        this.shownAt = System.currentTimeMillis();
        this.dismissMode = mode;
        this.timedSeconds = seconds;
        this.flashing = flash;

        lines = new ArrayList<>();
        if (isLots)
        {
            lines.add("\u26A0  " + issues.size() + " issues - see chat");
        }
        else if (isPassed)
        {
            lines.add("No issues.");
        }
        else
        {
            lines.addAll(issues);
        }
    }

    public void dismiss()
    {
        title = null;
        lines = null;
    }

    public boolean isVisible()
    {
        return title != null;
    }

    /** True if the given canvas point is inside the popup's last-rendered bounds. */
    public boolean containsPoint(Point p)
    {
        Rectangle b = getBounds();
        return p != null && b != null && b.width > 0 && b.height > 0 && b.contains(p);
    }

    /** Whether a click inside should close this popup. COMPLY popups persist until satisfied. */
    public boolean clickCloses()
    {
        return dismissMode != DismissMode.COMPLY;
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (title == null || lines == null)
        {
            return null;
        }

        boolean clickMode;
        int secs;
        switch (dismissMode)
        {
            case TIMED:
                clickMode = false;
                secs = timedSeconds;
                break;
            case COMPLY:
                // Stays up until the plugin clears it (player complies); no timer, no click-close.
                clickMode = false;
                secs = 0;
                break;
            case CLICK:
            default:
                clickMode = true;
                secs = 0;
                break;
        }

        // Auto-dismiss for timed popups (click-inside can still close early).
        if (!clickMode && secs > 0
                && System.currentTimeMillis() - shownAt > secs * 1000L)
        {
            dismiss();
            return null;
        }

        applyPosition();

        // Flash phase: when flashing (and enabled), pulse on a fixed period.
        boolean bright = true;
        if (flashing && config.flashingAlerts())
        {
            bright = ((System.currentTimeMillis() / FLASH_PERIOD_MS) % 2) == 0;
        }

        int baseAlpha = (int) (config.overlayOpacity() / 100.0 * 255);
        int alpha = bright ? baseAlpha : Math.max(40, baseAlpha / 2);

        Color barColour = isPassed ? NEUTRAL
                : (isLots ? config.lotsColor() : config.failColor());
        Color headerColour = bright ? barColour.brighter().brighter() : barColour.darker();

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(240, 0));
        panelComponent.setBackgroundColor(new Color(15, 15, 15, alpha));

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(title)
                .color(headerColour)
                .build());

        for (String line : lines)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(line)
                    .leftColor(Color.WHITE)
                    .build());
        }

        if (clickMode)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Click to close")
                    .leftColor(new Color(180, 180, 180))
                    .build());
        }

        return super.render(g);
    }

    private void applyPosition()
    {
        switch (config.overlayPosition())
        {
            case TOP_LEFT:
                setPosition(OverlayPosition.TOP_LEFT);
                break;
            case TOP_RIGHT:
                setPosition(OverlayPosition.TOP_RIGHT);
                break;
            case CENTER:
                setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
                break;
            case BOTTOM_LEFT:
                setPosition(OverlayPosition.BOTTOM_LEFT);
                break;
            case BOTTOM_RIGHT:
                setPosition(OverlayPosition.BOTTOM_RIGHT);
                break;
            default:
                setPosition(OverlayPosition.TOP_CENTER);
                break;
        }
    }
}