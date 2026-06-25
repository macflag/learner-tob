/*
 * Copyright (c) 2026, macflag
 * All rights reserved.
 * Licensed under the BSD 2-Clause License. See LICENSE for details.
 */
package com.learnertob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bank-driven presets for every learner role. Each preset is a list of
 * SlotReqs ("you need at least one valid item per slot"). Any valid
 * substitute passes. Cosmetic variants are all included.
 *
 * Spellbook constants match RuneLite's SPELLBOOK varbit values.
 */
public class Presets
{
    // ---- Spellbooks (varbit values) ----
    public static final int STANDARD = 0;
    public static final int ANCIENT  = 1;
    public static final int LUNAR    = 2;
    public static final int ARCEUUS  = 3;

    // ---- Mage weapon ----
    static final int EYE_OF_AYAK = 31113, SANGUINESTI_STAFF = 22323, TRIDENT_OF_SWAMP = 12899;

    // ---- Helm ----
    static final int TORVA_FULL_HELM = 26382, SANGUINE_TORVA_HELM = 28254;
    static final int OATHPLATE_HELM = 30750, RADIANT_OATHPLATE_HELM = 30777;
    static final int NEITIZNOT_FACEGUARD = 24271;
    static final int VOID_MELEE_HELM = 11665, VOID_MELEE_HELM_L = 24185;
    static final int VOID_MELEE_HELM_OR = 26477, VOID_MELEE_HELM_L_OR = 27007;

    // ---- Body ----
    static final int TORVA_PLATEBODY = 26384, SANGUINE_TORVA_BODY = 28256;
    static final int OATHPLATE_CHEST = 30753, RADIANT_OATHPLATE_CHEST = 30779;
    static final int BANDOS_CHESTPLATE = 11832;
    static final int ELITE_VOID_TOP = 13072, ELITE_VOID_TOP_L = 24178, ELITE_VOID_TOP_OR = 26469, ELITE_VOID_TOP_L_OR = 27003;
    static final int VOID_KNIGHT_TOP = 8839, VOID_KNIGHT_TOP_L = 24177, VOID_KNIGHT_TOP_OR = 26463, VOID_KNIGHT_TOP_L_OR = 27000;

    // ---- Legs ----
    static final int TORVA_PLATELEGS = 26386, SANGUINE_TORVA_LEGS = 28258;
    static final int OATHPLATE_LEGS = 30756, RADIANT_OATHPLATE_LEGS = 30781;
    static final int BANDOS_TASSETS = 11834;
    static final int ELITE_VOID_ROBE = 13073, ELITE_VOID_ROBE_L = 24180, ELITE_VOID_ROBE_OR = 26471, ELITE_VOID_ROBE_L_OR = 27004;
    static final int VOID_KNIGHT_ROBE = 8840, VOID_KNIGHT_ROBE_L = 24179, VOID_KNIGHT_ROBE_OR = 26465, VOID_KNIGHT_ROBE_L_OR = 27001;

    // ---- Melee cape ----
    static final int INFERNAL_CAPE = 21295, INFERNAL_CAPE_L = 24224, INFERNAL_MAX_CAPE_L = 24133;
    static final int FIRE_CAPE = 6570, FIRE_CAPE_L = 24223, FIRE_MAX_CAPE = 13329, FIRE_MAX_CAPE_L = 24134;

    // ---- Neck ----
    static final int AMULET_OF_RANCOUR = 29801, AMULET_OF_RANCOUR_S = 29804, AMULET_OF_TORTURE = 19553;

    // ---- Boots ----
    static final int AVERNIC_TREADS = 31088, AVERNIC_TREADS_MAX = 31097, PRIMORDIAL_BOOTS = 13239;

    // ---- Ring ----
    static final int ULTOR_RING = 28307, BERSERKER_RING_I = 11773;

    // ---- Shield ----
    static final int BOOK_OF_DEAD = 25818, AVERNIC_DEFENDER = 22477;
    static final int GHOMMALS_AVERNIC_5 = 27079, GHOMMALS_AVERNIC_5L = 27080;
    static final int GHOMMALS_AVERNIC_6 = 27551, GHOMMALS_AVERNIC_6L = 27553, DRAGON_DEFENDER = 8850;

