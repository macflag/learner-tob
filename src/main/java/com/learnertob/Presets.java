package com.learnertob;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bank-driven presets. Each preset is a list of SlotReqs — "you need at least
 * one valid item per slot". Any valid substitute passes. Cosmetic variants are
 * all included so e.g. Sanguine Scythe satisfies the Scythe slot.
 */
public class Presets
{
    public static final int ARCEUUS = 3;

    // ---- Mage weapon ----
    static final int EYE_OF_AYAK = 31113, SANGUINESTI_STAFF = 22323, TRIDENT_OF_SWAMP = 12899;

    // ---- Helm ----
    static final int TORVA_FULL_HELM = 26382, SANGUINE_TORVA_HELM = 28254;
    static final int OATHPLATE_HELM = 30750, RADIANT_OATHPLATE_HELM = 30777;
    static final int VOID_MELEE_HELM = 11665, VOID_MELEE_HELM_L = 24185;

    // ---- Body ----
    static final int TORVA_PLATEBODY = 26384, SANGUINE_TORVA_BODY = 28256;
    static final int OATHPLATE_CHEST = 30753, RADIANT_OATHPLATE_CHEST = 30779;
    static final int BANDOS_CHESTPLATE = 11832;
    static final int ELITE_VOID_TOP = 13072, ELITE_VOID_TOP_L = 24178, VOID_KNIGHT_TOP = 8839, VOID_KNIGHT_TOP_L = 24177;

    // ---- Legs ----
    static final int TORVA_PLATELEGS = 26386, SANGUINE_TORVA_LEGS = 28258;
    static final int OATHPLATE_LEGS = 30756, RADIANT_OATHPLATE_LEGS = 30781;
    static final int BANDOS_TASSETS = 11834;
    static final int ELITE_VOID_ROBE = 13073, ELITE_VOID_ROBE_L = 24180, VOID_KNIGHT_ROBE = 8840, VOID_KNIGHT_ROBE_L = 24179;

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
    static final int FEROCIOUS_GLOVES = 22981, VOID_KNIGHT_GLOVES = 8842, VOID_KNIGHT_GLOVES_L = 24182;

    // ---- Scythe / tentacle ----
    static final int SCYTHE_OF_VITUR = 22325, SCYTHE_UNCHARGED = 22324, HOLY_SCYTHE = 25736, SANGUINE_SCYTHE = 25739;
    static final int ABYSSAL_TENTACLE = 12006;

    // ---- Specs ----
    static final int DRAGON_CLAWS = 13652, BURNING_CLAWS = 29577;
    static final int ELDER_MAUL = 21003, DRAGON_WARHAMMER = 13576;

    // ---- Range cape switch ----
    static final int DIZANAS_MAX_CAPE_L = 28906, DIZANAS_MAX_CAPE = 28902, BLESSED_DIZANAS_QUIVER = 28955;
    static final int DIZANAS_QUIVER = 28951, AVAS_ASSEMBLER = 22109, AVAS_ASSEMBLER_L = 21924;

    // ---- Fixed inventory ----
    static final int BLAZING_BLOWPIPE = 28688, BLOWPIPE = 12926;
    static final int NECKLACE_OF_ANGUISH_OR = 22249, VOID_RANGER_HELM_L = 24184;
    static final int SALVE_AMULET_E = 10588, CRYSTAL_HALBERD = 23987;
    static final int BANDOS_GODSWORD_OR = 20370, BANDOS_GODSWORD = 11804, SULPHUR_BLADES = 29084;
    static final int SARADOMIN_BREW_4 = 6685, SUPER_RESTORE_4 = 3024;
    static final int DIVINE_SUPER_COMBAT_4 = 23685, SUPER_COMBAT_4 = 12695;
    static final int ANGLERFISH = 13441, RANGING_POTION_4 = 2444;
    static final int DIVINE_RUNE_POUCH_L = 27509, RUNE_POUCH = 12791, RADAS_BLESSING_4 = 22947;
    static final int ELITE_VOID_TOP_L_INV = 24178; // void switch pieces (inv for scythe setup)

