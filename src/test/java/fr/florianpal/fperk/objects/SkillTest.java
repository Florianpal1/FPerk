package fr.florianpal.fperk.objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTest {

    private static Skill skill(String type) {
        return new Skill("id", List.of("name"), type, "", 1F);
    }

    @ParameterizedTest
    @CsvSource({
            "FLY, FLY",
            "fly, FLY",
            "Fly, FLY",
            "'  fly  ', FLY",
            "fly_speed, FLY_SPEED",
    })
    @DisplayName("the type is trimmed and upper cased, so skill.yml is case insensitive")
    void normalisesType(String written, String expected) {
        assertEquals(expected, skill(written).getType());
    }

    @Test
    @DisplayName("a missing type becomes empty rather than null, so lookups never NPE")
    void nullTypeBecomesEmpty() {
        assertEquals("", skill(null).getType());
    }

    @Test
    @DisplayName("the level keeps its decimals : fly speed and addon parameters need them")
    void levelIsDecimal() {
        Skill skill = new Skill("id", List.of("name"), "FLY_SPEED", "", 0.25F);
        assertEquals(0.25F, skill.getLevel());
    }

    @Test
    void exposesItsFields() {
        Skill skill = new Skill("haste", List.of("&eHaste II", "second line"), "EFFECT", "fast_digging", 2F);

        assertEquals("haste", skill.getId());
        assertEquals("fast_digging", skill.getEffect());
        assertEquals(2, skill.getDisplayName().size());
        assertTrue(skill.getDisplayName().contains("&eHaste II"));
    }
}