    // ---- Gloves ----
    static final int FEROCIOUS_GLOVES = 22981;
    static final int VOID_KNIGHT_GLOVES = 8842, VOID_KNIGHT_GLOVES_L = 24182, VOID_KNIGHT_GLOVES_OR = 26467, VOID_KNIGHT_GLOVES_L_OR = 27002;

    // ---- Scythe / tentacle ----
    static final int SCYTHE_OF_VITUR = 22325, SCYTHE_UNCHARGED = 22324, HOLY_SCYTHE = 25736, SANGUINE_SCYTHE = 25739;
    static final int ABYSSAL_TENTACLE = 12006;

    // ---- Specs ----
    static final int DRAGON_CLAWS = 13652, BURNING_CLAWS = 29577;
    static final int ELDER_MAUL = 21003, DRAGON_WARHAMMER = 13576;

    // ---- Range cape switch (all cosmetic + (l) variants) ----
    static final int DIZANAS_MAX_CAPE = 28902, DIZANAS_MAX_CAPE_L = 28906;
    static final int DIZANAS_QUIVER = 28951, DIZANAS_QUIVER_L = 28953;
    static final int BLESSED_DIZANAS_QUIVER = 28955, BLESSED_DIZANAS_QUIVER_L = 28957;
    static final int AVAS_ASSEMBLER = 22109, AVAS_ASSEMBLER_L = 24222;
    static final int ASSEMBLER_MAX_CAPE = 21898, ASSEMBLER_MAX_CAPE_L = 24135;
    static final int MASORI_ASSEMBLER = 27374, MASORI_ASSEMBLER_L = 27376;
    static final int MASORI_ASSEMBLER_MAX_CAPE = 27363, MASORI_ASSEMBLER_MAX_CAPE_L = 27365;

    // ---- Fixed / shared inventory ----
    static final int BLAZING_BLOWPIPE = 28688, BLOWPIPE = 12926;
    static final int NECKLACE_OF_ANGUISH_OR = 22249;
    static final int VOID_RANGER_HELM = 11664, VOID_RANGER_HELM_L = 24184;
    static final int VOID_RANGER_HELM_OR = 26475, VOID_RANGER_HELM_L_OR = 27006;
    static final int SALVE_AMULET_E = 10588, CRYSTAL_HALBERD = 23987;
    static final int BANDOS_GODSWORD_OR = 20370, BANDOS_GODSWORD = 11804, SULPHUR_BLADES = 29084;
    static final int SARADOMIN_BREW_4 = 6685, SUPER_RESTORE_4 = 3024;
    static final int DIVINE_SUPER_COMBAT_4 = 23685, SUPER_COMBAT_4 = 12695;
    static final int ANGLERFISH = 13441, RANGING_POTION_4 = 2444;
    static final int DIVINE_RUNE_POUCH_L = 27509, RUNE_POUCH = 12791, RADAS_BLESSING_4 = 22947;

    // ---- RDPS-specific ----
    static final int BLACK_CHINCHOMPA = 11959;
    static final int LAVA_RUNE = 4699, ASTRAL_RUNE = 9075;

    // ---- Freeze-specific (mage gear) ----
    static final int BLOOD_ANCIENT_SCEPTRE = 28260;
    static final int ELIDINIS_WARD_OR = 27253;
    static final int VOID_MAGE_HELM = 11663, VOID_MAGE_HELM_L = 24183;
    static final int VOID_MAGE_HELM_OR = 26473, VOID_MAGE_HELM_L_OR = 27005;
    static final int OCCULT_NECKLACE_OR = 19720;
    static final int IMBUED_GUTHIX_MAX_CAPE = 21784;
    static final int SATURATED_HEART = 27641;

