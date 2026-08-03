

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

import fr.florianpal.fperk.objects.Skill;
import org.bukkit.configuration.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillConfig {

    private LinkedHashMap<String, Skill> skills;

    public void load(Configuration config) {
        skills = new LinkedHashMap<>();

        for (String index : config.getConfigurationSection("skills").getKeys(false)) {
            List<String> displayName = config.getStringList("skills." + index + ".displayName");
            // Free-form on purpose: the type points at a handler that an addon may register later.
            String type = config.getString("skills." + index + ".type");
            String effect = config.getString("skills." + index + ".effect");
            float level = (float) config.getDouble("skills." + index + ".level");

            skills.put(index, new Skill(index, displayName, type, effect, level));
        }
    }


    public Map<String, Skill> getSkills() {
        return skills;
    }
}
