package be.ppareit;

public class StringLib {

    /**
     * @param s
     *            String to test for being a hex string ('0x' at the beginning is not
     *            tested for)
     * @return true if s is a hex string
     */
    static public boolean isHexString(String s) {
        return s.matches("[0-9a-fA-F]+");
    }

}