    // ------------------------------------------------------------------
    //  SLOT GROUPS — any one ID satisfies the slot
    // ------------------------------------------------------------------
    static final Set<Integer> HELM_TORVA   = sub(TORVA_FULL_HELM, SANGUINE_TORVA_HELM);
    static final Set<Integer> HELM_OATH    = sub(OATHPLATE_HELM, RADIANT_OATHPLATE_HELM);
    static final Set<Integer> HELM_VOID    = sub(VOID_MELEE_HELM, VOID_MELEE_HELM_L);
    static final Set<Integer> HELM_ANY     = sub(TORVA_FULL_HELM, SANGUINE_TORVA_HELM, OATHPLATE_HELM, RADIANT_OATHPLATE_HELM);

    static final Set<Integer> BODY_NONVOID = sub(TORVA_PLATEBODY, SANGUINE_TORVA_BODY, OATHPLATE_CHEST, RADIANT_OATHPLATE_CHEST, BANDOS_CHESTPLATE);
    static final Set<Integer> BODY_VOID    = sub(ELITE_VOID_TOP, ELITE_VOID_TOP_L, VOID_KNIGHT_TOP, VOID_KNIGHT_TOP_L);

    static final Set<Integer> LEGS_NONVOID = sub(TORVA_PLATELEGS, SANGUINE_TORVA_LEGS, OATHPLATE_LEGS, RADIANT_OATHPLATE_LEGS, BANDOS_TASSETS);
    static final Set<Integer> LEGS_VOID    = sub(ELITE_VOID_ROBE, ELITE_VOID_ROBE_L, VOID_KNIGHT_ROBE, VOID_KNIGHT_ROBE_L);

    static final Set<Integer> CAPE_ANY     = sub(INFERNAL_CAPE, INFERNAL_CAPE_L, INFERNAL_MAX_CAPE_L, FIRE_CAPE, FIRE_CAPE_L, FIRE_MAX_CAPE, FIRE_MAX_CAPE_L);
    static final Set<Integer> NECK_ANY     = sub(AMULET_OF_RANCOUR, AMULET_OF_RANCOUR_S, AMULET_OF_TORTURE);
    static final Set<Integer> BOOTS_ANY    = sub(AVERNIC_TREADS, AVERNIC_TREADS_MAX, PRIMORDIAL_BOOTS);
    static final Set<Integer> RING_ANY     = sub(ULTOR_RING, BERSERKER_RING_I);
    static final Set<Integer> MAGE_ANY     = sub(EYE_OF_AYAK, SANGUINESTI_STAFF, TRIDENT_OF_SWAMP);
    static final Set<Integer> SCYTHE_ANY   = sub(SCYTHE_OF_VITUR, SCYTHE_UNCHARGED, HOLY_SCYTHE, SANGUINE_SCYTHE);
    static final Set<Integer> DEFENDER_ANY = sub(AVERNIC_DEFENDER, GHOMMALS_AVERNIC_5, GHOMMALS_AVERNIC_5L, GHOMMALS_AVERNIC_6, GHOMMALS_AVERNIC_6L, DRAGON_DEFENDER);
    static final Set<Integer> DPS_SPEC     = sub(DRAGON_CLAWS, BURNING_CLAWS);
    static final Set<Integer> DEF_SPEC     = sub(ELDER_MAUL, DRAGON_WARHAMMER);
    static final Set<Integer> QUIVER_ANY   = sub(DIZANAS_MAX_CAPE_L, DIZANAS_MAX_CAPE, BLESSED_DIZANAS_QUIVER, DIZANAS_QUIVER, AVAS_ASSEMBLER, AVAS_ASSEMBLER_L);
    static final Set<Integer> BLOWPIPE_ANY = sub(BLAZING_BLOWPIPE, BLOWPIPE);
    static final Set<Integer> BGS_ANY      = sub(BANDOS_GODSWORD_OR, BANDOS_GODSWORD);
    static final Set<Integer> POUCH_ANY    = sub(DIVINE_RUNE_POUCH_L, RUNE_POUCH);