    // ------------------------------------------------------------------
    //  SLOT GROUPS — any one ID satisfies the slot
    //  Order matters: the panel shows the FIRST owned item in the set, so the
    //  ideal/priority item is listed first (Oathplate before Torva/Bandos for
    //  body & legs; Torva before Oathplate for helm, for the strength bonus).
    // ------------------------------------------------------------------
    static final Set<Integer> HELM_TORVA   = sub(TORVA_FULL_HELM, SANGUINE_TORVA_HELM);
    static final Set<Integer> HELM_OATH    = sub(OATHPLATE_HELM, RADIANT_OATHPLATE_HELM);
    static final Set<Integer> OATH_CHEST   = sub(OATHPLATE_CHEST, RADIANT_OATHPLATE_CHEST);
    static final Set<Integer> OATH_LEGS    = sub(OATHPLATE_LEGS, RADIANT_OATHPLATE_LEGS);
    static final Set<Integer> HELM_VOID    = sub(VOID_MELEE_HELM, VOID_MELEE_HELM_L, VOID_MELEE_HELM_OR, VOID_MELEE_HELM_L_OR);
    static final Set<Integer> HELM_ANY     = sub(TORVA_FULL_HELM, SANGUINE_TORVA_HELM, OATHPLATE_HELM, RADIANT_OATHPLATE_HELM, NEITIZNOT_FACEGUARD);

    // Body/Legs: Oathplate listed FIRST so it's the ideal shown in the panel.
    static final Set<Integer> BODY_NONVOID = sub(OATHPLATE_CHEST, RADIANT_OATHPLATE_CHEST, TORVA_PLATEBODY, SANGUINE_TORVA_BODY, BANDOS_CHESTPLATE);
    static final Set<Integer> BODY_VOID    = sub(
            ELITE_VOID_TOP, ELITE_VOID_TOP_L, ELITE_VOID_TOP_OR, ELITE_VOID_TOP_L_OR,
            VOID_KNIGHT_TOP, VOID_KNIGHT_TOP_L, VOID_KNIGHT_TOP_OR, VOID_KNIGHT_TOP_L_OR);

    static final Set<Integer> LEGS_NONVOID = sub(OATHPLATE_LEGS, RADIANT_OATHPLATE_LEGS, TORVA_PLATELEGS, SANGUINE_TORVA_LEGS, BANDOS_TASSETS);
    static final Set<Integer> LEGS_VOID    = sub(
            ELITE_VOID_ROBE, ELITE_VOID_ROBE_L, ELITE_VOID_ROBE_OR, ELITE_VOID_ROBE_L_OR,
            VOID_KNIGHT_ROBE, VOID_KNIGHT_ROBE_L, VOID_KNIGHT_ROBE_OR, VOID_KNIGHT_ROBE_L_OR);

    // Void switch sets (elite preferred, but elite + regular + all cosmetics accepted)
    static final Set<Integer> VOID_TOP_ANY = sub(
            ELITE_VOID_TOP, ELITE_VOID_TOP_L, ELITE_VOID_TOP_OR, ELITE_VOID_TOP_L_OR);
    static final Set<Integer> VOID_ROBE_ANY = sub(
            ELITE_VOID_ROBE, ELITE_VOID_ROBE_L, ELITE_VOID_ROBE_OR, ELITE_VOID_ROBE_L_OR);
    static final Set<Integer> VOID_GLOVES_ANY = sub(
            VOID_KNIGHT_GLOVES, VOID_KNIGHT_GLOVES_L, VOID_KNIGHT_GLOVES_OR, VOID_KNIGHT_GLOVES_L_OR);
    static final Set<Integer> VOID_RANGER_HELM_ANY = sub(
            VOID_RANGER_HELM, VOID_RANGER_HELM_L, VOID_RANGER_HELM_OR, VOID_RANGER_HELM_L_OR);
    static final Set<Integer> VOID_MAGE_HELM_ANY = sub(
            VOID_MAGE_HELM, VOID_MAGE_HELM_L, VOID_MAGE_HELM_OR, VOID_MAGE_HELM_L_OR);

