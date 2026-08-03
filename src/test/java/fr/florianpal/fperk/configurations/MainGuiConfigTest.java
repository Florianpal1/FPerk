package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.configurations.gui.MainGuiConfig;
import fr.florianpal.fperk.enums.ActionType;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainGuiConfigTest {

    private static MainGuiConfig load(String document) {
        MainGuiConfig config = new MainGuiConfig();
        config.load(Yaml.of(document));
        return config;
    }

    private static final String FULL = """
            gui:
              size: 27
              name: "Perk ({Page}/{TotalPage})"
            skillFormat: "- {DisplayName}"
            perk:
              title: "&3Perk: &a{Name}"
              description:
                - "Enabled : {IsEnabled}"
                - "{skills}"
            block:
              '0':
                utility: barrier
                material: GRAY_STAINED_GLASS_PANE
                title: " "
                description: {}
              '1':
                utility: perk
              '2':
                utility: perk
              '18':
                utility: previous
                material: ARROW
                title: "&5Previous"
                description:
                  - "back"
                replacement:
                  material: AIR
                  title: "-"
                  description: {}
              '19':
                utility: next
                material: ARROW
                title: "&5Next"
                description:
                  - "forward"
                replacement:
                  material: AIR
                  title: "-"
                  description: {}
              '25':
                utility: action
                material: TNT
                type: RESET_ALL
                title: "&cReset"
                description:
                  - "everything off"
              '26':
                utility: close
                material: BARRIER
                title: "&cClose"
                description:
                  - "bye"
            """;

    @Test
    void readsTheOverallShape() {
        MainGuiConfig config = load(FULL);

        assertEquals(27, config.getSize());
        assertEquals("Perk ({Page}/{TotalPage})", config.getNameGui());
        assertEquals("- {DisplayName}", config.getSkillFormat());
        assertEquals("&3Perk: &a{Name}", config.getPerkTitle());
        assertEquals(List.of("Enabled : {IsEnabled}", "{skills}"), config.getPerkDescription());
    }

    @Test
    @DisplayName("each utility feeds its own list, keyed by slot index")
    void sortsBlocksByUtility() {
        MainGuiConfig config = load(FULL);

        assertEquals(List.of(1, 2), config.getPerkBlocks());
        assertEquals(1, config.getBarrierBlocks().size());
        assertEquals(0, config.getBarrierBlocks().get(0).getIndex());
        assertEquals(1, config.getPreviousBlocks().size());
        assertEquals(18, config.getPreviousBlocks().get(0).getIndex());
        assertEquals(1, config.getNextBlocks().size());
        assertEquals(19, config.getNextBlocks().get(0).getIndex());
        assertEquals(1, config.getActionBlocks().size());
        assertEquals(1, config.getCloseBlocks().size());
        assertEquals(26, config.getCloseBlocks().get(0).getIndex());
    }

    @Test
    void readsBlockAppearance() {
        Barrier close = load(FULL).getCloseBlocks().get(0);

        assertEquals(Material.BARRIER, close.getMaterial());
        assertEquals("&cClose", close.getTitle());
        assertEquals(List.of("bye"), close.getDescription());
        assertEquals("", close.getTexture(), "no texture means an empty string, never null");
    }

    @Test
    @DisplayName("the arrows carry the item shown when there is no page to go to")
    void readsArrowReplacements() {
        Barrier next = load(FULL).getNextBlocks().get(0);

        assertEquals(Material.ARROW, next.getMaterial());
        assertEquals(Material.AIR, next.getRemplacement().getMaterial());
        assertEquals("-", next.getRemplacement().getTitle());
        assertNotEquals(next.getMaterial(), next.getRemplacement().getMaterial(),
                "the click handler tells them apart by material");
    }

    @Test
    void readsActionType() {
        Action action = load(FULL).getActionBlocks().get(0);

        assertEquals(ActionType.RESET_ALL, action.getType());
        assertEquals(Material.TNT, action.getMaterial());
    }

    @Test
    void readsHeadTextures() {
        MainGuiConfig config = load("""
                gui:
                  size: 9
                  name: "t"
                skillFormat: ""
                perk:
                  title: "t"
                  description: {}
                block:
                  '0':
                    utility: barrier
                    material: PLAYER_HEAD
                    texture: "base64value"
                    title: " "
                    description: {}
                """);

        assertEquals("base64value", config.getBarrierBlocks().get(0).getTexture());
    }

    @Test
    @DisplayName("an unknown utility is skipped rather than breaking the menu")
    void ignoresUnknownUtility() {
        MainGuiConfig config = load("""
                gui:
                  size: 9
                  name: "t"
                skillFormat: ""
                perk:
                  title: "t"
                  description: {}
                block:
                  '0':
                    utility: nonsense
                    material: STONE
                    title: " "
                    description: {}
                  '1':
                    utility: perk
                """);

        assertEquals(List.of(1), config.getPerkBlocks());
        assertTrue(config.getBarrierBlocks().isEmpty());
    }

    @Test
    @DisplayName("an arrow without its replacement fails the load, and says so at startup")
    void rejectsAnArrowWithoutReplacement() {
        assertThrows(Exception.class, () -> load("""
                gui:
                  size: 9
                  name: "t"
                skillFormat: ""
                perk:
                  title: "t"
                  description: {}
                block:
                  '0':
                    utility: next
                    material: ARROW
                    title: "next"
                    description: {}
                """));
    }

    // --- the file actually shipped in the jar -------------------------------------------------

    @Test
    @DisplayName("the shipped menu is coherent : slots fit, arrows are distinguishable, perks fit")
    void shippedMenuIsCoherent() {
        MainGuiConfig config = new MainGuiConfig();
        config.load(Yaml.resource("gui/mainGui.yml"));

        assertEquals(45, config.getSize());
        assertEquals(21, config.getPerkBlocks().size());
        assertFalse(config.getPerkBlocks().isEmpty(), "with no perk slot the menu cannot paginate");

        int highest = 0;
        for (List<Barrier> blocks : List.of(config.getBarrierBlocks(), config.getNextBlocks(),
                config.getPreviousBlocks(), config.getCloseBlocks())) {
            for (Barrier block : blocks) {
                highest = Math.max(highest, block.getIndex());
            }
        }
        for (Action action : config.getActionBlocks()) {
            highest = Math.max(highest, action.getIndex());
        }
        for (int slot : config.getPerkBlocks()) {
            highest = Math.max(highest, slot);
        }

        assertTrue(highest < config.getSize(),
                "slot " + highest + " does not fit in a menu of " + config.getSize());
        assertEquals(0, config.getSize() % 9, "an inventory size must be a multiple of 9");

        for (Barrier arrow : config.getNextBlocks()) {
            assertNotEquals(arrow.getMaterial(), arrow.getRemplacement().getMaterial(),
                    "an arrow whose replacement shares its material never fires");
        }
        for (Barrier arrow : config.getPreviousBlocks()) {
            assertNotEquals(arrow.getMaterial(), arrow.getRemplacement().getMaterial(),
                    "an arrow whose replacement shares its material never fires");
        }
    }

    @Test
    @DisplayName("the shipped menu offers a way out and a way to turn everything off")
    void shippedMenuHasCloseAndReset() {
        MainGuiConfig config = new MainGuiConfig();
        config.load(Yaml.resource("gui/mainGui.yml"));

        assertFalse(config.getCloseBlocks().isEmpty());
        assertTrue(config.getActionBlocks().stream()
                .anyMatch(action -> action.getType() == ActionType.RESET_ALL));
    }
}
