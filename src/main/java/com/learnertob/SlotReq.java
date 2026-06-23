/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.util.Set;

/**
 * A single slot requirement: a human-readable label, the set of item IDs
 * that satisfy it (any one of them passes), the quantity needed, and whether
 * it lives in equipment or inventory.
 */
public class SlotReq
{
    public final String label;
    public final Set<Integer> validIds;
    public final int quantity;
    public final boolean equipped; // true = must be worn, false = inventory

    public SlotReq(String label, Set<Integer> validIds, int quantity, boolean equipped)
    {
        this.label = label;
        this.validIds = validIds;
        this.quantity = quantity;
        this.equipped = equipped;
    }
}