    // All IDs that should be highlighted in bank (everything across both presets)
    static final Set<Integer> ALL_RELEVANT_IDS = buildAllRelevant();

    private static Set<Integer> buildAllRelevant()
    {
        Set<Integer> s = new java.util.HashSet<>();
        s.addAll(HELM_TORVA); s.addAll(HELM_OATH); s.addAll(HELM_VOID);
        s.addAll(BODY_NONVOID); s.addAll(BODY_VOID);
        s.addAll(LEGS_NONVOID); s.addAll(LEGS_VOID);
        s.addAll(CAPE_ANY); s.addAll(NECK_ANY); s.addAll(BOOTS_ANY); s.addAll(RING_ANY);
        s.addAll(MAGE_ANY); s.addAll(SCYTHE_ANY); s.addAll(DEFENDER_ANY);
        s.addAll(DPS_SPEC); s.addAll(DEF_SPEC); s.addAll(QUIVER_ANY);
        s.addAll(BLOWPIPE_ANY); s.addAll(BGS_ANY); s.addAll(POUCH_ANY);
        s.add(BOOK_OF_DEAD); s.add(FEROCIOUS_GLOVES); s.add(VOID_KNIGHT_GLOVES); s.add(VOID_KNIGHT_GLOVES_L);
        s.add(ABYSSAL_TENTACLE); s.add(NECKLACE_OF_ANGUISH_OR); s.add(VOID_RANGER_HELM_L);
        s.add(SALVE_AMULET_E); s.add(CRYSTAL_HALBERD); s.add(SULPHUR_BLADES);
        s.add(SARADOMIN_BREW_4); s.add(SUPER_RESTORE_4); s.add(DIVINE_SUPER_COMBAT_4); s.add(SUPER_COMBAT_4);
        s.add(ANGLERFISH); s.add(RANGING_POTION_4); s.add(RADAS_BLESSING_4);
        s.add(ELITE_VOID_TOP_L); s.add(ELITE_VOID_ROBE_L); s.add(VOID_MELEE_HELM_L);
        return s;
    }

    // ------------------------------------------------------------------
    //  IS THIS A SCYTHE SETUP? — drives which preset to use
    // ------------------------------------------------------------------
    public static boolean hasScythe(Set<Integer> ownedIds)
    {
        return ownedIds.stream().anyMatch(SCYTHE_ANY::contains);
    }

