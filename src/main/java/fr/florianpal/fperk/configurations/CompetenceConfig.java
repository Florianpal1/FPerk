

/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.objects.Competence;
import fr.florianpal.fperk.objects.Perk;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class CompetenceConfig {

    private Map<String, Competence> competences;

    public void load(Configuration config) {
        competences = new HashMap<>();

        for (String index : config.getConfigurationSection("competences").getKeys(false)) {
            String displayName = config.getString("competences." + index + ".displayName");
            EffectType effectType = EffectType.valueOf(config.getString("competences." + index + ".type"));
            String effect = config.getString("competences." + index + ".effect");
            int level = config.getInt("competences." + index + ".level");

            competences.put(index, new Competence(index, displayName, effectType, effect, level));
        }
    }


    public Map<String, Competence> getCompetences() {
        return competences;
    }
}
