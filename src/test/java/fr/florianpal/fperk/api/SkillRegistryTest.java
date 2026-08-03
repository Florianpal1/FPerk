package fr.florianpal.fperk.api;

import fr.florianpal.fperk.FPerk;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillRegistryTest {

    private FPerk plugin;

    private SkillRegistry registry;

    private static class Simple extends SkillHandler {
        boolean registered;
        boolean unregistered;

        Simple(String id) {
            super(id);
        }

        @Override
        public void onRegister() {
            registered = true;
        }

        @Override
        public void onUnregister() {
            unregistered = true;
        }
    }

    @BeforeEach
    void setUp() {
        plugin = mock(FPerk.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("FPerkTest"));
        registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);
    }

    @Test
    void registersAndResolves() {
        Simple handler = new Simple("DOUBLE_JUMP");

        registry.register(plugin, handler);

        assertSame(handler, registry.get("DOUBLE_JUMP"));
        assertTrue(registry.isRegistered("DOUBLE_JUMP"));
        assertTrue(handler.registered, "onRegister runs right after registration");
        assertSame(plugin, handler.getOwner());
    }

    @Test
    @DisplayName("lookups are case and whitespace insensitive, like the type field of skill.yml")
    void resolvesCaseInsensitively() {
        Simple handler = new Simple("DOUBLE_JUMP");
        registry.register(plugin, handler);

        for (String written : List.of("double_jump", "Double_Jump", "  DOUBLE_JUMP  ")) {
            assertSame(handler, registry.get(written), written);
            assertTrue(registry.isRegistered(written), written);
        }
    }

    @Test
    @DisplayName("an unknown type resolves to null, which is what makes a perk skip it")
    void unknownTypeResolvesToNull() {
        assertNull(registry.get("NOPE"));
        assertNull(registry.get(null));
        assertFalse(registry.isRegistered("NOPE"));
    }

    @Test
    @DisplayName("a duplicate id is refused, and the message names the plugin that owns it")
    void refusesDuplicateIds() {
        Plugin addon = mock(Plugin.class);
        when(addon.getName()).thenReturn("OtherAddon");

        registry.register(addon, new Simple("DOUBLE_JUMP"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> registry.register(plugin, new Simple("double_jump")));

        assertTrue(error.getMessage().contains("DOUBLE_JUMP"));
        assertTrue(error.getMessage().contains("OtherAddon"));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null, new Simple("A")));
        assertThrows(IllegalArgumentException.class, () -> registry.register(plugin, null));
    }

    @Test
    void unregisterReturnsTheHandlerAndNotifiesIt() {
        Simple handler = new Simple("DOUBLE_JUMP");
        registry.register(plugin, handler);

        assertSame(handler, registry.unregister("double_jump"));
        assertTrue(handler.unregistered);
        assertFalse(registry.isRegistered("DOUBLE_JUMP"));
        assertNull(registry.unregister("DOUBLE_JUMP"), "unregistering twice is harmless");
    }

    @Test
    @DisplayName("unregisterAll only drops the handlers of that plugin")
    void unregisterAllIsScopedToItsOwner() {
        Plugin addon = mock(Plugin.class);
        when(addon.getName()).thenReturn("Addon");

        registry.register(plugin, new Simple("BUILTIN"));
        registry.register(addon, new Simple("ADDON_ONE"));
        registry.register(addon, new Simple("ADDON_TWO"));

        registry.unregisterAll(addon);

        assertTrue(registry.isRegistered("BUILTIN"));
        assertFalse(registry.isRegistered("ADDON_ONE"));
        assertFalse(registry.isRegistered("ADDON_TWO"));
    }

    @Test
    @DisplayName("a handler throwing in onRegister is logged, not propagated")
    void swallowsOnRegisterFailures() {
        SkillHandler broken = new SkillHandler("BROKEN") {
            @Override
            public void onRegister() {
                throw new IllegalStateException("boom");
            }
        };

        registry.register(plugin, broken);

        assertTrue(registry.isRegistered("BROKEN"), "the handler is still usable");
    }

    @Test
    @DisplayName("getIds lists what a wrong type in skill.yml is reported against")
    void listsRegisteredIds() {
        registry.register(plugin, new Simple("A"));
        registry.register(plugin, new Simple("B"));

        assertEquals(java.util.Set.of("A", "B"), registry.getIds());
        assertThrows(UnsupportedOperationException.class, () -> registry.getIds().add("C"));
    }

    @Test
    void tracksActiveStatePerPlayerAndSkill() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        registry.markActive(alice, "FLY");

        assertTrue(registry.isActive(alice, "FLY"));
        assertFalse(registry.isActive(bob, "FLY"));
        assertFalse(registry.isActive(alice, "HARVEST"));
    }

    @Test
    @DisplayName("the active state is keyed case insensitively too")
    void activeStateIsNormalised() {
        UUID uuid = UUID.randomUUID();

        registry.markActive(uuid, "fly");
        assertTrue(registry.isActive(uuid, "FLY"));

        registry.markInactive(uuid, " FLY ");
        assertFalse(registry.isActive(uuid, "fly"));
    }

    @Test
    void markInactiveOnAnUnknownSkillIsHarmless() {
        registry.markInactive(UUID.randomUUID(), "NEVER_SEEN");
    }

    @Test
    void listsTheActiveSkillsOfAPlayer() {
        UUID uuid = UUID.randomUUID();
        registry.markActive(uuid, "FLY");
        registry.markActive(uuid, "HARVEST");
        registry.markActive(UUID.randomUUID(), "AUTO_SMELT");

        assertEquals(java.util.Set.of("FLY", "HARVEST"), registry.getActiveSkills(uuid));
    }

    @Test
    @DisplayName("clear drops one player without touching the others")
    void clearIsScopedToOnePlayer() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        registry.markActive(alice, "FLY");
        registry.markActive(bob, "FLY");

        registry.clear(alice);

        assertFalse(registry.isActive(alice, "FLY"));
        assertTrue(registry.isActive(bob, "FLY"));
    }

    @Test
    @DisplayName("unregistering a skill forgets which players had it running")
    void unregisterDropsActiveState() {
        UUID uuid = UUID.randomUUID();
        registry.register(plugin, new Simple("FLY"));
        registry.markActive(uuid, "FLY");

        registry.unregister("FLY");

        assertFalse(registry.isActive(uuid, "FLY"));
    }
}
