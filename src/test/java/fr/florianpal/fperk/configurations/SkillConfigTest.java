package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.objects.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillConfigTest {

    private static Map<String, Skill> load(String document) {
        SkillConfig config = new SkillConfig();
        config.load(Yaml.of(document));
        return config.getSkills();
    }

    @Test
    void readsEveryField() {
        Skill skill = load("""
                skills:
                  "haste":
                    displayName:
                      - "&eHaste II"
                      - "second line"
                    type: EFFECT
                    effect: "fast_digging"
                    level: 2
                """).get("haste");

        assertEquals("haste", skill.getId());
        assertEquals(List.of("&eHaste II", "second line"), skill.getDisplayName());
        assertEquals("EFFECT", skill.getType());
        assertEquals("fast_digging", skill.getEffect());
        assertEquals(2F, skill.getLevel());
    }

    @Test
    @DisplayName("level is read as a decimal, so fly speed and addon parameters keep their precision")
    void readsDecimalLevels() {
        Map<String, Skill> skills = load("""
                skills:
                  "slow":
                    displayName: ["a"]
                    type: FLY_SPEED
                    effect: ""
                    level: 0.25
                  "strong":
                    displayName: ["b"]
                    type: FLY_SPEED
                    effect: ""
                    level: 2.5
                """);

        assertEquals(0.25F, skills.get("slow").getLevel());
        assertEquals(2.5F, skills.get("strong").getLevel());
    }

    @Test
    @DisplayName("the type is normalised on load, so skill.yml may use any case")
    void normalisesTypes() {
        Map<String, Skill> skills = load("""
                skills:
                  "a":
                    displayName: ["a"]
                    type: fly
                    effect: ""
                    level: 1
                  "b":
                    displayName: ["b"]
                    type: "  Auto_Smelt  "
                    effect: ""
                    level: 1
                """);

        assertEquals("FLY", skills.get("a").getType());
        assertEquals("AUTO_SMELT", skills.get("b").getType());
    }

    @Test
    @DisplayName("a skill with no type loads instead of breaking the whole file")
    void toleratesAMissingType() {
        Skill skill = load("""
                skills:
                  "broken":
                    displayName: ["a"]
                    level: 1
                """).get("broken");

        assertEquals("", skill.getType(), "resolving it later reports an unknown type");
        assertEquals(1F, skill.getLevel());
    }

    @Test
    @DisplayName("the optional fields fall back rather than failing the load")
    void toleratesMissingOptionalFields() {
        Skill skill = load("""
                skills:
                  "bare":
                    type: FLY
                """).get("bare");

        assertEquals("FLY", skill.getType());
        assertEquals(0F, skill.getLevel());
        assertNull(skill.getEffect());
        assertTrue(skill.getDisplayName().isEmpty());
    }

    @Test
    @DisplayName("declaration order is kept, because it drives the order of the lore lines")
    void keepsDeclarationOrder() {
        Map<String, Skill> skills = load("""
                skills:
                  "third":
                    displayName: ["c"]
                    type: FLY
                    effect: ""
                    level: 1
                  "first":
                    displayName: ["a"]
                    type: FLY
                    effect: ""
                    level: 1
                  "second":
                    displayName: ["b"]
                    type: FLY
                    effect: ""
                    level: 1
                """);

        assertEquals(List.of("third", "first", "second"), List.copyOf(skills.keySet()));
    }

    @Test
    @DisplayName("every skill of the shipped skill.yml declares a known builtin type")
    void shippedSkillsAreConsistent() {
        SkillConfig config = new SkillConfig();
        config.load(Yaml.resource("skill.yml"));

        assertEquals(12, config.getSkills().size());

        for (Skill skill : config.getSkills().values()) {
            assertTrue(DefaultResources.BUILTIN_TYPES.contains(skill.getType()),
                    "unknown type " + skill.getType() + " on skill " + skill.getId());
            assertTrue(skill.getDisplayName() != null && !skill.getDisplayName().isEmpty(),
                    "skill " + skill.getId() + " has no display name");
        }
    }

    @Test
    @DisplayName("every EFFECT of the shipped skill.yml names an effect, the others do not need one")
    void shippedEffectsAreNamed() {
        SkillConfig config = new SkillConfig();
        config.load(Yaml.resource("skill.yml"));

        for (Skill skill : config.getSkills().values()) {
            if ("EFFECT".equals(skill.getType())) {
                assertTrue(skill.getEffect() != null && !skill.getEffect().isBlank(),
                        "EFFECT skill " + skill.getId() + " names no effect");
            }
        }
    }
}
