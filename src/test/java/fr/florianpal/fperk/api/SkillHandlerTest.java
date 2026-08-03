package fr.florianpal.fperk.api;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The contract an addon codes against. Lives in the api package so that the package private
 * {@code bind} can be exercised the way the registry uses it.
 */
class SkillHandlerTest {

    private static final class Recording extends SkillHandler {

        final List<String> calls = new ArrayList<>();

        Recording(String id) {
            super(id);
        }

        @Override
        public void onEnable(SkillContext context) {
            calls.add("enable");
        }

        @Override
        public void onDisable(SkillContext context) {
            calls.add("disable");
        }
    }

    private static SkillContext context() {
        Skill skill = new Skill("s", List.of("n"), "TEST", "eff", 2.5F);
        PlayerPerk playerPerk = new PlayerPerk(1, UUID.randomUUID(), "p", new Date().getTime(), true);
        return new SkillContext(null, null, skill, playerPerk);
    }

    @Test
    @DisplayName("the id is trimmed and upper cased, matching how skill.yml is resolved")
    void normalisesId() {
        assertEquals("DOUBLE_JUMP", new Recording(" double_jump ").getId());
    }

    @Test
    @DisplayName("an unusable id is rejected at construction, not at registration")
    void rejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new Recording(null));
        assertThrows(IllegalArgumentException.class, () -> new Recording(""));
        assertThrows(IllegalArgumentException.class, () -> new Recording("   "));
    }

    @Test
    @DisplayName("onRestore falls back to onEnable, so most skills need not override it")
    void restoreDefaultsToEnable() {
        Recording handler = new Recording("TEST");

        handler.onRestore(context());

        assertEquals(List.of("enable"), handler.calls);
    }

    @Test
    @DisplayName("an overridden onRestore is used instead of onEnable")
    void restoreCanBeOverridden() {
        SkillHandler handler = new SkillHandler("TEST") {
            String phase;

            @Override
            public void onEnable(SkillContext context) {
                phase = "enable";
            }

            @Override
            public void onRestore(SkillContext context) {
                phase = "restore";
            }

            @Override
            public String toString() {
                return phase;
            }
        };

        handler.onRestore(context());

        assertEquals("restore", handler.toString());
    }

    @Test
    @DisplayName("the lifecycle methods are all optional")
    void lifecycleMethodsAreOptional() {
        SkillHandler bare = new SkillHandler("BARE") {
        };

        bare.onRegister();
        bare.onEnable(context());
        bare.onDisable(context());
        bare.onRestore(context());
        bare.onUnregister();
    }

    @Test
    @DisplayName("an unbound handler answers false rather than throwing")
    void isActiveWithoutBinding() {
        Recording handler = new Recording("TEST");

        assertFalse(handler.isActive(UUID.randomUUID()));
        assertFalse(handler.isActive((org.bukkit.entity.Player) null));
        assertNull(handler.getOwner());
    }

    @Test
    @DisplayName("once bound, isActive reads the registry state")
    void isActiveReadsTheRegistry() {
        FPerk plugin = mock(FPerk.class);
        SkillRegistry registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);

        Recording handler = new Recording("TEST");
        handler.bind(plugin, plugin);

        UUID uuid = UUID.randomUUID();
        assertFalse(handler.isActive(uuid));

        registry.markActive(uuid, "TEST");
        assertTrue(handler.isActive(uuid));

        registry.markInactive(uuid, "TEST");
        assertFalse(handler.isActive(uuid));
    }

    @Test
    void bindingExposesPluginAndOwner() {
        FPerk plugin = mock(FPerk.class);
        SkillRegistry registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);

        Recording handler = new Recording("TEST");
        handler.bind(plugin, plugin);

        assertSame(plugin, handler.getOwner());
    }

    @Test
    void toStringNamesTheClassAndTheId() {
        assertEquals("Recording[TEST]", new Recording("test").toString());
    }
}