    static final Set<Integer> CAPE_ANY     = sub(INFERNAL_CAPE, INFERNAL_CAPE_L, INFERNAL_MAX_CAPE_L, FIRE_CAPE, FIRE_CAPE_L, FIRE_MAX_CAPE, FIRE_MAX_CAPE_L);
    static final Set<Integer> NECK_ANY     = sub(AMULET_OF_RANCOUR, AMULET_OF_RANCOUR_S, AMULET_OF_TORTURE);
    static final Set<Integer> BOOTS_ANY    = sub(AVERNIC_TREADS, AVERNIC_TREADS_MAX, PRIMORDIAL_BOOTS);
    static final Set<Integer> RING_ANY     = sub(ULTOR_RING, BERSERKER_RING_I);
    static final Set<Integer> MAGE_ANY     = sub(EYE_OF_AYAK, SANGUINESTI_STAFF, TRIDENT_OF_SWAMP);
    static final Set<Integer> SCYTHE_ANY   = sub(SCYTHE_OF_VITUR, SCYTHE_UNCHARGED, HOLY_SCYTHE, SANGUINE_SCYTHE);
    static final Set<Integer> DEFENDER_ANY = sub(AVERNIC_DEFENDER, GHOMMALS_AVERNIC_5, GHOMMALS_AVERNIC_5L, GHOMMALS_AVERNIC_6, GHOMMALS_AVERNIC_6L, DRAGON_DEFENDER);
    static final Set<Integer> DPS_SPEC     = sub(DRAGON_CLAWS, BURNING_CLAWS);
    static final Set<Integer> DEF_SPEC     = sub(ELDER_MAUL, DRAGON_WARHAMMER);
    static final Set<Integer> QUIVER_ANY   = sub(
            DIZANAS_MAX_CAPE, DIZANAS_MAX_CAPE_L,
            DIZANAS_QUIVER, DIZANAS_QUIVER_L,
            BLESSED_DIZANAS_QUIVER, BLESSED_DIZANAS_QUIVER_L,
            AVAS_ASSEMBLER, AVAS_ASSEMBLER_L,
            ASSEMBLER_MAX_CAPE, ASSEMBLER_MAX_CAPE_L,
            MASORI_ASSEMBLER, MASORI_ASSEMBLER_L,
            MASORI_ASSEMBLER_MAX_CAPE, MASORI_ASSEMBLER_MAX_CAPE_L);
    static final Set<Integer> BLOWPIPE_ANY = sub(BLAZING_BLOWPIPE, BLOWPIPE);
    static final Set<Integer> BGS_ANY      = sub(BANDOS_GODSWORD_OR, BANDOS_GODSWORD);
    static final Set<Integer> POUCH_ANY    = sub(DIVINE_RUNE_POUCH_L, RUNE_POUCH);

    // Helm priority for the "better item in bank" warning (best first).
    // Torva (strength) > Oathplate (accuracy) > Faceguard.
    static final List<Set<Integer>> HELM_PRIORITY = priorityList(HELM_TORVA, HELM_OATH, sub(NEITIZNOT_FACEGUARD));
    // Body/Legs priority: Oathplate (accuracy) > Torva > Bandos.
    static final List<Set<Integer>> BODY_PRIORITY = priorityList(
            sub(OATHPLATE_CHEST, RADIANT_OATHPLATE_CHEST),
            sub(TORVA_PLATEBODY, SANGUINE_TORVA_BODY),
            sub(BANDOS_CHESTPLATE));
    static final List<Set<Integer>> LEGS_PRIORITY = priorityList(
            sub(OATHPLATE_LEGS, RADIANT_OATHPLATE_LEGS),
            sub(TORVA_PLATELEGS, SANGUINE_TORVA_LEGS),
            sub(BANDOS_TASSETS));
    // DPS spec: Dragon Claws > Burning Claws.
    static final List<Set<Integer>> DPS_SPEC_PRIORITY = priorityList(
            sub(DRAGON_CLAWS),
            sub(BURNING_CLAWS));
    // Defence spec: Elder Maul > Dragon Warhammer.
    static final List<Set<Integer>> DEF_SPEC_PRIORITY = priorityList(
            sub(ELDER_MAUL),
            sub(DRAGON_WARHAMMER));

