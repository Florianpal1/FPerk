
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

package fr.florianpal.fperk.objects.gui;

import org.bukkit.Material;

import java.util.List;

public class AmountBarrier extends Barrier {

    private final int amount;

    public AmountBarrier(int index, Material material, String title, List<String> description, int amount) {
        super(index, material, title, description);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
