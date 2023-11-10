
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

import fr.florianpal.fperk.managers.ConfigurationManager;
import fr.florianpal.fperk.objects.Competence;
import fr.florianpal.fperk.objects.Perk;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PerkConfig {

    private LinkedHashMap<String, Perk> perks;

    private static final String BASE = "perks";

    private static final String POINT = ".";

    public void load(Configuration config, ConfigurationManager configurationManager) {
        perks = new LinkedHashMap<>();

        for (String index : config.getConfigurationSection(BASE).getKeys(false)) {
            var displayName = config.getString(BASE + POINT + index + POINT + "displayName");
            var material = config.getString(BASE + POINT + index + POINT + "material");
            var competences = config.getStringList(BASE + POINT + index + POINT + "competences");
            var delais = config.getInt(BASE + POINT + index + POINT + "delais");
            var ignoreDelais = config.getBoolean(BASE + POINT + index + POINT + "ignoreDelais");
            var time = config.getInt(BASE + POINT + index + POINT + "time");
            var persistant = config.getBoolean(BASE + POINT + index + POINT + "persistant");
            var permission = config.getString(BASE + POINT + index + POINT + "permission");
            var texture = config.getString(BASE + POINT + index + POINT + "texture", "");

            Map<String, Competence> competenceMap = new HashMap<>();
            for(var competence : competences) {
                competenceMap.put(competence, configurationManager.getCompetenceConfig().getCompetences().get(competence));
            }

            perks.put(index,
                    new Perk(
                            index,
                            displayName,
                            Material.valueOf(material),
                            competenceMap,
                            delais,
                            ignoreDelais,
                            time,
                            persistant,
                            permission,
                            texture
                    )
            );
        }
    }


    public Map<String, Perk> getPerks() {
        return perks;
    }
}
