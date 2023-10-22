package fr.florianpal.fperk.objects;

import java.util.Date;
import java.util.UUID;

public class PlayerPerk {
    private int id;

    private final UUID playerUUID;

    private final String perk;

    private Date lastEnabled;

    private boolean enabled;

    public PlayerPerk(int id, UUID playerUUID, String perk, long lastEnabled, boolean enabled) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.perk = perk;
        this.lastEnabled = new Date(lastEnabled);
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPerk() {
        return perk;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastEnabled() {
        return lastEnabled;
    }

    public void setLastEnabled(Date lastEnabled) {
        this.lastEnabled = lastEnabled;
    }


}
