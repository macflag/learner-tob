package com.learnertob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds one clan preset: what gear + inventory + spellbook is required,
 * plus substitution groups that allow acceptable alternatives.
 *
 * You don't edit this file — edit Presets.java instead.
 */
public class TobPreset
{
    /** Item ID -> required quantity for worn equipment. */
    public final Map<Integer, Integer> equipment;

    /** Item ID -> required quantity for inventory. */
    public final Map<Integer, Integer> inventory;

    /**
     * Substitution groups for equipment.
     * Each Set is a group of IDs where any one satisfies the slot.
     * Example: {PRIMORDIAL_BOOTS, AVERNIC_TREADS} — either passes.
     */
    public final List<Set<Integer>> equipmentSubs;

    /**
     * Substitution groups for inventory items.
     */
    public final List<Set<Integer>> inventorySubs;

    /**
     * Required spellbook. Use Presets.ARCEUUS / ANCIENT / STANDARD / LUNAR.
     * Set to -1 to skip the spellbook check.
     */
    public final int spellbook;

    public TobPreset(
            Map<Integer, Integer> equipment,
            List<Set<Integer>> equipmentSubs,
            Map<Integer, Integer> inventory,
            List<Set<Integer>> inventorySubs,
            int spellbook)
    {
        this.equipment     = equipment;
        this.equipmentSubs = equipmentSubs != null ? equipmentSubs : new ArrayList<>();
        this.inventory     = inventory;
        this.inventorySubs = inventorySubs != null ? inventorySubs : new ArrayList<>();
        this.spellbook     = spellbook;
    }
}