    static final Set<Integer> ALL_RELEVANT_IDS = buildAllRelevant();

    private static Set<Integer> buildAllRelevant()
    {
        Set<Integer> s = new HashSet<>();
        s.addAll(HELM_TORVA); s.addAll(HELM_OATH); s.addAll(HELM_VOID); s.add(NEITIZNOT_FACEGUARD);
        s.addAll(BODY_NONVOID); s.addAll(BODY_VOID);
        s.addAll(LEGS_NONVOID); s.addAll(LEGS_VOID);
        s.addAll(CAPE_ANY); s.addAll(NECK_ANY); s.addAll(BOOTS_ANY); s.addAll(RING_ANY);
        s.addAll(MAGE_ANY); s.addAll(SCYTHE_ANY); s.addAll(DEFENDER_ANY);
        s.addAll(DPS_SPEC); s.addAll(DEF_SPEC); s.addAll(QUIVER_ANY);
        s.addAll(BLOWPIPE_ANY); s.addAll(BGS_ANY); s.addAll(POUCH_ANY);
        s.add(BOOK_OF_DEAD); s.add(FEROCIOUS_GLOVES);
        s.addAll(VOID_GLOVES_ANY); s.addAll(VOID_RANGER_HELM_ANY); s.addAll(VOID_MAGE_HELM_ANY);
        s.addAll(VOID_TOP_ANY); s.addAll(VOID_ROBE_ANY);
        s.add(ABYSSAL_TENTACLE); s.add(NECKLACE_OF_ANGUISH_OR);
        s.add(SALVE_AMULET_E); s.add(CRYSTAL_HALBERD); s.add(SULPHUR_BLADES);
        s.add(SARADOMIN_BREW_4); s.add(SUPER_RESTORE_4); s.add(DIVINE_SUPER_COMBAT_4); s.add(SUPER_COMBAT_4);
        s.add(ANGLERFISH); s.add(RANGING_POTION_4); s.add(RADAS_BLESSING_4);

        // RDPS
        s.add(BLACK_CHINCHOMPA); s.add(LAVA_RUNE); s.add(ASTRAL_RUNE);
        // Freeze mage gear
        s.add(BLOOD_ANCIENT_SCEPTRE); s.add(ELIDINIS_WARD_OR);
        s.add(OCCULT_NECKLACE_OR); s.add(IMBUED_GUTHIX_MAX_CAPE); s.add(SATURATED_HEART);
        return s;
    }

    // ------------------------------------------------------------------
    //  Detection helpers
    // ------------------------------------------------------------------
    public static boolean hasScythe(Set<Integer> ownedIds)
    {
        return ownedIds.stream().anyMatch(SCYTHE_ANY::contains);
    }

    /** True only if the player owns a full Oathplate set (any variant): helm + chest + legs. */
    public static boolean hasFullOathplate(Set<Integer> ownedIds)
    {
        return ownedIds.stream().anyMatch(HELM_OATH::contains)
                && ownedIds.stream().anyMatch(OATH_CHEST::contains)
                && ownedIds.stream().anyMatch(OATH_LEGS::contains);
    }

    /** Expected spellbook for a role (matches the SPELLBOOK varbit). */
    public static int expectedSpellbook(Role role)
    {
        switch (role)
        {
            case RANGED:       return LUNAR;
            case NORTH_FREEZE:
            case SOUTH_FREEZE: return ANCIENT;
            case MELEE:
            default:           return ARCEUUS;
        }
    }

    // ------------------------------------------------------------------
    //  REQUIREMENTS — role-aware
    // ------------------------------------------------------------------
    public static List<SlotReq> requirements(Role role, boolean scytheSetup)
    {
        switch (role)
        {
            case RANGED:       return rangedReqs(scytheSetup);
            case NORTH_FREEZE: return freezeReqs(scytheSetup, true);
            case SOUTH_FREEZE: return freezeReqs(scytheSetup, false);
            case MELEE:
            default:           return meleeReqs(scytheSetup);
        }
    }

