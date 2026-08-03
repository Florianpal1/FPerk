package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.managers.ConfigurationManager;
import fr.florianpal.fperk.objects.Perk;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PerkConfigTest {

    /**
     * Loads perks against the skills of the given {@code skill.yml} document.
     */
    private static Map<String, Perk> load(String perks, String skills) {
        SkillConfig skillConfig = new SkillConfig();
        skillConfig.load(Yaml.of(skills));

        ConfigurationManager manager = mock(ConfigurationManager.class);
        when(manager.getSkillConfig()).thenReturn(skillConfig);

        PerkConfig config = new PerkConfig();
        config.load(Yaml.of(perks), manager);
        return config.getPerks();
    }

    private static final String SKILLS = """
            skills:
              "fly":
                displayName: ["Fly"]
                type: FLY
                effect: ""
                level: 1
              "haste":
                displayName: ["Haste"]
                type: EFFECT
                effect: "fast_digging"
                level: 2
            """;

    @Test
    void readsEveryField() {
        Perk perk = load("""
                perks:
                  "aviator":
                    displayName: "&bAviator"
                    material: "ELYTRA"
                    skills:
                      - "fly"
                      - "haste"
                    delais: 60000
                    ignoreDelais: false
                    time: 600
                    persistant: false
                    permission: "fperk.aviator"
                    permissionBypass: "fperk.bypass"
                    texture: "abc"
                """, SKILLS).get("aviator");

        assertEquals("aviator", perk.getId());
        assertEquals("&bAviator", perk.getDisplayName());
        assertEquals(Material.ELYTRA, perk.getMaterial());
        assertEquals(60000, perk.getDelais());
        assertFalse(perk.isIgnoreDelais());
        assertEquals(600, perk.getTime());
        assertFalse(perk.isPersistant());
        assertEquals("fperk.aviator", perk.getPermission());
        assertEquals("fperk.bypass", perk.getPermissionBypass());
        assertEquals(2, perk.getSkills().size());
    }

    @Test
    @DisplayName("permissionBypass is null when unset, which is what waives the quota check")
    void bypassIsNullByDefault() {
        Perk perk = load("""
                perks:
                  "fly":
                    displayName: "Fly"
                    material: "ELYTRA"
                    skills: ["fly"]
                    delais: 0
                    ignoreDelais: true
                    time: 10
                    persistant: false
                    permission: "fperk.fly"
                """, SKILLS).get("fly");

        assertNull(perk.getPermissionBypass());
    }

    @Test
    @DisplayName("a skill id absent from skill.yml maps to null, and the perk still loads")
    void toleratesAnUnknownSkillId() {
        Perk perk = load("""
                perks:
                  "broken":
                    displayName: "Broken"
                    material: "STONE"
                    skills:
                      - "fly"
                      - "does_not_exist"
                    delais: 0
                    ignoreDelais: true
                    time: 10
                    persistant: false
                    permission: "fperk.broken"
                """, SKILLS).get("broken");

        assertEquals(2, perk.getSkills().size());
        assertNotNull(perk.getSkills().get("fly"));
        assertNull(perk.getSkills().get("does_not_exist"));
    }

    @Test
    @DisplayName("an unknown material stops the load, so the mistake is visible at startup")
    void rejectsAnUnknownMaterial() {
        assertThrows(IllegalArgumentException.class, () -> load("""
                perks:
                  "bad":
                    displayName: "Bad"
                    material: "NOT_A_MATERIAL"
                    skills: ["fly"]
                    delais: 0
                    ignoreDelais: true
                    time: 10
                    persistant: false
                    permission: "fperk.bad"
                """, SKILLS));
    }

    @Test
    @DisplayName("declaration order is kept, because it drives the order of the icons in the menu")
    void keepsDeclarationOrder() {
        Map<String, Perk> perks = load("""
                perks:
                  "third":
                    displayName: "c"
                    material: "STONE"
                    skills: ["fly"]
                    delais: 0
                    ignoreDelais: true
                    time: 1
                    persistant: true
                    permission: "p"
                  "first":
                    displayName: "a"
                    material: "STONE"
                    skills: ["fly"]
                    delais: 0
                    ignoreDelais: true
                    time: 1
                    persistant: true
                    permission: "p"
                """, SKILLS);

        assertEquals(List.of("third", "first"), List.copyOf(perks.keySet()));
    }

    // --- the file actually shipped in the jar -------------------------------------------------

    private static Map<String, Perk> shipped() {
        SkillConfig skillConfig = new SkillConfig();
        skillConfig.load(Yaml.resource("skill.yml"));

        ConfigurationManager manager = mock(ConfigurationManager.class);
        when(manager.getSkillConfig()).thenReturn(skillConfig);

        PerkConfig config = new PerkConfig();
        config.load(Yaml.resource("perk.yml"), manager);
        return config.getPerks();
    }

    @Test
    @DisplayName("every skill listed by the shipped perk.yml exists in the shipped skill.yml")
    void shippedSkillsAllResolve() {
        shipped().forEach((id, perk) -> perk.getSkills()
                .forEach((skillId, skill) -> assertNotNull(skill,
                        "perk " + id + " references the unknown skill " + skillId)));
    }

    @Test
    @DisplayName("each shipped perk carries its own permission, none is borrowed from another")
    void shippedPermissionsAreUnique() {
        Map<String, Perk> perks = shipped();

        long distinct = perks.values().stream().map(Perk::getPermission).distinct().count();

        assertEquals(perks.size(), distinct, "two perks share a permission");
        assertEquals("fperk.heal", perks.get("heal").getPermission(), "heal used to borrow fperk.fly");
    }

    @Test
    @DisplayName("bannedWorld is gone from the shipped file, since nothing reads it")
    void shippedFileHasNoDeadOption() {
        assertFalse(Yaml.resource("perk.yml").getKeys(true).stream()
                        .anyMatch(key -> key.endsWith("bannedWorld")),
                "bannedWorld is not implemented and must not be advertised");
    }

    @Test
    @DisplayName("the death and harvest perks of the shipped file are permanent, not timed")
    void shippedEventDrivenPerksArePersistent() {
        Map<String, Perk> perks = shipped();

        for (String id : List.of("keep_inventory", "keep_experience", "harvest", "auto_smelt",
                "auto_knockback", "vacuum", "second_chance")) {
            assertTrue(perks.get(id).isPersistant(), id + " should be persistant");
        }

        assertFalse(perks.get("fly").isPersistant());
        assertFalse(perks.get("heal").isPersistant());
    }
}
