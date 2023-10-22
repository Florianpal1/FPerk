
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

import fr.florianpal.fperk.enums.StatusType;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private String lang = "en";

    private Map<StatusType, String> status;


    public void load(Configuration config) {
        status = new HashMap<>();
        lang = config.getString("lang");

        status.put(StatusType.ACTIVATED, config.getString("status.activated"));
        status.put(StatusType.DESACTIVED, config.getString("status.desactived"));

    }

    public String getLang() {
        return lang;
    }

    public Map<StatusType, String> getStatus() {
        return status;
    }
}