    // ------------------------------------------------------------------
    //  BUILD SLOT REQUIREMENTS for the appropriate setup
    // ------------------------------------------------------------------
    public static List<SlotReq> requirements(boolean scytheSetup)
    {
        List<SlotReq> reqs = new ArrayList<>();

        if (scytheSetup)
        {
            // Worn
            reqs.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, true));
            reqs.add(new SlotReq("Helm", HELM_ANY, 1, true));
            reqs.add(new SlotReq("Body", BODY_NONVOID, 1, true));
            reqs.add(new SlotReq("Legs", LEGS_NONVOID, 1, true));
            reqs.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
            reqs.add(new SlotReq("Neck", NECK_ANY, 1, true));
            reqs.add(new SlotReq("Shield", sub(BOOK_OF_DEAD), 1, true));
            reqs.add(new SlotReq("Gloves", sub(FEROCIOUS_GLOVES), 1, true));
            reqs.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            reqs.add(new SlotReq("Ring", RING_ANY, 1, true));
            reqs.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            // Inventory
            reqs.add(new SlotReq("Scythe", SCYTHE_ANY, 1, false));
            reqs.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            reqs.add(new SlotReq("Void Top", sub(ELITE_VOID_TOP_L, ELITE_VOID_TOP), 1, false));
            reqs.add(new SlotReq("Void Robe", sub(ELITE_VOID_ROBE_L, ELITE_VOID_ROBE), 1, false));
            reqs.add(new SlotReq("Void Ranger Helm", sub(VOID_RANGER_HELM_L), 1, false));
            reqs.add(new SlotReq("Void Gloves", sub(VOID_KNIGHT_GLOVES_L, VOID_KNIGHT_GLOVES), 1, false));
            reqs.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
            reqs.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
            reqs.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            reqs.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            reqs.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            reqs.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            reqs.add(new SlotReq("BGS", BGS_ANY, 1, false));
            reqs.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
            reqs.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 3, false));
            reqs.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            reqs.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 3, false));
            reqs.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            reqs.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            reqs.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            reqs.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }
        else
        {
            // No scythe — void + tentacle worn
            reqs.add(new SlotReq("Abyssal Tentacle", sub(ABYSSAL_TENTACLE), 1, true));
            reqs.add(new SlotReq("Void Helm", HELM_VOID, 1, true));
            reqs.add(new SlotReq("Void Top", BODY_VOID, 1, true));
            reqs.add(new SlotReq("Void Robe", LEGS_VOID, 1, true));
            reqs.add(new SlotReq("Melee Cape", CAPE_ANY, 1, true));
            reqs.add(new SlotReq("Neck", NECK_ANY, 1, true));
            reqs.add(new SlotReq("Defender", DEFENDER_ANY, 1, true));
            reqs.add(new SlotReq("Void Gloves", sub(VOID_KNIGHT_GLOVES_L, VOID_KNIGHT_GLOVES), 1, true));
            reqs.add(new SlotReq("Boots", BOOTS_ANY, 1, true));
            reqs.add(new SlotReq("Ring", RING_ANY, 1, true));
            reqs.add(new SlotReq("Rada's Blessing", sub(RADAS_BLESSING_4), 1, true));
            // Inventory
            reqs.add(new SlotReq("Mage Weapon", MAGE_ANY, 1, false));
            reqs.add(new SlotReq("Blowpipe", BLOWPIPE_ANY, 1, false));
            reqs.add(new SlotReq("Void Ranger Helm", sub(VOID_RANGER_HELM_L), 1, false));
            reqs.add(new SlotReq("Range Cape", QUIVER_ANY, 1, false));
            reqs.add(new SlotReq("Anguish (or)", sub(NECKLACE_OF_ANGUISH_OR), 1, false));
            reqs.add(new SlotReq("DPS Spec", DPS_SPEC, 1, false));
            reqs.add(new SlotReq("Defense Spec", DEF_SPEC, 1, false));
            reqs.add(new SlotReq("Salve (e)", sub(SALVE_AMULET_E), 1, false));
            reqs.add(new SlotReq("Crystal Halberd", sub(CRYSTAL_HALBERD), 1, false));
            reqs.add(new SlotReq("BGS", BGS_ANY, 1, false));
            reqs.add(new SlotReq("Sulphur Blades", sub(SULPHUR_BLADES), 1, false));
            reqs.add(new SlotReq("Book of the Dead", sub(BOOK_OF_DEAD), 1, false));
            reqs.add(new SlotReq("Saradomin Brew", sub(SARADOMIN_BREW_4), 4, false));
            reqs.add(new SlotReq("Super Restore", sub(SUPER_RESTORE_4), 3, false));
            reqs.add(new SlotReq("Anglerfish", sub(ANGLERFISH), 4, false));
            reqs.add(new SlotReq("Divine Super Combat", sub(DIVINE_SUPER_COMBAT_4), 1, false));
            reqs.add(new SlotReq("Super Combat", sub(SUPER_COMBAT_4), 2, false));
            reqs.add(new SlotReq("Ranging Potion", sub(RANGING_POTION_4), 1, false));
            reqs.add(new SlotReq("Rune Pouch", POUCH_ANY, 1, false));
        }

        return reqs;
    }

    static Set<Integer> sub(int... ids)
    {
        Set<Integer> set = new java.util.LinkedHashSet<>();
        for (int id : ids) if (id > 0) set.add(id);
        return set;
    }
}