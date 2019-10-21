/*******************************************************************************
 * Copyright (c) 2011 - 2018 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/

package be.ppareit.nanopond;

import net.vrallev.android.cat.Cat;

import static be.ppareit.nanopond.NanoPond.POND_DEPTH;

public class Cell {

    long generation;
    long ID;
    long parentID;
    /**
     * Negative value if this was created by seeding a gnome into the world.<p>
     * Positive if this was created random by running the world.
     */
    long lineage;
    int energy;
    byte[] genome;

    private final static MTRandom rg = new MTRandom();
    private final static byte[] startBuffer = new byte[POND_DEPTH];

    static {
        for (int i = 0; i < POND_DEPTH; i++) {
            startBuffer[i] = (byte) 0xf; /* STOP instruction */
        }
    }


    public Cell() {
        ID = 0;
        parentID = 0;
        lineage = 0;
        generation = 0;
        energy = 0;
        genome = new byte[POND_DEPTH];
        System.arraycopy(startBuffer, 0, genome, 0, POND_DEPTH);
    }

    /**
     * Fill genome of the cell with random instruction
     */
    public void setRandomGenome() {
        for (int i = 0; i < POND_DEPTH; i++) {
            genome[i] = (byte) rg.nextInt(16);
        }
    }

    public String getHexa() {
        StringBuilder out = new StringBuilder();
        for (byte aGenome : genome) {
            out.append(Integer.toHexString(aGenome));
        }
        return out.substring(0, out.indexOf("ff") + 1);
    }

    public void setGenome(String hex) {
        System.arraycopy(startBuffer, 0, genome, 0, POND_DEPTH);
        for (int i = 0; i < hex.length(); ++i) {
            char ch = hex.charAt(i);
            if ('0' <= ch && ch <= '9') {
                genome[i] = (byte) (ch - '0');
            } else if ('a' <= ch && ch <= 'f') {
                genome[i] = (byte) (ch - 'a' + 10);
            } else if ('A' <= ch && ch <= 'F') {
                genome[i] = (byte) (ch - 'A' + 10);
            } else {
                Cat.e("Failed to parse hex string for the genome");
                setRandomGenome();
            }
        }
    }
}
