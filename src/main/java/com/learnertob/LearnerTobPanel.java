package com.learnertob;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
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
    private final LearnerTobConfig config;

    private boolean filterEnabled = false;
    private boolean scytheSetup   = true;
    private java.util.Set<Integer> readyIds = java.util.Collections.emptySet(); // equipped + inventory
    private java.util.Set<Integer> bankIds  = java.util.Collections.emptySet(); // bank only

    private final JLabel  titleLabel    = new JLabel();
    private final JLabel  subtitleLabel = new JLabel();
    private final JButton filterButton  = new JButton();
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final JPanel  itemsPanel    = new JPanel();

    private Runnable onFilterToggle;
    private java.util.function.Consumer<Role> onRoleChange;

    @Inject
    public LearnerTobPanel(LearnerTobConfig config)
    {
        super(false);
        this.config = config;
        buildUI();
    }

    public void setOnFilterToggle(Runnable r) { this.onFilterToggle = r; }
    public void setOnRoleChange(java.util.function.Consumer<Role> c) { this.onRoleChange = c; }
    public void setRole(Role r) { roleCombo.setSelectedItem(r); }
    public boolean isFilterEnabled() { return filterEnabled; }
    public void setScytheSetup(boolean s) { this.scytheSetup = s; }
    public void setOwnedIds(java.util.Set<Integer> ready, java.util.Set<Integer> bank) { this.readyIds = ready; this.bankIds = bank; }

    private void buildUI()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Role selector
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

        filterButton.setFont(FontManager.getRunescapeFont());
        filterButton.setFocusPainted(false);
        filterButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateFilterButton();
        filterButton.addActionListener((ActionEvent e) ->
        {
            filterEnabled = !filterEnabled;
            updateFilterButton();
            if (onFilterToggle != null) onFilterToggle.run();
        });

        // Stack: role on top, then title/subtitle, then filter button
        JPanel titleStack = new JPanel(new BorderLayout(0, 2));
        titleStack.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        titleStack.add(titleLabel,    BorderLayout.NORTH);
        titleStack.add(subtitleLabel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        center.add(rolePanel,   BorderLayout.NORTH);
        center.add(titleStack,  BorderLayout.CENTER);

        header.add(center,       BorderLayout.CENTER);
        header.add(filterButton, BorderLayout.SOUTH);

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

    private void updateFilterButton()
    {
        if (filterEnabled)
        {
            filterButton.setText("Bank Filter: ON");
            filterButton.setBackground(new Color(0, 130, 0));
            filterButton.setForeground(Color.WHITE);
        }
        else
        {
            filterButton.setText("Bank Filter: OFF");
            filterButton.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
            filterButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        }
    }

    public void refresh()
    {
        titleLabel.setText("MDPS \u2014 " + (scytheSetup ? "Scythe" : "No Scythe"));
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
        if (scytheSetup) addItem("Scythe", "x1", Presets.SCYTHE_ANY);
        else             addItem("Mage Weapon", "Eye of Ayak / Sang / Trident", Presets.MAGE_ANY);
        addItem("Blowpipe",     "x1", Presets.BLOWPIPE_ANY);
        addItem("Void Ranger Helm",  "x1", Presets.sub(Presets.VOID_RANGER_HELM_L));
        addItem("Range Cape",   "Quiver / Assembler", Presets.QUIVER_ANY);
        addItem("Amulet of Anguish",      "x1", Presets.sub(Presets.NECKLACE_OF_ANGUISH_OR));
        addItem("DPS Spec",     "Dragon Claws / Burning Claws", Presets.DPS_SPEC);
        addItem("Defense Spec", "Elder Maul / Dragon Warhammer", Presets.DEF_SPEC);
        addItem("Salve Amulet (e)",        "x1", Presets.sub(Presets.SALVE_AMULET_E));
        addItem("Crystal Halberd",      "x1", Presets.sub(Presets.CRYSTAL_HALBERD));
        addItem("Bandos Godsword",          "x1", Presets.BGS_ANY);
        addItem("Sulphur Blades",      "x1", Presets.sub(Presets.SULPHUR_BLADES));
        if (!scytheSetup) addItem("Book", "Book of the Dead", Presets.sub(Presets.BOOK_OF_DEAD));
        addItem("Saradomin Brews",        scytheSetup ? "x3" : "x4", Presets.sub(Presets.SARADOMIN_BREW_4));
        addItem("Super Restores",     "x3", Presets.sub(Presets.SUPER_RESTORE_4));
        addItem("Anglerfish",   scytheSetup ? "x3" : "x4", Presets.sub(Presets.ANGLERFISH));
        addItem("Super Combat Potion",       "x1 Divine + x2 Reg", Presets.sub(Presets.DIVINE_SUPER_COMBAT_4));
        addItem("Ranging Potion",      "x1", Presets.sub(Presets.RANGING_POTION_4));
        addItem("Rune Pouch",   "Divine Rune Pouch", Presets.POUCH_ANY);
        addItem("Spellbook",    "Arceuus");

        addSectionHeader("Acceptable Substitutions");
        addSub("Melee Cape",   "Infernal Cape / Fire Cape");
        addSub("Range Cape",   "Quiver / Assembler");
        addSub("Melee Amulet",         "Rancour / Torture");
        addSub("Boots",        "Avernic Treads / Primordial Boots");
        addSub("Ring",         "Ultor Ring / Berserker Ring (i)");
        addSub("Mage Weapon",  "Eye of Ayak / Sang Staff / Trident");
        addSub("DPS Spec",     "Dragon Claws / Burning Claws");
        addSub("Defense Spec", "Elder Maul / Dragon Warhammer");


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
        addItem(slot, value, null);
    }

    private void addItem(String slot, String value, java.util.Set<Integer> validIds)
    {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 0));
        row.setBackground(ColorScheme.DARK_GRAY_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel s = new JLabel(slot.toUpperCase());
        s.setFont(FontManager.getRunescapeSmallFont());
        s.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        JLabel v = new JLabel(value);
        v.setFont(FontManager.getRunescapeFont());

        // Colour based on ownership:
        // GREEN  = equipped or in inventory (ready to raid)
        // YELLOW = only in bank (need to withdraw)
        // RED    = don't have it anywhere
        if (validIds == null)
            v.setForeground(Color.WHITE);
        else
        {
            boolean ready = validIds.stream().anyMatch(readyIds::contains);
            boolean inBank = validIds.stream().anyMatch(bankIds::contains);
            if (ready)       v.setForeground(new Color(70, 200, 70));   // green
            else if (inBank) v.setForeground(new Color(230, 200, 60));  // yellow
            else             v.setForeground(new Color(220, 90, 90));   // red
        }

        row.add(s);
        row.add(v);
        itemsPanel.add(row);
    }

    private void addSub(String slot, String value)
    {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 0));
        row.setBackground(ColorScheme.DARK_GRAY_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel s = new JLabel(slot.toUpperCase());
        s.setFont(FontManager.getRunescapeSmallFont());
        s.setForeground(new Color(140, 140, 140));
        JLabel v = new JLabel(value);
        v.setFont(FontManager.getRunescapeFont());
        v.setForeground(new Color(185, 185, 185));
        row.add(s);
        row.add(v);
        itemsPanel.add(row);
    }
}