package be.ppareit.nanopond;

import net.vrallev.android.cat.Cat;

import static be.ppareit.nanopond.NanoPond.POND_DEPTH;

/**
 * Created by ppareit on 29/06/17.
 */

public class Cell {

    long generation;
    long ID;
    long parentID;
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
        this.ID = 0;
        this.parentID = 0;
        this.lineage = 0;
        this.generation = 0;
        this.energy = 0;
        this.genome = new byte[POND_DEPTH];
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
