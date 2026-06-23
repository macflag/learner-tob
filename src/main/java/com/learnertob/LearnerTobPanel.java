/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class LearnerTobPanel extends PluginPanel
{
    private boolean scytheSetup   = true;
    private Set<Integer> readyIds = java.util.Collections.emptySet(); // equipped + inventory
    private Set<Integer> bankIds  = java.util.Collections.emptySet(); // bank only

    // Maps an item ID the player owns -> its display name (provided by plugin)
    private Map<Integer, String> ownedNames = java.util.Collections.emptyMap();

    private final JLabel  titleLabel    = new JLabel();
    private final JLabel  subtitleLabel = new JLabel();
    private final JButton checkButton   = new JButton();
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final JPanel  itemsPanel    = new JPanel();

    private Runnable onRunCheck;
    private java.util.function.Consumer<Role> onRoleChange;

    @Inject
    public LearnerTobPanel()
    {
        super(false);
        buildUI();
    }

    public void setOnRunCheck(Runnable r) { this.onRunCheck = r; }
    public void setOnRoleChange(java.util.function.Consumer<Role> c) { this.onRoleChange = c; }
    public void setRole(Role r) { roleCombo.setSelectedItem(r); }
    public void setScytheSetup(boolean s) { this.scytheSetup = s; }
    public void setOwnedIds(Set<Integer> ready, Set<Integer> bank) { this.readyIds = ready; this.bankIds = bank; }
    public void setOwnedNames(Map<Integer, String> names) { this.ownedNames = names; }

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
            if (onRoleChange != null) onRoleChange.accept((Role) roleCombo.getSelectedItem());
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
        checkButton.addActionListener((ActionEvent e) ->
        {
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

        header.add(center,       BorderLayout.CENTER);
        header.add(checkButton, BorderLayout.SOUTH);

        itemsPanel.setLayout(new GridLayout(0, 1, 0, 0));
        itemsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(itemsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh()
    {
        titleLabel.setText("MDPS — " + (scytheSetup ? "Scythe" : "No Scythe"));
        subtitleLabel.setText("Arceuus | Fire/Blood/Aether/Death");

        itemsPanel.removeAll();

        addSectionHeader("Worn");
        if (scytheSetup)
        {
            addItem("Mage Weapon", "Eye of Ayak / Sang / Trident", Presets.MAGE_ANY);
            addItem("Helm",        "Torva / Oathplate", Presets.HELM_ANY);
            addItem("Body",        "Torva / Oathplate / Bandos", Presets.BODY_NONVOID);
            addItem("Legs",        "Torva / Oathplate / Bandos", Presets.LEGS_NONVOID);
            addItem("Cape",        "Infernal / Fire", Presets.CAPE_ANY);
            addItem("Neck",        "Rancour / Torture", Presets.NECK_ANY);
            addItem("Shield",      "Book of the Dead", Presets.sub(Presets.BOOK_OF_DEAD));
            addItem("Gloves",      "Ferocious Gloves", Presets.sub(Presets.FEROCIOUS_GLOVES));
            addItem("Boots",       "Avernic Treads / Primordial", Presets.BOOTS_ANY);
            addItem("Ring",        "Ultor / Berserker (i)", Presets.RING_ANY);
        }
        else
        {
            addItem("Weapon",      "Abyssal Tentacle", Presets.sub(Presets.ABYSSAL_TENTACLE));
            addItem("Helm",        "Void Melee Helm", Presets.HELM_VOID);
            addItem("Body",        "Elite Void Top", Presets.BODY_VOID);
            addItem("Legs",        "Elite Void Robe", Presets.LEGS_VOID);
            addItem("Cape",        "Infernal / Fire", Presets.CAPE_ANY);
            addItem("Neck",        "Rancour / Torture", Presets.NECK_ANY);
            addItem("Shield",      "Any Defender", Presets.DEFENDER_ANY);
            addItem("Gloves",      "Void Knight Gloves", Presets.sub(Presets.VOID_KNIGHT_GLOVES_L, Presets.VOID_KNIGHT_GLOVES));
            addItem("Boots",       "Avernic Treads / Primordial", Presets.BOOTS_ANY);
            addItem("Ring",        "Ultor / Berserker (i)", Presets.RING_ANY);
        }

        addSectionHeader("Inventory");
        if (scytheSetup) addItem("Scythe", "Scythe of Vitur", Presets.SCYTHE_ANY, "x1");
        else             addItem("Mage Weapon", "Eye of Ayak / Sang / Trident", Presets.MAGE_ANY);
        addItem("Blowpipe",     "Blazing / Toxic", Presets.BLOWPIPE_ANY, "x1");
        addItem("Void Ranger Helm",  "Void Ranger Helm", Presets.sub(Presets.VOID_RANGER_HELM_L), "x1");
        addItem("Range Cape",   "Quiver / Assembler", Presets.QUIVER_ANY);
        addItem("Amulet of Anguish",      "Necklace of Anguish (or)", Presets.sub(Presets.NECKLACE_OF_ANGUISH_OR), "x1");
        addItem("DPS Spec",     "Dragon Claws / Burning Claws", Presets.DPS_SPEC);
        addItem("Defense Spec", "Elder Maul / Dragon Warhammer", Presets.DEF_SPEC);
        addItem("Salve Amulet (e)",        "Salve Amulet (e)", Presets.sub(Presets.SALVE_AMULET_E), "x1");
        addItem("Crystal Halberd",      "Crystal Halberd", Presets.sub(Presets.CRYSTAL_HALBERD), "x1");
        addItem("Bandos Godsword",          "Bandos Godsword", Presets.BGS_ANY, "x1");
        addItem("Sulphur Blades",      "Sulphur Blades", Presets.sub(Presets.SULPHUR_BLADES), "x1");
        if (!scytheSetup) addItem("Book", "Book of the Dead", Presets.sub(Presets.BOOK_OF_DEAD), "x1");
        addItem("Saradomin Brews",        "Saradomin Brew(4)", Presets.sub(Presets.SARADOMIN_BREW_4), scytheSetup ? "x3" : "x4");
        addItem("Super Restores",     "Super Restore(4)", Presets.sub(Presets.SUPER_RESTORE_4), "x3");
        addItem("Anglerfish",   "Anglerfish", Presets.sub(Presets.ANGLERFISH), scytheSetup ? "x3" : "x4");
        addItem("Super Combat Potion",       "Divine Super Combat(4)", Presets.sub(Presets.DIVINE_SUPER_COMBAT_4), "x1 Divine + x2 Reg");
        addItem("Ranging Potion",      "Ranging Potion(4)", Presets.sub(Presets.RANGING_POTION_4), "x1");
        addItem("Rune Pouch",   "Divine Rune Pouch", Presets.POUCH_ANY);
        addItem("Spellbook",    "Arceuus");

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void addSectionHeader(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeBoldFont());
        label.setForeground(Color.ORANGE);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        itemsPanel.add(label);
    }

    private void addItem(String slot, String value)
    {
        addItem(slot, value, null, null);
    }

    private void addItem(String slot, String fallback, Set<Integer> validIds)
    {
        addItem(slot, fallback, validIds, null);
    }

    /**
     * Adds a loadout row. When {@code qty} is given (e.g. "x3"), it is appended
     * to the displayed name so consumable counts stay visible even after the
     * row resolves to the player's actual owned item.
     */
    private void addItem(String slot, String fallback, Set<Integer> validIds, String qty)
    {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 0));
        row.setBackground(ColorScheme.DARK_GRAY_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel s = new JLabel(slot.toUpperCase());
        s.setFont(FontManager.getRunescapeSmallFont());
        s.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        String display = fallback;
        Color colour = Color.WHITE;

        if (validIds != null)
        {
            // Find the specific item the player owns (ready first, then bank)
            Integer ownedReady = validIds.stream().filter(readyIds::contains).findFirst().orElse(null);
            Integer ownedBank  = validIds.stream().filter(bankIds::contains).findFirst().orElse(null);

            if (ownedReady != null)
            {
                display = nameFor(ownedReady, fallback);
                colour  = new Color(70, 200, 70);   // green
            }
            else if (ownedBank != null)
            {
                display = nameFor(ownedBank, fallback);
                colour  = new Color(230, 200, 60);  // yellow
            }
            else
            {
                display = fallback;                 // show generic option list
                colour  = new Color(220, 90, 90);   // red
            }
        }

        // Append the required quantity for consumable rows
        if (qty != null && !qty.isEmpty())
        {
            display = display + "  " + qty;
        }

        JLabel v = new JLabel(display);
        v.setFont(FontManager.getRunescapeFont());
        v.setForeground(colour);
        row.add(s);
        row.add(v);
        itemsPanel.add(row);
    }

    /** Returns the real item name for an owned ID, or the fallback if unknown. */
    private String nameFor(int id, String fallback)
    {
        String n = ownedNames.get(id);
        return (n == null || n.isEmpty()) ? fallback : n;
    }
}