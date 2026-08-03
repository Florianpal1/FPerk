package fr.florianpal.fperk.api;

import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.Skill;
import org.bukkit.entity.Player;

/**
 * Everything a {@link SkillHandler} needs to know about the invocation currently in progress.
 *
 * <p>A context always describes one single skill of one single perk for one single player. When a
 * perk holds several skills, the handler of each one is called with its own context.</p>
 */
public final class SkillContext {

    private final Player player;

    private final Perk perk;

    private final Skill skill;

    private final PlayerPerk playerPerk;

    public SkillContext(Player player, Perk perk, Skill skill, PlayerPerk playerPerk) {
        this.player = player;
        this.perk = perk;
        this.skill = skill;
        this.playerPerk = playerPerk;
    }

    /**
     * The player the skill is applied to. Always online.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The perk that carries this skill, as defined in {@code perk.yml}.
     */
    public Perk getPerk() {
        return perk;
    }

    /**
     * The skill entry of {@code skill.yml} that triggered this handler.
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * The stored state of the perk for this player.
     */
    public PlayerPerk getPlayerPerk() {
        return playerPerk;
    }

    /**
     * Shortcut for {@code getSkill().getLevel()} : the {@code level} field of {@code skill.yml}.
     */
    public float getLevel() {
        return skill.getLevel();
    }

    /**
     * Shortcut for {@code getSkill().getEffect()} : the free-form {@code effect} field of
     * {@code skill.yml}. An addon may use it to carry any parameter it needs.
     */
    public String getEffect() {
        return skill.getEffect();
    }
}