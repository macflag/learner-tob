package com.learnertob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.Color;

/**
 * Gear check result popup using RuneLite's built-in OverlayPanel.
 * This uses RuneLite's own positioning/dragging system so click-to-close,
 * dragging, and screen position all work correctly out of the box.
 */
public class GearCheckOverlay extends OverlayPanel
{
    private final LearnerTobConfig config;

    private String       title    = null;
    private List<String> lines    = null;
    private boolean      isPassed = false;
    private boolean      isLots   = false;
    private long         shownAt  = 0;

    @Inject
    public GearCheckOverlay(LearnerTobConfig config)
    {
        this.config = config;
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);
    }

    public void showResult(String title, List<String> issues)
    {
        this.title    = title;
        this.isPassed = issues.isEmpty();
        this.isLots   = issues.size() > 3;
        this.shownAt  = System.currentTimeMillis();

        lines = new ArrayList<>();
        if (isLots)
        {
            lines.add("\u26A0  " + issues.size() + " issues — see chat");
        }
        else if (isPassed)
        {
            lines.add("\u2705  All good!");
        }
        else
        {
            for (String issue : issues)
                lines.add(simplify(issue));
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
        if (title == null || lines == null) return null;

        // Auto-dismiss
        if (config.overlayDismiss() != OverlayDismiss.CLICK)
        {
            int secs = dismissSeconds();
            if (System.currentTimeMillis() - shownAt > secs * 1000L)
            {
                dismiss();
                return null;
            }
        }

        // Apply RuneLite position setting
        switch (config.overlayPosition())
        {
            case TOP_LEFT:     setPosition(OverlayPosition.TOP_LEFT);     break;
            case TOP_RIGHT:    setPosition(OverlayPosition.TOP_RIGHT);    break;
            case CENTER:       setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT); break;
            case BOTTOM_LEFT:  setPosition(OverlayPosition.BOTTOM_LEFT);  break;
            case BOTTOM_RIGHT: setPosition(OverlayPosition.BOTTOM_RIGHT); break;
            default:           setPosition(OverlayPosition.TOP_CENTER);   break;
        }

        float titleSize = titleFontSize();
        float bodySize  = bodyFontSize();
        int alpha = (int)(config.overlayOpacity() / 100.0 * 255);

        // Title bar colour
        Color barCol = isPassed ? config.passColor()
                : (isLots ? config.lotsColor() : config.failColor());

        // Build panel content
        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(240, 0));
        panelComponent.setBackgroundColor(new Color(15, 15, 15, alpha));

        // Title
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(title)
                        .color(barCol.brighter().brighter())
                        .build()
        );

        // Body lines
        for (String line : lines)
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left(line)
                            .leftColor(Color.WHITE)
                            .build()
            );
        }

        // Dismiss hint
        if (config.overlayDismiss() == OverlayDismiss.CLICK)
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Click to close")
                            .leftColor(new Color(180, 180, 180))
                            .build()
            );
        }

        return super.render(g);
    }

    private int dismissSeconds()
    {
        switch (config.overlayDismiss())
        {
            case SECONDS_3:  return 3;
            case SECONDS_5:  return 5;
            case SECONDS_10: return 10;
            default:         return Integer.MAX_VALUE;
        }
    }

    private float titleFontSize()
    {
        switch (config.overlayFontSize()) { case SMALL: return 11f; case LARGE: return 16f; default: return 13f; }
    }

    private float bodyFontSize()
    {
        switch (config.overlayFontSize()) { case SMALL: return 10f; case LARGE: return 14f; default: return 12f; }
    }

    private static String simplify(String raw)
    {
        // Plugin now sends clean messages directly — just pass through
        return raw;
    }
}