    /**
     * Setup-aware overload. The only extra state today is MDPS No-Scythe with an
     * Oathplate + Abyssal tentacle setup, which the plugin auto-detects from the
     * player's bank. Everything else falls through to the two-arg behaviour.
     */
    public static List<SlotReq> requirements(Role role, boolean scytheSetup, boolean oathplateWhip)
    {
        if (role == Role.MELEE && !scytheSetup && oathplateWhip)
            return meleeOathplateWhipReqs();
        return requirements(role, scytheSetup);
    }

    /** Back-compat: defaults to melee. */
    public static List<SlotReq> requirements(boolean scytheSetup)
    {
        return meleeReqs(scytheSetup);
    }

    // ---- MDPS (unchanged behaviour) ----
    private static List<SlotReq> meleeReqs(boolean scythe)
    {
        List<SlotReq> r = new ArrayList<>();
        if (scythe)
        {
            r.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, true));
            r.add(new SlotReq("Helm", HELM_ANY, 1, true));
            r.add(new SlotReq("Body", BODY_NONVOID, 1, true));
            r.add(new SlotReq("Legs", LEGS_NONVOID, 1, true));
            r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
            r.add(new SlotReq("Neck", NECK_ANY, 1, true));
            r.add(new SlotReq("Shield", sub(BOOK_OF_DEAD), 1, true));
            r.add(new SlotReq("Gloves", sub(FEROCIOUS_GLOVES), 1, true));
            r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            r.add(new SlotReq("Ring", RING_ANY, 1, true));
            r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            r.add(new SlotReq("Scythe", SCYTHE_ANY, 1, false));
            r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            r.add(new SlotReq("Void Top", VOID_TOP_ANY, 1, false));
            r.add(new SlotReq("Void Robe", VOID_ROBE_ANY, 1, false));
            r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, false));
            r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, false));
            r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
            r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
            r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            r.add(new SlotReq("BGS", BGS_ANY, 1, false));
            r.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
            r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 3, false));
            r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 3, false));
            r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            r.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }
        else
        {
            r.add(new SlotReq("Abyssal Tentacle", sub(ABYSSAL_TENTACLE), 1, true));
            r.add(new SlotReq("Void Helm", HELM_VOID, 1, true));
            r.add(new SlotReq("Void Top", BODY_VOID, 1, true));
            r.add(new SlotReq("Void Robe", LEGS_VOID, 1, true));
            r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
            r.add(new SlotReq("Neck", NECK_ANY, 1, true));
            r.add(new SlotReq("Defender", DEFENDER_ANY, 1, true));
            r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, true));
            r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            r.add(new SlotReq("Ring", RING_ANY, 1, true));
            r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            r.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, false));
            r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, false));
            r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
            r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
            r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            r.add(new SlotReq("BGS", BGS_ANY, 1, false));
            r.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
            r.add(new SlotReq("Book of the Dead", sub(BOOK_OF_DEAD), 1, false));
            r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 4, false));
            r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 4, false));
            r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            r.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }
        return r;
    }

    // ---- MDPS, No-Scythe, Oathplate + Abyssal tentacle ----
    // Worn = Oathplate (locked) + tentacle + Avernic defender + ferocious gloves.
    // Void is carried as the ranged switch. Auto-selected when the player owns
    // full Oathplate + a tentacle and has no scythe.
    private static List<SlotReq> meleeOathplateWhipReqs()
    {
        List<SlotReq> r = new ArrayList<>();

        // Worn
        r.add(new SlotReq("Oathplate Helm", HELM_OATH, 1, true));
        r.add(new SlotReq("Oathplate Chest", OATH_CHEST, 1, true));
        r.add(new SlotReq("Oathplate Legs", OATH_LEGS, 1, true));
        r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
        r.add(new SlotReq("Neck", NECK_ANY, 1, true));
        r.add(new SlotReq("Abyssal Tentacle", sub(ABYSSAL_TENTACLE), 1, true));
        r.add(new SlotReq("Defender", DEFENDER_ANY, 1, true));
        r.add(new SlotReq("Gloves", sub(FEROCIOUS_GLOVES), 1, true));
        r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
        r.add(new SlotReq("Ring", RING_ANY, 1, true));
        r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));

        // Inventory — void ranged switch
        r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, false));
        r.add(new SlotReq("Void Top", VOID_TOP_ANY, 1, false));
        r.add(new SlotReq("Void Robe", VOID_ROBE_ANY, 1, false));
        r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, false));
        r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
        r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
        r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));

        // Inventory — specs + utility
        r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
        r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
        r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
        r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
        r.add(new SlotReq("BGS", BGS_ANY, 1, false));
        r.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
        r.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, false));
        r.add(new SlotReq("Book of the Dead", sub(BOOK_OF_DEAD), 1, false));

        // Inventory — consumables
        r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 3, false));
        r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
        r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 2, false));
        r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
        r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
        r.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
        r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));

        return r;
    }

    // ---- RDPS ----
    // Mirrors MDPS, plus Black Chinchompa (x20, stackable), Lava + Astral runes, Eye of Ayak switch.
    private static List<SlotReq> rangedReqs(boolean scythe)
    {
        List<SlotReq> r = new ArrayList<>();
        if (scythe)
        {
            r.add(new SlotReq("Helm", HELM_ANY, 1, true));
            r.add(new SlotReq("Body", BODY_NONVOID, 1, true));
            r.add(new SlotReq("Legs", LEGS_NONVOID, 1, true));
            r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
            r.add(new SlotReq("Neck", NECK_ANY, 1, true));
            r.add(new SlotReq("Shield", sub(BOOK_OF_DEAD), 1, true));
            r.add(new SlotReq("Gloves", sub(FEROCIOUS_GLOVES), 1, true));
            r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            r.add(new SlotReq("Ring", RING_ANY, 1, true));
            r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            r.add(new SlotReq("Scythe", SCYTHE_ANY, 1, false));
            r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            // Chins worn (thrown weapon slot) for the scythe setup. Keeping them
            // equipped also frees an inventory slot so the Rune Pouch fits in 28.
            r.add(new SlotReq("Chinchompas", sub(BLACK_CHINCHOMPA), 20, true));
            r.add(new SlotReq("Void Top", VOID_TOP_ANY, 1, false));
            r.add(new SlotReq("Void Robe", VOID_ROBE_ANY, 1, false));
            r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, false));
            r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, false));
            r.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, false));
            r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
            r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
            r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 3, false));
            r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 2, false));
            r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            r.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            r.add(new SlotReq("Lava Runes", sub(LAVA_RUNE), 100, false));
            r.add(new SlotReq("Astral Runes", sub(ASTRAL_RUNE), 100, false));
            r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }
        else
        {
            r.add(new SlotReq("Void Helm", HELM_VOID, 1, true));
            r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, true));
            r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, true));
            r.add(new SlotReq("Abyssal Tentacle", sub(ABYSSAL_TENTACLE), 1, true));
            r.add(new SlotReq("Void Top", BODY_VOID, 1, true));
            r.add(new SlotReq("Defender", DEFENDER_ANY, 1, true));
            r.add(new SlotReq("Void Robe", LEGS_VOID, 1, true));
            r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, true));
            r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            r.add(new SlotReq("Ring", RING_ANY, 1, true));
            r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, false));
            r.add(new SlotReq("Chinchompas", sub(BLACK_CHINCHOMPA), 20, false));
            r.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, false));
            r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, false));
            r.add(new SlotReq("Neck", NECK_ANY, 1, false));
            r.add(new SlotReq("Book of the Dead", sub(BOOK_OF_DEAD), 1, false));
            r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 4, false));
            r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 3, false));
            r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            r.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            r.add(new SlotReq("Lava Runes", sub(LAVA_RUNE), 100, false));
            r.add(new SlotReq("Astral Runes", sub(ASTRAL_RUNE), 100, false));
            r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }
        return r;
    }

    // ---- FREEZE (North/South) ----
    // Worn = ranged-void freeze setup with Blood Ancient Sceptre + Elidinis' ward.
    // north=true keeps Sulphur Blades; south drops it for +1 Anglerfish.
    private static List<SlotReq> freezeReqs(boolean scythe, boolean north)
    {
        List<SlotReq> r = new ArrayList<>();

        // Worn (identical for both freeze variants)
        r.add(new SlotReq("Void Ranger Helm", VOID_RANGER_HELM_ANY, 1, true));
        r.add(new SlotReq("Range Cape", QUIVER_ANY, 1, true));
        r.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, true));
        r.add(new SlotReq("Blood Ancient Sceptre", sub(BLOOD_ANCIENT_SCEPTRE), 1, true));
        r.add(new SlotReq("Void Top", BODY_VOID, 1, true));
        r.add(new SlotReq("Elidinis' Ward (or)", sub(ELIDINIS_WARD_OR), 1, true));
        r.add(new SlotReq("Void Robe", LEGS_VOID, 1, true));
        r.add(new SlotReq("Void Gloves", VOID_GLOVES_ANY, 1, true));
        r.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
        r.add(new SlotReq("Ring", RING_ANY, 1, true));
        r.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));

        // Inventory — mage switch
        r.add(new SlotReq("Void Mage Helm", VOID_MAGE_HELM_ANY, 1, false));
        r.add(new SlotReq("Occult (or)", sub(OCCULT_NECKLACE_OR), 1, false));
        r.add(new SlotReq("Imbued Guthix Cape", sub(IMBUED_GUTHIX_MAX_CAPE), 1, false));
        r.add(new SlotReq("Saturated Heart", sub(SATURATED_HEART), 1, false));
        r.add(new SlotReq("Eye of Ayak", MAGE_ANY, 1, false));

        // Inventory — ranged/utility
        r.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));

        // Inventory — melee switch (scythe vs no-scythe)
        if (scythe)
        {
            r.add(new SlotReq("Scythe", SCYTHE_ANY, 1, false));
            r.add(new SlotReq("Helm", HELM_ANY, 1, false));
            r.add(new SlotReq("Body", BODY_NONVOID, 1, false));
            r.add(new SlotReq("Legs", LEGS_NONVOID, 1, false));
            r.add(new SlotReq("Gloves", sub(FEROCIOUS_GLOVES), 1, false));
        }
        else
        {
            r.add(new SlotReq("Abyssal Tentacle", sub(ABYSSAL_TENTACLE), 1, false));
            r.add(new SlotReq("Void Melee Helm", HELM_VOID, 1, false));
            r.add(new SlotReq("Defender", DEFENDER_ANY, 1, false));
        }

        // Shared switches
        r.add(new SlotReq("Neck", NECK_ANY, 1, false));
        r.add(new SlotReq("Melee Cape", CAPE_ANY, 1, false));
        r.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
        r.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
        r.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
        r.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));

        // North freeze keeps Sulphur Blades; south swaps it out (handled below).
        if (north)
        {
            r.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
        }

        // Consumables — counts differ by scythe and north/south
        r.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 3, false));
        r.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 2, false));

        int angler;
        if (scythe) angler = north ? 2 : 3;
        else        angler = north ? 3 : 4;
        r.add(new SlotReq("Anglerfish", sub(ANGLERFISH), angler, false));

        r.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
        r.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), scythe ? 1 : 2, false));
        r.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));

        return r;
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------
    static Set<Integer> sub(int... ids)
    {
        Set<Integer> set = new LinkedHashSet<>();
        for (int id : ids) if (id > 0) set.add(id);
        return set;
    }

    @SafeVarargs
    static List<Set<Integer>> priorityList(Set<Integer>... tiers)
    {
        List<Set<Integer>> list = new ArrayList<>();
        for (Set<Integer> t : tiers) list.add(t);
        return list;
    }
}