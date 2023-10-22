
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

package fr.florianpal.fperk.configurations.gui;

import fr.florianpal.fperk.configurations.AbstractGuiConfiguration;
import fr.florianpal.fperk.enums.ActionType;
import fr.florianpal.fperk.objects.gui.Action;
import fr.florianpal.fperk.objects.gui.Barrier;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public class MainGuiConfig extends AbstractGuiConfiguration {
    private  List<Integer> perkBlocks = new ArrayList<>();

    private String perkTitle;

    private List<String> perkDescription;

    private String competenceFormat;

    public void load(Configuration config) {
        barrierBlocks = new ArrayList<>();
        previousBlocks = new ArrayList<>();
        nextBlocks = new ArrayList<>();
        perkBlocks = new ArrayList<>();
        closeBlocks = new ArrayList<>();
        actionBlocks = new ArrayList<>();

        for (String index : config.getConfigurationSection("block").getKeys(false)) {
            if (config.getString("block." + index + ".utility").equalsIgnoreCase("previous")) {

                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.valueOf(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        new Barrier(
                                Integer.parseInt(index),
                                Material.valueOf(config.getString("block." + index + ".replacement.material")),
                                config.getString("block." + index + ".replacement.title"),
                                config.getStringList("block." + index + ".replacement.description")
                        )
                );
                previousBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("next")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.valueOf(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        new Barrier(
                                Integer.parseInt(index),
                                Material.valueOf(config.getString("block." + index + ".replacement.material")),
                                config.getString("block." + index + ".replacement.title"),
                                config.getStringList("block." + index + ".replacement.description")
                        )

                );
                nextBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("action")) {
                Action action = new Action(
                        Integer.parseInt(index),
                        Material.valueOf(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description"),
                        ActionType.valueOf(config.getString("block." + index + ".type"))
                );
                actionBlocks.add(action);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("barrier")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.valueOf(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description")
                );
                barrierBlocks.add(barrier);
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("perk")) {
                perkBlocks.add(Integer.valueOf(index));
            } else if (config.getString("block." + index + ".utility").equalsIgnoreCase("close")) {
                Barrier barrier = new Barrier(
                        Integer.parseInt(index),
                        Material.valueOf(config.getString("block." + index + ".material")),
                        config.getString("block." + index + ".title"),
                        config.getStringList("block." + index + ".description")
                );
                closeBlocks.add(barrier);
            }
        }
        size = config.getInt("gui.size");
        nameGui = config.getString("gui.name");

        perkTitle = config.getString("perk.title");
        perkDescription = config.getStringList("perk.description");

        competenceFormat = config.getString("competenceFormat");
    }

    public List<Integer> getPerkBlocks() {
        return perkBlocks;
    }

    public String getPerkTitle() {
        return perkTitle;
    }

    public List<String> getPerkDescription() {
        return perkDescription;
    }

    public String getCompetenceFormat() {
        return competenceFormat;
    }
}
