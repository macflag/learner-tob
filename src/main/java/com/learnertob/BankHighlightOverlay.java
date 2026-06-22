package com.learnertob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class BankHighlightOverlay extends WidgetItemOverlay
{
    private static final Color HAVE_FILL   = new Color(0,   180, 0,   60);
    private static final Color MISS_FILL   = new Color(180, 0,   0,   60);
    private static final Color HAVE_BORDER = new Color(0,   220, 0,   220);
    private static final Color MISS_BORDER = new Color(220, 0,   0,   220);

    private final LearnerTobPanel panel;

    private volatile Set<Integer> requiredIds = Collections.emptySet();
    private volatile Set<Integer> presentIds  = Collections.emptySet();

    @Inject
    public BankHighlightOverlay(LearnerTobPanel panel)
    {
        this.panel = panel;
        showOnBank();
        showOnInventory();
        showOnEquipment();
    }

    public void setRequiredIds(Set<Integer> ids) { this.requiredIds = ids; }
    public void setPresentIds(Set<Integer> ids)  { this.presentIds  = ids; }

    @Override
    public void renderItemOverlay(Graphics2D g, int itemId, WidgetItem item)
    {
        if (!panel.isFilterEnabled()) return;
        if (!requiredIds.contains(itemId)) return;

        Rectangle b = item.getCanvasBounds();
        if (b == null || b.width == 0) return;

        boolean have = presentIds.contains(itemId);
        g.setColor(have ? HAVE_FILL : MISS_FILL);
        g.fillRect(b.x, b.y, b.width, b.height);
        g.setColor(have ? HAVE_BORDER : MISS_BORDER);
        g.drawRect(b.x, b.y, b.width - 1, b.height - 1);
    }
}