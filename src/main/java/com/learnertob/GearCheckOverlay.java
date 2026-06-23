/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Gear check result popup built on RuneLite's {@link OverlayPanel}.
 * RuneLite handles dragging and screen positioning; a click anywhere
 * dismisses it (see the plugin's mouse listener) when in click mode,
 * otherwise it auto-dismisses after the configured number of seconds.
 */
public class GearCheckOverlay extends OverlayPanel
{
    private final LearnerTobConfig config;

    private String title = null;
    private List<String> lines = null;
    private boolean isPassed = false;
    private boolean isLots = false;
    private long shownAt = 0;

    @Inject
    public GearCheckOverlay(LearnerTobConfig config)
    {
        this.config = config;
        setPosition(OverlayPosition.TOP_CENTER);
    }

    public void showResult(String resultTitle, List<String> issues)
    {
        this.title = resultTitle;
        this.isPassed = issues.isEmpty();
        this.isLots = issues.size() > 3;
        this.shownAt = System.currentTimeMillis();

        lines = new ArrayList<>();
        if (isLots)
        {
            lines.add("\u26A0  " + issues.size() + " issues - see chat");
        }
        else if (isPassed)
        {
            lines.add("\u2705  All good!");
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

    @Override
    public Dimension render(Graphics2D g)
    {
        if (title == null || lines == null)
        {
            return null;
        }

        // Auto-dismiss after the configured time, unless in click-to-close mode
        if (config.overlayDismiss() != OverlayDismiss.CLICK)
        {
            int secs = dismissSeconds();
            if (System.currentTimeMillis() - shownAt > secs * 1000L)
            {
                dismiss();
                return null;
            }
        }

        applyPosition();

        int alpha = (int) (config.overlayOpacity() / 100.0 * 255);
        Color barColour = isPassed ? config.passColor()
                : (isLots ? config.lotsColor() : config.failColor());

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(240, 0));
        panelComponent.setBackgroundColor(new Color(15, 15, 15, alpha));

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(title)
                .color(barColour.brighter().brighter())
                .build());

        for (String line : lines)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(line)
                    .leftColor(Color.WHITE)
                    .build());
        }

        if (config.overlayDismiss() == OverlayDismiss.CLICK)
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

    private int dismissSeconds()
    {
        switch (config.overlayDismiss())
        {
            case SECONDS_3:
                return 3;
            case SECONDS_5:
                return 5;
            case SECONDS_10:
                return 10;
            default:
                return Integer.MAX_VALUE;
        }
    }
}