/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class LearnerTobPanel extends PluginPanel
{
    // Dark cockpit: no green. NEUTRAL marks "ready/correct"; RED marks a problem.
    private static final Color NEUTRAL = new Color(140, 140, 140);
    private static final Color RED     = new Color(220, 90, 90);

    private static final int CELL_W = 40;
    private static final int CELL_H = 40;
    private static final int GAP    = 5;
    // Inventory is 4 columns wide. Every section is laid out to this width
    // so the left edges line up and the equipment cross can be centered.
    private static final int BLOCK_W = 4 * CELL_W + 3 * GAP; // 169

    private boolean scytheSetup = true;
    private Role currentRole = Role.MELEE;
    private List<ResolvedSlot> resolvedSlots = new ArrayList<>();
    private String spellbookName = "Arceuus";
    private String subtitle = "";
    private boolean spellbookOk = true;

    private final JLabel titleLabel    = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JButton checkButton  = new JButton();
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final ScrollablePanel contentPanel = new ScrollablePanel();

    private Runnable onRunCheck;
    private java.util.function.Consumer<Role> onRoleChange;

    // ------------------------------------------------------------------
    //  Data carrier (built on client thread, rendered on EDT)
    // ------------------------------------------------------------------
    public static final class ResolvedSlot
    {
        public final int itemId;
        public final String itemName;
        public final BufferedImage sprite;
        public final Color border;
        public final int quantity;   // expand into this many cells in inventory
        public final boolean equipped;
        public final int equipSlot;  // EquipmentInventorySlot index; -1 if inventory
        public final int badge;      // stack count shown as a superscript; 0 = none

        public ResolvedSlot(int itemId, String itemName, BufferedImage sprite,
                            Color border, int quantity, boolean equipped, int equipSlot,
                            int badge)
        {
            this.itemId    = itemId;
            this.itemName  = itemName;
            this.sprite    = sprite;
            this.border    = border;
            this.quantity  = quantity;
            this.equipped  = equipped;
            this.equipSlot = equipSlot;
            this.badge     = badge;
        }
    }

    @Inject
    public LearnerTobPanel()
    {
        super(false);
        buildUI();
    }

    // ------------------------------------------------------------------
    //  Setters
    // ------------------------------------------------------------------
    public void setOnRunCheck(Runnable r)                            { this.onRunCheck = r; }
    public void setOnRoleChange(java.util.function.Consumer<Role> c) { this.onRoleChange = c; }
    public void setRole(Role r) { roleCombo.setSelectedItem(r); this.currentRole = r; }
    public void setScytheSetup(boolean s) { this.scytheSetup = s; }

    public void setResolvedSlots(List<ResolvedSlot> slots,
                                 String spellbookName, boolean spellbookOk,
                                 String subtitle)
    {
        this.subtitle = subtitle;
        this.resolvedSlots = slots;
        this.spellbookName = spellbookName;
        this.spellbookOk   = spellbookOk;
    }

    // ------------------------------------------------------------------
    //  UI skeleton
    // ------------------------------------------------------------------
    private void buildUI()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel rolePanel = new JPanel(new BorderLayout(4, 0));
        rolePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setFont(FontManager.getRunescapeFont());
        roleLbl.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        roleCombo.setFont(FontManager.getRunescapeFont());
        roleCombo.setFocusable(false);
        roleCombo.addActionListener(e -> {
            currentRole = (Role) roleCombo.getSelectedItem();
            if (onRoleChange != null) onRoleChange.accept(currentRole);
        });
        rolePanel.add(roleLbl, BorderLayout.WEST);
        rolePanel.add(roleCombo, BorderLayout.CENTER);

        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        subtitleLabel.setFont(FontManager.getRunescapeFont());
        subtitleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        checkButton.setText("Run Gear Check");
        checkButton.setFont(FontManager.getRunescapeFont());
        checkButton.setFocusPainted(false);
        checkButton.setBackground(new Color(60, 120, 180));
        checkButton.setForeground(Color.WHITE);
        checkButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        checkButton.addActionListener((ActionEvent e) -> {
            if (onRunCheck != null) onRunCheck.run();
        });

        JPanel titleStack = new JPanel(new BorderLayout(0, 2));
        titleStack.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        titleStack.add(titleLabel,    BorderLayout.NORTH);
        titleStack.add(subtitleLabel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        center.add(rolePanel,   BorderLayout.NORTH);
        center.add(titleStack,  BorderLayout.CENTER);

        header.add(center,      BorderLayout.CENTER);
        header.add(checkButton, BorderLayout.SOUTH);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        JScrollPane scroll = new JScrollPane(contentPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    //  Refresh
    // ------------------------------------------------------------------
    public void refresh()
    {
        titleLabel.setText(currentRole.toString() + " \u2014 " + (scytheSetup ? "Scythe" : "No Scythe"));
        subtitleLabel.setText(subtitle);

        contentPanel.removeAll();

        // Inventory
        contentPanel.add(sectionHeader("Inventory"));
        contentPanel.add(Box.createVerticalStrut(6));
        List<ResolvedSlot> invSlots = new ArrayList<>();
        for (ResolvedSlot s : resolvedSlots)
            if (!s.equipped) invSlots.add(s);
        contentPanel.add(buildGrid(invSlots, 4, 28));

        contentPanel.add(Box.createVerticalStrut(16));

        // Equipment (3-col cross, centered within the inventory width)
        contentPanel.add(sectionHeader("Equipment"));
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(buildEquipCross());

        contentPanel.add(Box.createVerticalStrut(16));

        // Spellbook
        contentPanel.add(sectionHeader("Spellbook"));
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(buildSpellbook());

        // Rune Pouch
        List<ResolvedSlot> runeSlots = new ArrayList<>();
        for (ResolvedSlot s : resolvedSlots)
            if (s.equipSlot == -99) runeSlots.add(s);
        if (!runeSlots.isEmpty())
        {
            contentPanel.add(Box.createVerticalStrut(16));
            contentPanel.add(sectionHeader("Rune Pouch"));
            contentPanel.add(Box.createVerticalStrut(6));
            int runeCount = runeSlots.size();
            int runeCells = runeCount <= 4 ? 4 : ((runeCount + 3) / 4) * 4;
            contentPanel.add(buildGrid(runeSlots, 4, runeCells));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ------------------------------------------------------------------
    //  Section builders
    // ------------------------------------------------------------------

    /** Fixed-size grid of item cells, left-aligned. */
    private JPanel buildGrid(List<ResolvedSlot> slots, int cols, int totalCells)
    {
        int rows = (int) Math.ceil((double) totalCells / cols);
        int w = cols * CELL_W + (cols - 1) * GAP;
        int h = rows * CELL_H + (rows - 1) * GAP;

        JPanel grid = new JPanel(new GridLayout(rows, cols, GAP, GAP));
        grid.setBackground(ColorScheme.DARK_GRAY_COLOR);
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockSize(grid, w, h);

        int filled = 0;
        for (ResolvedSlot s : slots)
        {
            int copies = Math.max(1, s.quantity);
            for (int i = 0; i < copies && filled < totalCells; i++, filled++)
                grid.add(itemCell(s));
        }
        while (filled < totalCells)
        {
            grid.add(emptyCell());
            filled++;
        }
        return grid;
    }

    /** 3-column worn-equipment cross, centered inside a full-width block. */
    private JPanel buildEquipCross()
    {
        JLabel[] cells = new JLabel[15];
        for (int i = 0; i < 15; i++) cells[i] = emptyCell();

        for (ResolvedSlot s : resolvedSlots)
        {
            if (!s.equipped) continue;
            int idx = crossIndex(s.equipSlot);
            if (idx >= 0 && idx < 15)
                cells[idx] = itemCell(s);
        }

        int crossW = 3 * CELL_W + 2 * GAP;
        int crossH = 5 * CELL_H + 4 * GAP;

        JPanel cross = new JPanel(new GridLayout(5, 3, GAP, GAP));
        cross.setBackground(ColorScheme.DARK_GRAY_COLOR);
        lockSize(cross, crossW, crossH);
        for (int i = 0; i < 15; i++) cross.add(cells[i]);

        // Center the cross within the inventory-width block
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setBackground(ColorScheme.DARK_GRAY_COLOR);
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockSize(wrap, BLOCK_W, crossH);
        wrap.add(cross);
        return wrap;
    }

    private JPanel buildSpellbook()
    {
        JLabel cell = new JLabel(spellbookName, SwingConstants.CENTER);
        cell.setOpaque(true);
        cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        cell.setForeground(Color.WHITE);
        cell.setFont(FontManager.getRunescapeFont());
        cell.setBorder(BorderFactory.createLineBorder(spellbookOk ? NEUTRAL : RED, 2));
        lockSize(cell, BLOCK_W, CELL_H);
        cell.setToolTipText(spellbookOk ? "Correct spellbook" : "Switch to " + spellbookName);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setBackground(ColorScheme.DARK_GRAY_COLOR);
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockSize(wrap, BLOCK_W, CELL_H);
        wrap.add(cell);
        return wrap;
    }

    // ------------------------------------------------------------------
    //  Cells
    // ------------------------------------------------------------------

    private JLabel emptyCell()
    {
        JLabel cell = new JLabel();
        cell.setOpaque(true);
        cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        lockSize(cell, CELL_W, CELL_H);
        cell.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1));
        return cell;
    }

    /** Draws an OSRS-style stack count (yellow with shadow) onto a copy of the sprite. */
    private BufferedImage withBadge(BufferedImage src, int count)
    {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setFont(FontManager.getRunescapeSmallFont());
        String txt = String.valueOf(count);
        g.setColor(Color.BLACK);
        g.drawString(txt, 2, 11);
        g.setColor(Color.YELLOW);
        g.drawString(txt, 1, 10);
        g.dispose();
        return out;
    }

    private JLabel itemCell(ResolvedSlot s)
    {
        JLabel cell = new JLabel();
        cell.setOpaque(true);
        cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.setVerticalAlignment(SwingConstants.CENTER);
        lockSize(cell, CELL_W, CELL_H);
        cell.setBorder(BorderFactory.createLineBorder(s.border, 2));
        cell.setToolTipText(s.itemName);

        if (s.sprite != null)
        {
            cell.setIcon(new ImageIcon(s.badge > 0 ? withBadge(s.sprite, s.badge) : s.sprite));
        }
        else
        {
            String t = s.itemName.length() > 7 ? s.itemName.substring(0, 6) + "\u2026" : s.itemName;
            cell.setText(t);
            cell.setFont(FontManager.getRunescapeSmallFont());
            cell.setForeground(Color.WHITE);
        }
        return cell;
    }

    // ------------------------------------------------------------------
    //  Cross layout — maps EquipmentInventorySlot index to cell position.
    //  Row0(_, HEAD, _)  Row1(CAPE, AMULET, AMMO)  Row2(WEAPON, BODY, SHIELD)
    //  Row3(_, LEGS, _)  Row4(GLOVES, BOOTS, RING)
    // ------------------------------------------------------------------
    private int crossIndex(int slot)
    {
        switch (slot)
        {
            case 0:  return 1;   // HEAD
            case 1:  return 3;   // CAPE
            case 2:  return 4;   // AMULET
            case 13: return 5;   // AMMO  (quiver / blessing)
            case 3:  return 6;   // WEAPON
            case 4:  return 7;   // BODY
            case 5:  return 8;   // SHIELD
            case 7:  return 10;  // LEGS
            case 9:  return 12;  // GLOVES
            case 10: return 13;  // BOOTS
            case 12: return 14;  // RING
            default: return -1;
        }
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private static void lockSize(JPanel c, int w, int h)
    {
        Dimension d = new Dimension(w, h);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    private static void lockSize(JLabel c, int w, int h)
    {
        Dimension d = new Dimension(w, h);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    private JLabel sectionHeader(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeBoldFont());
        label.setForeground(Color.ORANGE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return label;
    }

    /**
     * Content panel that does NOT track the viewport width, so the GridLayout
     * cells keep their fixed size instead of stretching to fill the scroll pane.
     */
    private static final class ScrollablePanel extends JPanel implements Scrollable
    {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 48; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}